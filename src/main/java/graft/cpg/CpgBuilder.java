package graft.cpg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import graft.*;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import graft.traversal.CpgTraversalSource;
import graft.utils.SootUtil;

import static graft.Const.*;
import static graft.utils.FileUtil.*;

/**
 * Handles the actual construction of the CPG.
 *
 * @author Wim Keirsgieter
 */
public class CpgBuilder {

    private Banner banner;

    private static Logger log = LoggerFactory.getLogger(CpgBuilder.class);

    public CpgBuilder() {
        banner = new Banner("CPG construction");
    }

    public void buildCpg(String targetDir) {
        log.info("Building CPG");
        log.info("Target directory: {})", targetDir);

        long start = System.currentTimeMillis();
        try {
            SootUtil.configureSoot();

            banner.println("Target directory: " + targetDir);
            File target = getTarget(targetDir);
            List<File> classFiles = getClassFiles(target);

            int nrClasses = classFiles.size();
            log.info("{} class(es) to load", nrClasses);
            banner.println(nrClasses + " class(es)");

            List<String> classNames = classFiles.stream()
                    .map(file -> getClassName(target, file))
                    .collect(Collectors.toList());

            log.info("Loading class(es)...");
            SootUtil.loadClasses(classNames.toArray(new String[0]));
            long loaded = System.currentTimeMillis();

            for (int i = 0; i < nrClasses; i++) {
                String className = classNames.get(i);
                File classFile = classFiles.get(i);

                log.debug("Building CPG for class '{}'...", className);
                SootClass cls = Scene.v().loadClassAndSupport(className);
                Vertex classNode = buildCpg(cls, classFile);

                // TODO: add edge from package, not root
                log.debug("Adding class edge");
                Graft.cpg().traversal()
                        .cpgRoot().as("root")
                        .addAstE(CLASS, CLASS)
                        .from("root").to(classNode)
                        .iterate();
            }
            long built = System.currentTimeMillis();

            log.info("Individual CPGs loaded, adding interprocedural edges...");
            Interproc.genInterprocEdges();
            long interproc = System.currentTimeMillis();


            banner.println("CPG constructed successfully");

            // performance stats
            long timeToLoad = loaded - start;
            long timeToBuild = built - loaded;
            long timeToInter = interproc - built;
            banner.println("Class(es) loaded in " + displayMillis(timeToLoad));
            banner.println("Individual CPGs built in " + displayMillis(timeToBuild));
            banner.println("Interprocedural edges generated in " + displayMillis(interproc));
            banner.println("Total elapsed time: " + displayMillis(timeToLoad + timeToBuild + timeToInter));

            // CPG stats
            long nrNodes = Graft.cpg().traversal().V().count().next();
            long nrEdges = Graft.cpg().traversal().E().count().next();
            long nrMethods = (long) Graft.cpg().traversal().entries().count().next();
            banner.println(nrMethods + " methods in " + nrClasses + " classes");
            banner.println(nrNodes + " nodes");
            banner.println(nrEdges + " edges");

        } catch (GraftRuntimeException e) {
            banner.println(e.getClass().getName() + " during CPG construction");
            banner.println(e.getMessage());
        }

        banner.display();
    }

    private File getTarget(String targetDir) {
        File target = new File(targetDir);

        if (!target.exists()) {
            throw new GraftRuntimeException("Target directory '" + targetDir + "' does not exist");
        }
        if (!target.isDirectory()) {
            throw new GraftRuntimeException("Target directory '" + targetDir + "' is not a directory");
        }

        return target;
    }

    private List<File> getClassFiles(File target) {
        List<File> classFiles = SootUtil.getClassFiles(target);

        if (classFiles.size() == 0) {
            throw new GraftRuntimeException("No class files in target directory");
        }

        return classFiles;
    }

    private static String displayMillis(long millis) {
        // TODO
        return millis + "ms";
    }

