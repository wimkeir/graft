package graft.db;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class GraphUtil {

    private static Graph graph;
    private static boolean graphReady = false;

    public static void initGraph() {
        graph = TinkerGraph.open();
        graphReady = true;
    }

    public static Graph graph() {
        if (graphReady) {
            return graph;
        } else {
            initGraph();
            return graph;
        }
    }
    
}
