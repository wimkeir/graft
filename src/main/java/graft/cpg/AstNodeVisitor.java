package graft.cpg;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.AstBuilder.*;
import static graft.cpg.CfgBuilder.*;
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
        log.trace("Unhandled node type '{}', ignoring", node.getClass());
    }

    @Override
    public void defaultAction(NodeList list, AstWalkContext context) {
        log.trace("NodeList visitor method not implemented");
    }

    @Override
    public void visit(CompilationUnit cu, AstWalkContext context) {
        log.trace("Visiting CompilationUnit");
        context.update(cu);
        log.debug("Walking AST of file '{}'", context.currentFileName());
    }

    // ********************************************************************************************
    // declarations
    // ********************************************************************************************

    // TODO: enums

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

    // ********************************************************************************************
    // statements
    // ********************************************************************************************

    @Override
    public void visit(AssertStmt stmt, AstWalkContext context) {
        log.trace("Visiting AssertStmt");
        Vertex assertVertex = genCfgNode(context, stmt.getBegin(), ASSERT_STMT, stmt.toString());
        Vertex condVertex = genExprNode(stmt.getCheck(), context).get(0);
        genAstEdge(assertVertex, condVertex, EXPR, EXPR);
        addAssertStmt(stmt, assertVertex, context);
    }

    @Override
    public void visit(BlockStmt stmt, AstWalkContext context) {
        log.trace("Visiting BlockStmt");
    }

    @Override
    public void visit(BreakStmt stmt, AstWalkContext context) {
        // TODO: resolve as goto in post-processing
        log.trace("Visiting BreakStmt");
        Vertex breakVertex = genCfgNode(context, stmt.getBegin(), JUMP_STMT, stmt.toString());
        stmt.getValue().ifPresent(label -> {
            addNodeProperty(breakVertex, LABEL, ((NameExpr) label).getNameAsString());
        });
        addJumpStmt(stmt, breakVertex, context);
    }

    @Override
    public void visit(CatchClause clause, AstWalkContext context) {
        log.trace("Visiting CatchClause");
        enterCatch(clause, context);
    }

    @Override
    public void visit(ContinueStmt stmt, AstWalkContext context) {
        // TODO: resolve as goto in post-processing
        log.trace("Visiting ContinueStmt");
        Vertex contVertex = genCfgNode(context, stmt.getBegin(), JUMP_STMT, stmt.toString());
        stmt.getLabel().ifPresent(label -> {
            addNodeProperty(contVertex, LABEL, label.asString());
        });
        addJumpStmt(stmt, contVertex, context);
    }

    @Override
    public void visit(DoStmt stmt, AstWalkContext context) {
        log.trace("Visiting DoStmt");
        enterDoWhile(stmt, context);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt stmt, AstWalkContext context) {
        log.trace("Visiting ExplicitConstructorInvocationStmt");
        // TODO
    }

    @Override
    public void visit(ExpressionStmt stmt, AstWalkContext context) {
        log.trace("Visiting ExpressionStmt");
        Vertex stmtVertex = genCfgNode(context, stmt.getBegin(), EXPR_STMT, stmt.toString());
        Vertex exprVertex = genExprNode(stmt.getExpression(), context).get(0);
        genAstEdge(stmtVertex, exprVertex, EXPR, EXPR);
        addExprStmt(stmt, stmtVertex, context);
    }

    @Override
    public void visit(ForEachStmt stmt, AstWalkContext context) {
        log.trace("Visiting ForEachStmt");
        enterForEach(stmt, context);
    }

    @Override
    public void visit(ForStmt stmt, AstWalkContext context) {
        log.trace("Visiting ForStmt");
        enterFor(stmt, context);
    }

    @Override
    public void visit(IfStmt stmt, AstWalkContext context) {
        log.trace("Visiting IfStmt");
        enterIf(stmt, context);
    }

    @Override
    public void visit(LabeledStmt stmt, AstWalkContext context) {
        log.trace("Visiting LabeledStmt");
        labelNextStmt(context, stmt.getLabel().asString());
    }

    @Override
    public void visit(LocalClassDeclarationStmt stmt, AstWalkContext context) {
        log.trace("Visiting LocalClassDeclarationStmt");
        // TODO
    }

    @Override
    public void visit(ReturnStmt stmt, AstWalkContext context) {
        log.trace("Visiting ReturnStmt");
        Vertex retVertex = genCfgNode(context, stmt.getBegin(), RETURN_STMT, stmt.toString());
        stmt.getExpression().ifPresent(expr -> {
            Vertex exprVertex = genExprNode(expr, context).get(0);
            genAstEdge(retVertex, exprVertex, RETURNS, RETURNS);
        });
        addReturnStmt(stmt, retVertex, context);
    }

    @Override
    public void visit(SwitchEntry entry, AstWalkContext context) {
        log.trace("Visiting SwitchEntry");
        enterSwitchCase(entry, context);
    }

    @Override
    public void visit(SwitchStmt stmt, AstWalkContext context) {
        log.trace("Visiting SwitchStmt");
        enterSwitch(stmt, context);
    }

    @Override
    public void visit(SynchronizedStmt stmt, AstWalkContext context) {
        log.trace("Visiting SynchronizedStmt");
        enterSynchronized(stmt, context);
    }

    @Override
    public void visit(ThrowStmt stmt, AstWalkContext context) {
        log.trace("Visiting ThrowStmt");
        Vertex throwVertex = genCfgNode(context, stmt.getBegin(), THROW_STMT, stmt.toString());
        Vertex excVertex = genExprNode(stmt.getExpression(), context).get(0);
        genAstEdge(throwVertex, excVertex, THROWS, THROWS);
        addThrowStmt(stmt, throwVertex, context);

    }

    @Override
    public void visit(TryStmt stmt, AstWalkContext context) {
        log.trace("Visiting TryStmt");
        enterTry(stmt, context);
    }

    @Override
    public void visit(UnparsableStmt stmt, AstWalkContext context) {
        log.error("Unparsable statement in file '{}': {}", context.currentFilePath(), stmt.toString());
    }

    @Override
    public void visit(WhileStmt stmt, AstWalkContext context) {
        log.trace("Visiting WhileStmt");
        enterWhile(stmt, context);
    }

}