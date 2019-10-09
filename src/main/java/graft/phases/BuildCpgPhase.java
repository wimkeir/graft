package graft.phases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import graft.Banner;
import graft.Options;
import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;
import graft.utils.FileUtil;
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
    public void run() {
        log.info("Running BuildCpgPhase");
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        Banner banner = new Banner();
        banner.println("BuildCpgPhase");
        banner.println("Target dir: " + targetDir);

        List<File> classFiles = SootUtil.getClassFiles(targetDir);
        if (classFiles.size() == 0) {
            banner.println("No class files in target dir");
            banner.display();
            return;
        }

        int nrClasses = classFiles.size();
        banner.println("Loading " + nrClasses + " classes:");

        List<String> classNames = new ArrayList<>();
        for (File classFile : classFiles) {
            String className = FileUtil.getClassName(targetDir, classFile);
            banner.println("- " + className);
            classNames.add(className);
        }

        SootUtil.loadClasses(classNames.toArray(new String[0]));

        for (int i = 0; i < nrClasses; i++) {
            log.debug("Building CPG of class '{}'", classNames.get(i));
            SootClass cls = Scene.v().loadClassAndSupport(classNames.get(i));
            CpgBuilder.buildCpg(cls, classFiles.get(i));
        }

        banner.println("CPG constructed successfully");
        banner.println("Nodes: " + CpgUtil.getNodeCount());
        banner.println("Edges: " + CpgUtil.getEdgeCount());
        banner.display();
    }

}
