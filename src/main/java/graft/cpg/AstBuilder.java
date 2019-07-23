package graft.cpg;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.db.GraphUtil.graph;

/**
 * Generate AST nodes and subtrees.
 *
 * @author Wim Keirsgieter
 */
class AstBuilder {

    private static Logger log = LoggerFactory.getLogger(AstBuilder.class);

    /**
     * Generate an AST node for the given parameter.
     *
     * @param param the parameter node
     * @return the generated AST node
     */
    static Vertex genParamNode(Parameter param) {
        // TODO: varargs, annotations, modifiers
        String textLabel = param.getType().asString() + " " + param.getNameAsString();
        Vertex paramVertex = genAstNode(PARAM, textLabel);
        addNodeProperty(paramVertex, JAVA_TYPE, param.getType().asString());
        addNodeProperty(paramVertex, NAME, param.getNameAsString());
        return paramVertex;
    }

    /**
     * Generate one or more AST nodes (possibly a subtree) for the given expression.
     *
     * @param expr the expression node
     * @return the generated AST node or subtree
     */
    static List<Vertex> genExprNode(Expression expr) {
        List<Vertex> vertices = new ArrayList<>();
        if (expr instanceof ArrayAccessExpr) {
            vertices.add(genArrayAccessExprNode((ArrayAccessExpr) expr));
        } else if (expr instanceof AssignExpr) {
            vertices.add(genAssignExprNode((AssignExpr) expr));
        } else if (expr instanceof InstanceOfExpr) {
            vertices.add(genInstanceOfExprNode((InstanceOfExpr) expr));
        } else if (expr instanceof BinaryExpr) {
            vertices.add(genBinaryExprNode((BinaryExpr) expr));
        } else if (expr instanceof UnaryExpr) {
            vertices.add(genUnaryExprNode((UnaryExpr) expr));
        } else if (expr instanceof LiteralExpr) {
            vertices.add(genLiteralExprNode((LiteralExpr) expr));
        } else if (expr instanceof MethodCallExpr) {
            vertices.add(genMethodCallExprNode((MethodCallExpr) expr));
        } else if (expr instanceof NameExpr) {
            vertices.add(genNameExprNode((NameExpr) expr));
        } else if (expr instanceof ObjectCreationExpr) {
            vertices.add(genObjectCreationExprNode((ObjectCreationExpr) expr));
        } else if (expr instanceof SuperExpr) {
            vertices.add(genSuperExprNode((SuperExpr) expr));
        } else if (expr instanceof ThisExpr) {
            vertices.add(genThisExprNode((ThisExpr) expr));
        } else if (expr instanceof VariableDeclarationExpr) {
            // TODO: factor this out and return single vertex
            VariableDeclarationExpr varDeclExpr = (VariableDeclarationExpr) expr;
            for (VariableDeclarator varDecl : varDeclExpr.getVariables()) {
                if (!varDecl.getInitializer().isPresent()) {
                    continue;
                }
                Vertex exprVertex = genAstNode(ASSIGN_EXPR, varDecl.getNameAsString());
                addNodeProperty(exprVertex, OPERATOR, EQUALS);
                String targetTextLabel = varDecl.getType().asString() + " " + varDecl.getNameAsString();
                Vertex target = genAstNode(LOCAL_VAR, targetTextLabel);
                addNodeProperty(target, JAVA_TYPE, varDecl.getType().asString());
                addNodeProperty(target, NAME, varDecl.getNameAsString());
                genAstEdge(exprVertex, target, TARGET, TARGET);
                Vertex value = genExprNode(varDecl.getInitializer().get()).get(0);
                genAstEdge(exprVertex, value, VALUE, VALUE);
                vertices.add(exprVertex);
            }
        } else {
            log.warn("Unhandled expression type '{}', no AST node generated", expr.getClass());
        }

        return vertices;
    }

    // ********************************************************************************************
    // expressions
    // ********************************************************************************************

    private static Vertex genAnnotationExprNode(AnnotationExpr expr) {
        return null;    // TODO: annotations switch in config
    }

    private static Vertex genArrayAccessExprNode(ArrayAccessExpr expr) {
        Vertex exprVertex = genAstNode(ARRAY_ACCESS_EXPR, expr.toString());
        Vertex baseVertex = genExprNode(expr.getName()).get(0);
        Vertex idxVertex = genExprNode(expr.getIndex()).get(0);
        genAstEdge(exprVertex, baseVertex, BASE, BASE);
        genAstEdge(exprVertex, idxVertex, INDEX, INDEX);
        return exprVertex;
    }

    private static Vertex genArrayCreationExprNode(ArrayCreationExpr expr) {
        return null;    // TODO
    }

    private static Vertex genArrayInitializerExprNode(ArrayInitializerExpr expr) {
        return null;    // TODO
    }

    private static Vertex genAssignExprNode(AssignExpr expr) {
        Vertex exprVertex = genAstNode(ASSIGN_EXPR, expr.toString());
        // addNodeProperty(exprVertex, OPERATOR, getAssignOp(expr.getOperator()));
        Vertex target = genExprNode(expr.getTarget()).get(0);
        Vertex value = genExprNode(expr.getValue()).get(0);
        genAstEdge(exprVertex, target, TARGET, TARGET);
        genAstEdge(exprVertex, value, VALUE, VALUE);
        return exprVertex;
    }

