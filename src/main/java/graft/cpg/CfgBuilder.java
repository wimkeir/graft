package graft.cpg;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;

import graft.Graft;
import graft.cpg.visitors.StmtVisitor;
import graft.traversal.CpgTraversalSource;
import graft.utils.SootUtil;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Generate the control flow graph.
 *
 * @author Wim Keirsgieter
 */
public class CfgBuilder {

    private static Logger log = LoggerFactory.getLogger(CfgBuilder.class);

    // ********************************************************************************************
    // public methods
    // ********************************************************************************************

    /**
     * Build a CFG with AST subtrees from the given unit graph, storing the generated nodes in a
     * map with their corresponding units as keys.
     *
     * @param unitGraph the unit graph
     * @param generatedNodes the map to store the generated nodes in
     * @return the method entry vertex of the CFG (ie. the root of this method's CFG)
     */
    public static Vertex buildCfg(UnitGraph unitGraph, Map<Unit, Vertex> generatedNodes) {
        SootMethod method = unitGraph.getBody().getMethod();
        log.debug("Building CFG for method '{}'", method.getName());
        Vertex entryNode = genMethodEntry(method);

        for (Unit head : unitGraph.getHeads()) {
            Vertex headVertex = genUnitNode(head, unitGraph, generatedNodes);
            genEmptyEdge(entryNode, headVertex);
        }

        return entryNode;
    }

    /**
     * Generate a base CFG node for the given Jimple statement.
     *
     * @param stmt the statement to generate a node for
     * @param nodeType the node type
     * @param textLabel the node's text label
     * @return the newly generated node
     */
    public static Vertex genStmtNode(Stmt stmt, String nodeType, String textLabel) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex node = g.addV(CFG_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .property(SRC_LINE_NO, SootUtil.getLineNr(stmt))
                .next();
        // Graft.cpg().commit();
        return node;
    }

    /**
     * Generate an interprocedural call edge between two CFG nodes.
     *
     * @param from the start vertex
     * @param to the end vertex
     * @param context the calling context
     * @return the newly generated edge
     */
    static Edge genCallEdge(Vertex from, Vertex to, String context) {
        return genInterprocEdge(from, to, CALL, context);
    }

