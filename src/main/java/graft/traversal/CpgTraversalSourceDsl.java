package graft.traversal;


import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.cpg.structure.VertexDescription;

import static graft.Const.*;
import static graft.traversal.__.*;

public class CpgTraversalSourceDsl extends GraphTraversalSource {

    public CpgTraversalSourceDsl(Graph graph, TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public CpgTraversalSourceDsl(Graph graph) {
        super(graph);
    }

    public CpgTraversal<Vertex, Vertex> getMatches(VertexDescription descr) {
        return getV().matches(descr);
    }

    public CpgTraversal<Vertex, Path> pathsBetween(Vertex v, Vertex w, String edgeLabel) {
        return (CpgTraversal<Vertex, Path>) V(v)
                .repeat(out(edgeLabel).simplePath())
                .until(is(w))
                .path();
    }

    /**
     * Get all invoke expressions that invoke a method matching the given signature pattern.
     *
     * @param sigPattern a regex specifying the method signature pattern
     * @return a traversal containing all invoke expressions nodes of the matching methods
     */
    @SuppressWarnings("unchecked")
    public CpgTraversal<Vertex, Vertex> getCallsTo(String sigPattern) {
        return (CpgTraversal<Vertex, Vertex>) getV()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, INVOKE_EXPR)
                .hasPattern(METHOD_SIG, sigPattern);
    }

    protected CpgTraversal<Vertex, Vertex> getV() {
        CpgTraversalSource clone = (CpgTraversalSource) this.clone();
        clone.getBytecode().addStep(GraphTraversal.Symbols.V);

        CpgTraversal<Vertex, Vertex> traversal = new DefaultCpgTraversal<>(clone);
        traversal.asAdmin().addStep(new GraphStep<>(traversal.asAdmin(), Vertex.class, true));

        return traversal;
    }

}