    private static Vertex genBinaryExprNode(BinaryExpr expr) {
        Vertex exprVertex = genAstNode(BINARY_EXPR, expr.toString());
        addNodeProperty(exprVertex, OPERATOR, getBinaryOp(expr.getOperator()));
        Vertex lop = genExprNode(expr.getLeft()).get(0);
        Vertex rop = genExprNode(expr.getRight()).get(0);
        genAstEdge(exprVertex, lop, LEFT_OPERAND, LEFT_OPERAND);
        genAstEdge(exprVertex, rop, RIGHT_OPERAND, RIGHT_OPERAND);
        return exprVertex;
    }

    private static Vertex genCastExprNode(CastExpr expr) {
        return null;    // TODO
    }

    private static Vertex genClassExprNode(ClassExpr expr) {
        return null;    // TODO
    }

    private static Vertex genConditionalExprNode(ConditionalExpr expr) {
        return null;    // TODO: transform to if/else
    }

    private static Vertex genEnclosedExprNode(EnclosedExpr expr) {
        return genExprNode(expr.getInner()).get(0);
    }

    private static Vertex genFieldAccessExprNode(FieldAccessExpr expr) {
        return null;    // TODO
    }

    private static Vertex genInstanceOfExprNode(InstanceOfExpr expr) {
        Vertex exprVertex = genAstNode(INSTANCEOF_EXPR, expr.toString());
        addNodeProperty(exprVertex, CHECK_TYPE, expr.getTypeAsString());
        Vertex opVertex = genExprNode(expr.getExpression()).get(0);
        genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);
        return exprVertex;
    }

    private static Vertex genLambdaExprNode(LambdaExpr expr) {
        return null;    // TODO
    }

    private static Vertex genLiteralExprNode(LiteralExpr expr) {
        Vertex exprVertex = genAstNode(LITERAL, expr.toString());
        String type;
        Object value;
        if (expr.isBooleanLiteralExpr()) {
            type = BOOLEAN;
            value = ((BooleanLiteralExpr) expr).getValue();
        } else if (expr.isCharLiteralExpr()) {
            type = CHAR;
            value = ((CharLiteralExpr) expr).getValue();
        } else if (expr.isDoubleLiteralExpr()) {
            type = DOUBLE;
            value = ((DoubleLiteralExpr) expr).getValue();
        } else if (expr.isIntegerLiteralExpr()) {
            type = INT;
            value = Integer.parseInt(((IntegerLiteralExpr) expr).getValue());
        } else if (expr.isLongLiteralExpr()) {
            type = LONG;
            value = ((LongLiteralExpr) expr).getValue();
        } else if (expr.isNullLiteralExpr()) {
            type = NULL;
            value = NULL;
        } else if (expr.isStringLiteralExpr()) {
            type = STRING;
            value = ((StringLiteralExpr) expr).getValue();
        } else {
            log.warn("Unrecognised literal expression '{}'", expr.toString());
            type = UNKNOWN;
            value = NONE;
        }
        addNodeProperty(exprVertex, JAVA_TYPE, type);
        addNodeProperty(exprVertex, VALUE, value);

        return exprVertex;
    }

    private static Vertex genMethodCallExprNode(MethodCallExpr expr) {
        Vertex exprVertex = genAstNode(CALL_EXPR, expr.toString());
        // TODO: scope
        addNodeProperty(exprVertex, CALLS, expr.getNameAsString());
        int i = 0;
        for (Expression arg : expr.getArguments()) {
            Vertex argVertex = genExprNode(arg).get(0);
            Edge argEdge = genAstEdge(exprVertex, argVertex, ARG, ARG);
            addEdgeProperty(argEdge, INDEX, i++);
        }
        return exprVertex;
    }

    private static Vertex genNameExprNode(NameExpr expr) {
        Vertex exprVertex = genAstNode(LOCAL_VAR, expr.getNameAsString());
        // TODO: resolve type, value if known
        addNodeProperty(exprVertex, NAME, expr.getNameAsString());
        return exprVertex;
    }

    private static Vertex genObjectCreationExprNode(ObjectCreationExpr expr) {
        Vertex exprVertex = genAstNode(NEW_EXPR, expr.toString());
        // TODO: scope
        addNodeProperty(exprVertex, JAVA_TYPE, expr.getTypeAsString());
        int i = 0;
        for (Expression arg : expr.getArguments()) {
            Vertex argVertex = genExprNode(arg).get(0);
            Edge argEdge = genAstEdge(exprVertex, argVertex, ARG, ARG);
            addEdgeProperty(argEdge, INDEX, "" + i++);
        }
        return exprVertex;
    }

    private static Vertex genSuperExprNode(SuperExpr expr) {
        Vertex exprVertex = genAstNode(SUPER_EXPR, expr.toString());
        expr.getTypeName().ifPresent(type -> addNodeProperty(exprVertex, JAVA_TYPE, type.asString()));
        return exprVertex;
    }

    private static Vertex genThisExprNode(ThisExpr expr) {
        Vertex exprVertex = genAstNode(THIS_EXPR, expr.toString());
        expr.getTypeName().ifPresent(type -> addNodeProperty(exprVertex, JAVA_TYPE, type.asString()));
        return exprVertex;
    }

    private static Vertex genUnaryExprNode(UnaryExpr expr) {
        Vertex exprVertex = genAstNode(UNARY_EXPR, expr.toString());
        Vertex opVertex = genExprNode(expr.getExpression()).get(0);
        genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);
        return exprVertex;
    }

    // ********************************************************************************************
    // utility methods
    // TODO: these should be private (find outside uses and refactor)
    // ********************************************************************************************

    static Vertex genAstNode(String nodeType, String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addV(AST_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

    static Edge genAstEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addE(AST_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

}
