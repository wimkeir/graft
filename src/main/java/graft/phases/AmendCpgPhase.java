package graft.phases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import graft.Banner;
import graft.Graft;
import graft.Options;
import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;
import graft.utils.FileUtil;
import graft.utils.SootUtil;

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
    public void run() {
        log.info("Running AmendCpgPhase...");
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        Banner banner = new Banner();
        banner.println("AmendCpgPhase");
        banner.println("Target dir: " + targetDir);

        List<File> classFiles = SootUtil.getClassFiles(targetDir);
        if (classFiles.size() == 0) {
            banner.println("No class files in target dir");
            banner.display();
            return;
        }

        List<File> amendedClasses = new ArrayList<>();
        List<String> classNames = new ArrayList<>();
        banner.println("Files changed:");
        for (File classFile : classFiles) {
            String className = FileUtil.getClassName(targetDir, classFile);
            String hash = FileUtil.hashFile(classFile);
            if (!hash.equals(CpgUtil.getClassHash(className))) {
                log.info("Class '{}' has been changed", className);
                banner.println("- " + classFile.getName() + " (" + className + ")");
                amendedClasses.add(classFile);
                classNames.add(className);
            }
        }

        if (amendedClasses.size() == 0) {
            banner.println("No files changed - nothing to do");
            banner.display();
            return;
        }
        banner.println(amendedClasses.size() + " classes to amend");

        Vertex cpgRoot = Graft.cpg().traversal().V().hasLabel(CPG_ROOT).next();
        SootUtil.loadClasses(classNames.toArray(new String[0]));
        long prevNodes = CpgUtil.getNodeCount();
        long prevEdges = CpgUtil.getEdgeCount();

        for (int i = 0; i < amendedClasses.size(); i++) {
            log.debug("Amending CPG of class '{}'", classNames.get(i));
            SootClass cls = Scene.v().loadClassAndSupport(classNames.get(i));
            CpgBuilder.amendCpg(cpgRoot, cls, amendedClasses.get(i));
        }

        banner.println("CPG amended successfully");
        banner.println("Nodes: " + CpgUtil.getNodeCount() + " (prev " + prevNodes + ")");
        banner.println("Edges: " + CpgUtil.getEdgeCount() + " (prev " + prevEdges + ")");
        banner.display();
    }

}
