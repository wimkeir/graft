package graft.cpg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import graft.traversal.CpgTraversalSource;
import graft.utils.SootUtil;

import graft.*;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.traversal.__.*;
import static graft.utils.DisplayUtil.*;
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

    /**
     * Build a CPG for the program in the given target directory.
     *
     * @param targetDir the target directory of the program
     */
    public void buildCpg(String targetDir) {
        log.info("Building CPG");
        log.info("Target directory: {})", targetDir);
        banner.println("Target directory: " + targetDir);

        long start = System.currentTimeMillis();
        try {

            // get class files in target director
            File target = getTarget(targetDir);
            List<File> classFiles = getClassFiles(target);
            int nrClasses = classFiles.size();
            if (nrClasses == 0) {
                banner.println("No class files in target dir");
                banner.display();
                return;
            }
            log.info("{} class(es) to load", nrClasses);
            banner.println(nrClasses + " class(es)");

            // get class names from class files
            List<String> classNames = classFiles.stream()
                    .map(file -> getClassName(target, file))
                    .collect(Collectors.toList());

            // load classes into Scene
            log.info("Loading class(es)...");
            SootUtil.loadClasses(classNames.toArray(new String[0]));
            long loaded = System.currentTimeMillis();

            // build CPGs for all classes
            for (int i = 0; i < nrClasses; i++) {
                String className = classNames.get(i);
                File classFile = classFiles.get(i);

                // load class from Scene
                log.debug("Building CPG for class '{}'...", className);
                SootClass cls = Scene.v().loadClassAndSupport(className);

                // build class CPG
                Vertex classNode = buildCpg(cls, classFile);

                // class edge from root or package node to class node
                log.debug("Adding class edge");
                String packageName = cls.getPackageName();
                System.out.println("Class " + cls.getName() + ", package " + cls.getPackageName());
                Vertex packageNode = packageNodes(packageName);
                Graft.cpg().traversal()
                        .addAstE(CLASS, CLASS)
                        .from(packageNode).to(classNode)
                        .iterate();
            }
            long built = System.currentTimeMillis();

            // generate interproc edges
            log.info("Individual CPGs loaded, adding interprocedural edges...");
            Interproc.genInterprocEdges();
            long interproc = System.currentTimeMillis();

            banner.println("CPG constructed successfully");

            // performance stats
            long timeToLoad = loaded - start;
            long timeToBuild = built - loaded;
            long timeToInter = interproc - built;
            banner.println("Class(es) loaded in " + displayTime(timeToLoad));
            banner.println("Individual CPGs built in " + displayTime(timeToBuild));
            banner.println("Interprocedural edges generated in " + displayTime(timeToInter));
            banner.println("Total elapsed time: " + displayTime(timeToLoad + timeToBuild + timeToInter));

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

    /**
     * Update the CPG given changes in the configured target directory.
     */
    public void amendCpg() {
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        banner = new Banner();
        banner.println("Amending CPG");
        banner.println("Target dir: " + targetDir);

        List<File> classFiles = getClassFiles(targetDir);
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

        long start = System.currentTimeMillis();

        Vertex cpgRoot = Graft.cpg().traversal().V().hasLabel(CPG_ROOT).next();
        SootUtil.loadClasses(classNames.toArray(new String[0]));
        long prevNodes = Graft.cpg().nrV();
        long prevEdges = Graft.cpg().nrE();

        for (int i = 0; i < amendedClasses.size(); i++) {
            log.debug("Amending CPG of class '{}'", classNames.get(i));
            SootClass cls = Scene.v().loadClassAndSupport(classNames.get(i));
            CpgBuilder.amendCpg(cpgRoot, cls, amendedClasses.get(i));
        }

        Graft.cpg().commit();

        banner.println("CPG amended successfully in " + (System.currentTimeMillis() - start) + "ms");
        banner.println("Nodes: " + Graft.cpg().nrV() + " (prev " + prevNodes + ")");
        banner.println("Edges: " + Graft.cpg().nrE() + " (prev " + prevEdges + ")");
        banner.display();
    }

    /**
     * Get a list of all classes changed since CPG construction.
     *
     * @return a list of changed classes
     */
    public static List<File> amendedClasses() {
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        List<File> classFiles = getClassFiles(targetDir);

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

    private static Vertex buildCpg(SootClass cls, File classFile) {
        String fileHash = UNKNOWN;
        try {
            fileHash = hashFile(classFile);
        } catch (GraftException e) {
            log.warn("Could not hash file '{}'", classFile.getName(), e);
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
                log.debug("No active body for method {}", method.getSignature());
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
                log.debug("Class resolution failed for method {}", method.getSignature());
            }
        }

        Graft.cpg().commit();

        return classNode;
    }

    private static Vertex buildCpg(Body body) {
        UnitGraph unitGraph = new BriefUnitGraph(body);
        CfgBuilder cfgBuilder = new CfgBuilder(unitGraph, new AstBuilder());
        Vertex methodEntry = cfgBuilder.buildCfg();
        PdgBuilder.buildPdg(unitGraph, cfgBuilder.generatedNodes());
        return methodEntry;
    }

    private static void amendCpg(Vertex cpgRoot, SootClass cls, File classFile) {
        CpgTraversalSource g = Graft.cpg().traversal();
        g.V().hasLabel(AST_NODE)
                .has(NODE_TYPE, CLASS)
                .has(FULL_NAME, cls.getName())
                .drop()
                .iterate();

        for (SootMethod method : cls.getMethods()) {
            CpgUtil.dropMethod(method.getSignature());
        }
        CpgUtil.dropClass(cls.getName());

        Vertex classNode = buildCpg(cls, classFile);
        Graft.cpg().traversal()
                .addAstE(CLASS, CLASS)
                .from(cpgRoot).to(classNode)
                .iterate();

        // entry nodes of methods in amended classes
        CpgTraversal entries = Graft.cpg().traversal().V()
                .and(
                        V(classNode).astOut(METHOD).store("entries"),
                        V(classNode).astOut(CONSTRUCTOR).store("entries")
                ).cap("entries").unfold().dedup();
        while (entries.hasNext()) {
            Vertex entry = (Vertex) entries.next();
            Interproc.genInterprocEdges(entry);
        }

        Graft.cpg().commit();
    }

    private static File getTarget(String targetDir) {
        File target = new File(targetDir);

        if (!target.exists()) {
            throw new GraftRuntimeException("Target directory '" + targetDir + "' does not exist");
        }
        if (!target.isDirectory()) {
            throw new GraftRuntimeException("Target directory '" + targetDir + "' is not a directory");
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private static Vertex packageNodes(String packageName) {
        String[] packages = packageName.split("\\.");
        Vertex prev = Graft.cpg().root();
        for (String pack : packages) {
            if (pack.length() == 0) continue;
            Vertex packNode = (Vertex) Graft.cpg().traversal().V()
                    .coalesce(
                            packageOf(pack),
                            addPackage(pack)
                    ).next();
            Graft.cpg().traversal()
                    .addAstE(PACKAGE, PACKAGE)
                    .from(prev).to(packNode)
                    .iterate();
            prev = packNode;
        }
        return prev;
    }

}
