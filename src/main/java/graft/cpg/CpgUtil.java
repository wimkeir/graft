package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import soot.Type;

import graft.cpg.visitors.TypeVisitor;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

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

}
