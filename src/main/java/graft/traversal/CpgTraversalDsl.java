package graft.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;

/**
 * TODO: javadoc
 *
 * @author Wim Keirsgieter
 */
@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    // ********************************************************************************************
    // general purpose traversals
    // ********************************************************************************************

    /**
     * Returns true if the property specified by the given key has a value that matches the given regex.
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

    default GraphTraversal<S, ?> copy() {
        return this.clone();
    }

    default GraphTraversal<S, ?> matches(String regex) {
        return filter(t -> t.get().toString().matches(regex));
    }

    // ********************************************************************************************
    // TODO: categorise
    // ********************************************************************************************

    default GraphTraversal<S, Vertex> locals(String varName) {
        return (CpgTraversal<S, Vertex>) hasLabel(AST_NODE)
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, varName);
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> entries() {
        return (CpgTraversal) V().hasLabel(CFG_NODE).has(NODE_TYPE, ENTRY);
    }

    default GraphTraversal<S, Vertex> entryOf(String methodSig) {
        return entries().has(METHOD_SIG, methodSig);
    }

    default GraphTraversal<S, Vertex> packageNodes() {
        return astV(PACKAGE);
    }

    default GraphTraversal<S, Vertex> packageOf(String packName) {
        return packageNodes().has(PACKAGE_NAME, packName);
    }

    default GraphTraversal<S, Vertex> assignStmts() {
        return (CpgTraversal<S, Vertex>) has(NODE_TYPE, ASSIGN_STMT);
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> source() {
        CpgTraversal g = (CpgTraversal) this.clone();
        String[] varName = new String[]{};
        return g.hasLabel(AST_NODE)
                .has(NODE_TYPE, LOCAL_VAR)
                .sideEffect(t -> {
                    Vertex v = (Vertex) t;
                    varName[0] = v.value(NAME);
                })
                .dataDepIn(varName[0]);
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Path> controlFlowsTo(Vertex w) {
        // TODO: only intraproc!
        return repeat((CpgTraversal) timeLimit(1000).outE(CFG_EDGE).has(INTERPROC, false).inV().simplePath())
                .until(is(w))
                .path();
    }

    default GraphTraversal<S, Vertex> stmt() {
        return coalesce(
                hasLabel(CFG_NODE),
                repeat(((CpgTraversal) timeLimit(500)).astIn()).until(label().is(CFG_NODE))
        );
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> stmtRoot() {
        // TODO: really get this working
        // TODO: make sure this works for AST nodes "above" the statements
        return until(label().is(CFG_NODE))
                .repeat((CpgTraversal) __.timeLimit(5).astIn());
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> cfgRoot() {
        return until(label().is(CFG_NODE)).repeat(((CpgTraversal) timeLimit(100)).astIn());
    }

    // ********************************************************************************************
    // V traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Vertex> cfgV() {
        return V().hasLabel(CFG_NODE);
    }

    default GraphTraversal<S, Vertex> cfgV(String nodeType) {
        return cfgV().has(NODE_TYPE, nodeType);
    }

    // Abstract syntax tree

    default GraphTraversal<S, Vertex> astV() {
        return V().hasLabel(AST_NODE);
    }

    default GraphTraversal<S, Vertex> astV(String nodeType) {
        return astV().has(NODE_TYPE, nodeType);
    }

    // ********************************************************************************************
    // in traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Vertex> cfgIn() {
        return in(CFG_EDGE);
    }

    default GraphTraversal<S, Vertex> cfgIn(boolean interproc) {
        return inE(CFG_EDGE).has(INTERPROC, interproc).outV();
    }

    default GraphTraversal<S, Vertex> cfgIn(String edgeType) {
        return inE(CFG_EDGE).has(EDGE_TYPE, edgeType).outV();
    }

    default GraphTraversal<S, Vertex> cfgIn(boolean interproc, String edgeType) {
        return inE(CFG_EDGE)
                .has(INTERPROC, interproc)
                .has(EDGE_TYPE, edgeType)
                .outV();
    }

    // Abstract syntax tree

    default GraphTraversal<S, Vertex> astIn() {
        return in(AST_EDGE);
    }

    default GraphTraversal<S, Vertex> astIn(String edgeType) {
        return inE(AST_EDGE).has(EDGE_TYPE, edgeType).outV();
    }

    // Program dependence graph

    default GraphTraversal<S, Vertex> pdgIn() {
        return in(PDG_EDGE);
    }

    default GraphTraversal<S, Vertex> pdgIn(boolean interproc) {
        return inE(PDG_EDGE).has(INTERPROC, interproc).outV();
    }

    default GraphTraversal<S, Vertex> pdgIn(String edgeType) {
        return inE(PDG_EDGE).has(EDGE_TYPE, edgeType).outV();
    }

    default GraphTraversal<S, Vertex> pdgIn(boolean interproc, String edgeType) {
        return inE(PDG_EDGE)
                .has(INTERPROC, interproc)
                .has(EDGE_TYPE, edgeType)
                .outV();
    }

    default GraphTraversal<S, Vertex> dataDepIn() {
        return inE(PDG_EDGE).has(EDGE_TYPE, DATA_DEP).outV();
    }

    default GraphTraversal<S, Vertex> dataDepIn(String varName) {
        return inE(PDG_EDGE)
                .has(EDGE_TYPE, DATA_DEP)
                .has(VAR_NAME, varName)
                .outV();
    }

    // ********************************************************************************************
    // out traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Vertex> cfgOut() {
        return out(CFG_EDGE);
    }

    default GraphTraversal<S, Vertex> cfgOut(boolean interproc) {
        return outE(CFG_EDGE).has(INTERPROC, interproc).inV();
    }

    default GraphTraversal<S, Vertex> cfgOut(String edgeType) {
        return outE(CFG_EDGE).has(EDGE_TYPE, edgeType).inV();
    }

    default GraphTraversal<S, Vertex> cfgOut(boolean interproc, String edgeType) {
        return outE(CFG_EDGE)
                .has(INTERPROC, interproc)
                .has(EDGE_TYPE, edgeType)
                .outV();
    }

    default GraphTraversal<S, Vertex> outEmpty() {
        return outE(CFG_EDGE).has(EDGE_TYPE, EMPTY).inV();
    }

    // Abstract syntax tree

    default GraphTraversal<S, Vertex> astOut() {
        return out(AST_EDGE);
    }

    default GraphTraversal<S, Vertex> astOut(String edgeType) {
        return outE(AST_EDGE).has(EDGE_TYPE, edgeType).inV();
    }

    // Program dependence graph

    default GraphTraversal<S, Vertex> pdgOut() {
        return out(PDG_EDGE);
    }

    default GraphTraversal<S, Vertex> pdgOut(boolean interproc) {
        return outE(PDG_EDGE).has(INTERPROC, interproc).inV();
    }

    default GraphTraversal<S, Vertex> pdgOut(String edgeType) {
        return outE(PDG_EDGE).has(EDGE_TYPE, edgeType).inV();
    }

    default GraphTraversal<S, Vertex> pdgOut(boolean interproc, String edgeType) {
        return outE(PDG_EDGE)
                .has(INTERPROC, interproc)
                .has(EDGE_TYPE, edgeType)
                .inV();
    }

    default GraphTraversal<S, Vertex> outDataDep() {
        return outE(PDG_EDGE).has(EDGE_TYPE, DATA_DEP).inV();
    }

    default GraphTraversal<S, Vertex> outDataDep(String varName) {
        return outE(PDG_EDGE).has(VAR_NAME, varName).inV();
    }

    // ********************************************************************************************
    // inE traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Edge> cfgInE() {
        return inE(CFG_EDGE);
    }

    default GraphTraversal<S, Edge> cfgInE(String edgeType) {
        return cfgInE().has(EDGE_TYPE, edgeType);
    }

    // Abstract syntax tree

    default GraphTraversal<S, Edge> astInE() {
        return inE(AST_EDGE);
    }

    default GraphTraversal<S, Edge> astInE(String edgeType) {
        return astInE().has(EDGE_TYPE, edgeType);
    }

    // Program dependence graph

    default GraphTraversal<S, Edge> pdgInE() {
        return inE(PDG_EDGE);
    }

    default GraphTraversal<S, Edge> pdgInE(String edgeType) {
        return pdgInE().has(EDGE_TYPE, edgeType);
    }

    // ********************************************************************************************
    // outE traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Edge> cfgOutE() {
        return outE(CFG_EDGE);
    }

    default GraphTraversal<S, Edge> cfgOutE(String edgeType) {
        return cfgOutE().has(EDGE_TYPE, edgeType);
    }

    // Abstract syntax tree

    default GraphTraversal<S, Edge> astOutE() {
        return outE(AST_EDGE);
    }

    default GraphTraversal<S, Edge> astOutE(String edgeType) {
        return astOutE().has(EDGE_TYPE, edgeType);
    }

    // Program dependence graph

    default GraphTraversal<S, Edge> pdgOutE() {
        return outE(PDG_EDGE);
    }

    default GraphTraversal<S, Edge> pdgOutE(String edgeType) {
        return pdgOutE().has(EDGE_TYPE, edgeType);
    }

    // ********************************************************************************************
    // addV traversals
    // ********************************************************************************************

    default GraphTraversal<S, Vertex> addCpgRoot(String name, String target, String classpath) {
        return (GraphTraversal<S, Vertex>) addV(CPG_ROOT)
                .property(NODE_TYPE, CPG_ROOT)
                .property(PROJECT_NAME, name)
                .property(TARGET_DIR, target)
                .property(CLASSPATH, classpath)
                .property(TEXT_LABEL, name);
    }


    default GraphTraversal<S, Vertex> addPackage(String name) {
        return addAstV(PACKAGE, name).property(PACKAGE_NAME, name);
    }

    // Control flow graph

    default GraphTraversal<S, Vertex> addCfgV() {
        return (GraphTraversal<S, Vertex>) addV(CFG_NODE);
    }

    default GraphTraversal<S, Vertex> addCfgV(String nodeType, String textLabel) {
        return addCfgV()
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel);
    }

    default GraphTraversal<S, Vertex> addStmtNode(String nodeType, String textLabel, int line) {
        return addCfgV(nodeType, textLabel)
                .property(SRC_LINE_NO, line);
    }

    default GraphTraversal<S, Vertex> addEntryNode(String name, String signature, String type) {
        return addCfgV(ENTRY, name)
                .property(METHOD_NAME, name)
                .property(METHOD_SIG, signature)
                .property(JAVA_TYPE, type);
    }

    // Abstract syntax tree

    default GraphTraversal<S, Vertex> addAstV() {
        return (GraphTraversal<S, Vertex>) addV(AST_NODE);
    }

    default GraphTraversal<S, Vertex> addAstV(String nodeType, String textLabel) {
        return addAstV()
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel);
    }

    default GraphTraversal<S, Vertex> addExprNode(String exprType, String textLabel, String javaType) {
        return addAstV(EXPR, textLabel)
                .property(EXPR_TYPE, exprType)
                .property(JAVA_TYPE, javaType);
    }

    default GraphTraversal<S, Vertex> addConstNode(String javaType, String textLabel, String value) {
        return addAstV(CONSTANT, textLabel)
                .property(JAVA_TYPE, javaType)
                .property(VALUE, value);
    }

    default GraphTraversal<S, Vertex> addRefNode(String refType, String textLabel, String javaType) {
        return addAstV(REF, textLabel)
                .property(REF_TYPE, refType)
                .property(JAVA_TYPE, javaType);
    }

    default GraphTraversal<S, Vertex> addLocalNode(String javaType, String textLabel, String name) {
        return addAstV(LOCAL_VAR, textLabel)
                .property(JAVA_TYPE, javaType)
                .property(NAME, name);
    }

    // ********************************************************************************************
    // addE traversals
    // ********************************************************************************************

    // Control flow graph

    default GraphTraversal<S, Edge> addCfgE() {
        return (GraphTraversal<S, Edge>) addE(CFG_EDGE);
    }

    default GraphTraversal<S, Edge> addCfgE(String edgeType, String textLabel, boolean interproc) {
        return addCfgE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .property(INTERPROC, interproc);
    }

    default GraphTraversal<S, Edge> addEmptyEdge() {
        return addCfgE(EMPTY, EMPTY, false);
    }

    default GraphTraversal<S, Edge> addCondEdge(String condition) {
        return addCfgE(CONDITION, condition, false)
                .property(CONDITION, condition);
    }

    default GraphTraversal<S, Edge> genCallEdge() {
        return addCfgE(CALL, CALL, true);
    }

    default GraphTraversal<S, Edge> genRetEdge() {
        return addCfgE(RET, RET, true);
    }

    // Abstract syntax tree

    default GraphTraversal<S, Edge> addAstE() {
        return (GraphTraversal<S, Edge>) addE(AST_EDGE);
    }

    default GraphTraversal<S, Edge> addAstE(String edgeType, String textLabel) {
        return addAstE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel);
    }

    // Program dependence graph

    default GraphTraversal<S, Edge> addPdgE() {
        return (GraphTraversal<S, Edge>) addE(PDG_EDGE);
    }

    default GraphTraversal<S, Edge> addPdgE(String edgeType, String textLabel) {
        return addPdgE()
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel);
    }

    default GraphTraversal<S, Edge> addDataDepE(String var) {
        return addPdgE(DATA_DEP, var)
                .property(VAR_NAME, var);
    }

    default GraphTraversal<S, Edge> addArgDepE() {
        return addPdgE(ARG_DEP, ARG_DEP);
    }

    default GraphTraversal<S, Edge> addRetDepE() {
        return addPdgE(RET_DEP, RET_DEP);
    }

    // ********************************************************************************************
    // statement AST traversals
    // ********************************************************************************************

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

}