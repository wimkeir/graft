package graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global options for Graft.
 *
 * @author Wim Keirsgieter
 */
public class Options {

    private static Logger log = LoggerFactory.getLogger(Options.class);

    /**
     * Singleton options instance.
     */
    private static Config options;

    /**
     * Initialize options (at start of Graft run).
     *
     * @param config the Graft configuration file
     */
    public static void init(Config config) {
        if (options != null) {
            throw new GraftRuntimeException("Options already initialized");
        }
        options = config.copy();
        if (log.isDebugEnabled()) {
            options.debug();
        }
    }

    /**
     * Check if the global options have been initialized.
     *
     * @return true if the global options have been initialized, else false
     */
    public static boolean isInit() {
        return options != null;
    }

    /**
     * Get the current global options instance.
     *
     * @return the current options instance
     */
    public static Config v() {
        if (options == null) {
            throw new GraftRuntimeException("Options not initialized");
        }
        return options;
    }

}
