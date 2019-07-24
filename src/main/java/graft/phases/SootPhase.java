package graft.phases;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class SootPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(SootPhase.class);

    public SootPhase(Configuration options) {
        setSootOptions(options);
    }

    @Override
    public PhaseResult run() {
        log.info("Running SootPhase");
        // PackManager.v().getPack("wjtp").add(new Transform("wjtp.cpg", new CpgTransformer()));
        // soot.Main.main(new String[]{"Simple"});

        Scene.v().loadNecessaryClasses();
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            System.out.println(cls.getName());
            for (SootMethod method : cls.getMethods()) {
                System.out.println(method.getName());
            }
        }

        return new PhaseResult(this, true);
    }

    public static Configuration getOptions(Configuration config) {
        // TODO: don't hardwire options!
        Configuration options = new BaseConfiguration();
        options.setProperty("soot-classpath", "etc/examples/simple:etc/jars/jce.jar");
        options.setProperty("verbose", true);
        options.setProperty("debug", true);
        options.setProperty("debug-resolver", true);
        options.setProperty("ast-metrics", true);
        options.setProperty("output-dir", "etc/sootOutput");
        options.setProperty("exclude-packages", Arrays.asList("java", "sun", "jdk"));
        return options;
    }

    private void setSootOptions(Configuration options) {
        // mandatory Soot options (we need these for our analyses)
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_whole_program(true);
        Options.v().set_time(true);

        // TODO: where to set this?
        Options.v().set_process_dir(Collections.singletonList("etc/examples/simple"));

        // TODO: confirm what these options do
        // Options.v().set_no_bodies_for_excluded(true);

        // TODO: this should be configurable, but we'd need to map output formats from string reprs
        Options.v().set_output_format(Options.output_format_J);

        // XXX: these options are for debugging and should be disabled in prod
        Options.v().set_print_tags_in_output(true);
        Options.v().set_dump_cfg(Collections.singletonList("ALL"));
        Options.v().set_show_exception_dests(true);

        // TODO (maybe useful):
        //  -phase-option key:val
        //  -via,shimple
        //  -throw-analysis
        //  -omit-excepting-unit-edges
        //  -trim-cfgs
        //  -dynamic-dir/-class/-package
        //   annotation options...

        // configurable Soot options
        if (options.containsKey("soot-classpath")) {
            Options.v().set_soot_classpath(options.getString("soot-classpath"));
        }
        if (options.containsKey("verbose")) {
            Options.v().set_verbose(options.getBoolean("verbose"));
        }
        if (options.containsKey("debug")) {
            Options.v().set_debug(options.getBoolean("debug"));
        }
        if (options.containsKey("debug-resolver")) {
            Options.v().set_debug(options.getBoolean("debug-resolver"));
        }
        if (options.containsKey("ast-metrics")) {
            Options.v().set_ast_metrics(options.getBoolean("ast-metrics"));
        }
        if (options.containsKey("main-class")) {
            Options.v().set_main_class(options.getString("main-class"));
        }
        if (options.containsKey("output-dir")) {
            Options.v().set_output_dir(options.getString("output-dir"));
        }
        if (options.containsKey("include-packages")) {
            Options.v().set_include(options.getList(String.class, "include-packages"));
        }
        if (options.containsKey("exclude-packages")) {
            Options.v().set_exclude(options.getList(String.class, "exclude-packages"));
        }
    }

//    class CpgTransformer extends SceneTransformer {
//
//        @Override
//        protected void internalTransform(String phaseName, Map<String, String> options) {
//            System.out.println("IN INTERNAL TRANSFORM");
//            for (SootClass cls : Scene.v().getClasses()) {
//                System.out.println(cls.getName());
//            }
//        }
//    }
}
