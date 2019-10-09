package graft.phases;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import graft.utils.FileUtil;
import graft.utils.SootUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import graft.Options;
import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;

import static graft.Const.*;

/**
 * Amends the current CPG if the class files in the target directory have changed since its construction.
 *
 * @author Wim Keirsgieter
 */
public class AmendCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    public AmendCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running AmendCpgPhase...");
        String targetDir = Options.v().getString(OPT_TARGET_DIR);

        // get all class files in target dir
        List<File> classFiles = Arrays.asList(SootUtil.getClassFiles(targetDir));
        if (classFiles.size() == 0) {
            return new PhaseResult(this, false, "No class files found in target directory");
        }

        // fill list of classes to be amended if their file hashes have changed
        List<File> amendedClasses = new ArrayList<>();
        List<String> classNames = new ArrayList<>();
        for (File classFile : classFiles) {
            // TODO: package prefixes
            String className = classFile.getName().replace(".class", "");
            String hash = FileUtil.hashFile(classFile);
            if (!hash.equals(CpgUtil.getClassHash(className))) {
                log.info("Class '{}' has been changed", className);
                amendedClasses.add(classFile);
                classNames.add(className);
            }
        }

        if (amendedClasses.size() == 0) {
            String details = "No changes to class files";
            return new PhaseResult(this, true, details);
        }

        SootUtil.loadClasses(classNames.toArray(new String[0]));

        for (int i = 0; i < amendedClasses.size(); i++) {
            log.debug("Amending CPG of class '{}'", classNames.get(i));
            SootClass cls = Scene.v().loadClassAndSupport(classNames.get(i));
            CpgBuilder.amendCpg(cls, amendedClasses.get(i));
        }

        // TODO: display phase output directly from phase

        String details = String.format("| Nodes: %1$-89d |\n", CpgUtil.getNodeCount());
        details += String.format("| Edges: %1$-89d |\n", CpgUtil.getEdgeCount());
        return new PhaseResult(this, true, details);
    }

}
