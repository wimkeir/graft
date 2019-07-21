package graft.cpg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.db.GraphUtil.graph;

class AstBuilder {

    private static Logger log = LoggerFactory.getLogger(AstBuilder.class);

    static Vertex genParamNode(Parameter param, AstWalkContext context) {
        // TODO: varargs, annotations
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        String textLabel = param.getType().asString() + " " + param.getNameAsString();
        Vertex paramVertex = genAstNode(context, param.getBegin(), PARAM, textLabel);
        return g.V(paramVertex)
                .property(JAVA_TYPE, param.getType().asString())
                .property(NAME, param.getNameAsString())
                .next();
    }

    static List<Vertex> genExprNode(Expression expr, AstWalkContext context) {

        List<Vertex> vertices = new ArrayList<>();
        if (expr instanceof ArrayAccessExpr) {
            vertices.add(genArrayAccessExprNode((ArrayAccessExpr) expr, context));
        } else if (expr instanceof AssignExpr) {
            vertices.add(genAssignExprNode((AssignExpr) expr, context));
        } else if (expr instanceof InstanceOfExpr) {
            vertices.add(genInstanceOfExprNode((InstanceOfExpr) expr, context));
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
        } else if (expr instanceof SuperExpr) {
            vertices.add(genSuperExprNode((SuperExpr) expr, context));
        } else if (expr instanceof ThisExpr) {
            vertices.add(genThisExprNode((ThisExpr) expr, context));
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

    private static Vertex genAnnotationExprNode(AnnotationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genArrayAccessExprNode(ArrayAccessExpr expr, AstWalkContext context) {
        Vertex exprVertex = genAstNode(context, expr.getBegin(), ARRAY_ACCESS_EXPR, expr.toString());
        Vertex baseVertex = genExprNode(expr.getName(), context).get(0);
        Vertex idxVertex = genExprNode(expr.getIndex(), context).get(0);
        genAstEdge(exprVertex, baseVertex, BASE, BASE);
        genAstEdge(exprVertex, idxVertex, INDEX, INDEX);
        return exprVertex;
    }

    private static Vertex genArrayCreationExprNode(ArrayCreationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genArrayInitializerExprNode(ArrayInitializerExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genAssignExprNode(AssignExpr expr, AstWalkContext context) {
        // TODO: operator
        Vertex exprVertex = genAstNode(context, expr.getBegin(), ASSIGN_EXPR, expr.toString());
        Vertex target = genExprNode(expr.getTarget(), context).get(0);
        Vertex value = genExprNode(expr.getValue(), context).get(0);
        genAstEdge(exprVertex, target, TARGET, TARGET);
        genAstEdge(exprVertex, value, VALUE, VALUE);
        return exprVertex;
    }

    private static Vertex genBinaryExprNode(BinaryExpr expr, AstWalkContext context) {
        Vertex exprVertex = genAstNode(context, expr.getBegin(), BINARY_EXPR, expr.toString());
        Vertex lop = genExprNode(expr.getLeft(), context).get(0);
        Vertex rop = genExprNode(expr.getRight(), context).get(0);
        genAstEdge(exprVertex, lop, LEFT_OPERAND, LEFT_OPERAND);
        genAstEdge(exprVertex, rop, RIGHT_OPERAND, RIGHT_OPERAND);
        return exprVertex;
    }

    private static Vertex genCastExprNode(CastExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genClassExprNode(ClassExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genConditionalExprNode(ConditionalExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genEnclosedExprNode(EnclosedExpr expr, AstWalkContext context) {
        return genExprNode(expr.getInner(), context).get(0);
    }

    private static Vertex genFieldAccessExprNode(FieldAccessExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genInstanceOfExprNode(InstanceOfExpr expr, AstWalkContext context) {
        Vertex exprVertex = genAstNode(context, expr.getBegin(), INSTANCEOF_EXPR, expr.toString());
        // TODO: ref type and operand
        return exprVertex;
    }

    private static Vertex genLambdaExprNode(LambdaExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genLiteralExprNode(LiteralExpr expr, AstWalkContext context) {
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

    private static Vertex genMethodCallExprNode(MethodCallExpr expr, AstWalkContext context) {
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

    private static Vertex genNameExprNode(NameExpr expr, AstWalkContext context) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);

        Vertex exprVertex = genAstNode(context, expr.getBegin(), LOCAL_VAR, expr.getNameAsString());
        // TODO: resolve type, value if known
        g.V(exprVertex).property(NAME, expr.getNameAsString());

        return exprVertex;
    }

    private static Vertex genObjectCreationExprNode(ObjectCreationExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genSuperExprNode(SuperExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), SUPER_EXPR, textLabel);
        // TODO: type
        // TODO: link w/ super constructor in interproc
        return exprVertex;
    }

    private static Vertex genSwitchExprNode(SwitchExpr expr, AstWalkContext context) {
        return null;    // TODO
    }

    private static Vertex genThisExprNode(ThisExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), THIS_EXPR, textLabel);
        // TODO: type
        return exprVertex;
    }

    private static Vertex genUnaryExprNode(UnaryExpr expr, AstWalkContext context) {
        String textLabel = expr.toString();
        Vertex exprVertex = genAstNode(context, expr.getBegin(), UNARY_EXPR, textLabel);
        Vertex opVertex = genExprNode(expr.getExpression(), context).get(0);
        genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);
        return exprVertex;
    }

}
