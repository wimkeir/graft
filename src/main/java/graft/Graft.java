package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.db.GraphUtil;
import graft.utils.LogUtil;
import graft.utils.SootUtil;

import static graft.Const.*;

/**
 * TODO: javadoc
 */
public class Graft {

    private static Logger log = LoggerFactory.getLogger(Graft.class);

    /**
     * TODO: javadoc
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CLI cli = new CLI(args);
        Config config = cli.hasOpt(CLI_OPT_CONFIG) ?
                Config.fromFileWithDefaults(cli.getOpt(CLI_OPT_CONFIG)) :
                Config.getDefault();
        Options.init(config, cli);
        LogUtil.setLogLevel(Options.v().getString(OPT_GENERAL_LOG_LEVEL));
        log.debug("Running with configuration {}", config.toString());

        GraphUtil.initGraph();
        SootUtil.configureSoot();

        GraftRun graftRun = new GraftRun();
        GraftResult result = graftRun.run();

        GraphUtil.closeGraph();

        output(result.toString());
    }

    private static void output(String s) {
        System.out.println(s);
    }

}
