package graft.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;
import static graft.traversal.__.*;

public class CpgTraversalSourceDsl extends GraphTraversalSource {

    // TODO: same as CpgTraversal

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    public CpgTraversalSourceDsl(Graph graph, TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public CpgTraversalSourceDsl(Graph graph) {
        super(graph);
    }

    public CpgTraversal<Vertex, Vertex> cpgRoot() {
        return getV()
                .hasLabel(CPG_ROOT)
                .has(NODE_TYPE, CPG_ROOT);
    }

    public CpgTraversal<Vertex, Vertex> locals(String varName) {
        // TODO: specify method sig too
        return getV()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, varName);
    }

    public CpgTraversal<Vertex, Vertex> arg(String sigPattern, int index) {
        return callsTo(sigPattern)
                .outE(AST_EDGE)
                .has(EDGE_TYPE, ARG)
                .has(INDEX, index)
                .inV();
    }

    public CpgTraversal<Vertex, Vertex> classNodes() {
        return astV(CLASS);
    }

    public CpgTraversal<Vertex, Vertex> entries() {
        return cfgV(ENTRY);
    }

    public CpgTraversal<Vertex, Vertex> entryOf(String methodSig) {
        return entries().has(METHOD_SIG, methodSig);
    }

    public CpgTraversal<Vertex, Vertex> returns() {
        return cfgV(RETURN_STMT);
    }

    public CpgTraversal<Vertex, Vertex> returnsOf(String methodSig) {
        return entryOf(methodSig).astOut(STATEMENT).has(NODE_TYPE, RETURN_STMT);
    }

    @SuppressWarnings("unchecked")
    public CpgTraversal<Vertex, Vertex> callsTo(String sigPattern) {
        return (CpgTraversal<Vertex, Vertex>) invokeExprs().hasPattern(METHOD_SIG, sigPattern);
    }

    public CpgTraversal<Vertex, Vertex> invokeExprs() {
        return astV(EXPR).has(EXPR_TYPE, INVOKE_EXPR);
    }

    public CpgTraversal<Vertex, Vertex> invokesOf(String methodSig) {
        return invokeExprs().has(METHOD_SIG, methodSig);
    }

    public CpgTraversal<Vertex, Vertex> paramsOf(String methodSig) {
        return entryOf(methodSig)
                .astOut(STATEMENT).has(NODE_TYPE, ASSIGN_STMT)
                .where(getVal().and(
                        has(NODE_TYPE, REF),
                        has(REF_TYPE, PARAM_REF)
                )
        );
    }

    // ********************************************************************************************
    // V traversals
    // ********************************************************************************************

    // Control flow graph

    public CpgTraversal<Vertex, Vertex> cfgV() {
        return getV().hasLabel(CFG_NODE);
    }

    public CpgTraversal<Vertex, Vertex> cfgV(String nodeType) {
        return cfgV().has(NODE_TYPE, nodeType);
    }

    // Abstract syntax tree

    public CpgTraversal<Vertex, Vertex> astV() {
        return getV().hasLabel(AST_NODE);
    }

    public CpgTraversal<Vertex, Vertex> astV(String nodeType) {
        return astV().has(NODE_TYPE, nodeType);
    }

    // ********************************************************************************************
    // E traversals
    // ********************************************************************************************

    // Control flow graph

    public CpgTraversal<Edge, Edge> cfgE() {
        return getE().hasLabel(CFG_EDGE);
    }

    public CpgTraversal<Edge, Edge> cfgE(boolean interproc) {
        return cfgE().has(INTERPROC, interproc);
    }

    public CpgTraversal<Edge, Edge> cfgE(String edgeType) {
        return cfgE().has(EDGE_TYPE, edgeType);
    }

    public CpgTraversal<Edge, Edge> cfgE(String edgeType, boolean interproc) {
        return cfgE(interproc).has(EDGE_TYPE, edgeType);
    }

    // Abstract syntax tree

    public CpgTraversal<Edge, Edge> astE() {
        return getE().hasLabel(AST_EDGE);
    }

