package graft.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.cpg.structure.VertexDescription;

import java.util.List;
import java.util.Set;

import static graft.Const.*;

/**
 * TODO: javadoc
 *
 * @author Wim Keirsgieter
 */
@GremlinDsl(traversalSource = "graft.traversal.CpgTraversalSourceDsl")
public interface CpgTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    default GraphTraversal<S, Vertex> locals(String varName) {
        // TODO: specify method sig too
        return (CpgTraversal<S, Vertex>) V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, varName);
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
                .inDataDep(varName[0]);
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Path> controlFlowsTo(Vertex w) {
        // TODO: only intraproc!
        return repeat((CpgTraversal) timeLimit(1000).outE(CFG_EDGE).has(INTERPROC, false).inV().simplePath())
                .until(is(w))
                .path();
    }

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> unsanitized(CpgTraversal sanitizer) {
        Set<Vertex> sanitizers = sanitizer.toSet();
        CpgTraversal g = (CpgTraversal) this.clone();
        return (CpgTraversal<S, Vertex>) filter(t -> {
            Path dataFlowPath = (Path) t.get();
            for (int i = 0; i < dataFlowPath.size() - 2; i += 2) {
                CpgTraversal start = (CpgTraversal) g.clone().V(dataFlowPath.get(i));
                List<Path> controlFlowPaths = start.controlFlowsTo(dataFlowPath.get(i + 2)).toList();
                for (Path cfgPath : controlFlowPaths) {
                    for (int j = 0; j < cfgPath.size(); j++) {
                        if (sanitizers.contains(cfgPath.get(j))) {
                            return false;
                        }
                    }
                }
            }
            return true;
        });
    }

//    default GraphTraversal<S, Path> unsanitizedDataFlows(CpgTraversal sanitizer) {
//        return filter(t -> {
//            Path dataFlow = (Path) t.get();
//            for (int i = 0; i < dataFlow.size() - 2; i += 2) {
//
//            }
//        })
//    }
//
//    default GraphTraversal<S, Path> unsanitizedControlFlows(CpgTraversal sanitizer) {
//        return filter(t -> {
//
//        })
//    }

    default GraphTraversal<S, Vertex> inDataDep() {
        return inE(PDG_EDGE).has(EDGE_TYPE, DATA_DEP).outV();
    }

    default GraphTraversal<S, Vertex> inDataDep(String varName) {
        return inDataDep().has(VAR_NAME, varName);
    }

    default GraphTraversal<S, Vertex> outDataDep() {
        return outE(PDG_EDGE).has(EDGE_TYPE, DATA_DEP).inV();
    }

    default GraphTraversal<S, Vertex> outDataDep(String varName) {
        return outDataDep().has(VAR_NAME, varName);
    }

    default GraphTraversal<S, Vertex> outEmpty() {
        return outE(CFG_EDGE).has(EDGE_TYPE, EMPTY).inV();
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

    default GraphTraversal<S, Edge> genCallEdge(String context) {
        return addCfgE(CALL, CALL + ":" + context, true)
                .property(CONTEXT, context);
    }

    default GraphTraversal<S, Edge> genRetEdge(String context) {
        return addCfgE(RET, RET + ":" + context, true)
                .property(CONTEXT, context);
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
    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> matches(VertexDescription descr) {
        return (GraphTraversal<S, Vertex>) hasLabel(descr.LABEL).filter(t -> {
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

    default GraphTraversal<S, ?> copy() {
        return this.clone();
    }

}