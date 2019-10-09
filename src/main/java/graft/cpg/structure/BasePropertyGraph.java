package graft.cpg.structure;

import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;
import graft.db.TinkerGraphUtil;
import graft.traversal.CpgTraversalSource;
import graft.utils.DotUtil;

/**
 * A base class for property graph implementations.
 *
 * TODO: more detailed javadoc
 */
public class BasePropertyGraph {

    private Graph g;

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    BasePropertyGraph(Graph g) {
        assert g != null;
        this.g = g;
    }

    // ********************************************************************************************
    // implemented GGraph methods
    // ********************************************************************************************

    public CpgTraversalSource traversal() {
        return g.traversal(CpgTraversalSource.class);
    }

    public CodePropertyGraph asCpg() {
        if (this instanceof CodePropertyGraph) {
            return (CodePropertyGraph) this;
        }
        if (CodePropertyGraph.isCpg(g)) {
            return new CodePropertyGraph(g);
        }
        throw new GraftException("Graph is not a CPG");
    }

    public void toDot(String filename) {
        // TODO: graph name
        DotUtil.graphToDot(this, filename, "graphName");
    }

    public void dump(String filename) {
        // TODO
    }

    public void commit() {
        if (g instanceof Neo4jGraph) {
            g.tx().commit();
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    // TODO: use configurable graph backend

    /**
     * Load a basic property graph from the given file.
     *
     * @param filename the file to load the graph from
     * @return the graph loaded from the file
     */
    public static BasePropertyGraph fromFile(String filename) {
        Graph g = TinkerGraphUtil.fromFile(filename);
        return new BasePropertyGraph(g);
    }

    /**
     * Load a basic property graph from the graph database specified in the given config.
     *
     * @param config the config of the graph database
     * @return the graph loaded from the database
     */
    public static BasePropertyGraph fromConfig(Configuration config) {
        Graph g = TinkerGraphUtil.fromConfig(config);
        return new BasePropertyGraph(g);
    }

}
