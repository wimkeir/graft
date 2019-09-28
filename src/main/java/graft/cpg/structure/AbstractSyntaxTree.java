package graft.cpg.structure;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import graft.GraftException;

/**
 * TODO: detailed documentation
 */
public class AbstractSyntaxTree extends BasePropertyGraph {

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a new AST from the given Gremlin graph.
     *
     * @param g the graph containing the AST
     * @throws GraftException if the graph does not contain an AST
     */
    AbstractSyntaxTree(Graph g) {
        super(g);
        if (!isAst(g)) {
            throw new GraftException("Graph is not an AST");
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Check if the given Gremlin graph contains an AST.
     *
     * @param g the graph to check
     * @return true if the graph contains an AST, else false.
     */
    public static boolean isAst(Graph g) {
        // TODO: validate schema
        return true;
    }

    /**
     * Load an AST from the given file.
     *
     * @param filename the file to load the AST from
     * @return the AST loaded from the file
     * @throws GraftException if the file does not contain an AST
     */
    public static AbstractSyntaxTree fromFile(String filename) {
        return BasePropertyGraph.fromFile(filename).asAst();
    }

    /**
     * Load an AST from the graph database specified in the given configuration.
     *
     * @param config the configuration of the graph database to load
     * @return the AST loaded from the graph
     * @throws GraftException if the graph database does not contain an AST
     */
    public static AbstractSyntaxTree fromConfig(Configuration config) {
        return BasePropertyGraph.fromConfig(config).asAst();
    }

}