    public CpgTraversal<Edge, Edge> astE(String edgeType) {
        return astE().has(EDGE_TYPE, edgeType);
    }

    // Program dependence graph

    public CpgTraversal<Edge, Edge> pdgE() {
        return getE().hasLabel(PDG_EDGE);
    }

    public CpgTraversal<Edge, Edge> pdgE(boolean interproc) {
        return pdgE().has(INTERPROC, interproc);
    }

    public CpgTraversal<Edge, Edge> pdgE(String edgeType) {
        return pdgE().has(EDGE_TYPE, edgeType);
    }

    public CpgTraversal<Edge, Edge> pdgE(String edgeType, boolean interproc) {
        return pdgE(interproc).has(EDGE_TYPE, edgeType);
    }

    // ********************************************************************************************
    // addV traversals
    // ********************************************************************************************

    public CpgTraversal<Vertex, Vertex> addCpgRoot(String name, String target, String classpath) {
        return (CpgTraversal<Vertex, Vertex>) addV(CPG_ROOT)
                .property(NODE_TYPE, CPG_ROOT)
                .property(PROJECT_NAME, name)
                .property(TARGET_DIR, target)
                .property(CLASSPATH, classpath)
                .property(TEXT_LABEL, name);
    }

    // Control flow graph

    public CpgTraversal<Vertex, Vertex> addCfgV() {
        return (CpgTraversal<Vertex, Vertex>) addV(CFG_NODE);
    }

    public CpgTraversal<Vertex, Vertex> addCfgV(String nodeType, String textLabel) {
        return addCfgV()
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel);
    }

    public CpgTraversal<Vertex, Vertex> addStmtNode(String nodeType, String textLabel, int line) {
        return addCfgV(nodeType, textLabel)
                .property(SRC_LINE_NO, line);
    }

    public CpgTraversal<Vertex, Vertex> addEntryNode(String name, String signature, String type) {
        return addCfgV(ENTRY, name)
                .property(METHOD_NAME, name)
                .property(METHOD_SIG, signature)
                .property(JAVA_TYPE, type);
    }

    // Abstract syntax tree

    public CpgTraversal<Vertex, Vertex> addAstV() {
        return (CpgTraversal<Vertex, Vertex>) addV(AST_NODE);
    }

    public CpgTraversal<Vertex, Vertex> addAstV(String nodeType, String textLabel) {
        return addAstV()
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel);
    }

    public CpgTraversal<Vertex, Vertex> addExprNode(String exprType, String textLabel, String javaType) {
        return addAstV(EXPR, textLabel)
                .property(EXPR_TYPE, exprType)
                .property(JAVA_TYPE, javaType);
    }

    public CpgTraversal<Vertex, Vertex> addConstNode(String javaType, String textLabel, String value) {
        return addAstV(CONSTANT, textLabel)
                .property(JAVA_TYPE, javaType)
                .property(VALUE, value);
    }

    public CpgTraversal<Vertex, Vertex> addRefNode(String refType, String textLabel, String javaType) {
        return addAstV(REF, textLabel)
                .property(REF_TYPE, refType)
                .property(JAVA_TYPE, javaType);
    }

    public CpgTraversal<Vertex, Vertex> addLocalNode(String javaType, String textLabel, String name) {
        return addAstV(LOCAL_VAR, textLabel)
                .property(JAVA_TYPE, javaType)
                .property(NAME, name);
    }

    // ********************************************************************************************
    // addE traversals
    // ********************************************************************************************

    // Control flow graph

    public CpgTraversal<Edge, Edge> addCfgE() {
        return (CpgTraversal<Edge, Edge>) addE(CFG_EDGE);
    }

    public CpgTraversal<Edge, Edge> addCfgE(String edgeType, String textLabel, boolean interproc) {
        return addCfgE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .property(INTERPROC, interproc);
    }

    public CpgTraversal<Edge, Edge> addEmptyEdge() {
        return addCfgE(EMPTY, EMPTY, false);
    }

    public CpgTraversal<Edge, Edge> addCondEdge(String condition) {
        return addCfgE(CONDITION, condition, false)
                .property(CONDITION, condition);
    }

    public CpgTraversal<Edge, Edge> genCallEdge() {
        return addCfgE(CALL, CALL, true);
    }

    public CpgTraversal<Edge, Edge> genRetEdge() {
        return addCfgE(RET, RET, true);
    }

    // Abstract syntax tree

    public CpgTraversal<Edge, Edge> addAstE() {
        return (CpgTraversal<Edge, Edge>) addE(AST_EDGE);
    }

    public CpgTraversal<Edge, Edge> addAstE(String edgeType, String textLabel) {
        return addAstE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel);
    }

    // Program dependence graph

    public CpgTraversal<Edge, Edge> addPdgE() {
        return (CpgTraversal<Edge, Edge>) addE(PDG_EDGE);
    }

    public CpgTraversal<Edge, Edge> addPdgE(String edgeType, String textLabel) {
        return addPdgE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel);
    }

    public CpgTraversal<Edge, Edge> addDataDepE(String var) {
        return addPdgE(DATA_DEP, var)
                .property(VAR_NAME, var);
    }

    public CpgTraversal<Edge, Edge> addArgDepE() {
        return addPdgE(ARG_DEP, ARG_DEP);
    }

    public CpgTraversal<Edge, Edge> addRetDepE() {
        return addPdgE(RET_DEP, RET_DEP);
    }

    // ********************************************************************************************
    //
    // ********************************************************************************************

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
                .where(and(
                        getVal()
                        .has(NODE_TYPE, LOCAL_VAR)
                        .has(REF_TYPE, true),
                        getTgt()
                        .has(NODE_TYPE, LOCAL_VAR)
                        .has(REF_TYPE, true)
                ));
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
        return astV()
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
        return cfgV(ASSIGN_STMT);
    }

    public CpgTraversal<Vertex, Path> pathsBetween(Vertex v, Vertex w, String edgeLabel) {
        return (CpgTraversal<Vertex, Path>) V(v)
                .repeat(timeLimit(1000).out(edgeLabel).simplePath())
                .until(is(w))
                .path();
    }

    @SuppressWarnings("unchecked")
    public CpgTraversal<Vertex, Path> dataFlowsBetween(CpgTraversal source, CpgTraversal sink) {
        return getV().where(source)
                .repeat(timeLimit(1000).outDataDep().simplePath())
                .until(sink)
                .path();
    }

