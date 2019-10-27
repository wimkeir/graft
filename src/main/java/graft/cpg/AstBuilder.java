package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.Ref;

import graft.Graft;
import graft.cpg.visitors.ConstantVisitor;
import graft.cpg.visitors.ExprVisitor;
import graft.cpg.visitors.RefVisitor;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

/**
 * Generate AST nodes and subtrees.
 *
 * @author Wim Keirsgieter
 */
public class AstBuilder {

    private static Logger log = LoggerFactory.getLogger(AstBuilder.class);

    // ********************************************************************************************
    // public methods
    // ********************************************************************************************

    /**
     * Generate an AST node (possibly a subtree) for the given Soot value.
     *
     * @param value the Soot value
     * @return the generated AST node
     */
    public static Vertex genValueNode(Value value) {
        if (value instanceof Local) {
            return genLocalNode((Local) value);
        } else if (value instanceof Constant) {
            return genConstantNode((Constant) value);
        } else if (value instanceof Expr) {
            return genExprNode((Expr) value);
        } else if (value instanceof Ref) {
            return genRefNode((Ref) value);
        } else {
            log.warn("Unhandled Value type '{}', no AST node generated", value.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Generate a base AST node with the given node type and text label properties.
     *
     * @param nodeType the type of the AST node
     * @param textLabel the text label of the AST node
     * @return the generated AST node
     */
    public static Vertex genAstNode(String nodeType, String textLabel) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex node = g.addV(AST_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .next();
        // Graft.cpg().commit();
        return node;
    }

    /**
     * Generate a base AST edge between the two given nodes, with the given edge type and text label properties.
     *
     * @param from the out-vertex of the AST edge
     * @param to the in-vertex of the AST edge
     * @param edgeType the type of the AST edge
     * @param textLabel the text label of the AST edge
     * @return the generated AST edge
     */
    public static Edge genAstEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Edge edge = g.addE(AST_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
        // Graft.cpg().commit();
        return edge;
    }

    /**
     * Generate an expression AST node with the given expression type, text label and Java type.
     *
     * @param exprType the expression type
     * @param textLabel the text label
     * @param type the Java type of the expression
     * @return the newly generated node
     */
    public static Vertex genExprNode(String exprType, String textLabel, String type) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex exprNode = g.addV(AST_NODE)
                .property(NODE_TYPE, EXPR)
                .property(EXPR_TYPE, exprType)
                .property(TEXT_LABEL, textLabel)
                .property(JAVA_TYPE, type)
                .next();
        // Graft.cpg().commit();
        return exprNode;
    }

    /**
     * Generate a new expression AST node with the given new expression type, text label and Java type.
     *
     * @param newExprType the new expression type
     * @param textLabel the text label
     * @param type the Java type of the expression
     * @return the newly generated node
     */
    public static Vertex genNewExprNode(String newExprType, String textLabel, String type) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex exprNode = g.addV(AST_NODE)
                .property(NODE_TYPE, EXPR)
                .property(EXPR_TYPE, NEW_EXPR)
                .property(NEW_EXPR_TYPE, newExprType)
                .property(TEXT_LABEL, textLabel)
                .property(JAVA_TYPE, type)
                .next();
        // Graft.cpg().commit();
        return exprNode;
    }

    /**
     * Generate a constant AST node with the given constant type, text label and value.
     *
     * @param type the constant type
     * @param textLabel the text label
     * @param value the value of the constant
     * @return the newly generated node
     */
    public static Vertex genConstantNode(String type, String textLabel, String value) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex constNode = g.addV(AST_NODE)
                .property(NODE_TYPE, CONSTANT)
                .property(JAVA_TYPE, type)
                .property(VALUE, value)
                .property(TEXT_LABEL, textLabel)
                .next();
        // Graft.cpg().commit();
        return constNode;
    }

    /**
     * Generate a reference AST node with the given reference type, text label and Java type.
     *
     * @param refType the reference type
     * @param textLabel the text label
     * @param javaType the Java type of the expression
     * @return the newly generated node
     */
    public static Vertex genRefNode(String refType, String javaType, String textLabel) {
        CpgTraversalSource g = Graft.cpg().traversal();
        Vertex refNode = g.addV(AST_NODE)
                .property(NODE_TYPE, REF)
                .property(REF_TYPE, refType)
                .property(JAVA_TYPE, javaType)
                .property(TEXT_LABEL, textLabel)
                .next();
        // Graft.cpg().commit();
        return refNode;
    }

    /**
     * Generate a field reference AST node from the given field.
     *
     * @param field the field to generate a reference node for
     * @param textLabel the text label
     * @return the newly generated node
     */
    public static Vertex genFieldRefNode(SootField field, String textLabel) {
        CpgTraversalSource g = Graft.cpg().traversal();
        String fieldType = field.isStatic() ? STATIC_FIELD_REF : INSTANCE_FIELD_REF;
        Vertex fieldRefNode = g.addV(AST_NODE)
                .property(NODE_TYPE, REF)
                .property(REF_TYPE, FIELD_REF)
                .property(FIELD_REF_TYPE, fieldType)
                .property(FIELD_NAME, field.getName())
                .property(FIELD_SIG, field.getSignature())
                .property(JAVA_TYPE, CpgUtil.getTypeString(field.getType()))
                .property(TEXT_LABEL, textLabel)
                .next();
        // Graft.cpg().commit();
        return fieldRefNode;

    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generates an AST node for a local variable
    private static Vertex genLocalNode(Local local) {
        Vertex localNode = genAstNode(LOCAL_VAR, local.getName());
        CpgUtil.addNodeProperty(localNode, NAME, local.getName());
        CpgUtil.addNodeProperty(localNode, JAVA_TYPE, CpgUtil.getTypeString(local.getType()));
        if (local.getType() instanceof RefType) {
            CpgUtil.addNodeProperty(localNode, REF_TYPE, true);
        } else {
            CpgUtil.addNodeProperty(localNode, REF_TYPE, false);
        }
        return localNode;
    }

    // Generates an AST node for a constant value
    private static Vertex genConstantNode(Constant constant) {
        ConstantVisitor visitor = new ConstantVisitor();
        constant.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for an expression
    private static Vertex genExprNode(Expr expr) {
        ExprVisitor visitor = new ExprVisitor();
        expr.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for a reference
    private static Vertex genRefNode(Ref ref) {
        RefVisitor visitor = new RefVisitor();
        ref.apply(visitor);
        return (Vertex) visitor.getResult();
    }

}
