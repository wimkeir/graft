package graft.cpg.structure;

import graft.utils.DotUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;
import graft.db.TinkerGraphUtil;
import graft.traversal.CpgTraversalSource;

/**
 * A base class for property graph implementations.
 *
 * TODO: more detailed javadoc
 */
public class BasePropertyGraph implements GGraph {

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

    @Override
    public CpgTraversalSource traversal() {
        return g.traversal(CpgTraversalSource.class);
    }

    @Override
    public CodePropertyGraph asCpg() {
        if (CodePropertyGraph.isCpg(g)) {
            return new CodePropertyGraph(g);
        }
        throw new GraftException("Graph is not a CPG");
    }

    @Override
    public void toDot(String filename) {
        // TODO: graph name
        DotUtil.graphToDot(g, filename, "graphName");
    }

    @Override
    public void dump(String filename) {
        // TODO
    }

    @Override
    public void commit() {
        // TODO
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
    public static GGraph fromFile(String filename) {
        Graph g = TinkerGraphUtil.fromFile(filename);
        return new BasePropertyGraph(g);
    }

    /**
     * Load a basic property graph from the graph database specified in the given config.
     *
     * @param config the config of the graph database
     * @return the graph loaded from the database
     */
    public static GGraph fromConfig(Configuration config) {
        Graph g = TinkerGraphUtil.fromConfig(config);
        return new BasePropertyGraph(g);
    }

}