//    @SuppressWarnings("unchecked")
//    public CpgTraversal<Vertex, Path> controlFlowsBetween(CpgTraversal sources, CpgTraversal sinks) {
//        // TODO: only intraproc!
//        return (CpgTraversal<Vertex, Path>) sources.copy()
//                .repeat(timeLimit(1000).outE(CFG_EDGE).has(INTERPROC, false).simplePath())
//                .until(sinks.copy())
//                .path();
//    }

    @SuppressWarnings("unchecked")
    public CpgTraversal<Vertex, Path> controlFlowsBetween(Vertex v, Vertex w) {
        // TODO: only intraproc!
        return (CpgTraversal<Vertex, Path>) V(v)
                .repeat(timeLimit(1000).outEmpty().simplePath())
                .until(is(w))
                .path();
    }

    protected CpgTraversal<Vertex, Vertex> getV() {
        CpgTraversalSource clone = (CpgTraversalSource) this.clone();
        clone.getBytecode().addStep(GraphTraversal.Symbols.V);

        CpgTraversal<Vertex, Vertex> traversal = new DefaultCpgTraversal<>(clone);
        traversal.asAdmin().addStep(new GraphStep<>(traversal.asAdmin(), Vertex.class, true));

        return traversal;
    }

    protected CpgTraversal<Edge, Edge> getE() {
        CpgTraversalSource clone = (CpgTraversalSource) this.clone();
        clone.getBytecode().addStep(GraphTraversal.Symbols.E);

        CpgTraversal<Edge, Edge> traversal = new DefaultCpgTraversal<>(clone);
        traversal.asAdmin().addStep(new GraphStep<>(traversal.asAdmin(), Edge.class, true));

        return traversal;
    }

}
