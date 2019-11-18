package graft.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.PhaseOptions;
import soot.Scene;
import soot.jimple.Stmt;
import soot.tagkit.*;

import graft.GraftRuntimeException;
import graft.Options;

import static graft.Const.*;

/**
 * Utility methods for dealing with Soot.
 *
 * @author Wim Keirsgieter
 */
public class SootUtil {

    private static Logger log = LoggerFactory.getLogger(SootUtil.class);
    private static boolean configured = false;

    /**
     * Configure Soot options for CPG transformation.
     */
    public static void configureSoot() {
        if (configured) {
            throw new GraftRuntimeException("Soot has already been configured");
        }
        configured = true;

        // set application mode
        soot.options.Options.v().set_app(true);

        // make sure classpath is configured correctly
        soot.options.Options.v().set_soot_classpath(Options.v().getString(OPT_CLASSPATH));
        soot.options.Options.v().set_prepend_classpath(true);

        // keep debugging info
        soot.options.Options.v().set_keep_line_number(true);
        soot.options.Options.v().set_keep_offset(true);

        // ignore library code
        soot.options.Options.v().set_no_bodies_for_excluded(true);
        soot.options.Options.v().set_allow_phantom_refs(true);

        // exclude java.lang packages
        List<String> excluded = new ArrayList<>();
        excluded.add("java.lang");
        soot.options.Options.v().set_exclude(excluded);

        // keep variable names
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
    }

    /**
     * Given a list of class names, load them into the Scene.
     *
     * @param classNames a list of class names
     */
    public static void loadClasses(String[] classNames) {
        if (!configured) {
            throw new GraftRuntimeException("Soot has not been configured");
        }
        for (String className : classNames) {
            log.debug("Adding basic class '{}' to scene", className);
            Scene.v().addBasicClass(className);
        }
        log.debug("Loading classes...");
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    /**
     * Get the line number of a Jimple statement if it is set.
     *
     * @param stmt the Jimple statement
     * @return the line number of the statement or -1 if not set
     */
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

    /**
     * Get the source file name of a Jimple statement if it is set.
     *
     * @param stmt the Jimple statement
     * @return the source file name of the statement or UNKNOWN if not set
     */
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
