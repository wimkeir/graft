package graft.db;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftRuntimeException;
import graft.Options;
import graft.cpg.structure.CodePropertyGraph;

import java.nio.file.Paths;

import static graft.Const.*;

/**
 * TODO: javadocs
 */
public class GraphUtil {

    // TODO
    // javadocs
    // lots of redundancies here, sort them out

    private static Logger log = LoggerFactory.getLogger(GraphUtil.class);

    private static GraphUtil v;

    /**
     * Initialize the CPG.
     */
    public static CodePropertyGraph getCpg() {
        log.debug("Initializing graph...");
        switch (getDbImplementation()) {
            case TINKERGRAPH:
                return CodePropertyGraph.fromGraph(tinkerGraph());
            case NEO4J:
                return CodePropertyGraph.fromDir(Options.v().getString(OPT_DB_DIRECTORY));
            default:
                throw new GraftRuntimeException("Unknown graph implementation '"
                                         + Options.v().getString(OPT_DB_IMPLEMENTATION)
                                         + "'");
        }
    }

    public static CodePropertyGraph newTinkergraphCpg() {
        return CodePropertyGraph.fromGraph(TinkerGraph.open());
    }

    public static CodePropertyGraph newNeo4jCpg(String dir) {
        if (Paths.get(dir).toFile().listFiles().length != 0) {
            throw new GraftRuntimeException("Folder '" + dir + "' already contains Neo4j database");
        }
        Configuration config = new BaseConfiguration();
        config.setProperty("gremlin.neo4j.directory", dir);
        config.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        config.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        return CodePropertyGraph.fromGraph(Neo4jUtil.fromConfig(config));
    }

    public static String getDbImplementation() {
        return Options.v().getString(OPT_DB_IMPLEMENTATION);
    }

    public static boolean validDbImplementation(String impl) {
        return impl.equals(TINKERGRAPH) || impl.equals(NEO4J);
    }

    public static String dbFileName() {
        String format = Options.v().getString(OPT_DB_FILE_FORMAT);
        switch (format) {
            case "json":
                return Options.v().getString(DB_FILE_NAME) + ".json";
            case "xml":
            case "graphml":
                return Options.v().getString(DB_FILE_NAME) + ".xml";
            case "kryo":
                return Options.v().getString(DB_FILE_NAME) + ".kryo";
            default:
                throw new GraftRuntimeException("Unrecognised DB file format '" + format);
        }
    }

    private static Graph tinkerGraph() {
        log.debug("Initialising TinkerGraph implementation");
        if (Options.v().containsKey(OPT_DB_FILE)) {
            String filename = Options.v().getString(OPT_DB_FILE);
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
        return Neo4jUtil.fromConfig(neo4jConfig);
    }
    
}
