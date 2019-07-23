package graft.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;

@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    /**
     * Returns the ith parameter node of a method entry vertex.
     *
     * @param i the index of the parameter
     * @return the ith parameter
     */
    default GraphTraversal<S, Vertex> ithParam(int i) {
        return hasLabel(CFG_NODE).has(NODE_TYPE, ENTRY)
                .outE(AST_EDGE)
                .has(EDGE_TYPE, PARAM)
                .has(INDEX, i)
                .inV();
    }

    /**
     * Returns the ith argument node of a method call expression vertex.
     *
     * @param i the index of the argument
     * @return the ith argument
     */
    default GraphTraversal<S, Vertex> ithArg(int i) {
        // TODO: call expressions aren't the only expressions with args
        return hasLabel(AST_NODE).has(NODE_TYPE, CALL_EXPR)
                .outE(AST_EDGE)
                .has(EDGE_TYPE, ARG)
                .has(INDEX, i)
                .inV();
    }

}