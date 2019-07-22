package graft.cpg;

import com.github.javaparser.Position;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

import static graft.Const.*;
import static graft.cpg.AstBuilder.*;
import static graft.cpg.CpgBuilder.*;

/**
 * Generate the control flow graph.
 *
 * @author Wim Keirsgieter
 */
class CfgBuilder {

    static void labelNextStmt(AstWalkContext context, String label) {
        // TODO
    }

    static void enterCatch(CatchClause clause, AstWalkContext context) {
        String textLabel = "catch (" + clause.getParameter().toString() + ")";
        Vertex catchVertex = genCfgNode(context, clause.getBegin(), CATCH, textLabel);
        Vertex paramVertex = genParamNode(clause.getParameter(), context);
        genAstEdge(catchVertex, paramVertex, PARAM, PARAM);
    }

    static void enterDoWhile(DoStmt doWhile, AstWalkContext context) {

    }

    static void enterFor(ForStmt forLoop, AstWalkContext context) {
        // TODO: initialisation, body stmts..., update, conditional
        String textLabel = "for (" + forLoop.getInitialization() + "; " + forLoop.getCompare() + "; " + forLoop.getUpdate() + ")";
        Vertex stmtVertex = genCfgNode(context, forLoop.getBegin(), FOR_STMT, textLabel);
        context.enterStmt(forLoop, stmtVertex);
        for (Expression init : forLoop.getInitialization()) {
            Vertex initVertex = genExprNode(init, context).get(0);
            genAstEdge(stmtVertex, initVertex, INIT, INIT);
        }
        forLoop.getCompare().ifPresent(comp -> {
            Vertex compVertex = genExprNode(comp, context).get(0);
            genAstEdge(stmtVertex, compVertex, PRED, PRED);
        });
        for (Expression update : forLoop.getUpdate()) {
            Vertex updateVertex = genExprNode(update, context).get(0);
            genAstEdge(stmtVertex, updateVertex, UPDATE, UPDATE);
        }
        context.exitStmt(forLoop, stmtVertex);
    }

    static void enterForEach(ForEachStmt forEach, AstWalkContext context) {

    }

    static void enterIf(IfStmt ifStmt, AstWalkContext context) {
        String textLabel = "if " + "(" + ifStmt.getCondition().toString() + ")";
        Vertex stmtVertex = genCfgNode(context, ifStmt.getBegin(), IF_STMT, textLabel);
        context.enterStmt(ifStmt, stmtVertex);
        Vertex predVertex = genExprNode(ifStmt.getCondition(), context).get(0);
        genAstEdge(stmtVertex, predVertex, PRED, PRED);
        context.exitStmt(ifStmt, stmtVertex);
    }

    static void enterSwitchCase(SwitchEntry switchCase, AstWalkContext context) {

    }

    static void enterSwitch(SwitchStmt switchStmt, AstWalkContext context) {

    }

    static void enterSynchronized(SynchronizedStmt syncStmt, AstWalkContext context) {

    }

    static void enterTry(TryStmt tryStmt, AstWalkContext context) {

    }

    static void enterWhile(WhileStmt whileStmt, AstWalkContext context) {
        String textLabel = "while (" + whileStmt.getCondition().toString() + ")";
        Vertex stmtVertex = genCfgNode(context, whileStmt.getBegin(), WHILE_STMT, textLabel);
        context.enterStmt(whileStmt, stmtVertex);
        Vertex guard = genExprNode(whileStmt.getCondition(), context).get(0);
        genAstEdge(stmtVertex, guard, GUARD, GUARD);
        context.exitStmt(whileStmt, stmtVertex);
    }

    static void addAssertStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        addCfgNode(stmt, stmtVertex, context);
    }

    static void addExprStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        addCfgNode(stmt, stmtVertex, context);
    }

    static void addJumpStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        addCfgNode(stmt, stmtVertex, context);
    }

    static void addReturnStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        addCfgNode(stmt, stmtVertex, context);
    }

    static void addThrowStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {

    }

    private static void addStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {

    }

    private static void addCfgNode(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        // TODO
        context.exitStmt(stmt, stmtVertex);
    }

    // TODO: this should be private
    static Vertex genCfgNode(AstWalkContext context, Optional<Position> pos, String nodeType, String textLabel) {
        return genCpgNode(context, CFG_NODE, pos, nodeType, textLabel);
    }

    static Edge genCfgEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        return genCpgEdge(CFG_EDGE, from, to, edgeType, textLabel);
    }
}
