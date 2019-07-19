package graft.cpg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
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
        Vertex paramVertex = baseAstNode(param, PARAM, textLabel, context);

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        g.V(paramVertex)
                .property(JAVA_TYPE, param.getType().asString())
                .property(NAME, param.getNameAsString())
                .iterate();
        g.addE(AST_EDGE)
                .from(context.astTail()).to(paramVertex)
                .property(INDEX, context.getParamIndex())
                .property(EDGE_TYPE, PARAM)
                .property(TEXT_LABEL, PARAM)
                .iterate();

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
        throw new UnsupportedOperationException("AssertStmt visitor not implemented yet");
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

        String textLabel = stmt.toString();
        Vertex exprStmtVertex = baseCfgNode(stmt, EXPR_STMT, textLabel, context);

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        g.addE(CFG_EDGE)
                .from(context.cfgTail()).to(exprStmtVertex)
                .property(EDGE_TYPE, EMPTY)
                .property(TEXT_LABEL, EMPTY)
                .iterate();

        Vertex expr = genExprNode(stmt.getExpression(), context).get(0);

        if (expr != null) {
            g.addE(AST_EDGE)
                    .from(exprStmtVertex).to(expr)
                    .property(EDGE_TYPE, EXPR)
                    .property(TEXT_LABEL, EXPR)
                    .iterate();
        }

        context.setAstTail(exprStmtVertex);
        context.setCfgTail(exprStmtVertex);
    }

    @Override
    public void visit(ForEachStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ForEachStmt visitor ot implemented yet");
    }

    @Override
    public void visit(ForStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ForStmt visitor not implemented yet");
    }

    @Override
    public void visit(IfStmt stmt, AstWalkContext context) {
        // TODO NB: true/false edges!
        log.trace("Visiting IfStmt");

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        BlockStmt thenBlock;
        if (stmt.hasThenBlock()) {
            thenBlock = stmt.getThenStmt().asBlockStmt();
        } else {
            NodeList<Statement> stmts = new NodeList<>();
            stmts.add(stmt.getThenStmt());
            thenBlock = new BlockStmt(stmts);
        }
        stmt.remove(stmt.getThenStmt());

        String textLabel = "if " + "(" + stmt.getCondition().toString() + ")";
        Vertex v = baseCfgNode(stmt, IF_STMT, textLabel, context);
        g.addE(CFG_EDGE)
                .from(context.cfgTail()).to(v)
                .property(EDGE_TYPE, EMPTY)
                .property(TEXT_LABEL, EMPTY)
                .iterate();

        Vertex predVertex = genExprNode(stmt.getCondition(), context).get(0);
        g.addE(AST_EDGE)
                .from(v).to(predVertex)
                .property(EDGE_TYPE, PRED)
                .property(TEXT_LABEL, PRED)
                .iterate();

        context.setCfgTail(v);
        for (Statement thenStmt : thenBlock.getStatements()) {
            thenStmt.accept(this, context);
        }
        Vertex thenTail = context.cfgTail();

        BlockStmt elseBlock;
        if (stmt.hasElseBranch() && stmt.hasElseBlock()) {
            elseBlock = stmt.getElseStmt().get().asBlockStmt();
            stmt.remove(stmt.getElseStmt().get());
        } else if (stmt.hasElseBranch()) {
            NodeList<Statement> stmts = new NodeList<>();
            stmts.add(stmt.getElseStmt().get());
            elseBlock = new BlockStmt(stmts);
            stmt.remove(stmt.getElseStmt().get());
        } else {
            elseBlock = null;
        }

        context.setCfgTail(v);
        if (elseBlock != null) {
            for (Statement elseStmt : elseBlock.getStatements()) {
                elseStmt.accept(this, context);
            }
        }
        Vertex elseTail = context.cfgTail();

        Vertex phi = g.addV(CFG_NODE)
                .property(NODE_TYPE, PHI)
                .property(TEXT_LABEL, PHI)
                .next();

        g.addE(CFG_EDGE)
                .from(thenTail).to(phi)
                .property(EDGE_TYPE, EMPTY)
                .property(TEXT_LABEL, EMPTY)
                .iterate();
        g.addE(CFG_EDGE)
                .from(elseTail).to(phi)
                .property(EDGE_TYPE, EMPTY)
                .property(TEXT_LABEL, EMPTY)
                .iterate();

        context.setCfgTail(phi);
    }

    @Override
    public void visit(LocalClassDeclarationStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("LocalClassDeclarationStmt visitor not implemented yet");
    }

    @Override
    public void visit(ReturnStmt stmt, AstWalkContext context) {
        throw new UnsupportedOperationException("ReturnStmt visitor not implemented yet");
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
        throw new UnsupportedOperationException("WhileStmt visitor implemented yet");
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
        Vertex entryNode = baseCfgNode(decl, ENTRY, textLabel, context);
        context.setAstTail(entryNode);
        context.setCfgTail(entryNode);
    }

    @Override
    public void visit(PackageDeclaration decl, AstWalkContext context) {
        log.trace("Visiting PackageDeclaration");
        context.update(decl);
    }

    // TODO: some serious refactoring necessary here

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
                Vertex v = baseAstNode(varDecl, ASSIGN_EXPR, varDecl.getNameAsString(), context);
                // TODO: operator

                String targetTextLabel = varDecl.getType().asString() + " " + varDecl.getNameAsString();
                Vertex target = baseAstNode(varDecl.getName(), LOCAL_VAR, targetTextLabel, context);
                g.V(target)
                        .property(JAVA_TYPE, varDecl.getType().asString())
                        .property(NAME, varDecl.getNameAsString())
                        .iterate();
                g.addE(AST_EDGE)
                        .from(v).to(target)
                        .property(EDGE_TYPE, TARGET)
                        .property(TEXT_LABEL, TARGET)
                        .iterate();

                List<Vertex> values = genExprNode(initOpt.get(), context);
                for (Vertex value : values) {
                    g.addE(AST_EDGE)
                            .from(v).to(value)
                            .property(EDGE_TYPE, VALUE)
                            .property(TEXT_LABEL, VALUE)
                            .iterate();
                }
                vertices.add(v);
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
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex v;
        String textLabel = expr.toString();
        v = baseAstNode(expr, ASSIGN_EXPR, textLabel, context);
        // TODO: operator

        Vertex target = genExprNode(expr.getTarget(), context).get(0);
        Vertex value = genExprNode(expr.getValue(), context).get(0);

        g.addE(AST_EDGE)
                .from(v).to(target)
                .property(EDGE_TYPE, TARGET)
                .property(TEXT_LABEL, TARGET)
                .iterate();

        g.addE(AST_EDGE)
                .from(v).to(value)
                .property(EDGE_TYPE, VALUE)
                .property(TEXT_LABEL, VALUE)
                .iterate();
        return v;
    }

    private Vertex genBinaryExprNode(BinaryExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex v;
        String textLabel = expr.toString();
        v = baseAstNode(expr, BINARY_EXPR, textLabel, context);

        Vertex lop = genExprNode(expr.getLeft(), context).get(0);
        Vertex rop = genExprNode(expr.getRight(), context).get(0);

        g.addE(AST_EDGE)
                .from(v).to(lop)
                .property(EDGE_TYPE, LEFT_OPERAND)
                .property(TEXT_LABEL, LEFT_OPERAND)
                .iterate();

        g.addE(AST_EDGE)
                .from(v).to(rop)
                .property(EDGE_TYPE, RIGHT_OPERAND)
                .property(TEXT_LABEL, RIGHT_OPERAND)
                .iterate();
        return v;
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
        return null;    // TODO
    }

    private Vertex genFieldAccessExprNode(FieldAccessExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genInstanceOfExprNode(InstanceOfExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genLambdaExprNode(LambdaExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genLiteralExprNode(LiteralExpr expr, AstWalkContext context) {
        // TODO: handle all subclasses

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex v;
        if (expr instanceof IntegerLiteralExpr) {
            IntegerLiteralExpr intExpr = (IntegerLiteralExpr) expr;
            v = baseAstNode(intExpr, LITERAL, intExpr.toString(), context);
            g.V(v)
                    .property(JAVA_TYPE, INT)
                    .property(VALUE, intExpr.asInt())
                    .iterate();
        } else if (expr instanceof StringLiteralExpr) {
            StringLiteralExpr strExpr = (StringLiteralExpr) expr;
            v = baseAstNode(strExpr, LITERAL, strExpr.toString(), context);
            g.V(v)
                    .property(JAVA_TYPE, INT)
                    .property(VALUE, strExpr.asString())
                    .iterate();
        } else {
            v = null;
        }
        return v;
    }

    private Vertex genMethodCallExprNode(MethodCallExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        Vertex v = baseAstNode(expr, CALL_EXPR, expr.toString(), context);
        g.V(v)
                // TODO: scope
                .property(CALLS, expr.getNameAsString())
                .iterate();

        int i = 0;
        for (Expression arg : expr.getArguments()) {
            Vertex argVertex = genExprNode(arg, context).get(0);
            g.addE(AST_EDGE)
                    .from(v).to(argVertex)
                    .property(EDGE_TYPE, ARG)
                    .property(TEXT_LABEL, ARG)
                    .property(INDEX, i++)
                    .iterate();
        }

        return v;
    }

    private Vertex genNameExprNode(NameExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        Vertex v = baseAstNode(expr, LOCAL_VAR, expr.getNameAsString(), context);
        // TODO: resolve type, value if known
        g.V(v)
                .property(NAME, expr.getNameAsString());

        return v;
    }

    private Vertex genObjectCreationExprNode(ObjectCreationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genSuperExprNode(SuperExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genSwitchExprNode(SwitchExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genThisExprNode(ThisExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private Vertex genUnaryExprNode(UnaryExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        Vertex v;
        String textLabel = expr.toString();
        v = baseAstNode(expr, UNARY_EXPR, textLabel, context);
        UnaryExpr unaryExpr = (UnaryExpr) expr;

        Vertex op = genExprNode(unaryExpr.getExpression(), context).get(0);
        g.addE(AST_EDGE)
                .from(v).to(op)
                .property(EDGE_TYPE, OPERAND)
                .property(TEXT_LABEL, OPERAND)
                .iterate();
        return v;
    }

    private int getLineNr(Node node) {
        Optional<Position> posOpt = node.getBegin();
        if (posOpt.isPresent()) {
            return posOpt.get().line;
        } else {
            return -1;
        }
    }

    private int getColNr(Node node) {
        Optional<Position> posOpt = node.getBegin();
        if (posOpt.isPresent()) {
            return posOpt.get().column;
        } else {
            return -1;
        }
    }

    private Vertex baseCfgNode(Node node, String type, String textLabel, AstWalkContext context) {
        return baseCpgNode(node, CFG_NODE, type, textLabel, context);
    }

    private Vertex baseAstNode(Node node, String type, String textLabel, AstWalkContext context) {
        return baseCpgNode(node, AST_NODE, type, textLabel, context);
    }

    private Vertex baseCpgNode(Node node, String label, String nodeType, String textLabel, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addV(label)
                .property(NODE_TYPE, nodeType)
                .property(FILE_PATH, context.currentFilePath())
                .property(FILE_NAME, context.currentFileName())
                .property(PACKAGE_NAME, context.currentPackage())
                .property(CLASS_NAME, context.currentClass())
                .property(METHOD_NAME, context.currentMethod())
                .property(TEXT_LABEL, textLabel)
                .property(LINE_NO, getLineNr(node))
                .property(COL_NO, getColNr(node))
                .next();
    }

}