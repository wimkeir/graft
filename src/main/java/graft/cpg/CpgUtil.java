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
 *
 * @author Wim Keirsgieter
 */
class CpgUtil {

    /**
     * Returns the line number of a position if present (else returns -1).
     *
     * @param pos the position
     * @return the line number, or -1 if not present
     */
    static int lineNr(Optional<Position> pos) {
        if (pos.isPresent()) {
            return pos.get().line;
        }
        return -1;
    }

    /**
     * Returns the column number of a position if present (else returns -1).
     *
     * @param pos the position
     * @return the column number, or -1 if not present
     */
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
    static void addNodeProperty(Vertex node, String key, Object value) {
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
    static void addEdgeProperty(Edge edge, String key, Object value) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        g.E(edge).property(key, value).iterate();
    }

    /**
     * Returns the direct successor of the given CFG node in the CPG.
     *
     * @param node the node whose successor to find
     * @return the successor of the given node
     */
    static Vertex getNextCfgNode(Vertex node) {
        assert node.label().equals(CFG_NODE);
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.V(node).out(CFG_EDGE).next();
        // TODO: what if this node doesn't exist?
    }

}
