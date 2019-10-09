package graft.cpg.structure;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;

/**
 * An implementation of the code property graph.
 *
 * TODO: very detailed documentation
 */
public class CodePropertyGraph extends BasePropertyGraph {

    // TODO: name

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a new code property graph from the given Gremlin graph.
     *
     * @param g the graph to instantiate the CPG from
     * @throws GraftException if the graph does not contain a CPG
     */
    CodePropertyGraph(Graph g) {
        super(g);
        if (!isCpg(g)) {
            throw new GraftException("Graph is not a CPG");
        }
    }

    // ********************************************************************************************
    // public instance methods
    // ********************************************************************************************

    // TODO: use traversals for getX methods

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Check if the given Gremlin graph contains a CPG.
     *
     * @param g the graph to check
     * @return true if the graph contains a CPG, else false.
     */
    public static boolean isCpg(Graph g) {
        // TODO: validate schema
        return true;
    }

    /**
     * Load a CPG from the given file.
     *
     * @param filename the file to load the CPG from
     * @return the CPG loaded from the file
     * @throws GraftException if the file does not contain a CPG
     */
    public static CodePropertyGraph fromFile(String filename) {
        return BasePropertyGraph.fromFile(filename).asCpg();
    }

    /**
     * Load a CPG from the graph database specified in the given configuration.
     *
     * @param config the configuration of the graph database to load
     * @return the CPG loaded from the graph
     * @throws GraftException if the graph database does not contain a CPG
     */
    public static CodePropertyGraph fromConfig(Configuration config) {
        return BasePropertyGraph.fromConfig(config).asCpg();
    }

    public static CodePropertyGraph fromGraph(Graph g) {
        return new CodePropertyGraph(g);
    }

}