    /**
     * Generate an interprocedural return edge between two CFG nodes.
     *
     * @param from the start vertex
     * @param to the end vertex
     * @param context the calling context
     * @return the newly generated edge
     */
    static Edge genRetEdge(Vertex from, Vertex to, String context) {
        return genInterprocEdge(from, to, RET, context);
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generate a CFG node for the given unit, with its successors
    private static Vertex genUnitNode(Unit unit, UnitGraph unitGraph, Map<Unit, Vertex> generated) {
        if (unit instanceof GotoStmt) {
            // collapse goto statements
            return genUnitNode(((GotoStmt) unit).getTarget(), unitGraph, generated);
        }

        CpgTraversalSource g = Graft.cpg().traversal();
        log.trace("Generating Unit '{}'", unit.toString());

        Vertex unitVertex = getOrGenUnitNode(unit, generated);

        // handle possible conditional edges
        if (unit instanceof IfStmt) {
            return genIfAndSuccs(unitGraph, unitVertex, (IfStmt) unit, generated);
        } else if (unit instanceof LookupSwitchStmt) {
            return genLookupSwitchAndSuccs(unitGraph, unitVertex, (LookupSwitchStmt) unit, generated);
        } else if (unit instanceof TableSwitchStmt) {
            return genTableSwitchAndSuccs(unitGraph, unitVertex, (TableSwitchStmt) unit, generated);
        }

        List<Unit> succs = unitGraph.getSuccsOf(unit);
        assert succs.size() <= 1;

        if (succs.size() == 1) {
            Vertex succNode = genUnitNode(succs.get(0), unitGraph, generated);
            genEmptyEdge(unitVertex, succNode);
        } else {
            if (!(unit instanceof RetStmt || unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt)) {
                log.warn("Non-return node with no children: '{}'", unit.toString());
            }
        }

        return unitVertex;
    }

    private static Vertex genIfAndSuccs(UnitGraph unitGraph, Vertex ifNode, IfStmt ifStmt, Map<Unit, Vertex> generated) {
        for (Unit succ : unitGraph.getSuccsOf(ifStmt)) {
            Vertex succNode = genUnitNode(succ, unitGraph, generated);
            if (succ.equals(ifStmt.getTarget())) {
                genConditionalEdge(ifNode, succNode, TRUE);
            } else {
                genConditionalEdge(ifNode, succNode, FALSE);
            }
        }
        return ifNode;
    }

    private static Vertex genLookupSwitchAndSuccs(UnitGraph unitGraph,
                                                  Vertex switchNode,
                                                  LookupSwitchStmt switchStmt,
                                                  Map<Unit, Vertex> generated) {
        assert switchStmt.getTargetCount() == unitGraph.getSuccsOf(switchStmt).size();
        for (int i = 0; i < switchStmt.getTargetCount(); i++) {
            Vertex targetNode = genUnitNode(switchStmt.getTarget(i), unitGraph, generated);
            // TODO: how to handle lookup values?
            genConditionalEdge(switchNode, targetNode, switchStmt.getLookupValue(i) + "");
        }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget(), unitGraph, generated);
            genConditionalEdge(switchNode, defaultNode, DEFAULT_TARGET);
        }
        return switchNode;
    }

    private static Vertex genTableSwitchAndSuccs(UnitGraph unitGraph,
                                                 Vertex switchNode,
                                                 TableSwitchStmt switchStmt,
                                                 Map<Unit, Vertex> generated) {
        assert (switchStmt.getLowIndex() - switchStmt.getLowIndex()) == unitGraph.getSuccsOf(switchStmt).size();
        for (int i = switchStmt.getLowIndex(); i < switchStmt.getHighIndex(); i++) {
            if (switchStmt.getTarget(i) == null) {
                // TODO: can this happen? fake labels to fill holes...
                log.debug("Ignoring non-existent table switch target");
                continue;
            }
            Vertex targetNode = genUnitNode(switchStmt.getTarget(i), unitGraph, generated);
            // TODO: how to handle table values?
            genConditionalEdge(switchNode, targetNode, i + "");
        }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget(), unitGraph, generated);
            genConditionalEdge(switchNode, defaultNode, DEFAULT_TARGET);
        }
        return switchNode;
    }

    // Try to get the node from the map of generated nodes - if it's not there, generate it using the statement visitor
    private static Vertex getOrGenUnitNode(Unit unit, Map<Unit, Vertex> generated) {
        Vertex node = generated.get(unit);
        if (node == null) {
            StmtVisitor visitor = new StmtVisitor();
            unit.apply(visitor);
            node = (Vertex) visitor.getResult();
            assert node != null;
            generated.put(unit, node);
        }
        return node;
    }

    private static Vertex genMethodEntry(SootMethod method) {
        // TODO: param AST nodes?
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex node = g.addV(CFG_NODE)
                .property(NODE_TYPE, ENTRY)
                .property(TEXT_LABEL, method.getName())
                .property(METHOD_NAME, method.getName())
                .property(METHOD_SIG, method.getSignature())
                .property(JAVA_TYPE, CpgUtil.getTypeString(method.getReturnType()))
                .property(SRC_LINE_NO, -1)
                .next();
        // Graft.cpg().commit();
        return node;
    }

    private static Edge genEmptyEdge(Vertex from, Vertex to) {
        // see https://stackoverflow.com/a/52447622
        // we have to watch out that we're not creating duplicate edges here
        assert from != null;
        assert to != null;
        CpgTraversalSource g = Graft.cpg().traversal();
        Edge edge = g.V(from).as("v").V(to)
                .coalesce(
                        inE(CFG_EDGE).where(outV().as("v")),
                        addE(CFG_EDGE)
                                .from("v").to(to)
                                .property(EDGE_TYPE, EMPTY)
                                .property(TEXT_LABEL, EMPTY)
                                .property(INTERPROC, false))
                .next();
        // Graft.cpg().commit();
        return edge;
    }

    private static Edge genConditionalEdge(Vertex from, Vertex to, String condition) {
        // TODO: condition object rather?
        assert from != null;
        assert to != null;
        CpgTraversalSource g = Graft.cpg().traversal();
        Edge edge = g.addE(CFG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, CONDITION)
                .property(TEXT_LABEL, condition)
                .property(INTERPROC, false)
                .property(CONDITION, condition)
                .next();
        // Graft.cpg().commit();
        return edge;
    }

    private static Edge genInterprocEdge(Vertex from, Vertex to, String edgeType, String context) {
        // TODO: context object rather?
        assert from != null;
        assert to != null;
        assert edgeType.equals(CALL) || edgeType.equals(RET);
        CpgTraversalSource g = Graft.cpg().traversal();
        Edge edge = g.addE(CFG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, edgeType) // TODO: include context in label
                .property(INTERPROC, TRUE)
                .property(CONTEXT, context)
                .next();
        // Graft.cpg().commit();
        return edge;
    }

}
