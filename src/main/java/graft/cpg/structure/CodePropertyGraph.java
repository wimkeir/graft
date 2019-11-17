package graft.cpg.structure;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.GraftRuntimeException;
import graft.Options;

import graft.db.Neo4jUtil;
import graft.db.TinkerGraphUtil;

import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;

import graft.utils.DotUtil;

import static graft.Const.*;

/**
 * An implementation of the code property graph structure.
 *
 * @author Wim Keirsgieter
 */
public class CodePropertyGraph {

    private static Logger log = LoggerFactory.getLogger(CodePropertyGraph.class);

    private String name;
    private Graph g;

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    /**
     * Instantiate a code property graph from the given Gremlin graph.
     *
     * @param g the graph to instantiate the CPG from
     */
    private CodePropertyGraph(Graph g) {
        this.g = g;

        CpgTraversal root = traversal().cpgRoot();
        if (!root.hasNext()) {
            throw new GraftRuntimeException("No CPG root in graph");
        }

        name = ((Vertex) root.next()).value(PROJECT_NAME);

        if (Options.isInit()) {
            if (!Options.v().containsKey(OPT_PROJECT_NAME)) {
                log.warn("Project name not set in options");
            } else {
                if (!name.equals(Options.v().getString(OPT_PROJECT_NAME))) {
                    log.warn("CPG name '{}' differs from project name '{}' in options");
                }
            }
        }
    }

    // ********************************************************************************************
    // instance methods
    // ********************************************************************************************

    public String name() {
        return name;
    }

    // ********************************************************************************************
    // CPG traversals
    // ********************************************************************************************

    public CpgTraversalSource traversal() {
        return g.traversal(CpgTraversalSource.class);
    }

    public CpgTraversal V() {
        return traversal().V();
    }

    public CpgTraversal E() {
        return traversal().E();
    }

    public long nrV() {
        return traversal().V().count().next();
    }

    public long nrAstV() {
        return (long) traversal().astV().count().next();
    }

    public long nrCfgV() {
        return (long) traversal().cfgV().count().next();
    }

    public long nrE() {
        return (long) traversal().E().count().next();
    }

    public long nrAstE() {
        return (long) traversal().astE().count().next();
    }

    public long nrCfgE() {
        return (long) traversal().cfgE().count().next();
    }

    public long nrPdgE() {
        return (long) traversal().pdgE().count().next();
    }

    public long nrAliasE() {
        return (long) traversal().aliasE().count().next();
    }

    // ********************************************************************************************
    // visualization / serialization
    // ********************************************************************************************

    /**
     * Write the CPG to a dot file.
     *
     * @param filename the name of the dot file to write the CPG to.
     */
    public void toDot(String filename) {
        DotUtil.graphToDot(this, filename, name);
    }

    /**
     * Dump the CPG to a graph file.
     *
     * @param filename the name of the file to dump the CPG to.
     */
    public void dump(String filename) {
        g.traversal().io(filename).write().iterate();
    }

    /**
     * Generate a status banner with information about the CPG.
     *
     * @return a status banner for the CPG
     */
    public Banner status() {
        Banner banner = new Banner("Code Property Graph");
        banner.println("Project name: " + name);
        banner.println();

        banner.println("NODES");
        banner.println("-----");
        banner.println(nrV() + " TOTAL");
        banner.println("* " + nrAstV() + " AST nodes");
        banner.println("* " + nrCfgV() + " CFG nodes");
        banner.println();

        banner.println("EDGES");
        banner.println("-----");
        banner.println(nrE() + " TOTAL");
        banner.println("* " + nrAstE() + " AST edges");
        banner.println("* " + nrCfgE() + " CFG edges");
        banner.println("* " + nrPdgE() + " PDG edges");
        banner.println("* " + nrAliasE() + " alias edges");

        return banner;
    }

    // ********************************************************************************************
    // database interaction
    // ********************************************************************************************

    /**
     * Commit the current transaction if the underlying database supports it.
     */
    public void commit() {
        if (g.features().graph().supportsTransactions()) {
            g.tx().commit();
        }
    }

    /**
     * Attempt to close the database connection.
     */
    public void close() {
        try {
            g.close();
        } catch (Exception e) {
            throw new GraftRuntimeException("Unable to close CPG", e);
        }
    }

    // ********************************************************************************************
    // static methods
    // ********************************************************************************************

    /**
     * Load a Tinkergraph CPG from the given file.
     *
     * @param filename the file to load the CPG from
     * @return the CPG loaded from the file
     */
    public static CodePropertyGraph fromFile(String filename) {
        Graph g = TinkerGraphUtil.fromFile(filename);
        return new CodePropertyGraph(g);
    }

    /**
     * Load a Neo4j CPG from the given directory.
     *
     * @param dirName the directory of the Neo4j database
     * @return the CPG loaded from the directory
     */
    public static CodePropertyGraph fromDir(String dirName) {
        Configuration neo4jConfig = new BaseConfiguration();
        neo4jConfig.setProperty("gremlin.neo4j.directory", dirName);
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.nodes.enabled", "true");
        neo4jConfig.setProperty("gremlin.neo4j.conf.dbms.auto_index.relationships.enabled", "true");
        Graph g = Neo4jUtil.fromConfig(neo4jConfig);
        return new CodePropertyGraph(g);
    }

    /**
     * Initialize a new empty CPG using the given graph database instance.
     *
     * @param g the graph database instance
     * @param name the project name
     * @param target the project target directory
     * @param classpath the project classpath
     * @return the newly initialized CPG
     */
    public static CodePropertyGraph initCpg(Graph g, String name, String target, String classpath) {
        g.traversal(CpgTraversalSource.class).addCpgRoot(name, target, classpath).iterate();
        if (g.features().graph().supportsTransactions()) {
            g.tx().commit();
        }
        return new CodePropertyGraph(g);
    }

    /**
     * Initialize a new empty CPG using the given graph database instance, using the project name,
     * target directory and classpath set in the global options.
     *
     * @param g the graph database instance
     * @return the newly intialized CPG
     */
    public static CodePropertyGraph initCpg(Graph g) {
        String name = Options.v().getString(OPT_PROJECT_NAME);
        String target = Options.v().getString(OPT_TARGET_DIR);
        String classpath = Options.v().getString(OPT_CLASSPATH);
        return initCpg(g, name, target, classpath);
    }

}
