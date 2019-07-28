package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.tagkit.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import graft.cpg.visitors.StmtVisitor;
import graft.db.GraphUtil;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

/**
 * Generate the control flow graph.
 *
 * @author Wim Keirsgieter
 */
public class CfgBuilder {

    private static Logger log = LoggerFactory.getLogger(CfgBuilder.class);

    /**
     * Build a CFG for the given method body, with an AST edge from the given class node.
     *
     * @param classNode the node corresponding to the declaring class of the method
     * @param body the method body
     */
    public static void buildCfg(Vertex classNode, Body body) {
        log.debug("Building CFG for method '{}'", body.getMethod().getName());

        // generate entry node and attach to class AST node
        Vertex entryNode = genCfgNode(null, ENTRY, body.getMethod().getName());
        if (body.getMethod().isConstructor()) {
            AstBuilder.genAstEdge(classNode, entryNode, CONSTRUCTOR, CONSTRUCTOR);
        } else {
            AstBuilder.genAstEdge(classNode, entryNode, METHOD, METHOD);
        }

        // generate control flow graph and add nodes recursively
        UnitGraph unitGraph = new BriefUnitGraph(body);
        for (Unit head : unitGraph.getHeads()) {
            Vertex headVertex = genCfgNodeAndSuccs(unitGraph, head);
            if (headVertex == null) {
                continue;
            }
            genCfgEdge(entryNode, headVertex, EMPTY, EMPTY);
        }
    }

    /**
     * Generate a CFG node with the given node type and text label properties.
     *
     * @param stmt the statement to generate a node for (contains source file info)
     * @param nodeType the node type of the CFG node
     * @param textLabel the text label of the CFG node
     * @return the generated CFG node
     */
    public static Vertex genCfgNode(Stmt stmt, String nodeType, String textLabel) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return g.addV(CFG_NODE)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .property(FILE_PATH, getSourcePath(stmt))
                .property(FILE_NAME, getSourceFile(stmt))
                .property(LINE_NO, getLineNr(stmt))
                .next();
    }

    /**
     * Generate a CFG edge between two nodes with the given edge type and text label properties.
     *
     * @param from the out-vertex of the CFG edge
     * @param to the in-vertex of the CFG edge
     * @param edgeType the type of the CFG edge
     * @param textLabel the text label of the CFG edge
     * @return the generated CFG edge
     */
    public static Edge genCfgEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return g.addE(CFG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

    // Recursively generate CFG nodes for a given unit and its children, with CFG edges between them
    private static Vertex genCfgNodeAndSuccs(UnitGraph unitGraph, Unit unit) {
        log.trace("Generating node and succs for unit '{}'", unit.toString());
        Stmt stmt = (Stmt) unit;
        StmtVisitor visitor = new StmtVisitor();
        stmt.apply(visitor);
        Vertex stmtVertex = (Vertex) visitor.getResult();

        if (stmtVertex == null) {
            log.trace("Stmt vertex is NULL");
            return null;
        }

        for (Unit succ : unitGraph.getSuccsOf(unit)) {
            Vertex succVertex = genCfgNodeAndSuccs(unitGraph, succ);
            if (succVertex == null) {
                continue;
            }
            // TODO: conditional edges
            if (stmt instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) stmt;
                if (succ.equals(ifStmt.getTarget())) {
                    genCfgEdge(stmtVertex, succVertex, TRUE, TRUE);
                } else {
                    genCfgEdge(stmtVertex, succVertex, FALSE, FALSE);
                }
            } else {
                genCfgEdge(stmtVertex, succVertex, EMPTY, EMPTY);
            }
        }

        return stmtVertex;
    }

    private static String getSourcePath(Stmt stmt) {
        if (stmt == null) {
            return UNKNOWN;
        }
        return UNKNOWN; // TODO
    }

    private static String getSourceFile(Stmt stmt) {
        if (stmt == null) {
            return UNKNOWN;
        }
        if (stmt.getTag("SourceFileTag") != null) {
            return ((SourceFileTag) stmt.getTag("SourceFileTag")).getSourceFile();
        }
        return UNKNOWN;
    }

    private static int getLineNr(Stmt stmt) {
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

}
