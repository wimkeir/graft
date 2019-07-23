package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.CpgBuilder;
import graft.db.GraphUtil;
import graft.utils.DotUtil;
import graft.utils.LogUtil;

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
        validateArgs(args);

        GraftConfig config = null;
        try {
            if (args.length == 2) {
                config = GraftConfig.fromFile(args[1]);
            } else {
                log.info("No config file specified, using default config");
                config = GraftConfig.defaultConfig();
            }
        } catch (GraftException e) {
            log.error("Unable to configure Graft", e);
            System.exit(1);
        }
        LogUtil.setLogLevel(config.getString("general.log-level"));
        log.debug("Running with configuration {}", config.toString());

        GraphUtil.initGraph();
        CpgBuilder cpgBuilder = new CpgBuilder(args[0]);

        try {
            cpgBuilder.buildCpg();
            DotUtil.cpgToDot("etc/dot/cpg.dot", "cpg");
        } catch (GraftException e) {
            log.error("Unable to build CFG: {}", e.getMessage(), e);
        }
    }

    private static void validateArgs(String[] args) {
        if (args.length < 1) {
            log.error("Invalid command line arguments: no source root specified");
            System.exit(1);
        }
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            log.debug("Running with command line arguments: {}", sb.toString());
        }
    }

}
