package graft.phases;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.options.Options;

import graft.cpg.CpgBuilder;

/**
 * TODO: this needs some serious work...
 */
public class BuildCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    public BuildCpgPhase(Configuration options) {
//        setSootOptions(options);
    }

    @Override
    public PhaseResult run() {
        log.info("Running BuildCpgPhase");

//        if (PackManager.v().onlyStandardPacks()) {
//            for (Pack pack : PackManager.v().allPacks()) {
//                Options.v().warnForeignPhase(pack.getPhaseName());
//                for (Transform tr : pack) {
//                    Options.v().warnForeignPhase(tr.getPhaseName());
//                }
//            }
//        }
//        Options.v().warnNonexistentPhase();
//        Options.v().set_unfriendly_mode(true);

        PackManager.v().getPack("jtp").add(new Transform("jtp.cfg", new CpgTransformer()));
        soot.Main.main(new String[]{"-keep-line-number", "-cp", "etc/examples/simple:etc/jars/jce.jar", "-pp", "-process-dir", "etc/examples/simple", "-app"});

        return new PhaseResult(this, true);
    }

    public static Configuration getOptions(Configuration config) {
        // TODO: don't hardwire options!
        Configuration options = new BaseConfiguration();
        options.setProperty("soot-classpath", "etc/examples/simple:etc/jars/jce.jar");
//        options.setProperty("main-class", "Simple");
        options.setProperty("verbose", true);
        options.setProperty("debug", true);
        options.setProperty("debug-resolver", true);
        options.setProperty("ast-metrics", true);
        options.setProperty("output-dir", "etc/sootOutput");
//        options.setProperty("exclude-packages", Arrays.asList("java", "sun", "jdk"));
        return options;
    }

    private void setSootOptions(Configuration options) {
        // mandatory Soot options (we need these for our analyses)
        Options.v().set_keep_line_number(true);
        Options.v().set_app(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_time(true);

        // TODO: where to set this?
        Options.v().set_process_dir(Collections.singletonList("etc/examples/simple"));

        // TODO: confirm what these options do
//        Options.v().set_no_bodies_for_excluded(true);

        // TODO: this should be configurable, but we'd need to map output formats from string reprs
        Options.v().set_output_format(Options.output_format_J);

        // XXX: these options are for debugging and should be disabled in prod
//        Options.v().set_print_tags_in_output(true);
//        Options.v().set_dump_cfg(Collections.singletonList("ALL"));
//        Options.v().set_show_exception_dests(true);

        // TODO (maybe useful):
        //  -phase-option key:val
        //  -via-shimple
        //  -throw-analysis
        //  -omit-excepting-unit-edges
        //  -trim-cfgs
        //  -dynamic-dir/-class/-package
        //  -interactive-mode
        //   annotation options...

        // configurable Soot options
        if (options.containsKey("soot-classpath")) {
            log.trace("Setting soot-classpath to '{}'", options.getString("soot-classpath"));
            Options.v().set_soot_classpath(options.getString("soot-classpath"));
        }
        if (options.containsKey("verbose")) {
            log.trace("Setting verbose to '{}'", options.getBoolean("verbose"));
            Options.v().set_verbose(options.getBoolean("verbose"));
        }
        if (options.containsKey("debug")) {
            log.trace("Setting debug to '{}'", options.getBoolean("debug"));
            Options.v().set_debug(options.getBoolean("debug"));
        }
        if (options.containsKey("debug-resolver")) {
            log.trace("Setting debug-resolver to '{}'", options.getBoolean("debug-resolver"));
            Options.v().set_debug(options.getBoolean("debug-resolver"));
        }
        if (options.containsKey("ast-metrics")) {
            log.trace("Setting ast-metrics to '{}'", options.getBoolean("ast-metrics"));
            Options.v().set_ast_metrics(options.getBoolean("ast-metrics"));
        }
        if (options.containsKey("main-class")) {
            log.trace("Setting main-class to '{}'", options.getString("main-class"));
            Options.v().set_main_class(options.getString("main-class"));
        }
        if (options.containsKey("output-dir")) {
            log.trace("Setting output-dir to '{}'", options.getString("output-dir"));
            Options.v().set_output_dir(options.getString("output-dir"));
        }
        if (options.containsKey("include-packages")) {
            Options.v().set_include(options.getList(String.class, "include-packages"));
        }
        if (options.containsKey("exclude-packages")) {
            Options.v().set_exclude(options.getList(String.class, "exclude-packages"));
        }
    }

    private static class CpgTransformer extends BodyTransformer {

        @Override
        protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
            log.debug("CpgTransformer transforming body of method '{}'", body.getMethod().getName());
            CpgBuilder.buildCpg(body);
        }
    }

}
