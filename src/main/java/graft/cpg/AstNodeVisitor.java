package graft.cpg;

import java.util.Optional;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.AstBuilder.*;
import static graft.cpg.CpgUtil.*;
import static graft.db.GraphUtil.graph;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<AstWalkContext> {

    // TODO: support up to Java 11

    private static Logger log = LoggerFactory.getLogger(AstNodeVisitor.class);

    @Override
    public void defaultAction(Node node, AstWalkContext context) {
        log.debug("Unhandled node type '{}', ignoring", node.getClass());
    }

    @Override
    public void defaultAction(NodeList list, AstWalkContext context) {
        log.debug("NodeList visitor method not implemented");
    }

    // TODO: enums, union/intersection types, annotations

    @Override
    public void visit(CompilationUnit cu, AstWalkContext context) {
        log.trace("Visiting CompilationUnit");
        context.update(cu);
        log.debug("Walking AST of file '{}'", context.currentFileName());
    }

    @Override
    public void visit(TypeParameter param, AstWalkContext context) {
        // a generic type parameter
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // statements / blocks
    // ********************************************************************************************

    @Override
    public void visit(AssertStmt stmt, AstWalkContext context) {
        log.trace("Visiting AssertStmt");
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), ASSERT_STMT, stmt.toString());
        Vertex condVertex = genExprNode(stmt.getCheck(), context).get(0);
        genAstEdge(stmtVertex, condVertex, EXPR, EXPR);
        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(BlockStmt stmt, AstWalkContext context) {
        log.trace("Visiting BlockStmt");
    }

    @Override
    public void visit(BreakStmt stmt, AstWalkContext context) {
        // TODO: link this to correct place
        log.trace("Visiting BreakStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), BREAK_STMT, stmt.toString());

        Optional<Expression> labelOpt = stmt.getValue();
        labelOpt.ifPresent(label -> {
            assert label instanceof NameExpr;
            g.V(stmtVertex).property(LABEL, ((NameExpr) label).getNameAsString()).iterate();
        });

        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(CatchClause clause, AstWalkContext context) {
        throw new UnsupportedOperationException("CatchClause visitor not implemented yet");
    }

    @Override
    public void visit(ContinueStmt stmt, AstWalkContext context) {
        // TODO: link this to the correct place
        log.trace("Visiting ContinueStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), CONTINUE_STMT, stmt.toString());

        Optional<SimpleName> labelOpt = stmt.getLabel();
        labelOpt.ifPresent(label -> {
            g.V(stmtVertex).property(LABEL, label.asString()).iterate();
        });

        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(DoStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("DoStmt visitor not implemented yet");
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ExplicitConstructorInvocationStmt visitor not implemented yet");
    }

    @Override
    public void visit(ExpressionStmt stmt, AstWalkContext context) {
        log.trace("Visiting ExpressionStmt");
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), EXPR_STMT, stmt.toString());

        Vertex expr = genExprNode(stmt.getExpression(), context).get(0);
        if (expr != null) {
            genAstEdge(stmtVertex, expr, EXPR, EXPR);
        }

        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(ForEachStmt stmt, AstWalkContext context) {
        log.trace("Visiting ForEachStmt");
        // TODO
    }

    @Override
    public void visit(ForStmt stmt, AstWalkContext context) {
        log.trace("Visiting ForStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        String textLabel = "for (" + stmt.getInitialization() + "; " + stmt.getCompare() + "; " + stmt.getUpdate() + ")";
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), FOR_STMT, textLabel);
        context.enterStmt(stmt, stmtVertex);

        for (Expression init : stmt.getInitialization()) {
            Vertex initVertex = genExprNode(init, context).get(0);
            genAstEdge(stmtVertex, initVertex, INIT, INIT);
        }
        stmt.getCompare().ifPresent(comp -> {
            Vertex compVertex = genExprNode(comp, context).get(0);
            genAstEdge(stmtVertex, compVertex, PRED, PRED);
        });
        for (Expression update : stmt.getUpdate()) {
            Vertex updateVertex = genExprNode(update, context).get(0);
            genAstEdge(stmtVertex, updateVertex, UPDATE, UPDATE);
        }

        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(IfStmt stmt, AstWalkContext context) {
        // TODO NB: true/false edges!
        log.trace("Visiting IfStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        String textLabel = "if " + "(" + stmt.getCondition().toString() + ")";
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), IF_STMT, textLabel);
        context.enterStmt(stmt, stmtVertex);

        Vertex predVertex = genExprNode(stmt.getCondition(), context).get(0);
        genAstEdge(stmtVertex, predVertex, PRED, PRED);
        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(LabeledStmt stmt, AstWalkContext context) {
        log.trace("Visiting LabeledStmt");
        // TODO: set label in context for next stmt
    }

    @Override
    public void visit(LocalClassDeclarationStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("LocalClassDeclarationStmt visitor not implemented yet");
    }

    @Override
    public void visit(ReturnStmt stmt, AstWalkContext context) {
        log.trace("Visiting ReturnStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), RETURN_STMT, stmt.toString());

        Optional<Expression> exprOpt = stmt.getExpression();
        if (exprOpt.isPresent()) {
            Vertex expr = genExprNode(exprOpt.get(), context).get(0);
            genAstEdge(stmtVertex, expr, RETURNS, RETURNS);
            g.V(stmtVertex).property(VOID, FALSE);
        } else {
            g.V(stmtVertex).property(VOID, TRUE);
        }

        context.exitStmt(stmt, stmtVertex);
    }

    @Override
    public void visit(SwitchEntry entry, AstWalkContext context) {
        throw new UnsupportedOperationException("SwitchEntry visitor not implemented yet");
    }

    @Override
    public void visit(SwitchStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("SwitchStmt visitor not implemented yet");
    }

    @Override
    public void visit(SynchronizedStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("SynchronizedStmt visitor not implemented yet");
    }

    @Override
    public void visit(ThrowStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ThrowStmt visitor not implemented yet");
    }

    @Override
    public void visit(TryStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("TryStmt visitor not implemented yet");
    }

    @Override
    public void visit(UnparsableStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("UnparsableStmt visitor not implemented yet");
    }

    @Override
    public void visit(WhileStmt stmt, AstWalkContext context) {
        // TODO NB: true/false edges
        log.trace("Visiting WhileStmt");
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        String textLabel = "while (" + stmt.getCondition().toString() + ")";
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), WHILE_STMT, textLabel);
        context.enterStmt(stmt, stmtVertex);

        Vertex guard = genExprNode(stmt.getCondition(), context).get(0);
        genAstEdge(stmtVertex, guard, GUARD, GUARD);

        context.exitStmt(stmt, stmtVertex);
    }

    private void enterStmt(Statement stmt, AstWalkContext context) {
        log.trace("Entering stmt " + stmt.toString().replace('\n', ' '));
    }

    private void exitStmt(Statement stmt, Vertex stmtVertex, AstWalkContext context) {
        log.trace("Exiting stmt " + stmt.toString().replace('\n', ' '));
    }

    // ********************************************************************************************
    // declarations
    // ********************************************************************************************

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, AstWalkContext context) {
        log.trace("Visiting ClassOrInterfaceDeclaration");
        context.update(decl);
        log.debug("Walking AST of class '{}'", context.currentClass());
    }

    @Override
    public void visit(ConstructorDeclaration decl, AstWalkContext context) {
        throw new UnsupportedOperationException("ConstructorDeclaration visitor implemented yet");
    }

    @Override
    public void visit(FieldDeclaration decl, AstWalkContext context) {
        throw new UnsupportedOperationException("FieldDeclaration visitor implemented yet");
    }

    @Override
    public void visit(MethodDeclaration decl, AstWalkContext context) {
        log.trace("Visiting MethodDeclaration");
        context.update(decl);
        log.debug("Walking AST of method '{}'", context.currentMethod());
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        String textLabel = decl.getDeclarationAsString(true, false, true);
        Vertex entryNode = genCfgNode(context, decl.getBegin(), ENTRY, textLabel);

        int index = 0;
        for (Parameter param : decl.getParameters()) {
            Vertex paramVertex = genParamNode(param, context);
            Edge paramEdge = genAstEdge(entryNode, paramVertex, PARAM, PARAM);
            g.V(paramEdge).property(INDEX, index).iterate();
        }
        context.setCfgTail(entryNode);
    }

    @Override
    public void visit(PackageDeclaration decl, AstWalkContext context) {
        log.trace("Visiting PackageDeclaration");
        context.update(decl);
    }

}