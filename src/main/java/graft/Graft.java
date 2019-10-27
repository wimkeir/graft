package graft;

import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.analysis.GraftAnalysis;
import graft.analysis.TaintAnalysis;
import graft.cpg.CpgBuilder;
import graft.cpg.structure.CodePropertyGraph;
import graft.db.GraphUtil;
import graft.utils.LogUtil;
import graft.utils.SootUtil;

import static graft.Const.*;

/**
 * TODO: javadoc
 */
public class Graft {

    private static Logger log = LoggerFactory.getLogger(Graft.class);

    private static final String GRAFT_DIR_NAME = ".graft";
    private static final String DB_FOLDER_NAME = "db";
   // TODO: make format configurable
    private static final String DB_FILE_NAME = "cpg.json";
    private static final String PROPERTIES_FILE_NAME = "graft.properties";

    private static CodePropertyGraph cpg;

    private static Path USER_HOME_DIR;
    private static Path WORKING_DIR;
    private static Path GRAFT_DIR;
    private static Path DB_FOLDER;
    private static Path DB_FILE;
    private static Path PROPERTIES_FILE;

    public static CodePropertyGraph cpg() {
        return cpg;
    }

    private static void init() {
        checkOrExit(!GRAFT_DIR.toFile().exists(), "There is already an existing Graft project in this directory");
        checkOrExit(GRAFT_DIR.toFile().mkdir(), "Could not create Graft directory");
        log.info("Created Graft folder in directory '{}'", WORKING_DIR);
        initOpts();

        // TODO: do this in a much more robust way
        Scanner in = new Scanner(new InputStreamReader(System.in));

        System.out.print("1. Project name: ");
        String projName = in.next();

        System.out.print("2. Target directory: ");
        String targetDir = in.next();

        System.out.print("3. Classpath: ");
        String classpath = in.next();

        System.out.print("4. Database [" + Options.v().getString(OPT_DB_IMPLEMENTATION) + "]: ");
        String dbImpl = in.next();

        // TODO: validate inputs!
        Options.v().setProperty(OPT_PROJECT_NAME, projName);
        Options.v().setProperty(OPT_TARGET_DIR, targetDir);
        // TODO: rename this to just classpath, we'll always use -pp
        Options.v().setProperty(OPT_SOOT_OPTIONS_CLASSPATH, classpath);
        Options.v().setProperty(OPT_DB_IMPLEMENTATION, dbImpl);

        // TODO: do this in GraphUtil or somewhere
        if (Options.v().getString(OPT_DB_IMPLEMENTATION).equals(NEO4J)) {
            log.debug("Initialising Neo4j options");
            Options.v().setProperty(OPT_DB_DIRECTORY, DB_FOLDER.toString());
            cpg = GraphUtil.getCpg();
        } else if (Options.v().getString(OPT_DB_IMPLEMENTATION).equals(TINKERGRAPH)) {
            log.debug("Initialising Tinkergraph options");
            // before setting load-from so that we don't load from nonexistent file
            cpg = GraphUtil.getCpg();
            if (!DB_FILE.toFile().mkdir()) {
                log.error("Could not create DB folder");
                System.exit(1);
            }
            Options.v().setProperty(OPT_DB_LOAD_FROM, DB_FILE.toString());
            Options.v().setProperty(OPT_DB_DUMP_TO, DB_FILE.toString());
        } else {
            // TODO
            log.warn("Unrecognised DB implementation '{}'", Options.v().getString(OPT_DB_IMPLEMENTATION));
        }

        Options.v().debug();
        Options.v().toFile(PROPERTIES_FILE.toString());

        CpgBuilder.genCpgRoot(projName, targetDir, classpath);

        log.info("Graft project '{}' initialised - run `graft build` to build CPG", projName);
        shutdown();
    }

