package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import soot.jimple.*;

import graft.cpg.AstBuilder;
import graft.cpg.CfgBuilder;

import static graft.Const.*;

/**
 * Visitor applied to Jimple statements to create CFG nodes for them.
 */
public class StmtVisitor extends AbstractStmtSwitch {

    // Helper method to create a CFG node for definition statements, with AST nodes for the target and value
    private void definitionStmt(DefinitionStmt stmt) {
        Vertex assignVertex = CfgBuilder.genCfgNode(ASSIGN_STMT, stmt.toString());

        Vertex leftOpVertex = AstBuilder.genValueNode(stmt.getLeftOp());
        Vertex rightOpVertex = AstBuilder.genValueNode(stmt.getRightOp());
        AstBuilder.genAstEdge(assignVertex, leftOpVertex, TARGET, TARGET);
        AstBuilder.genAstEdge(assignVertex, rightOpVertex, VALUE, VALUE);

        setResult(assignVertex);
    }

    @Override
    public void caseAssignStmt(AssignStmt stmt) {
        definitionStmt(stmt);
    }

    @Override
    public void caseBreakpointStmt(BreakpointStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
        // TODO: is this a synchronized block?
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseGotoStmt(GotoStmt stmt) {
        // TODO: resolve (ignore?)
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {
        definitionStmt(stmt);
    }

    @Override
    public void caseIfStmt(IfStmt stmt) {
        Vertex ifVertex = CfgBuilder.genCfgNode(CONDITIONAL_STMT, stmt.toString());

        Vertex condVertex = AstBuilder.genValueNode(stmt.getCondition());
        AstBuilder.genAstEdge(ifVertex, condVertex, EXPR, EXPR);

        setResult(ifVertex);
    }

    @Override
    public void caseInvokeStmt(InvokeStmt stmt) {
        Vertex invokeVertex = CfgBuilder.genCfgNode(INVOKE_STMT, stmt.toString());

        Vertex exprVertex = AstBuilder.genValueNode(stmt.getInvokeExpr());
        AstBuilder.genAstEdge(invokeVertex, exprVertex, EXPR, EXPR);

        setResult(invokeVertex);
    }

    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseNopStmt(NopStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseRetStmt(RetStmt stmt) {
        // TODO: ret stmt vs return stmt?
        Vertex retVertex = CfgBuilder.genCfgNode(RETURN_STMT, stmt.toString());
        setResult(retVertex);
    }

    @Override
    public void caseReturnStmt(ReturnStmt stmt) {
        Vertex retVertex = CfgBuilder.genCfgNode(RETURN_STMT, stmt.toString());

        Vertex opVertex = AstBuilder.genValueNode(stmt.getOp());
        AstBuilder.genAstEdge(retVertex, opVertex, RETURNS, RETURNS);

        setResult(retVertex);
    }

    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
        Vertex retVertex = CfgBuilder.genCfgNode(RETURN_STMT, stmt.toString());
        setResult(retVertex);
    }

    @Override
    public void caseTableSwitchStmt(TableSwitchStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseThrowStmt(ThrowStmt stmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void defaultCase(Object obj) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
