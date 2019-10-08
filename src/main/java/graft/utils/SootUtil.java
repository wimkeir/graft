package graft.utils;

import java.util.ArrayList;
import java.util.List;

import graft.Options;

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
     * Get a list of Soot options from the current Graft options.
     *
     * @return a list of Soot command line options
     */
    public static List<String> getSootOptions() {
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

        if (Options.v().containsKey("soot.options.process-dir")) {
            for (String dir : Options.v().getStringArray("soot.options.process-dir")) {
                sootOptions.add("-process-dir");
                sootOptions.add(dir);
            }
        }

        if (Options.v().containsKey("soot.options.exclude")) {
            for (String pkg : Options.v().getStringArray("soot.options.exclude")) {
                sootOptions.add("-exclude");
                sootOptions.add(pkg);
            }
        }

        if (Options.v().containsKey("soot.options.src-prec")) {
            sootOptions.add("-src-prec");
            sootOptions.add(Options.v().getString("soot.options.src-prec"));
        }

        if (Options.v().containsKey("soot.options.soot-classpath")) {
            sootOptions.add("-soot-classpath");
            sootOptions.add(Options.v().getString("soot.options.soot-classpath"));
        }

        if (Options.v().containsKey("soot.options.output-format")) {
            sootOptions.add("-output-format");
            sootOptions.add(Options.v().getString("soot.options.output-format"));
        }

        if (Options.v().containsKey("soot.options.main-class")) {
            sootOptions.add("-main-class");
            sootOptions.add(Options.v().getString("soot.options.main-class"));
        }

        if (Options.v().containsKey("soot.options.output-dir")) {
            sootOptions.add("-output-dir");
            sootOptions.add(Options.v().getString("soot.options.output-dir"));
        }

        if (Options.v().getBoolean("soot.options.verbose", false)) {
            sootOptions.add("-verbose");
        }

        return sootOptions;
    }
}