    private static void build() {
        startup();
        checkOrExit(GRAFT_DIR.toFile().exists(), "Directory is not a Graft project");
        checkOrExit(PROPERTIES_FILE.toFile().exists(), "No properties file in Graft directory");

        initOpts(PROPERTIES_FILE.toString());
        cpg = GraphUtil.getCpg();
        SootUtil.configureSoot();

        if (cpg.traversal().V().count().next() > 1) {
            // CPG already exists
            System.out.print("CPG already exists for project '"
                    + Options.v().getString(OPT_PROJECT_NAME)
                    + "' - overwrite? y/n: ");
            Scanner in = new Scanner(new InputStreamReader(System.in));
            String choice = in.next();
            while (!(choice.equals("y") || choice.equals("n"))) {
                System.out.print("Choose y/n: ");
                choice = in.next();
            }

            if (choice.equals("y")) {
                Vertex cpgRoot = cpg.traversal().V().hasLabel(CPG_ROOT).next();
                CpgBuilder.buildCpg(cpgRoot);
                shutdown();
            } else {
                log.info("Not overwriting CPG");
                shutdown();
            }
        }

        Vertex cpgRoot = cpg.traversal().V().hasLabel(CPG_ROOT).next();
        CpgBuilder.buildCpg(cpgRoot);
        shutdown();
    }

    private static void update() {
        startup();
        checkOrExit(GRAFT_DIR.toFile().exists(), "Directory is not a Graft project");
        checkOrExit(PROPERTIES_FILE.toFile().exists(), "No properties file in Graft directory");


        initOpts(PROPERTIES_FILE.toString());
        cpg = GraphUtil.getCpg();
        SootUtil.configureSoot();

        CpgBuilder.amendCpg();
        shutdown();
    }

    private static void runAnalysis(String analysisClass) {
        startup();
        checkOrExit(GRAFT_DIR.toFile().exists(), "Directory is not a Graft project");
        checkOrExit(PROPERTIES_FILE.toFile().exists(), "No properties file in Graft directory");

        initOpts(PROPERTIES_FILE.toString());
        cpg = GraphUtil.getCpg();
        SootUtil.configureSoot();

        GraftAnalysis analysis;
        switch (analysisClass) {
            case TAINT_ANALYSIS:
                analysis = new TaintAnalysis();
                break;
            default:
                // TODO: try and load class dynamically
                throw new RuntimeException("NOT IMPLEMENTED");
        }

        // XXX
        Options.v().setProperty(OPT_TAINT_SANITIZER, "etc/demo/simple/sanitizer.groovy");
        Options.v().setProperty(OPT_TAINT_SINK, "etc/demo/simple/sink.groovy");
        Options.v().setProperty(OPT_TAINT_SOURCE, "etc/demo/simple/source.groovy");

        analysis.doAnalysis(cpg);

        shutdown();
    }

    private static void shell() {
        // TODO
    }

    private static void dot(String filename) {
        startup();
        Path graftDir = WORKING_DIR.resolve(GRAFT_DIR);
        checkOrExit(graftDir.toFile().exists(), "Directory is not a Graft project");

        // TODO: config in home dir, user provided etc
        Config config = Config.fromFileWithDefaults(WORKING_DIR.resolve(GRAFT_DIR).resolve(PROPERTIES_FILE).toString());
        Options.init(config);
        LogUtil.setLogLevel(DEBUG); // XXX

        cpg = GraphUtil.getCpg();
        cpg.toDot(filename);
        shutdown();
    }

    private static void startup() {
        USER_HOME_DIR = Paths.get(System.getProperty("user.home"));
        WORKING_DIR = FileSystems.getDefault().getPath("").toAbsolutePath();
        log.debug("Working directory: {}", WORKING_DIR);
        log.debug("User home directory: {}", USER_HOME_DIR);

        GRAFT_DIR = WORKING_DIR.resolve(GRAFT_DIR_NAME);
        PROPERTIES_FILE = GRAFT_DIR.resolve(PROPERTIES_FILE_NAME);
        DB_FOLDER = GRAFT_DIR.resolve(DB_FOLDER_NAME);
        DB_FILE = DB_FOLDER.resolve(DB_FILE_NAME);
    }

    private static void shutdown() {
        assert cpg != null;
        if (Options.v().getString(OPT_DB_IMPLEMENTATION).equals(TINKERGRAPH)) {
            String graphFile = Options.v().getString(OPT_DB_DUMP_TO);
            assert graphFile != null;
            log.debug("Dumping CPG to file '{}'", graphFile);
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

    /**
     * TODO: javadoc
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        checkOrExit(0, args.length >= 1, "Invalid command line arguments");

        String cmd = args[0];
        startup();
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
            case CMD_SHELL:
                shell();
                break;
            case CMD_DOT:
                if (args.length < 2) {
                    log.error("No dotfile provided");
                    System.exit(0);
                }
                dot(args[1]);
                break;
            default:
                log.error("Unrecognised command '{}'", cmd);
                System.exit(1);
        }

    }

}
