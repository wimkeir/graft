package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.traversal.CpgTraversalSource;

import static graft.db.GraphUtil.graph;

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
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
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
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        g.E(edge).property(key, value).iterate();
    }

}
