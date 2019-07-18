package graft;

import graft.utils.DotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.CpgBuilder;
import graft.db.GraphUtil;

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
        // TODO
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            log.debug("Running with command line arguments: {}", sb.toString());
        }
    }

}
