package graft.cpg;

import java.util.Optional;

import com.github.javaparser.Position;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.db.GraphUtil.graph;

/**
 * Utility methods for CPG construction.
 */
class CpgUtil {

    static int lineNr(Optional<Position> pos) {
        if (pos.isPresent()) {
            return pos.get().line;
        }
        return -1;
    }

    static int colNr(Optional<Position> pos) {
        if (pos.isPresent()) {
            return pos.get().column;
        }
        return -1;
    }

    /**
     * Adds a string property to the given node.
     *
     * @param node the node to add the property to
     * @param key the property key
     * @param value the property value
     */
    static void addNodeProperty(Vertex node, String key, String value) {
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
    static void addEdgeProperty(Edge edge, String key, String value) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        g.E(edge).property(key, value).iterate();
    }

    static Vertex getNextCfgNode(Vertex node) {
        assert node.label().equals(CFG_NODE);
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.V(node).out(CFG_EDGE).next();
    }

}
