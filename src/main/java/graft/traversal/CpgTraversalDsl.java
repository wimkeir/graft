package graft.traversal;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.analysis.taint.MethodSanitizer;
import graft.analysis.taint.SanitizerDescription;
import graft.analysis.taint.SinkDescription;
import graft.cpg.CpgUtil;
import graft.utils.GraphUtil;

import static graft.Const.*;

/**
 * TODO: javadoc
 *
 * @author Wim Keirsgieter
 */
@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    /**
     * Checks whether the current node is a reassignment of the given local variable.
     *
     * @param varName the name of the local variable to check
     * @return a filter traversal that returns true if the node is a reassignment, otherwise false
     */
    default GraphTraversal<S, ?> reassigns(String varName) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return filter(it -> {
            Vertex vertex  = (Vertex) it.get();
            CpgTraversal targetName = g.V(vertex)
                    .outE(AST_EDGE)
                    .has(EDGE_TYPE, TARGET)
                    .inV()
                    .values(NAME);
            return targetName.hasNext() && varName.equals(targetName.next());
        });
    }

    /**
     * Checks whether the current node sanitizes the given local variable, according to the given sanitizer descriptions.
     *
     * The sanitizer description specifies which argument indices are considered sanitized. If none are specified, then
     * all arguments are considered sanitized.
     *
     * @param sanitizers the sanitizer descriptions
     * @param varName the variable to check for sanitization
     * @return a filter traversal that returns true if the node sanitizes the variable, otherwise false
     */
    default GraphTraversal<S, ?> sanitizes(List<SanitizerDescription> sanitizers, String varName) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return filter(it -> {
            Vertex vertex = (Vertex) it.get();
            for (SanitizerDescription sanitizer : sanitizers) {
                if (sanitizer instanceof MethodSanitizer) {
                    MethodSanitizer methSan = (MethodSanitizer) sanitizer;
                    List<Vertex> sanInvokes = CpgUtil.getInvokeExprs(vertex, methSan.sigPattern);

                    for (Vertex sanInvoke : sanInvokes) {
                        // if no args specified, we assume all args are sanitized
                        if (methSan.sanitizesArgs.size() == 0) {
                            CpgTraversal args = g.V(sanInvoke)
                                    .outE(AST_EDGE)
                                    .has(EDGE_TYPE, ARG)
                                    .inV();
                            while (args.hasNext()) {
                                Vertex arg = (Vertex) args.next();
                                if (arg.value(NAME).equals(varName)) {
                                    return true;
                                }
                            }
                        } else {
                            for (int argIndex : methSan.sanitizesArgs) {
                                Vertex arg = g.V(sanInvoke).ithArg(argIndex).next();
                                if (arg.value(NAME).equals(varName)) {
                                    return true;
                                }
                            }
                        }
                    }
                } else {
                    // TODO: conditional sanitizers
                }
            }
            return false;
        });
    }

    /**
     * Returns a traversal containing all argument nodes that are "sunk" in the current sink node, according to the given
     * sink description.
     *
     * The sink description specifies which argument indices should be considered sunk. If none are specified, then all
     * arguments are considered sunk.
     *
     * @param sink the description of the sink
     * @return a traversal containing the arguments sunk
     */
    default GraphTraversal<S, Vertex> sunkArgs(SinkDescription sink) {
        return outE(AST_EDGE)
                .has(EDGE_TYPE, ARG)
                .filter(it -> {
                    if (sink.sinksArgs.size() == 0) return true;
                    Edge argEdge = it.get();
                    int index = argEdge.value(INDEX);
                    return sink.sinksArgs.contains(index);
                }).inV();
    }

    /**
     * Returns the ith argument node of a method call expression vertex.
     *
     * @param i the index of the argument
     * @return the ith argument
     */
    default GraphTraversal<S, Vertex> ithArg(int i) {
        // TODO: call expressions aren't the only expressions with args
        return hasLabel(AST_NODE).has(NODE_TYPE, INVOKE_EXPR)
                .outE(AST_EDGE)
                .has(EDGE_TYPE, ARG)
                .has(INDEX, i)
                .inV();
    }

    /**
     * Returns true if the property specified by the given key has a value that sanitizes the given regex.
     *
     * @param key the key of the property to check
     * @param regex the regex pattern to check the property value against
     * @return true if the property value sanitizes the regex, else false
     */
    default GraphTraversal<S, ?> hasPattern(String key, String regex) {
        return filter(x -> {
            if (x.get() instanceof Vertex) {
                Vertex vertex = (Vertex) x.get();
                return vertex.value(key).toString().matches(regex);
            } else if (x.get() instanceof Edge) {
                Edge edge = (Edge) x.get();
                return edge.value(key).toString().matches(regex);
            } else {
                return false;
            }
        });
    }

}