package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class Options {

    private static Logger log = LoggerFactory.getLogger(Options.class);

    // TODO: option consts here

    private static Config options;

    public static void init(Config config) {
        if (options != null) {
            throw new GraftException("Options already initialized");
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
            throw new GraftException("Options not initialized");
        }
        return options;
    }

}
