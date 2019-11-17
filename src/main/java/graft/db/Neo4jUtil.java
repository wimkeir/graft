package graft.db;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;

/**
 * Utility methods for interacting with Neo4j graph databases.
 *
 * @author Wim Keirsgieter
 */
public class Neo4jUtil {

    /**
     * Initialize a Neo4j graph instance in the given directory.
     *
     * @param dir the Neo4j directory
     * @return the Neo4j graph instance
     */
    public static Neo4jGraph fromDir(String dir) {
        Configuration config = new BaseConfiguration();
        config.setProperty("gremlin.neo4j.directory", dir);
        config.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        config.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        return fromConfig(config);
    }

    /**
     * Initialize a Neo4j graph instance from the given config.
     *
     * @param config the Neo4j config
     * @return the Neo4j graph instance
     */
    public static Neo4jGraph fromConfig(Configuration config) {
        Neo4jGraph g = Neo4jGraph.open(config);
        return g;
    }

}
