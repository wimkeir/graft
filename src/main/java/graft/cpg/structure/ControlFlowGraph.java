package graft.cpg.structure;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;

public class ControlFlowGraph extends BasePropertyGraph {

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a new control flow graph from the given Gremlin graph.
     *
     * @param g the graph containing the CFG
     * @throws GraftException if the graph does not contain a CFG
     */
    public ControlFlowGraph(Graph g) {
        super(g);
        if (!isCfg(g)) {
            throw new GraftException("Graph is not a CFG");
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Check if the given Gremlin graph contains a CFG.
     *
     * @param g the graph to check
     * @return true if the graph contains a CFG, else false.
     */
    public static boolean isCfg(Graph g) {
        // TODO: validate schema
        return true;
    }

    /**
     * Load a CFG from the given file.
     *
     * @param filename the file to load the CFG from
     * @return the CPG loaded from the file
     * @throws GraftException if the file does not contain a CFG
     */
    public static ControlFlowGraph fromFile(String filename) {
        return BasePropertyGraph.fromFile(filename).asCfg();
    }

    /**
     * Load a CFG from the graph database specified in the given configuration.
     *
     * @param config the configuration of the graph database to load
     * @return the CFG loaded from the graph
     * @throws GraftException if the graph database does not contain a CFG
     */
    public static ControlFlowGraph fromConfig(Configuration config) {
        return BasePropertyGraph.fromConfig(config).asCfg();
    }

}
