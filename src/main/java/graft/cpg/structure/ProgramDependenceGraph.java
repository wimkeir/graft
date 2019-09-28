package graft.cpg.structure;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;

/**
 * TODO: javadoc
 */
public class ProgramDependenceGraph extends BasePropertyGraph {

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a new program dependence graph from the given Gremlin graph.
     *
     * @param g the graph containing the PDG
     * @throws GraftException if the graph does not contain a PDG
     */
    public ProgramDependenceGraph(Graph g) {
        super(g);
        if (!isPdg(g)) {
            throw new GraftException("Graph is not a PDG");
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Check if the given Gremlin graph contains a PDG.
     *
     * @param g the graph to check
     * @return true if the graph contains a PDG, else false.
     */
    public static boolean isPdg(Graph g) {
        // TODO: validate schema
        return true;
    }

    /**
     * Load a PDG from the given file.
     *
     * @param filename the file to load the PDG from
     * @return the PDG loaded from the file
     * @throws GraftException if the file does not contain a PDG
     */
    public static ProgramDependenceGraph fromFile(String filename) {
        return BasePropertyGraph.fromFile(filename).asPdg();
    }

    /**
     * Load a PDG from the graph database specified in the given configuration.
     *
     * @param config the configuration of the graph database to load
     * @return the PDG loaded from the graph
     * @throws GraftException if the graph database does not contain a PDG
     */
    public static ProgramDependenceGraph fromConfig(Configuration config) {
        return BasePropertyGraph.fromConfig(config).asPdg();
    }

}
