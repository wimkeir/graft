package graft.db;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftRuntimeException;
import graft.Options;
import graft.cpg.structure.CodePropertyGraph;

import static graft.Const.*;

/**
 * Utility methods for initializing the CPG and interacting with the graph database.
 *
 * @author Wim Keirsgieter
 */
public class GraphUtil {

    private static Logger log = LoggerFactory.getLogger(GraphUtil.class);

    /**
     * Initialize the CPG.
     */
    public static CodePropertyGraph getCpg() {
        log.debug("Initializing graph...");
        switch (Options.v().getString(OPT_DB_IMPLEMENTATION)) {
            case TINKERGRAPH:
                return CodePropertyGraph.fromFile(Options.v().getString(OPT_DB_FILE));
            case NEO4J:
                return CodePropertyGraph.fromDir(Options.v().getString(OPT_DB_DIRECTORY));
            default:
                throw new GraftRuntimeException(
                        "Unknown graph implementation '" +
                        Options.v().getString(OPT_DB_IMPLEMENTATION) + "'");
        }
    }

    /**
     * Initialize a new Tinkergraph CPG.
     *
     * @return a new Tinkergraph CPG.
     */
    public static CodePropertyGraph newTinkergraphCpg() {
        Graph g = TinkerGraph.open();
        return CodePropertyGraph.initCpg(g);
    }

    /**
     * Initialize a new Neo4j CPG.
     *
     * @param dir the Neo4j directory
     * @return a new Neo4j CPG
     */
    public static CodePropertyGraph newNeo4jCpg(String dir) {
        Graph g = Neo4jUtil.fromDir(dir);
        return CodePropertyGraph.initCpg(g);
    }
    
}
