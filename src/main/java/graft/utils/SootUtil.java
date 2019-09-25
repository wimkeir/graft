package graft.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with Soot.
 *
 * @author Wim Keirsgieter
 */
public class SootUtil {

    private static Logger log = LoggerFactory.getLogger(SootUtil.class);

    /**
     * Get a list of command line options to pass to Soot from a config object.
     *
     * @param config the config object
     * @return a list of Soot command line options
     */
    public static List<String> getSootOptions(Configuration config) {
        // Other soot options (maybe useful):
        //  -phase-option key:val
        //  -via-shimple (stp phase)
        //  -throw-analysis
        //  -trim-cfgs
        //  -dynamic-dir/-class/-package
        //  -interactive-mode
        //   annotation options...

        // mandatory options
        List<String> sootOptions = new ArrayList<>();
        sootOptions.add("-keep-line-number");           // keep original line numbers in stmt tags
        sootOptions.add("-app");                        // run in application mode (process all classes referenced)
        sootOptions.add("-prepend-classpath");          // prepend the Soot classpath to the argument classpath
        sootOptions.add("-no-bodies-for-excluded");     // don't generate Jimple bodies for excluded packages

        // debug options
        if (log.isDebugEnabled()) {
            sootOptions.add("-print-tags");             // show stmt tags in Jimple output
        }
        if (log.isTraceEnabled()) {
            sootOptions.add("-debug");                  // enable Soot debug logs
        }

        // configurable options

        if (config.containsKey("soot.options.process-dir")) {
            for (String dir : config.getStringArray("soot.options.process-dir")) {
                sootOptions.add("-process-dir");
                sootOptions.add(dir);
            }
        }

        if (config.containsKey("soot.options.exclude")) {
            for (String pkg : config.getStringArray("soot.options.exclude")) {
                sootOptions.add("-exclude");
                sootOptions.add(pkg);
            }
        }

        if (config.containsKey("soot.options.src-prec")) {
            sootOptions.add("-src-prec");
            sootOptions.add(config.getString("soot.options.src-prec"));
        }

        if (config.containsKey("soot.options.soot-classpath")) {
            sootOptions.add("-soot-classpath");
            sootOptions.add(config.getString("soot.options.soot-classpath"));
        }

        if (config.containsKey("soot.options.output-format")) {
            sootOptions.add("-output-format");
            sootOptions.add(config.getString("soot.options.output-format"));
        }

        if (config.containsKey("soot.options.main-class")) {
            sootOptions.add("-main-class");
            sootOptions.add(config.getString("soot.options.main-class"));
        }

        if (config.containsKey("soot.options.output-dir")) {
            sootOptions.add("-output-dir");
            sootOptions.add(config.getString("soot.options.output-dir"));
        }

        if (config.getBoolean("soot.options.verbose", false)) {
            sootOptions.add("-verbose");
        }

        return sootOptions;
    }
}
