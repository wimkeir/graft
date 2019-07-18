package graft;

public class Const {

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

    // CFG edge types
    public static final String EMPTY = "E";

    // AST node types
    public static final String PARAM = "param";

    // AST node property keys
    public static final String JAVA_TYPE = "java-type";
    public static final String NAME = "name";

    // AST edge property keys
    public static final String INDEX = "index";

    // miscellaneous constants
    public static final String NONE = "<none>";
    public static final String UNKNOWN = "<unknown>";

}
