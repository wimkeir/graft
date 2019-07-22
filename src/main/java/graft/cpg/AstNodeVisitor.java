package graft.cpg;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.context.*;

import static graft.Const.*;
import static graft.cpg.AstBuilder.*;
import static graft.cpg.CfgBuilder.*;
import static graft.cpg.CpgUtil.*;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends GenericVisitorWithDefaults<ContextStack, ContextStack> {

    // TODO: support up to Java 11
    // TODO: keep AstBuilder methods to CfgBuilder

    private static Logger log = LoggerFactory.getLogger(AstNodeVisitor.class);

    @Override
    public ContextStack defaultAction(Node node, ContextStack contextStack) {
        log.trace("Unhandled node type '{}', ignoring", node.getClass());
        return contextStack;
    }

    @Override
    public ContextStack defaultAction(NodeList list, ContextStack contextStack) {
        log.trace("NodeList visitor method not implemented");
        return contextStack;
    }

    @Override
    public ContextStack visit(CompilationUnit cu, ContextStack contextStack) {
        log.trace("Visiting CompilationUnit");
        AstWalkContext context = contextStack.getCurrentContext();
        context.update(cu);
        contextStack.setCurrentContext(context);
        log.debug("Walking AST of file '{}'", context.currentFileName());
        return contextStack;
    }

    // ********************************************************************************************
    // declarations
    // ********************************************************************************************

    // TODO: enums

    @Override
    public ContextStack visit(ClassOrInterfaceDeclaration decl, ContextStack contextStack) {
        log.trace("Visiting ClassOrInterfaceDeclaration");
        AstWalkContext context = contextStack.getCurrentContext();
        context.update(decl);
        contextStack.setCurrentContext(context);
        log.debug("Walking AST of class '{}'", context.currentClass());
        return contextStack;
    }

    @Override
    public ContextStack visit(ConstructorDeclaration decl, ContextStack contextStack) {
        throw new UnsupportedOperationException("ConstructorDeclaration visitor implemented yet");
    }

    @Override
    public ContextStack visit(FieldDeclaration decl, ContextStack contextStack) {
        throw new UnsupportedOperationException("FieldDeclaration visitor implemented yet");
    }

    @Override
    public ContextStack visit(MethodDeclaration decl, ContextStack contextStack) {
        log.trace("Visiting MethodDeclaration");
        AstWalkContext context = contextStack.getCurrentContext();
        String textLabel = decl.getDeclarationAsString(true, false, true);
        Vertex entryNode = genCfgNode(decl.getBegin(), ENTRY, textLabel, contextStack.getCurrentContext());
        int index = 0;
        for (Parameter param : decl.getParameters()) {
            Vertex paramVertex = genParamNode(param);
            Edge paramEdge = genAstEdge(entryNode, paramVertex, PARAM, PARAM);
            addEdgeProperty(paramEdge, INDEX, "" + index);
        }
        context.update(decl, entryNode);
        contextStack.setCurrentContext(context);
        log.debug("Walking AST of method '{}'", context.currentMethod());
        return contextStack;
    }

    @Override
    public ContextStack visit(PackageDeclaration decl, ContextStack contextStack) {
        log.trace("Visiting PackageDeclaration");
        AstWalkContext context = contextStack.getCurrentContext();
        context.update(decl);
        contextStack.setCurrentContext(context);
        return contextStack;
    }

    // ********************************************************************************************
    // statements
    // ********************************************************************************************

    @Override
    public ContextStack visit(AssertStmt stmt, ContextStack contextStack) {
        log.trace("Visiting AssertStmt");
        Vertex assertVertex = genCfgNode(stmt.getBegin(), ASSERT_STMT, stmt.toString(), contextStack.getCurrentContext());
        Vertex condVertex = genExprNode(stmt.getCheck()).get(0);
        genAstEdge(assertVertex, condVertex, EXPR, EXPR);
        return addStmtNode(assertVertex, contextStack);
    }

    @Override
    public ContextStack visit(BlockStmt stmt, ContextStack contextStack) {
        log.trace("Visiting BlockStmt");
        return contextStack;
    }

    @Override
    public ContextStack visit(BreakStmt stmt, ContextStack contextStack) {
        // TODO: resolve as goto in post-processing
        log.trace("Visiting BreakStmt");
        Vertex breakVertex = genCfgNode(stmt.getBegin(), JUMP_STMT, stmt.toString(), contextStack.getCurrentContext());
        stmt.getValue().ifPresent(label -> {
            addNodeProperty(breakVertex, LABEL, ((NameExpr) label).getNameAsString());
        });
        return addStmtNode(breakVertex, contextStack);
    }

    @Override
    public ContextStack visit(CatchClause clause, ContextStack contextStack) {
        log.trace("Visiting CatchClause");
        return enterCatch(clause, contextStack);
    }

    @Override
    public ContextStack visit(ContinueStmt stmt, ContextStack contextStack) {
        // TODO: resolve as goto in post-processing
        log.trace("Visiting ContinueStmt");
        Vertex contVertex = genCfgNode(stmt.getBegin(), JUMP_STMT, stmt.toString(), contextStack.getCurrentContext());
        stmt.getLabel().ifPresent(label -> {
            addNodeProperty(contVertex, LABEL, label.asString());
        });
        return addStmtNode(contVertex, contextStack);
    }

    @Override
    public ContextStack visit(DoStmt stmt, ContextStack contextStack) {
        log.trace("Visiting DoStmt");
        return enterDoWhile(stmt, contextStack);
    }

    @Override
    public ContextStack visit(ExplicitConstructorInvocationStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ExplicitConstructorInvocationStmt");
        return contextStack;
    }

    @Override
    public ContextStack visit(ExpressionStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ExpressionStmt");
        Vertex stmtVertex = genCfgNode(stmt.getBegin(), EXPR_STMT, stmt.toString(), contextStack.getCurrentContext());
        Vertex exprVertex = genExprNode(stmt.getExpression()).get(0);
        genAstEdge(stmtVertex, exprVertex, EXPR, EXPR);
        return addStmtNode(stmtVertex, contextStack);
    }

    @Override
    public ContextStack visit(ForEachStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ForEachStmt");
        return enterForEach(stmt, contextStack);
    }

    @Override
    public ContextStack visit(ForStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ForStmt");
        return enterFor(stmt, contextStack);
    }

    @Override
    public ContextStack visit(IfStmt stmt, ContextStack contextStack) {
        log.trace("Visiting IfStmt");
        return enterIf(stmt, contextStack);
    }

    @Override
    public ContextStack visit(LabeledStmt stmt, ContextStack contextStack) {
        log.trace("Visiting LabeledStmt");
        return labelNextStmt(stmt.getLabel().asString(), contextStack);
    }

    @Override
    public ContextStack visit(LocalClassDeclarationStmt stmt, ContextStack contextStack) {
        log.trace("Visiting LocalClassDeclarationStmt");
        return contextStack;
    }

    @Override
    public ContextStack visit(ReturnStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ReturnStmt");
        Vertex retVertex = genCfgNode(stmt.getBegin(), RETURN_STMT, stmt.toString(), contextStack.getCurrentContext());
        stmt.getExpression().ifPresent(expr -> {
            Vertex exprVertex = genExprNode(expr).get(0);
            genAstEdge(retVertex, exprVertex, RETURNS, RETURNS);
        });
        return addStmtNode(retVertex, contextStack);
    }

    @Override
    public ContextStack visit(SwitchEntry entry, ContextStack contextStack) {
        log.trace("Visiting SwitchEntry");
        return enterSwitchCase(entry, contextStack);
    }

    @Override
    public ContextStack visit(SwitchStmt stmt, ContextStack contextStack) {
        log.trace("Visiting SwitchStmt");
        return enterSwitch(stmt, contextStack);
    }

    @Override
    public ContextStack visit(SynchronizedStmt stmt, ContextStack contextStack) {
        log.trace("Visiting SynchronizedStmt");
        return enterSynchronized(stmt, contextStack);
    }

    @Override
    public ContextStack visit(ThrowStmt stmt, ContextStack contextStack) {
        log.trace("Visiting ThrowStmt");
        Vertex throwVertex = genCfgNode(stmt.getBegin(), THROW_STMT, stmt.toString(), contextStack.getCurrentContext());
        Vertex excVertex = genExprNode(stmt.getExpression()).get(0);
        genAstEdge(throwVertex, excVertex, THROWS, THROWS);
        return addStmtNode(throwVertex, contextStack);
    }

    @Override
    public ContextStack visit(TryStmt stmt, ContextStack contextStack) {
        log.trace("Visiting TryStmt");
        return enterTry(stmt, contextStack);
    }

    @Override
    public ContextStack visit(UnparsableStmt stmt, ContextStack contextStack) {
        log.error("Unparsable statement in file '{}': {}",
                    contextStack.getCurrentContext().currentFilePath(),
                    stmt.toString());
        return contextStack;
    }

    @Override
    public ContextStack visit(WhileStmt stmt, ContextStack contextStack) {
        log.trace("Visiting WhileStmt");
        return enterWhile(stmt, contextStack);
    }

}