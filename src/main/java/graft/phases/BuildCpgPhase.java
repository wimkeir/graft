package graft.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;

import graft.cpg.CpgBuilder;

/**
 * TODO: javadoc
 */
public class BuildCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    private Configuration options;

    public BuildCpgPhase(Configuration options) {
        this.options = options;
    }

    @Override
    public PhaseResult run() {
        log.info("Running BuildCpgPhase");

        PackManager.v().getPack("jtp").add(new Transform("jtp.cfg", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                log.debug("{} phase: transforming body of method '{}'", phaseName, body.getMethod().getName());
                CpgBuilder.buildCpg(body);
            }
        }));
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        List<String> sootOptions = getSootOptions(options);
        String[] sootArgs = new String[(sootOptions).size()];

        log.debug("Running soot with options: ");
        int i = 0;
        for (String option : sootOptions) {
            log.debug(option);
            sootArgs[i++] = option;
        }
        soot.Main.main(sootArgs);

        return new PhaseResult(this, true);
    }

    public static Configuration getOptions(Configuration config) {
        Configuration options = new BaseConfiguration();
        options.addProperty("process-dir", config.getStringArray("soot.options.process-dir"));
        options.addProperty("exclude", config.getStringArray("soot.options.exclude"));
        options.addProperty("src-prec", config.getString("soot.options.src-prec"));
        options.addProperty("output-format", config.getString("soot.options.output-format"));
        options.addProperty("main-class", config.getString("soot.options.main-class"));
        options.addProperty("output-dir", config.getString("soot.options.output-dir"));
        options.addProperty("soot-classpath", config.getString("soot.options.soot-classpath"));
        return options;
    }

    private List<String> getSootOptions(Configuration options) {
        // TODO (maybe useful):
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
            sootOptions.add("-verbose");                // enable Soot verbose mode
        }
        if (log.isTraceEnabled()) {
            sootOptions.add("-debug");
        }

        // configurable options

        // TODO: get either process dirs or file from command line
        if (options.containsKey("process-dir")) {
            for (String dir : options.getStringArray("process-dir")) {
                sootOptions.add("-process-dir");
                sootOptions.add(dir);
            }
        }

        if (options.containsKey("exclude")) {
            for (String pkg : options.getStringArray("exclude")) {
                sootOptions.add("-exclude");
                sootOptions.add(pkg);
            }
        }

        if (options.containsKey("src-prec")) {
            sootOptions.add("-src-prec");
            sootOptions.add(options.getString("src-prec"));
        }

        if (options.containsKey("soot-classpath")) {
            sootOptions.add("-soot-classpath");
            sootOptions.add(options.getString("soot-classpath"));
        }

        if (options.containsKey("output-format")) {
            sootOptions.add("-output-format");
            sootOptions.add(options.getString("output-format"));
        }

        if (options.containsKey("main-class")) {
            sootOptions.add("-main-class");
            sootOptions.add(options.getString("main-class"));
        }

        if (options.containsKey("output-dir")) {
            sootOptions.add("-output-dir");
            sootOptions.add(options.getString("output-dir"));
        }

        return sootOptions;
    }

}
