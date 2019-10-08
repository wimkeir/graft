package graft.db;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;

import static graft.Const.*;

/**
 *
 */
public class Neo4jUtil {

    public static Neo4jGraph fromFile(String filename) {
        return fromFile(DEFAULT_DB_DIRECTORY, filename);
    }

    public static Neo4jGraph fromFile(String neo4jDir, String filename) {
        Neo4jGraph g = fromDir(neo4jDir);
        g.traversal().io(filename).read();
        return g;
    }

    public static Neo4jGraph fromDir(String neo4jDir) {
        Neo4jGraph g = Neo4jGraph.open(neo4jDir);
        return g;
    }

    public static Neo4jGraph fromConfig(Configuration config) {
        Neo4jGraph g = Neo4jGraph.open(config);
        return g;
    }

}
