package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.Ref;

import graft.cpg.visitors.ConstantVisitor;
import graft.cpg.visitors.ExprVisitor;
import graft.cpg.visitors.RefVisitor;
import graft.db.GraphUtil;
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
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        Vertex node = g.addV(AST_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .next();
        // GraphUtil.graph().tx().commit();
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
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        Edge edge = g.addE(AST_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
        // GraphUtil.graph().tx().commit();
        return edge;
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generates an AST node for a local variable
    private static Vertex genLocalNode(Local local) {
        Vertex localNode = genAstNode(LOCAL_VAR, local.getName());
        CpgUtil.addNodeProperty(localNode, NAME, local.getName());
        CpgUtil.addNodeProperty(localNode, JAVA_TYPE, CpgUtil.getTypeString(local.getType()));
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
