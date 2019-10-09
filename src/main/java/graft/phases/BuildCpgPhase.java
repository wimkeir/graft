package graft.phases;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;
import graft.Options;
import graft.utils.SootUtil;

import static graft.Const.*;

/**
 * This phase handles the actual construction of the (intraprocedural) CPG, using Soot.
 *
 * @author Wim Keirsgieter
 */
public class BuildCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    public BuildCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running BuildCpgPhase");
        String targetDir = Options.v().getString(OPT_TARGET_DIR);
        List<File> classFiles = Arrays.asList(SootUtil.getClassFiles(targetDir));
        if (classFiles.size() == 0) {
            return new PhaseResult(this, true, "No class files found in target directory");
        }
        int nrClasses = classFiles.size();
        log.debug("Loading {} classes", nrClasses);

        List<String> classNames = new ArrayList<>();
        for (File classFile : classFiles) {
            // TODO: package prefixes
            classNames.add(classFile.getName().replace(".class", ""));
        }

        SootUtil.loadClasses(classNames.toArray(new String[0]));

        for (int i = 0; i < nrClasses; i++) {
            log.debug("Building CPG of class '{}'", classNames.get(i));
            SootClass cls = Scene.v().loadClassAndSupport(classNames.get(i));
            CpgBuilder.buildCpg(cls, classFiles.get(i));
        }

        // TODO: display phase output directly from phase

        String details = String.format("| Nodes: %1$-89d |\n", CpgUtil.getNodeCount());
        details += String.format("| Edges: %1$-89d |\n", CpgUtil.getEdgeCount());
        return new PhaseResult(this, true, details);
    }

}
