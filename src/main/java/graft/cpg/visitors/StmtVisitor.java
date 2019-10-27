package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;
import soot.jimple.*;

import graft.Graft;
import graft.cpg.AstBuilder;
import graft.utils.SootUtil;

import static graft.Const.*;

/**
 * Visitor applied to Jimple statements to create CFG nodes for them.
 *
 * @author Wim Keirsgieter
 */
public class StmtVisitor extends AbstractStmtSwitch {

    private static Logger log = LoggerFactory.getLogger(StmtVisitor.class);

    @Override
    public void caseAssignStmt(AssignStmt stmt) {
        log.trace("Visiting AssignStmt");
        caseDefinitionStmt(stmt);
    }

    @Override
    public void caseBreakpointStmt(BreakpointStmt stmt) {
        log.trace("Visiting BreakpointStmt");
        log.info("Encountered breakpoint instruction during CFG construction");
        setResult(Graft.cpg().traversal()
                .addStmtNode(BREAKPOINT_STMT, stmt.toString(), SootUtil.getLineNr(stmt))
                .next());
    }

    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
        log.trace("Visiting EnterMonitorStmt");
        caseStmtWithOp(stmt, ENTER_MONITOR_STMT, stmt.getOp(), MONITOR);
    }

    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
        log.trace("Visiting ExitMonitorStmt");
        caseStmtWithOp(stmt, EXIT_MONITOR_STMT, stmt.getOp(), MONITOR);
    }

    @Override
    public void caseGotoStmt(GotoStmt stmt) {
        log.trace("Visiting GotoStmt - ignoring");
    }

    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {
        log.trace("Visiting IdentityStmt");
        caseDefinitionStmt(stmt);
    }

    @Override
    public void caseIfStmt(IfStmt stmt) {
        log.trace("Visiting IfStmt");
        caseStmtWithOp(stmt, CONDITIONAL_STMT, stmt.getCondition(), CONDITION);
    }

    @Override
    public void caseInvokeStmt(InvokeStmt stmt) {
        log.trace("Visiting InvokeStmt");
        caseStmtWithOp(stmt, INVOKE_STMT, stmt.getInvokeExpr(), INVOKE_EXPR);
    }

    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
        log.trace("Visiting LookupSwitchStmt");
        caseStmtWithOp(stmt, LOOKUP_SWITCH_STMT, stmt.getKey(), SWITCH_KEY);
    }

    @Override
    public void caseNopStmt(NopStmt stmt) {
        log.trace("Visiting NopStmt - ignoring");
    }

    @Override
    public void caseRetStmt(RetStmt stmt) {
        log.trace("Visiting RetStmt");
        setResult(Graft.cpg().traversal()
                .addStmtNode(RETURN_STMT, stmt.toString(), SootUtil.getLineNr(stmt))
                .next());
    }

    @Override
    public void caseReturnStmt(ReturnStmt stmt) {
        log.trace("Visiting ReturnStmt");
        caseStmtWithOp(stmt, RETURN_STMT, stmt.getOp(), RETURNS);
    }

    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
        log.trace("Visiting ReturnVoidStmt");
        setResult(Graft.cpg().traversal()
                .addStmtNode(RETURN_STMT, stmt.toString(), SootUtil.getLineNr(stmt))
                .next());
    }

    @Override
    public void caseTableSwitchStmt(TableSwitchStmt stmt) {
        log.trace("Visiting TableSwitchStmt");
        caseStmtWithOp(stmt, TABLE_SWITCH_STMT, stmt.getKey(), SWITCH_KEY);
    }

    @Override
    public void caseThrowStmt(ThrowStmt stmt) {
        log.trace("Visiting ThrowStmt");
        caseStmtWithOp(stmt, THROW_STMT, stmt.getOp(), THROWS);
    }

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Stmt class '{}'", obj.getClass());
        throw new RuntimeException("Unrecognised statement class (see logs for details)");
    }

    // Generate CFG node for definition statements, with AST nodes for the target and value
    private void caseDefinitionStmt(DefinitionStmt stmt) {
        Vertex stmtNode = (Vertex) Graft.cpg().traversal()
                .addStmtNode(BREAKPOINT_STMT, stmt.toString(), SootUtil.getLineNr(stmt))
                .next();

        Graft.cpg().traversal()
                .addAstE(TARGET, TARGET)
                .from(stmtNode)
                .to(AstBuilder.genValueNode(stmt.getLeftOp()))
                .iterate();
        Graft.cpg().traversal()
                .addAstE(VALUE, VALUE)
                .from(stmtNode)
                .to(AstBuilder.genValueNode(stmt.getRightOp()))
                .iterate();

        setResult(stmtNode);
    }

    // Generates a CFG node for a statement of the given type with an operand of the given type
    private void caseStmtWithOp(Stmt stmt, String stmtType, Value op, String opType) {
        Vertex stmtNode = (Vertex) Graft.cpg().traversal()
                .addStmtNode(stmtType, stmt.toString(), SootUtil.getLineNr(stmt))
                .next();

        Graft.cpg().traversal()
                .addAstE(opType, opType)
                .from(stmtNode)
                .to(AstBuilder.genValueNode(op))
                .iterate();

        setResult(stmtNode);
    }

}
