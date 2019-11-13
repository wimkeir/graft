package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class Options {

    // TODO
    // javadocs
    // make sure we know when options are configured or not

    private static Logger log = LoggerFactory.getLogger(Options.class);

    private static Config options;

    static void init(Config config) {
        if (options != null) {
            throw new GraftRuntimeException("Options already initialized");
        }
        options = config.copy();

        if (log.isDebugEnabled()) {
            log.info("Running with options:");
            Iterator<String> keys = config.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                log.info("- " + key + ": " + config.getProperty(key));
            }
        }
    }

    public static Config v() {
        if (options == null) {
            throw new GraftRuntimeException("Options not initialized");
        }
        return options;
    }

}
