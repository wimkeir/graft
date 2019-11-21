package graft;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.analysis.AliasAnalysis;
import graft.analysis.GraftAnalysis;
import graft.analysis.TaintAnalysis;

import graft.cpg.CpgBuilder;
import graft.cpg.structure.CodePropertyGraph;

import graft.utils.LogUtil;
import graft.utils.SootUtil;

import static graft.Const.*;
import static graft.db.GraphUtil.*;

/**
 * Graft is a code property graph analysis tool for Java programs.
 *
 * See the README for more information.
 *
 * @author Wim Keirsgieter
 */
public class Graft {

    private static Logger log = LoggerFactory.getLogger(Graft.class);

    private static final Path WORKING_DIR     = Paths.get("").toAbsolutePath();
    private static final Path GRAFT_DIR       = WORKING_DIR.resolve(GRAFT_DIR_NAME);
    private static final Path DB_FOLDER       = GRAFT_DIR.resolve(DB_FOLDER_NAME);
    private static final Path PROPERTIES_FILE = GRAFT_DIR.resolve(PROPERTIES_FILE_NAME);

    /**
     * The project CPG instance.
     */
    private static CodePropertyGraph cpg;

    /**
     * Get a reference to the project CPG.
     *
     * @return a reference to the project CPG.
     */
    public static CodePropertyGraph cpg() {
        if (cpg == null) {
            throw new GraftRuntimeException("CPG not initialised");
        }
        return cpg;
    }

    private static void userOpts() {
        Scanner in = new Scanner(new InputStreamReader(System.in));

        System.out.print("1. Project name: ");
        String name = in.next();
        while (!name.matches(PROJECT_NAME_REGEX)) {
            System.out.println("Invalid project name '" + name + "'");
            System.out.print("1. Project name: ");
            name = in.next();
        }

        System.out.print("2. Target directory: ");
        String targetDir = in.next();

        System.out.print("3. Classpath: ");
        String classpath = in.next();

        System.out.print("4. Database [tinkergraph, neo4j]: ");
        String impl = in.next();
        while (!(impl.equals(TINKERGRAPH) || impl.equals(NEO4J))) {
            System.out.println("Invalid database implementation '" + impl + "'");
            System.out.print("4. Database [tinkergraph, neo4j]: ");
            impl = in.next();
        }

        Options.v().setProperty(OPT_PROJECT_NAME, name);
        Options.v().setProperty(OPT_TARGET_DIR, targetDir);
        Options.v().setProperty(OPT_CLASSPATH, classpath);
        Options.v().setProperty(OPT_DB_IMPLEMENTATION, impl);
    }

    private static void initNewDb() {
        checkOrExit(DB_FOLDER.toFile().mkdir(), "Could not create DB folder");

        switch (Options.v().getString(OPT_DB_IMPLEMENTATION)) {
            case TINKERGRAPH:
                String dbFile = DB_FOLDER.resolve(dbFileName()).toString();
                Options.v().setProperty(OPT_DB_FILE, dbFile);
                cpg = newTinkergraphCpg();
                break;
            case NEO4J:
                String dbDir = DB_FOLDER.toString();
                Options.v().setProperty(OPT_DB_DIRECTORY, dbDir);
                cpg = newNeo4jCpg(dbDir);
                break;
            default:
                log.error("Cannot initialise new database");
                throw new GraftRuntimeException(
                        "Unrecognised database implementation '" +
                        Options.v().getString(OPT_DB_IMPLEMENTATION) + "'");
        }
    }

    // ********************************************************************************************
    // commands
    // ********************************************************************************************

    /**
     * Initialise a new Graft project in the current working directory.
     *
     * The user is prompted for a project name, target directory, and project classpath. They are
     * also given an option to choose the database implementation to use (default Tinkergraph).
     *
     * A Graft folder is created in the current directory with contents:
     *  - graft.properties (stores the project configuration - should not be edited)
     *  - db/ (the CPG is persisted here)
     *
     * If using Neo4j, the db folder will contain the Neo4j database binaries. If using Tinkergraph,
     * the graph will be read from and written to a file stored in the db folder (the file format
     * is configurable, default JSON).
     *
     * This command will fail if there is already a Graft folder in the current directory.
     */
    private static void init() {
        checkOrExit(!GRAFT_DIR.toFile().exists(), "Graft folder already exists");

        // create Graft folder (exit if unsuccessful)
        checkOrExit(GRAFT_DIR.toFile().mkdir(), "Could not create Graft folder");
        log.info("Created Graft folder in directory '{}'", WORKING_DIR);

        // load default configuration
        initOpts();

        // prompt user for project options
        userOpts();

        // set up new database
        initNewDb();

        // write project configuration to properties file
        Options.v().debug();
        Options.v().toFile(PROPERTIES_FILE);

        Banner banner = new Banner("New Graft project: " + Options.v().getString(OPT_PROJECT_NAME));
        banner.println("Target directory: " + Options.v().getString(OPT_TARGET_DIR));
        banner.println("Database: " + Options.v().getString(OPT_DB_IMPLEMENTATION));
        banner.display();

        shutdown();
    }

