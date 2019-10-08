package graft.db;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftException;
import graft.Options;

import static graft.Const.*;

/**
 * TODO: javadocs
 */
public class GraphUtil {

    private static Logger log = LoggerFactory.getLogger(GraphUtil.class);
    private static Graph graph;

    /**
     * Initialize the graph database.
     */
    public static void initGraph() {
        log.debug("Initializing graph...");
        switch (Options.v().getString(OPT_DB_IMPLEMENTATION)) {
            case TINKERGRAPH:
                initTinkerGraph();
                break;
            case NEO4J:
                initNeo4j();
                break;

            default:
                throw new GraftException("Unknown graph implementation '"
                                         + Options.v().getString(OPT_DB_IMPLEMENTATION)
                                         + "'");
        }
    }

    /**
     * Get a reference to the graph database.
     *
     * @return reference to the graph database
     * @throws GraftException if the graph has not been initialized
     */
    public static Graph graph() {
        if (graph != null) {
            return graph;
        } else {
            throw new GraftException("Graph not initialized");
        }
    }

    /**
     * Close the graph database, if it is still open.
     */
    public static void closeGraph() {
        log.debug("Closing graph...");
        if (graph != null) {
            try {
                graph.close();
            } catch (Exception e) {
                throw new GraftException("Could not close graph", e);
            }
        } else {
            log.warn("Graph already closed");
        }
    }

    private static void initTinkerGraph() {
        log.debug("Initialising TinkerGraph implementation");
        graph = TinkerGraph.open();
    }

    private static void initNeo4j() {
        log.debug("Initializing Neo4j implementation");
        Configuration neo4jConfig = new BaseConfiguration();
        neo4jConfig.setProperty("gremlin.neo4j.directory", Options.v().getString(OPT_DB_DIRECTORY));
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        graph = Neo4jGraph.open(neo4jConfig);
    }
    
}
