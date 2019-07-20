package graft.cpg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.db.GraphUtil.graph;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<AstWalkContext> {

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
    public void visit(Parameter param, AstWalkContext context) {
        // TODO: varargs, annotations
        log.trace("Visiting Parameter");
        String textLabel = param.getType().asString() + " " + param.getNameAsString();
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex paramVertex = genAstNode(context, param.getBegin(), PARAM, textLabel);
        g.V(paramVertex)
                .property(JAVA_TYPE, param.getType().asString())
                .property(NAME, param.getNameAsString())
                .iterate();
        Edge paramEdge = genAstEdge(context.cfgTail(), paramVertex, PARAM, PARAM);
        g.V(paramEdge).property(INDEX, context.getParamIndex()).iterate();
        context.incrementParamIndex();
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
        throw new UnsupportedOperationException("BreakStmt visitor not implemented yet");
    }

    @Override
    public void visit(CatchClause clause, AstWalkContext context) {
        throw new UnsupportedOperationException("CatchClause visitor not implemented yet");
    }

    @Override
    public void visit(ContinueStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ContinueStmt visitor not implemented yet");
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
        throw new UnsupportedOperationException("ForEachStmt visitor ot implemented yet");
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

        String textLabel = decl.getDeclarationAsString(true, false, true);
        Vertex entryNode = genCfgNode(context, decl.getBegin(), ENTRY, textLabel);
        context.setCfgTail(entryNode);
    }

    @Override
    public void visit(PackageDeclaration decl, AstWalkContext context) {
        log.trace("Visiting PackageDeclaration");
        context.update(decl);
    }

    private List<Vertex> genExprNode(Expression expr, AstWalkContext context) {

        List<Vertex> vertices = new ArrayList<>();
        if (expr instanceof AssignExpr) {
            vertices.add(genAssignExprNode((AssignExpr) expr, context));
        } else if (expr instanceof BinaryExpr) {
            vertices.add(genBinaryExprNode((BinaryExpr) expr, context));
        } else if (expr instanceof UnaryExpr) {
            vertices.add(genUnaryExprNode((UnaryExpr) expr, context));
        } else if (expr instanceof LiteralExpr) {
            vertices.add(genLiteralExprNode((LiteralExpr) expr, context));
        } else if (expr instanceof MethodCallExpr) {
            vertices.add(genMethodCallExprNode((MethodCallExpr) expr, context));
        } else if (expr instanceof NameExpr) {
            vertices.add(genNameExprNode((NameExpr) expr, context));
        } else if (expr instanceof VariableDeclarationExpr) {
            GraphTraversalSource g = graph().traversal(CpgTraversalSource.class);
            VariableDeclarationExpr varDeclExpr = (VariableDeclarationExpr) expr;
            for (VariableDeclarator varDecl : varDeclExpr.getVariables()) {
                Optional<Expression> initOpt = varDecl.getInitializer();
                if (!initOpt.isPresent()) {
                    continue;
                }
                Vertex exprVertex = genAstNode(context, varDecl.getBegin(), ASSIGN_EXPR, varDecl.getNameAsString());
                // TODO: operator

                String targetTextLabel = varDecl.getType().asString() + " " + varDecl.getNameAsString();
                Vertex target = genAstNode(context, varDecl.getName().getBegin(), LOCAL_VAR, targetTextLabel);
                g.V(target)
                        .property(JAVA_TYPE, varDecl.getType().asString())
                        .property(NAME, varDecl.getNameAsString())
                        .iterate();
                g.addE(AST_EDGE)
                        .from(exprVertex).to(target)
                        .property(EDGE_TYPE, TARGET)
                        .property(TEXT_LABEL, TARGET)
                        .iterate();

                List<Vertex> values = genExprNode(initOpt.get(), context);
                for (Vertex value : values) {
                    genAstEdge(exprVertex, value, VALUE, VALUE);
                }
                vertices.add(exprVertex);
            }
        } else {
            log.warn("Unhandled expression type '{}', no node generated", expr.getClass());
        }

        return vertices;
    }

    private Vertex genAnnotationExprNode(AnnotationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genArrayAccessExprNode(ArrayAccessExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genArrayCreationExprNode(ArrayCreationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genArrayInitializerExprNode(ArrayInitializerExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genAssignExprNode(AssignExpr expr, AstWalkContext context) {
        // TODO: operator
        Vertex exprVertex = genAstNode(context, expr.getBegin(), ASSIGN_EXPR, expr.toString());
        Vertex target = genExprNode(expr.getTarget(), context).get(0);
        Vertex value = genExprNode(expr.getValue(), context).get(0);
        genAstEdge(exprVertex, target, TARGET, TARGET);
        genAstEdge(exprVertex, value, VALUE, VALUE);
        return exprVertex;
    }

    private Vertex genBinaryExprNode(BinaryExpr expr, AstWalkContext context) {
        Vertex exprVertex = genAstNode(context, expr.getBegin(), BINARY_EXPR, expr.toString());
        Vertex lop = genExprNode(expr.getLeft(), context).get(0);
        Vertex rop = genExprNode(expr.getRight(), context).get(0);
        genAstEdge(exprVertex, lop, LEFT_OPERAND, LEFT_OPERAND);
        genAstEdge(exprVertex, rop, RIGHT_OPERAND, RIGHT_OPERAND);
        return exprVertex;
    }

    private Vertex genCastExprNode(CastExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genClassExprNode(ClassExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genConditionalExprNode(ConditionalExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genEnclosedExprNode(EnclosedExpr expr, AstWalkContext context) {
        return genExprNode(expr.getInner(), context).get(0);
    }

    private Vertex genFieldAccessExprNode(FieldAccessExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genInstanceOfExprNode(InstanceOfExpr expr, AstWalkContext context) {
        Vertex exprVertex = genAstNode(context, expr.getBegin(), INSTANCEOF_EXPR, expr.toString());
        // TODO: ref type and operand
        return exprVertex;
    }

    private Vertex genLambdaExprNode(LambdaExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genLiteralExprNode(LiteralExpr expr, AstWalkContext context) {
        // TODO: handle all subclasses

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex exprVertex;
        if (expr instanceof IntegerLiteralExpr) {
            IntegerLiteralExpr intExpr = (IntegerLiteralExpr) expr;
            exprVertex = genAstNode(context, intExpr.getBegin(), LITERAL, intExpr.toString());
            g.V(exprVertex)
                    .property(JAVA_TYPE, INT)
                    .property(VALUE, intExpr.asInt())
                    .iterate();
        } else if (expr instanceof StringLiteralExpr) {
            StringLiteralExpr strExpr = (StringLiteralExpr) expr;
            exprVertex = genAstNode(context, strExpr.getBegin(), LITERAL, strExpr.toString());
            g.V(exprVertex)
                    .property(JAVA_TYPE, INT)
                    .property(VALUE, strExpr.asString())
                    .iterate();
        } else {
            exprVertex = null;
        }
        return exprVertex;
    }

    private Vertex genMethodCallExprNode(MethodCallExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        Vertex exprVertex = genAstNode(context, expr.getBegin(), CALL_EXPR, expr.toString());
        // TODO: scope
        g.V(exprVertex).property(CALLS, expr.getNameAsString()).iterate();

        int i = 0;
        for (Expression arg : expr.getArguments()) {
            Vertex argVertex = genExprNode(arg, context).get(0);
            Edge argEdge = genAstEdge(exprVertex, argVertex, ARG, ARG);
            g.E(argEdge).property(INDEX, i++).iterate();
        }

        return exprVertex;
    }

    private Vertex genNameExprNode(NameExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        Vertex exprVertex = genAstNode(context, expr.getBegin(), LOCAL_VAR, expr.getNameAsString());
        // TODO: resolve type, value if known
        g.V(exprVertex).property(NAME, expr.getNameAsString());

        return exprVertex;
    }

    private Vertex genObjectCreationExprNode(ObjectCreationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genSuperExprNode(SuperExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), SUPER_EXPR, textLabel);
        // TODO: type
        // TODO: link w/ super constructor in interproc
        return exprVertex;
    }

    private Vertex genSwitchExprNode(SwitchExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genThisExprNode(ThisExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), THIS_EXPR, textLabel);
        // TODO: type
        return exprVertex;
    }

    private Vertex genUnaryExprNode(UnaryExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), UNARY_EXPR, textLabel);
        Vertex opVertex = genExprNode(expr.getExpression(), context).get(0);
        genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);
        return exprVertex;
    }

}