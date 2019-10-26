package graft.utils;

import graft.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.PhaseOptions;
import soot.Scene;
import soot.jimple.Stmt;
import soot.tagkit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        soot.options.Options.v().set_allow_phantom_refs(true);

        List<String> excluded = new ArrayList<>();
        excluded.add("java.lang");
        soot.options.Options.v().set_exclude(excluded);

        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        // TODO:
        // excluded packages
        // output format
        // print tags
        // debug
        // output dir
    }

    public static List<File> getClassFiles(File targetDir) {
        List<File> classFiles = new ArrayList<>();
        for (File file : targetDir.listFiles()) {
            if (file.isDirectory()) {
                classFiles.addAll(getClassFiles(file));
            }
            if (file.getName().matches(CLASS_FILE_REGEX)) {
                classFiles.add(file);
            }
        }
        return classFiles;
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

    public static int getLineNr(Stmt stmt) {
        if (stmt == null) {
            return -1;
        }
        if (stmt.getTag("SourceLnPosTag") != null) {
            return ((SourceLnPosTag) stmt.getTag("SourceLnPosTag")).startLn();
        } else if (stmt.getTag("JimpleLineNumberTag") != null) {
            return ((JimpleLineNumberTag) stmt.getTag("JimpleLineNumberTag")).getLineNumber();
        } else if (stmt.getTag("LineNumberTag") != null) {
            return ((LineNumberTag) stmt.getTag("LineNumberTag")).getLineNumber();
        } else if (stmt.getTag("SourceLineNumberTag") != null) {
            return ((SourceLineNumberTag) stmt.getTag("SourceLineNumberTag")).getLineNumber();
        } else {
            return -1;
        }
    }

    public static String getSourceFile(Stmt stmt) {
        if (stmt == null) {
            return UNKNOWN;
        }
        if (stmt.getTag("SourceFileTag") != null) {
            return ((SourceFileTag) stmt.getTag("SourceFileTag")).getSourceFile();
        } else {
            return UNKNOWN;
        }
    }

}
