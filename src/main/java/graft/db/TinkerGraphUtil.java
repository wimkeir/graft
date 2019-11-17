package graft.db;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * Utility methods for interacting with Tinkergraph databases.
 *
 * @author Wim Keirsgieter
 */
public class TinkerGraphUtil {

    /**
     * Initialize a new Tinkergraph instance from the given graph file.
     *
     * @param filename the name of the graph file to load
     * @return the new Tinkergraph instance
     */
    public static TinkerGraph fromFile(String filename) {
        TinkerGraph g = TinkerGraph.open();
        g.traversal().io(filename).read().iterate();
        return g;
    }

}
