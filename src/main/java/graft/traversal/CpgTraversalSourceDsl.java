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

    /**
     * Get all assign statements of the form x = new T(), where x is a local
     * variable.
     *
     * @return all assign statements of the form x = new T()
     */
    public CpgTraversal<Vertex, Vertex> getNewAssignStmts() {
        return getAssignStmts()
                .getVal().has(NODE_TYPE, NEW_EXPR)
                .in(AST_EDGE)
                .getTgt().has(NODE_TYPE, LOCAL_VAR)
                .in(AST_EDGE);
    }

    /**
     * Get all assign statements of the form x = y, where x and y are both
     * local variables with reference types.
     *
     * @return all assign statements of the form x = y, where x and y are ref types
     */
    public CpgTraversal<Vertex, Vertex> getRefAssignStmts() {
        return getAssignStmts()
                .getVal()
                .has(NODE_TYPE, LOCAL_VAR)
                .has(REF_TYPE, true)
                .in(AST_EDGE)
                .getTgt()
                .has(NODE_TYPE, LOCAL_VAR)
                .has(REF_TYPE, true)
                .in(AST_EDGE);
    }

    /**
     * Get all assign statements of the form x.f = y, where x is a local variable
     * and y is a reference type.
     *
     * @param varName the name of the local variable
     * @return all store statements on attributes of that variable
     */
    public CpgTraversal<Vertex, Vertex> getStoreStmts(String varName) {
        return getAssignStmts()
                .getTgt().or(
                        has(NODE_TYPE, INSTANCE_FIELD_REF),
                        has(NODE_TYPE, STATIC_FIELD_REF)
                ).outE(AST_EDGE).has(EDGE_TYPE, BASE)
                .inV()
                .has(NAME, varName)
                .in(AST_EDGE).in(AST_EDGE);
    }

    /**
     * Get all assign statements of the form y = x.f, where x is a local variable
     * and y is a reference type.
     *
     * @param varName the name of the local variable
     * @return all load statements on attributes of that variable
     */
    public CpgTraversal<Vertex, Vertex> getLoadStmts(String varName) {
        return getAssignStmts()
                .getVal()
                .or(has(NODE_TYPE, STATIC_FIELD_REF, has(NODE_TYPE, INSTANCE_FIELD_REF)))
                .outE(AST_EDGE).has(EDGE_TYPE, BASE)
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, varName)
                .in(AST_EDGE).in(AST_EDGE)
                .getTgt()
                .has(NODE_TYPE, LOCAL_VAR);
    }

    /**
     * Get all references to fields of the given (reference type) variable, both
     * instance and static.
     *
     * @param varName the name of the variable
     * @return all field refs of the given variable
     */
    public CpgTraversal<Vertex, Vertex> getFieldRefs(String varName) {
        return getFieldRefs()
                .outE(AST_EDGE).has(EDGE_TYPE, BASE)
                .inV()
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, varName);
    }

    /**
     * Get all instance and static field references.
     *
     * @return all field refs in the graph
     */
    public CpgTraversal<Vertex, Vertex> getFieldRefs() {
        return getV()
                .hasLabel(AST_NODE)
                .or(
                    has(NODE_TYPE, INSTANCE_FIELD_REF),
                    has(NODE_TYPE, STATIC_FIELD_REF)
                );
    }

    /**
     * Get all assign statements in the graph.
     *
     * @return all assign statements in the graph
     */
    public CpgTraversal<Vertex, Vertex> getAssignStmts() {
        return getV()
                .hasLabel(CFG_NODE)
                .has(NODE_TYPE, ASSIGN_STMT);
    }

    public CpgTraversal<Vertex, Vertex> getMatches(VertexDescription descr) {
        return getV().matches(descr);
    }

    public CpgTraversal<Vertex, Path> pathsBetween(Vertex v, Vertex w, String edgeLabel) {
        return (CpgTraversal<Vertex, Path>) V(v)
                .repeat(timeLimit(1000).out(edgeLabel).simplePath())
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
