package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.CpgBuilder;

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
        CpgBuilder cpgBuilder = new CpgBuilder(args[0]);
        cpgBuilder.buildCpg();
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
