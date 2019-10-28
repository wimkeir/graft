package graft.cpg;

import java.util.HashMap;
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

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.traversal.__.*;

/**
 * Generate the control flow graph.
 *
 * @author Wim Keirsgieter
 */
public class CfgBuilder {

    private static Logger log = LoggerFactory.getLogger(CfgBuilder.class);

    private AstBuilder astBuilder;
    private Map<Unit, Vertex> generatedNodes;
    private UnitGraph unitGraph;
    private Vertex entryNode;

    public CfgBuilder(UnitGraph unitGraph, AstBuilder astBuilder) {
        this.unitGraph = unitGraph;
        this.astBuilder = astBuilder;
        this.generatedNodes = new HashMap<>();
    }

    // ********************************************************************************************
    // public methods
    // ********************************************************************************************

    public Vertex buildCfg() {
        SootMethod method = unitGraph.getBody().getMethod();
        log.debug("Building CFG for method '{}'", method.getName());
        entryNode = (Vertex) Graft.cpg().traversal()
                .addEntryNode(method.getName(), method.getSignature(), getTypeString(method.getReturnType()))
                .next();

        for (Unit head : unitGraph.getHeads()) {
            Vertex headVertex = genUnitNode(head);
            Graft.cpg().traversal()
                    .addEmptyEdge()
                    .from(entryNode).to(headVertex)
                    .iterate();
        }

        return entryNode;
    }

    public Map<Unit, Vertex> generatedNodes() {
        return generatedNodes;
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generate a CFG node for the given unit, with its successors
    @SuppressWarnings("unchecked")
    private Vertex genUnitNode(Unit unit) {
        if (unit instanceof GotoStmt) {
            // collapse goto statements
            return genUnitNode(((GotoStmt) unit).getTarget());
        }

        log.trace("Generating Unit '{}'", unit.toString());

        Vertex unitVertex = generatedNodes.get(unit);
        if (unitVertex == null) {
            StmtVisitor visitor = new StmtVisitor(astBuilder);
            unit.apply(visitor);
            unitVertex = (Vertex) visitor.getResult();

            // AST statement edge from entry node
            Graft.cpg().traversal()
                    .addAstE(STATEMENT, STATEMENT)
                    .from(entryNode).to(unitVertex)
                    .iterate();

            generatedNodes.put(unit, unitVertex);
        } else {
            return unitVertex;
        }

        // handle possible conditional edges
        if (unit instanceof IfStmt) {
            return genIfAndSuccs(unitVertex, (IfStmt) unit);
        } else if (unit instanceof LookupSwitchStmt) {
            return genLookupSwitchAndSuccs(unitVertex, (LookupSwitchStmt) unit);
        } else if (unit instanceof TableSwitchStmt) {
            return genTableSwitchAndSuccs(unitVertex, (TableSwitchStmt) unit);
        }

        List<Unit> succs = unitGraph.getSuccsOf(unit);
        assert succs.size() <= 1;

        if (succs.size() == 1) {
            Vertex succNode = genUnitNode(succs.get(0));
            Graft.cpg().traversal()
                    .V(unitVertex).as("v")
                    .V(succNode)
                    .coalesce(
                            inE(CFG_EDGE).where(outV().as("v")),
                            addEmptyEdge().from("v")
                    ).iterate();
        }

        return unitVertex;
    }

    private Vertex genIfAndSuccs(Vertex ifNode, IfStmt ifStmt) {
        for (Unit succ : unitGraph.getSuccsOf(ifStmt)) {
            Vertex succNode = genUnitNode(succ);
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

    private Vertex genLookupSwitchAndSuccs(Vertex switchNode, LookupSwitchStmt switchStmt) {
        for (int i = 0; i < switchStmt.getTargetCount(); i++) {
            Vertex targetNode = genUnitNode(switchStmt.getTarget(i));
            // TODO: how to handle lookup values?
            Graft.cpg().traversal()
                    .addCondEdge(switchStmt.getLookupValue(i) + "")
                    .from(switchNode).to(targetNode)
                    .iterate();
        }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget());
            Graft.cpg().traversal()
                    .addCondEdge(DEFAULT_TARGET)
                    .from(switchNode).to(defaultNode)
                    .iterate();
        }
        return switchNode;
    }

    private Vertex genTableSwitchAndSuccs(Vertex switchNode, TableSwitchStmt switchStmt) {
        for (Unit target : switchStmt.getTargets()) {
            Vertex targetNode = genUnitNode(target);
            // TODO: how to handle table values?
            Graft.cpg().traversal()
                    // TODO NB
                    .addCondEdge(UNKNOWN)
                    .from(switchNode).to(targetNode)
                    .iterate();
        }
        if (switchStmt.getDefaultTarget() != null) {
            Vertex defaultNode = genUnitNode(switchStmt.getDefaultTarget());
            Graft.cpg().traversal()
                    .addCondEdge(DEFAULT_TARGET)
                    .from(switchNode).to(defaultNode)
                    .iterate();
        }
        return switchNode;
    }

}
