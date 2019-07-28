package graft;

/**
 * Various constants used throughout the program.
 *
 * @author Wim Keirsgieter
 */
public class Const {

    // log levels
    public static final String TRACE = "trace";
    public static final String DEBUG = "debug";
    public static final String INFO = "info";
    public static final String WARN = "warn";
    public static final String ERROR = "error";
    public static final String ALL = "all";

    // invoke types
    public static final String INTERFACE = "interface";
    public static final String SPECIAL = "special";
    public static final String DYNAMIC = "dynamic";
    public static final String STATIC = "static";
    public static final String VIRTUAL = "virtual";

    // node labels
    public static final String CFG_NODE = "cfg-node";
    public static final String AST_NODE = "ast-node";

    // edge labels
    public static final String AST_EDGE = "ast-edge";
    public static final String CFG_EDGE = "cfg-edge";

    // property keys common to all nodes
    public static final String NODE_TYPE = "type";
    public static final String TEXT_LABEL = "text-label";

    // property keys common to all edges
    public static final String EDGE_TYPE = "type";

    // CFG node types
    public static final String ENTRY = "entry";
    public static final String ASSIGN_STMT = "assign-stmt";
    public static final String INVOKE_STMT = "invoke-stmt";
    public static final String PHI = "phi";
    public static final String RETURN_STMT = "return-stmt";
    public static final String THROW_STMT = "throw-stmt";
    public static final String CONDITIONAL_STMT = "conditional-stmt";

    // CFG edge types
    public static final String EMPTY = "E";
    public static final String BRANCH = "branch";

    // CFG node property keys
    public static final String FILE_PATH = "file-path";
    public static final String FILE_NAME = "file-name";
    public static final String PACKAGE_NAME = "package-name";
    public static final String CLASS_NAME = "class-name";
    public static final String METHOD_NAME = "method-name";
    public static final String LINE_NO = "line-no";
    public static final String COL_NO = "col-no";

    // AST node types
    public static final String CLASS = "class";
    public static final String BINARY_EXPR = "binary-expr";
    public static final String UNARY_EXPR = "unary-expr";
    public static final String THIS_EXPR = "this-expr";
    public static final String LOCAL_VAR = "local-var";
    public static final String SUPER_EXPR = "super-expr";
    public static final String INSTANCEOF_EXPR = "instanceof-expr";
    public static final String INVOKE_EXPR = "invoke-expr";
    public static final String LITERAL = "literal";
    public static final String ARRAY_ACCESS_EXPR = "array-access-expr";
    public static final String NEW_EXPR = "new-expr";

    // AST edge types
    public static final String EXPR = "expr";
    public static final String ARG = "arg";
    public static final String BASE = "base";
    public static final String TARGET = "target";
    public static final String VALUE = "value";
    public static final String LEFT_OPERAND = "left-op";
    public static final String RIGHT_OPERAND = "right-op";
    public static final String OPERAND = "operand";
    public static final String PARAM = "param";
    public static final String RETURNS = "returns";
    public static final String THROWS = "throws";
    public static final String METHOD = "method";
    public static final String CONSTRUCTOR = "constructor";

    // AST node property keys
    public static final String JAVA_TYPE = "java-type";
    public static final String NAME = "name";
    public static final String INVOKES = "invokes";
    public static final String OPERATOR = "operator";
    public static final String CHECK_TYPE = "check-type";
    public static final String SHORT_NAME = "short-name";
    public static final String FULL_NAME = "full-name";
    public static final String CAST_TYPE = "cast-type";
    public static final String INVOKE_TYPE = "invoke-type";

    // AST edge property keys
    public static final String INDEX = "index";

    // primitive types and literals
    public static final String BOOLEAN = "boolean";
    public static final String BYTE = "byte";
    public static final String CHAR = "char";
    public static final String DOUBLE = "double";
    public static final String FLOAT = "float";
    public static final String INT = "int";
    public static final String LONG = "long";
    public static final String STRING = "java.lang.String";
    public static final String VOID = "void";
    public static final String NULL = "null";

    // reference types
    public static final String ARRAY_REF = "array-ref";
    public static final String PARAM_REF = "param-ref";
    public static final String THIS_REF = "this-ref";

    // binary operators
    public static final String AND = "and";
    public static final String DIVIDE = "div";
    public static final String CMP = "cmpr";
    public static final String CMPL = "cmpl";
    public static final String CMPG = "cmpg";
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

    // unary operators
    public static final String CAST = "cast";
    public static final String NEGATION = "neg";
    public static final String LENGTH = "length";

    // miscellaneous constants
    public static final String NONE = "<none>";
    public static final String UNKNOWN = "<unknown>";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

}
