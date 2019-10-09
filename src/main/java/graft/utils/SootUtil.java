package graft.utils;

import graft.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.PhaseOptions;
import soot.Scene;

import java.io.File;

import static graft.Const.*;

/**
 * Utility methods for dealing with Soot.
 *
 * @author Wim Keirsgieter
 */
public class SootUtil {

    private static Logger log = LoggerFactory.getLogger(SootUtil.class);

    public static void configureSoot() {
        soot.options.Options.v().set_soot_classpath(Options.v().getString(OPT_SOOT_OPTIONS_CLASSPATH));
        soot.options.Options.v().set_prepend_classpath(true);
//        soot.options.Options.v().set_whole_program(true);
        soot.options.Options.v().set_keep_line_number(true);
        soot.options.Options.v().set_keep_offset(true);
        soot.options.Options.v().set_no_bodies_for_excluded(true);
        // TODO: set app or whole program?
        soot.options.Options.v().set_app(true);
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        // TODO:
        // excluded packages
        // output format
        // print tags
        // debug
        // output dir
    }

    public static File[] getClassFiles(String targetDir) {
        File dir = new File(targetDir);
        return dir.listFiles(file -> file.getName().matches(CLASS_FILE_REGEX));
    }

    public static void loadClasses(String[] classNames) {
        for (String className : classNames) {
            log.debug("Adding basic class '{}' to scene", className);
            Scene.v().addBasicClass(className);
        }
        log.debug("Loading classes...");
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

}
