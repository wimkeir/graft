package graft;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

import java.util.HashMap;
import java.util.Map;

public class Const {

    private static Map<AssignExpr.Operator, String> assignOps;
    private static Map<BinaryExpr.Operator, String> binaryOps;
    private static Map<UnaryExpr.Operator, String> unaryOps;

    // node labels
    public static final String CFG_NODE = "cfg-node";
    public static final String AST_NODE = "ast-node";

    // edge labels
    public static final String AST_EDGE = "ast-edge";
    public static final String CFG_EDGE = "cfg-edge";

    // property keys common to all nodes
    public static final String NODE_TYPE = "type";
    public static final String FILE_PATH = "file-path";
    public static final String FILE_NAME = "file-name";
    public static final String PACKAGE_NAME = "package-name";
    public static final String CLASS_NAME = "class-name";
    public static final String METHOD_NAME = "method-name";
    public static final String LINE_NO = "line-no";
    public static final String COL_NO = "col-no";
    public static final String TEXT_LABEL = "text-label";

    // property keys common to all edges
    public static final String EDGE_TYPE = "type";

    // CFG node types
    public static final String ENTRY = "entry";
    public static final String EXPR_STMT = "expr-stmt";
    public static final String IF_STMT = "if-stmt";
    public static final String PHI = "phi";
    public static final String WHILE_STMT = "while-stmt";
    public static final String RETURN_STMT = "return-stmt";

    // CFG edge types
    public static final String EMPTY = "E";
    public static final String BRANCH = "branch";

    // AST node types
    public static final String ASSIGN_EXPR = "var-assign";
    public static final String BINARY_EXPR = "binary-expr";
    public static final String UNARY_EXPR = "unary-expr";
    public static final String PARAM = "param";
    public static final String LOCAL_VAR = "local-var";
    public static final String CALL_EXPR = "call-expr";
    public static final String LITERAL = "literal";

    // AST edge types
    public static final String EXPR = "expr";
    public static final String ARG = "arg";
    public static final String PRED = "pred";
    public static final String TARGET = "target";
    public static final String VALUE = "value";
    public static final String LEFT_OPERAND = "left-op";
    public static final String RIGHT_OPERAND = "right-op";
    public static final String OPERAND = "operand";
    public static final String GUARD = "guard";
    public static final String RETURNS = "returns";

    // AST node property keys
    public static final String JAVA_TYPE = "java-type";
    public static final String NAME = "name";
    public static final String CALLS = "calls";
    public static final String OPERATOR = "operator";

    // binary operators
    // TODO: rename some of these
    public static final String AND = "and";
    public static final String BINARY_AND = "binary-and";
    public static final String BINARY_OR = "binary-or";
    public static final String DIVIDE = "div";
    public static final String EQUALS = "eq";
    public static final String GREATER = "gt";
    public static final String GREATER_EQUALS = "ge";
    public static final String LEFT_SHIFT = "left-shift";
    public static final String LESS = "lt";
    public static final String LESS_EQUALS = "le";
    public static final String MINUS = "minus";
    public static final String MULTIPLY = "multiply";
    public static final String NOT_EQUALS = "ne";
    public static final String OR = "or";
    public static final String PLUS = "plus";
    public static final String REMAINDER = "rem";
    public static final String SIGNED_RIGHT_SHIFT = "signed-right-shift";
    public static final String UNSIGNED_RIGHT_SHIFT = "unsigned-right-shift";
    public static final String XOR = "xor";

    // AST edge property keys
    public static final String INDEX = "index";

    // miscellaneous constants
    public static final String NONE = "<none>";
    public static final String UNKNOWN = "<unknown>";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // primitive types
    public static final String BOOLEAN = "boolean";
    public static final String CHAR = "char";
    public static final String DOUBLE = "double";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String VOID = "void";

    public enum Direction {
        LEFT,
        RIGHT;
    }

    public static String getAssignOp(AssignExpr.Operator op) {
        if (assignOps == null) {
            initAssignOps();
        }
        return assignOps.get(op);
    }

    public static String getBinaryOp(BinaryExpr.Operator op) {
        if (binaryOps == null) {
            initBinaryOps();
        }
        return binaryOps.get(op);
    }

    private static void initAssignOps() {
        assignOps = new HashMap<>();
        // TODO
    }

    private static void initBinaryOps() {
        binaryOps = new HashMap<>();
        binaryOps.put(BinaryExpr.Operator.AND, AND);
        binaryOps.put(BinaryExpr.Operator.BINARY_AND, BINARY_AND);
        binaryOps.put(BinaryExpr.Operator.BINARY_OR, BINARY_OR);
        binaryOps.put(BinaryExpr.Operator.DIVIDE, DIVIDE);
        binaryOps.put(BinaryExpr.Operator.EQUALS, EQUALS);
        binaryOps.put(BinaryExpr.Operator.GREATER, GREATER);
        binaryOps.put(BinaryExpr.Operator.GREATER_EQUALS, GREATER_EQUALS);
        binaryOps.put(BinaryExpr.Operator.LEFT_SHIFT, LEFT_SHIFT);
        binaryOps.put(BinaryExpr.Operator.LESS, LESS);
        binaryOps.put(BinaryExpr.Operator.LESS_EQUALS, LESS_EQUALS);
        binaryOps.put(BinaryExpr.Operator.MINUS, MINUS);
        binaryOps.put(BinaryExpr.Operator.MULTIPLY, MULTIPLY);
        binaryOps.put(BinaryExpr.Operator.NOT_EQUALS, NOT_EQUALS);
        binaryOps.put(BinaryExpr.Operator.OR, OR);
        binaryOps.put(BinaryExpr.Operator.PLUS, PLUS);
        binaryOps.put(BinaryExpr.Operator.REMAINDER, REMAINDER);
        binaryOps.put(BinaryExpr.Operator.SIGNED_RIGHT_SHIFT, SIGNED_RIGHT_SHIFT);
        binaryOps.put(BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT);
        binaryOps.put(BinaryExpr.Operator.XOR, XOR);
    }

}
