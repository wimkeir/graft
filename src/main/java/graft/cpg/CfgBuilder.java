package graft.cpg;

import java.util.List;
import java.util.Map;

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

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;

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
        Vertex entryNode = (Vertex) Graft.cpg().traversal()
                .addEntryNode(method.getName(), method.getSignature(), getTypeString(method.getReturnType()))
                .next();

        for (Unit head : unitGraph.getHeads()) {
            Vertex headVertex = genUnitNode(head, unitGraph, generatedNodes);
            Graft.cpg().traversal()
                    .addEmptyEdge()
                    .from(entryNode).to(headVertex)
                    .iterate();
        }

        return entryNode;
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
            Graft.cpg().traversal()
                    .addEmptyEdge()
                    .from(unitVertex).to(succNode)
                    .iterate();
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
                Graft.cpg().traversal()
                        .addCondEdge(TRUE)
                        .from(ifNode).to(succNode)
                        .iterate();
            } else {
                Graft.cpg().traversal()
                        .addCondEdge(FALSE)
                        .from(ifNode).to(succNode)
                        .iterate();
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
            Graft.cpg().traversal()
                    .addCondEdge(switchStmt.getLookupValue(i) + "")
                    .from(switchNode).to(targetNode)
                    .iterate();
        }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget(), unitGraph, generated);
            Graft.cpg().traversal()
                    .addCondEdge(DEFAULT_TARGET)
                    .from(switchNode).to(defaultNode)
                    .iterate();
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
            Graft.cpg().traversal()
                    .addCondEdge(i + "")
                    .from(switchNode).to(targetNode)
                    .iterate();         }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget(), unitGraph, generated);
            Graft.cpg().traversal()
                    .addCondEdge(DEFAULT_TARGET)
                    .from(switchNode).to(defaultNode)
                    .iterate();
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

}
