package graft.db;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftException;
import graft.Options;
import graft.cpg.structure.CodePropertyGraph;

import static graft.Const.*;

/**
 * TODO: javadocs
 */
public abstract class GraphUtil {

    private static Logger log = LoggerFactory.getLogger(GraphUtil.class);

    private static GraphUtil v;

    /**
     * Initialize the CPG.
     */
    public static CodePropertyGraph getCpg() {
        log.debug("Initializing graph...");
        switch (Options.v().getString(OPT_DB_IMPLEMENTATION)) {
            case TINKERGRAPH:
                return CodePropertyGraph.fromGraph(tinkerGraph());
            case NEO4J:
                return CodePropertyGraph.fromGraph(neo4jGraph());
            default:
                throw new GraftException("Unknown graph implementation '"
                                         + Options.v().getString(OPT_DB_IMPLEMENTATION)
                                         + "'");
        }
    }

    private static Graph tinkerGraph() {
        log.debug("Initialising TinkerGraph implementation");
        if (Options.v().containsKey(OPT_DB_LOAD_FROM)) {
            String filename = Options.v().getString(OPT_DB_LOAD_FROM);
            log.debug("Loading TinkerGraph from file '{}'", filename);
            return TinkerGraphUtil.fromFile(filename);
        }
        log.debug("Initialising new TinkerGraph instance");
        return TinkerGraph.open();
    }

    private static Graph neo4jGraph() {
        log.debug("Initializing Neo4j implementation");
        Configuration neo4jConfig = new BaseConfiguration();
        neo4jConfig.setProperty("gremlin.neo4j.directory", Options.v().getString(OPT_DB_DIRECTORY));
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        if (Options.v().containsKey(OPT_DB_LOAD_FROM)) {
            return Neo4jUtil.fromFile(Options.v().getString(OPT_DB_DIRECTORY), Options.v().getString(OPT_DB_LOAD_FROM));
        }
        return Neo4jUtil.fromConfig(neo4jConfig);
    }
    
}
