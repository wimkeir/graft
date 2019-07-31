package graft.cpg;

import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import soot.Type;

import graft.cpg.visitors.TypeVisitor;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Utility methods for CPG construction.
 *
 * @author Wim Keirsgieter
 */
public class CpgUtil {

    /**
     * Adds a string property to the given node.
     *
     * @param node the node to add the property to
     * @param key the property key
     * @param value the property value
     */
    public static void addNodeProperty(Vertex node, String key, Object value) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        g.V(node).property(key, value).iterate();
    }

    /**
     * Adds a string property to the given edge.
     *
     * @param edge the edge to add the property to
     * @param key the property key
     * @param value the property value
     */
    public static void addEdgeProperty(Edge edge, String key, Object value) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        g.E(edge).property(key, value).iterate();
    }

    /**
     * Get the string name of a Soot type using the type visitor.
     *
     * @param type the type to get the name of
     * @return the string name of the given type
     */
    public static String getTypeString(Type type) {
        TypeVisitor visitor = new TypeVisitor();
        type.apply(visitor);
        return visitor.getResult().toString();
    }

    /**
     * Get the CFG root of an AST node (returns the given vertex if it is already a CFG node).
     *
     * @param vertex the node to find the CFG root of
     * @return the CFG root of the node, or the node itself if it is already a CFG node
     */
    public static Vertex getCfgRoot(Vertex vertex) {
        if (vertex.label().equals(CFG_NODE))  {
            return vertex;
        }

        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return g.V(vertex)
                .repeat(in(AST_EDGE))
                .until(hasLabel(CFG_NODE))
                .next();
    }

    /**
     * Returns all invoke expression nodes in the AST subtree of the current node.
     *
     * @param vertex the root of the AST subtree to search for invoke expressions
     * @return a list of all invoke expressions found in the subtree
     */
    public static List<Vertex> getInvokeExprs(Vertex vertex) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> invokeExprs = new ArrayList<>();

        g.V(vertex).repeat(
                sideEffect(x -> {
                    Vertex v = (Vertex) x.get();
                    System.out.println(CpgUtil.debugVertex(v));
                    if (v.value(NODE_TYPE).equals(INVOKE_EXPR)) {
                        invokeExprs.add(v);
                    }
                }).out(AST_EDGE)
        ).iterate();

        // XXX
        System.out.println("Invoke exprs in " + debugVertex(vertex) + ":");
        for (Vertex expr : invokeExprs) {
            System.out.println(debugVertex(expr));
        }

        return invokeExprs;
    }

    /**
     * Returns all invoke expression nodes in the AST subtree of the current node that match the given
     * name and scope regex patterns.
     *
     * @param vertex the root of the AST subtree to search for invoke expressions
     * @param namePattern the regex to match the method name against
     * @param scopePattern the regex to match the method scope against
     */
    public static List<Vertex> getInvokeExprs(Vertex vertex, String namePattern, String scopePattern) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> invokeExprs = new ArrayList<>();

        g.V(vertex).repeat(
                sideEffect(x -> {
                    Vertex v = (Vertex) x.get();
                    if (v.value(NODE_TYPE).equals(INVOKE_EXPR) &&
                        v.value(METHOD_NAME).toString().matches(namePattern) &&
                        v.value(METHOD_SCOPE).toString().matches(scopePattern)) {
                        invokeExprs.add(v);
                    }
                }).out(AST_EDGE)
        ).iterate();

        return invokeExprs;
    }

    /**
     * Get a string representation of a vertex and its properties for debugging.
     *
     * @param vertex the vertex to get a string representation of
     * @return a string representation of the given vertex
     */
    public static String debugVertex(Vertex vertex) {
        return debugElement(vertex, NODE_TYPE);
    }

    /**
     * Get a string representation of an edge and its properties for debugging.
     *
     * @param edge the edge to get a string representation of
     * @return a string representation of the given edge
     */
    public static String debugEdge(Edge edge) {
        return debugElement(edge, EDGE_TYPE);
    }

    private static String debugElement(Element element, String typeKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(element.label()).append(" ");
        sb.append("(").append(element.value(typeKey).toString()).append(")");
        for (String key : element.keys()) {
            sb.append(" ");
            sb.append(key).append("=").append(element.value(key).toString());
        }
        sb.append(">");
        return sb.toString();
    }

}
