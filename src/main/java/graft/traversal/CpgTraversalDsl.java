package graft.traversal;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.analysis.taint.SanitizerDescription;
import graft.analysis.taint.SinkDescription;
import graft.utils.GraphUtil;

import static graft.Const.*;

/**
 * TODO: javadoc
 *
 * @author Wim Keirsgieter
 */
@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

//    /**
//     * Fills the given list with all invoke expression nodes found in the AST subtree of the given node.
//     *
//     * Note that any existing values in the list will not be modified.
//     *
//     * @param invokeExprs the list to add the invoke expression nodes to
//     * @return the identity traversal
//     */
//    default GraphTraversal<S, ?> getInvokeExprs(List<Vertex> invokeExprs) {
//        CpgTraversal clone = (CpgTraversal) this.clone();
//        clone.repeat(sideEffect(it -> {
//            Vertex vertex = (Vertex) it.get();
//            if (vertex.value(NODE_TYPE).equals(INVOKE_EXPR)) {
//                invokeExprs.add(vertex);
//            }
//        }).out(AST_EDGE)).iterate();
//        return this;
//    }

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
            if (targetName.hasNext() && varName.equals(targetName.next())) {
                return true;
            }
            return false;
        });
    }

    /**
     * Checks whether the current node sanitizes the given local variable, according to the given sanitizer descriptions.
     *
     * @param sanitizers the sanitizer descriptions
     * @param varName the variable to check for sanitization
     * @return a filter traversal that returns true if the node sanitizes the variable, otherwise false
     */
    default GraphTraversal<S, ?> sanitizes(List<SanitizerDescription> sanitizers, String varName) {
        return filter(it -> {
            for (SanitizerDescription sanitizer : sanitizers) {
                if (sanitizer.sanitizes((Vertex) it.get(), varName)) {
                    return true;
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
     * @param sink
     * @return
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