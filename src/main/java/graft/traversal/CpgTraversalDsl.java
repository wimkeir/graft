package graft.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.cpg.structure.VertexDescription;

import static graft.Const.*;

/**
 * TODO: javadoc
 * TODO: rename to GTraversal, GTraversalSource
 *
 * @author Wim Keirsgieter
 */
@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    /**
     * Get the value node of an assign statement.
     *
     * @return the value node of an assign stmt
     */
    default GraphTraversal<S, Vertex> getVal() {
        return hasLabel(CFG_NODE).has(NODE_TYPE, ASSIGN_STMT) // ensure we're on an assign stmt
                .outE(AST_EDGE).has(EDGE_TYPE, VALUE)
                .inV();
    }

    /**
     * Get the target node of an assign statement.
     *
     * @return the value node of an assign stmt
     */
    default GraphTraversal<S, Vertex> getTgt() {
        return hasLabel(CFG_NODE).has(NODE_TYPE, ASSIGN_STMT) // ensure we're on an assign stmt
                .outE(AST_EDGE).has(EDGE_TYPE, TARGET)
                .inV();
    }


    /**
     * TODO: javadoc
     *
     * @param descr
     * @return
     */
    default GraphTraversal<S, Vertex> matches(VertexDescription descr) {
        return (CpgTraversal<S, Vertex>) hasLabel(descr.LABEL).filter(t -> {
            Vertex v = (Vertex) t.get();
            for (String key : descr.keys()) {
                String pattern = descr.getPropPattern(key);
                if (!v.keys().contains(key)) {
                    return false;
                }
                if (!v.value(key).toString().matches(pattern)) {
                    return false;
                }
            }
            return true;
        });
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