    /**
     * Build the initial project CPG and persist it to the database.
     *
     * If the database already contains a CPG, it can be optionally overwritten.
     */
    private static void build() {
        startup();

        String targetDir = Options.v().getString(OPT_TARGET_DIR);
        checkOrExit(1, targetDir != null, "Target directory not set");
        CpgBuilder cpgBuilder;

        if (cpg.traversal().V().count().next() > 1) {
            System.out.println("CPG already exists for project '" + Options.v().getString(OPT_PROJECT_NAME) + "'...");
            System.out.println("Use 'graft update' instead");
            shutdown();
        }

        cpgBuilder = new CpgBuilder();
        cpgBuilder.buildCpg(targetDir);

        shutdown();
    }

    private static void update() {
        startup();
        CpgBuilder cpgBuilder = new CpgBuilder();
        cpgBuilder.amendCpg();
        shutdown();
    }

    private static void runAnalysis(String analysisClass) {
        startup();

        GraftAnalysis analysis;
        switch (analysisClass) {
            case TAINT_ANALYSIS:
                analysis = new TaintAnalysis("etc/taint.groovy");
                break;
            case ALIAS_ANALYSIS:
                analysis = new AliasAnalysis();
                break;
            default:
                // TODO: try and load class dynamically
                throw new RuntimeException("Unrecognised analysis class");
        }

        analysis.doAnalysis();
        shutdown();
    }

    private static void dot(String filename) {
        startup();
        cpg().toDot(filename);
        shutdown();
    }

    private static void dump(String filename) {
        startup();
        cpg().dump(filename);
        shutdown();
    }

    private static void status() {
        startup();
        cpg().status().display();
        shutdown();
    }

    private static void startup() {
        checkOrExit(GRAFT_DIR.toFile().exists(), "Directory is not a Graft project");
        checkOrExit(PROPERTIES_FILE.toFile().exists(), "No properties file in Graft directory");
        initOpts(PROPERTIES_FILE.toString());
        cpg = getCpg();
        SootUtil.configureSoot();

        if (log.isDebugEnabled()) {
            long V = cpg.traversal().V().count().next();
            log.debug("Node count: {}", V);
            long E = cpg.traversal().E().count().next();
            log.debug("Edge count: {}", E);
        }

        List<File> amendedClasses = CpgBuilder.amendedClasses();
        if (amendedClasses.size() != 0 && cpg.nrV() > 1) {
            log.info("{} classes changed since CPG construction", amendedClasses.size());
            log.info("Run 'graft update' to update CPG");
        }
    }

    private static void shutdown() {
        assert cpg != null;
        cpg.commit();
        if (Options.v().getString(OPT_DB_IMPLEMENTATION).equals(TINKERGRAPH)) {
            String graphFile = Options.v().getString(OPT_DB_FILE);
            assert graphFile != null;
            cpg.dump(graphFile);
        }
        cpg.close();
        System.exit(0);
    }

    private static void initOpts() {
        Config config = Config.getDefault();
        Options.init(config);
        LogUtil.setLogLevel(Options.v().getString(OPT_GENERAL_LOG_LEVEL));
    }

    private static void initOpts(String configFile) {
        Config config = Config.fromFileWithDefaults(configFile);
        Options.init(config);
        LogUtil.setLogLevel(Options.v().getString(OPT_GENERAL_LOG_LEVEL));
    }

    private static void checkOrExit(boolean condition, String fmt, Object ... args) {
        checkOrExit(1, condition, fmt, args);
    }

    private static void checkOrExit(int code, boolean condition, String fmt, Object ... args) {
        if (!condition) {
            log.error(fmt, args);
            System.exit(code);
        }
    }

    private static String dbFileName() {
        String format = Options.v().getString(OPT_DB_FILE_FORMAT);
        switch (format) {
            case "graphson":
            case "json":
                return Options.v().getString(OPT_PROJECT_NAME) + ".json";
            case "xml":
            case "graphml":
                return Options.v().getString(OPT_PROJECT_NAME) + ".xml";
            case "kryo":
                return Options.v().getString(OPT_PROJECT_NAME) + ".kryo";
            default:
                throw new GraftRuntimeException("Unrecognised DB file format '" + format);
        }
    }

    public static void main(String[] args) {
        checkOrExit(0, args.length >= 1, "Invalid command line arguments");

        String cmd = args[0];
        switch (cmd) {
            case CMD_INIT:
                init();
                break;
            case CMD_BUILD:
                build();
                break;
            case CMD_UPDATE:
                update();
                break;
            case CMD_RUN:
                if (args.length < 2) {
                    log.error("No analysis specified");
                    System.exit(0);
                }
                runAnalysis(args[1]);
                break;
            case CMD_DOT:
                checkOrExit(0, args.length == 2, "No dotfile provided");
                dot(args[1]);
                break;
            case CMD_DUMP:
                checkOrExit(0, args.length == 2, "No dump file provided");
                dump(args[1]);
                break;
            case CMD_STATUS:
                status();
                break;
            default:
                log.error("Unrecognised command '{}'", cmd);
                System.exit(1);
        }
    }

}
