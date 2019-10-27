package graft.cpg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import graft.Banner;
import graft.Graft;
import graft.Options;
import graft.traversal.CpgTraversalSource;
import graft.utils.FileUtil;
import graft.utils.SootUtil;


import static graft.Const.*;

/**
 * Handles the actual construction of the CPG.
 *
 * @author Wim Keirsgieter
 */
public class CpgBuilder {

    private static Logger log = LoggerFactory.getLogger(CpgBuilder.class);

    public static void buildCpg() {
        Vertex cpgRoot = (Vertex) Graft.cpg().traversal().cpgRoot().next();
        String targetDirName = Options.v().getString(OPT_TARGET_DIR);
        File targetDir = new File(targetDirName);

        Banner banner = new Banner();
        banner.println("CPG construction");
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
            Vertex classNode = buildCpg(cls, classFiles.get(i));
            AstBuilder.genAstEdge(cpgRoot, classNode, CLASS, CLASS);
        }

        banner.println("CPG constructed successfully");
        banner.println("Nodes: " + CpgUtil.getNodeCount());
        banner.println("Edges: " + CpgUtil.getEdgeCount());
        banner.display();
    }

    public static Vertex buildCpg(SootClass cls, File classFile) {
        // TODO: how does this handle interfaces, extensions, enums etc?
        Vertex classNode = AstBuilder.genAstNode(CLASS, cls.getShortName());
        CpgUtil.addNodeProperty(classNode, SHORT_NAME, cls.getShortName());
        CpgUtil.addNodeProperty(classNode, FULL_NAME, cls.getName());
        CpgUtil.addNodeProperty(classNode, FILE_NAME, classFile.getName());
        CpgUtil.addNodeProperty(classNode, FILE_PATH, classFile.getPath());
        CpgUtil.addNodeProperty(classNode, FILE_HASH, FileUtil.hashFile(classFile));

        for (SootMethod method : cls.getMethods()) {
            try {
                Body body = method.retrieveActiveBody();
                Vertex methodEntry = buildCpg(body);
                if (method.isConstructor()) {
                    AstBuilder.genAstEdge(classNode, methodEntry, CONSTRUCTOR, CONSTRUCTOR);
                } else {
                    AstBuilder.genAstEdge(classNode, methodEntry, METHOD, METHOD);
                }
            } catch (RuntimeException e) {
                log.warn("No body for method '{}'", method.getSignature(), e);
            }
        }

        return classNode;
    }

    /**
     * Build a CPG for the given method body.
     *
     * @param body the method body
     */
    public static Vertex buildCpg(Body body) {
        UnitGraph unitGraph = new BriefUnitGraph(body);
        Map<Unit, Vertex> unitNodes = new HashMap<>();
        Vertex methodEntry = CfgBuilder.buildCfg(unitGraph, unitNodes);
        PdgBuilder.buildPdg(unitGraph, unitNodes);

        // TODO: make sure to do this everywhere its needed
        if (Options.v().getString(OPT_DB_IMPLEMENTATION).equals(NEO4J)) {
            Graft.cpg().commit();
        }

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
        AstBuilder.genAstEdge(cpgRoot, classNode, CLASS, CLASS);
    }

    public static void genCpgRoot(String projName, String targetDir, String classpath) {
        CpgTraversalSource g = Graft.cpg().traversal();
        assert g.V().count().next() == 0; // XXX
        g.addV(CPG_ROOT)
                .property(NODE_TYPE, CPG_ROOT)
                .property(TEXT_LABEL, projName)
                .property(PROJECT_NAME, projName)
                .property(TARGET_DIR, targetDir)
                .property(CLASSPATH, classpath)
                .next();
    }

}