    public static Vertex buildCpg(SootClass cls, File classFile) {
        String fileHash = UNKNOWN;
        try {
            fileHash = hashFile(classFile);
        } catch (GraftException e) {
            log.warn("Could not has file '{}'", classFile.getName(), e);
        }

        Vertex classNode = (Vertex) Graft.cpg().traversal()
                .addAstV(CLASS, cls.getShortName())
                .property(SHORT_NAME, cls.getShortName())
                .property(FULL_NAME, cls.getName())
                .property(FILE_NAME, classFile.getName())
                .property(FILE_PATH, classFile.getPath())
                .property(FILE_HASH, fileHash)
                .next();

        log.debug("{} methods in class", cls.getMethodCount());
        for (SootMethod method : cls.getMethods()) {
            Body body;
            try {
                body = method.retrieveActiveBody();
            } catch (RuntimeException e) {
                // no active bodies for interface methods, abstract methods etc.
                log.debug(e.getMessage());
                continue;
            }
            if (body == null) continue;
            try {
                Vertex methodEntry = buildCpg(body);
                if (method.isConstructor()) {
                    Graft.cpg().traversal()
                            .addAstE(CONSTRUCTOR, CONSTRUCTOR)
                            .from(classNode).to(methodEntry)
                            .iterate();
                } else {
                    Graft.cpg().traversal()
                            .addAstE(METHOD, METHOD)
                            .from(classNode).to(methodEntry)
                            .iterate();
                }
            } catch (SootMethodRefImpl.ClassResolutionFailedException e) {
                // TODO: reason...
                log.debug(e.getMessage());
            }
        }

        Graft.cpg().commit();

        return classNode;
    }

    /**
     * Build a CPG for the given method body.
     *
     * @param body the method body
     */
    public static Vertex buildCpg(Body body) {
        UnitGraph unitGraph = new BriefUnitGraph(body);
        CfgBuilder cfgBuilder = new CfgBuilder(unitGraph, new AstBuilder());
        Vertex methodEntry = cfgBuilder.buildCfg();
        PdgBuilder.buildPdg(unitGraph, cfgBuilder.generatedNodes());

        return methodEntry;
    }

    public static void amendCpg() {
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        Banner banner = new Banner();
        banner.println("Amending CPG");
        banner.println("Target dir: " + targetDir);

        List<File> classFiles = SootUtil.getClassFiles(targetDir);
        if (classFiles.size() == 0) {
            banner.println("No class files in target dir");
            banner.display();
            return;
        }

        List<File> amendedClasses = amendedClasses();
        List<String> classNames = new ArrayList<>();
        banner.println("Files changed:");
        for (File classFile : amendedClasses) {
            String className = getClassName(targetDir, classFile);
            banner.println("- " + classFile.getName() + " (" + className + ")");
            classNames.add(className);
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

        Interproc.genInterprocEdges();

        Graft.cpg().commit();

        banner.println("CPG amended successfully");
        banner.println("Nodes: " + CpgUtil.getNodeCount() + " (prev " + prevNodes + ")");
        banner.println("Edges: " + CpgUtil.getEdgeCount() + " (prev " + prevEdges + ")");
        banner.display();
    }

    public static void amendCpg(Vertex cpgRoot, SootClass cls, File classFile) {
        CpgTraversalSource g = Graft.cpg().traversal();
        g.V().hasLabel(AST_NODE)
                .has(NODE_TYPE, CLASS)
                .has(FULL_NAME, cls.getName())
                .drop()
                .iterate();

        for (SootMethod method : cls.getMethods()) {
            CpgUtil.dropCfg(method);
        }

        Vertex classNode = buildCpg(cls, classFile);
        Graft.cpg().traversal()
                .addAstE(CLASS, CLASS)
                .from(cpgRoot).to(classNode)
                .iterate();

        Graft.cpg().commit();
    }

    public static List<File> amendedClasses() {
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        List<File> classFiles = SootUtil.getClassFiles(targetDir);

        List<File> amendedClasses = new ArrayList<>();
        for (File classFile : classFiles) {
            String className = getClassName(targetDir, classFile);
            try {
                String hash = hashFile(classFile);
                if (!hash.equals(CpgUtil.getClassHash(className))) {
                    amendedClasses.add(classFile);
                }
            } catch (GraftException e) {
                log.warn("Could not hash file '{}'", classFile.getName(), e);
            }
        }

        return amendedClasses;
    }

}
