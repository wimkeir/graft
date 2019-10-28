package graft.cpg.structure;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;
import graft.db.Neo4jUtil;
import graft.db.TinkerGraphUtil;
import graft.traversal.CpgTraversalSource;
import graft.utils.DotUtil;

/**
 * An implementation of the code property graph.
 *
 * TODO: very detailed documentation
 */
public class CodePropertyGraph {

    // TODO: get name from root node or args?
    private String name;
    private Graph g;

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a new code property graph from the given Gremlin graph.
     *
     * @param g the graph to instantiate the CPG from
     * @throws GraftException if the graph does not contain a CPG
     */
    private CodePropertyGraph(Graph g) {
        assert g != null;
        this.g = g;
        assert isCpg();
    }

    // ********************************************************************************************
    // public instance methods
    // ********************************************************************************************

    public boolean isCpg() {
        // TODO: validate schema
        return true;
    }

    public CpgTraversalSource traversal() {
        return g.traversal(CpgTraversalSource.class);
    }

    public void toDot(String filename) {
        // TODO: graph name
        DotUtil.graphToDot(this, filename, "graphName");
    }

    public void dump(String filename) {
        // TODO: robustify
        g.traversal().io(filename).write().iterate();
    }

    public void commit() {
        if (g.features().graph().supportsTransactions()) {
            g.tx().commit();
        }
    }

    public void close() {
        if (g instanceof Neo4jGraph) {
            try {
                g.close();
            } catch (Exception e) {
                throw new GraftException("Unable to close Neo4j graph", e);
            }
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Load a CPG from the given file.
     *
     * @param filename the file to load the CPG from
     * @return the CPG loaded from the file
     * @throws GraftException if the file does not contain a CPG
     */
    public static CodePropertyGraph fromFile(String filename) {
        Graph g = TinkerGraphUtil.fromFile(filename);
        return new CodePropertyGraph(g);
    }

    /**
     * Load a CPG from the graph database specified in the given configuration.
     *
     * @param config the configuration of the graph database to load
     * @return the CPG loaded from the graph
     * @throws GraftException if the graph database does not contain a CPG
     */
    public static CodePropertyGraph fromConfig(Configuration config) {
        Graph g = TinkerGraphUtil.fromConfig(config);
        return new CodePropertyGraph(g);
    }

    public static CodePropertyGraph fromGraph(Graph g) {
        return new CodePropertyGraph(g);
    }

    public static CodePropertyGraph fromDir(String dirName) {
        Configuration neo4jConfig = new BaseConfiguration();
        neo4jConfig.setProperty("gremlin.neo4j.directory", dirName);
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        return new CodePropertyGraph(Neo4jUtil.fromConfig(neo4jConfig));
    }

}
