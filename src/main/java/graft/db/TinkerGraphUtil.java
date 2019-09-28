package graft.db;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class TinkerGraphUtil {

    // TODO: configure IO reader
    public static TinkerGraph fromFile(String filename) {
        TinkerGraph g = TinkerGraph.open();
        g.traversal().io(filename).read().iterate();
        return g;
    }

    public static TinkerGraph fromConfig(Configuration config) {
        TinkerGraph g = TinkerGraph.open(config);
        return g;
    }

}
