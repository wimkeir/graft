package graft.cpg;

import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.context.*;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.AstBuilder.*;
import static graft.cpg.CpgUtil.*;
import static graft.db.GraphUtil.graph;

/**
 * Generate the control flow graph.
 *
 * @author Wim Keirsgieter
 */
class CfgBuilder {

    private static Logger log = LoggerFactory.getLogger(CfgBuilder.class);

    static ContextStack labelNextStmt(String label, ContextStack contextStack) {
        return contextStack;
    }

    // ********************************************************************************************
    // entering contexts
    // ********************************************************************************************

    static ContextStack enterCatch(CatchClause clause, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterDoWhile(DoStmt doWhile, ContextStack contextStack) {
        AstWalkContext context = contextStack.getCurrentContext();
        DoContext doContext = new DoContext(context, doWhile);
        contextStack.pushNewContext(doContext);
        return contextStack;
    }

    static ContextStack enterFor(ForStmt forLoop, ContextStack contextStack) {
        log.trace("Entering FOR context");
        AstWalkContext context = contextStack.getCurrentContext();
        Vertex lastInit = context.cfgTail();
        for (Expression init : forLoop.getInitialization()) {
            lastInit = genCfgNode(init.getBegin(), EXPR_STMT, init.toString(), contextStack.getCurrentContext());
            Vertex exprVertex = genExprNode(init).get(0);
            genAstEdge(lastInit, exprVertex, EXPR, EXPR);
            genCfgEdge(context.cfgTail(), lastInit, EMPTY, EMPTY);
            context.updateCfgTail(lastInit);
        }
        ForContext forContext = new ForContext(context, forLoop, lastInit);
        contextStack.pushNewContext(forContext);
        return contextStack;
    }

    static ContextStack enterForEach(ForEachStmt forEach, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterIf(IfStmt ifStmt, ContextStack contextStack) {
        log.trace("Entering IF context");
        Vertex conditional = genCfgNode(ifStmt.getBegin(),
                                        CONDITIONAL_STMT,
                                        ifStmt.getCondition().toString(),
                                        contextStack.getCurrentContext());
        Vertex exprVertex = genExprNode(ifStmt.getCondition()).get(0);
        genAstEdge(conditional, exprVertex, EXPR, EXPR);
        addStmtNode(conditional, contextStack);
        IfContext ifContext = new IfContext(contextStack.getCurrentContext(), ifStmt, conditional);
        contextStack.pushNewContext(ifContext);
        return contextStack;
    }

    static ContextStack enterSwitchCase(SwitchEntry switchCase, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterSwitch(SwitchStmt switchStmt, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterSynchronized(SynchronizedStmt syncStmt, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterTry(TryStmt tryStmt, ContextStack contextStack) {
        return contextStack;
    }

    static ContextStack enterWhile(WhileStmt whileStmt, ContextStack contextStack) {
        AstWalkContext context = contextStack.getCurrentContext();
        Vertex conditional = genCfgNode(whileStmt.getBegin(),
                                        CONDITIONAL_STMT,
                                        whileStmt.getCondition().toString(),
                                        context);
        genCfgEdge(context.cfgTail(), conditional, EMPTY, EMPTY);
        context.updateCfgTail(conditional);
        WhileContext whileContext = new WhileContext(context, whileStmt, conditional);
        contextStack.pushNewContext(whileContext);
        return contextStack;
    }

    // ********************************************************************************************
    // exiting contexts
    // ********************************************************************************************

    private static ContextStack exitDoWhile(ContextStack contextStack) {
        log.trace("Exiting DO context");
        assert contextStack.getCurrentContext() instanceof DoContext;
        DoContext context = (DoContext) contextStack.popCurrentContext();

        // generate conditional node
        Vertex conditional = genCfgNode(context.getConditionalExpr().getBegin(),
                                        CONDITIONAL_STMT,
                                        context.getConditionalExpr().toString(),
                                        context);

        // TODO
        return contextStack;
    }

    private static ContextStack exitIfContext(ContextStack contextStack) {
        log.trace("Exiting IF context");
        assert contextStack.getCurrentContext() instanceof IfContext;
        IfContext context = (IfContext) contextStack.popCurrentContext();

        // generate phi node
        Vertex phi = genCfgNode(Optional.empty(), PHI, PHI, contextStack.getCurrentContext());
        genCfgEdge(context.thenTail(), phi, EMPTY, EMPTY);
        genCfgEdge(context.elseTail(), phi, EMPTY, EMPTY);

        // set outer context tail to phi node
        AstWalkContext outerContext = contextStack.getCurrentContext();
        outerContext.updateCfgTail(phi);
        contextStack.setCurrentContext(outerContext);
        return contextStack;
    }

    private static ContextStack exitWhileContext(ContextStack contextStack) {
        log.trace("Exiting WHILE context");
        assert contextStack.getCurrentContext() instanceof WhileContext;
        WhileContext context = (WhileContext) contextStack.popCurrentContext();

        // draw edge back to conditional
        genCfgEdge(context.cfgTail(), context.getConditional(), EMPTY, EMPTY);

        // set conditional as outer context CFG tail
        AstWalkContext outerContext = contextStack.getCurrentContext();
        outerContext.updateCfgTail(context.getConditional());
        contextStack.setCurrentContext(outerContext);
        return contextStack;

    }

    private static ContextStack exitForContext(ContextStack contextStack) {
        log.trace("Exiting FOR context");
        assert contextStack.getCurrentContext() instanceof ForContext;
        ForContext context = (ForContext) contextStack.popCurrentContext();

        // generate update and check nodes
        for (Expression update : context.getUpdates()) {
            Vertex updateVertex = genCfgNode(Optional.empty(), EXPR_STMT, update.toString(), context);
            Vertex exprVertex = genExprNode(update).get(0);
            genAstEdge(updateVertex, exprVertex, EXPR, EXPR);
            genCfgEdge(context.cfgTail(), updateVertex, EMPTY, EMPTY);
            context.updateCfgTail(updateVertex);
        }
        Expression check = context.getCheck();
        if (check != null) {
            Vertex checkVertex = genCfgNode(Optional.empty(), CONDITIONAL_STMT, check.toString(), context);
            genCfgEdge(context.cfgTail(), checkVertex, EMPTY, EMPTY);
            genCfgEdge(checkVertex, getNextCfgNode(context.getLastInit()), TRUE, TRUE);
            AstWalkContext outerContext = contextStack.getCurrentContext();
            outerContext.updateCfgTail(checkVertex);
            contextStack.setCurrentContext(outerContext);
            return contextStack;
        } else {
            genCfgEdge(context.cfgTail(), context.getLastInit(), EMPTY, EMPTY);
            // TODO: this is an infinite loop, what should we do here?
            return contextStack;
        }
    }

    static ContextStack addStmtNode(Vertex stmtVertex, ContextStack contextStack) {
        AstWalkContext context = contextStack.getCurrentContext();
        if (context.inBlock() && context.getStmtsRemaining() == 0) {
            if (context instanceof IfContext) {
                context = exitIfContext(contextStack).getCurrentContext();
            } else if (context instanceof WhileContext) {
                context = exitWhileContext(contextStack).getCurrentContext();
            } else if (context instanceof ForContext) {
                context = exitForContext(contextStack).getCurrentContext();
            }
        } else if (context.inBlock()) {
            context.decrStmtsRemaining();
        }
        genCfgEdge(context.cfgTail(), stmtVertex, EMPTY, EMPTY);
        context.updateCfgTail(stmtVertex);
        contextStack.setCurrentContext(context);
        return contextStack;
    }

    // TODO: make this private
    public static Vertex genCfgNode(Optional<Position> pos, String nodeType, String textLabel, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addV(CFG_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .property(FILE_PATH, context.currentFilePath())
                .property(FILE_NAME, context.currentFileName())
                .property(PACKAGE_NAME, context.currentPackage())
                .property(CLASS_NAME, context.currentClass())
                .property(METHOD_NAME, context.currentMethod())
                .property(LINE_NO, lineNr(pos))
                .property(COL_NO, colNr(pos))
                .next();
    }

    private static Edge genCfgEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addE(CFG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

}
