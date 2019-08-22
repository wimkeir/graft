package graft.cpg;

import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.SootClass;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import graft.db.GraphUtil;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

/**
 * Handles the actual construction of the CPG.
 *
 * @author Wim Keirsgieter
 */
public class CpgBuilder {

    private static Logger log = LoggerFactory.getLogger(CpgBuilder.class);

    /**
     * Build a CPG for the given method body.
     *
     * @param body the method body
     */
    public static void buildCpg(Body body) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        SootClass cls = body.getMethod().getDeclaringClass();

        // create an AST node for the method's declaring class if it doesn't exist
        long count = g.V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, CLASS)
                .has(FULL_NAME, cls.getName())
                .count().next();
        if (count == 0) {
            Vertex classNode = AstBuilder.genAstNode(CLASS, cls.getShortName());
            CpgUtil.addNodeProperty(classNode, SHORT_NAME, cls.getShortName());
            CpgUtil.addNodeProperty(classNode, FULL_NAME, cls.getName());
        }

        UnitGraph unitGraph = new BriefUnitGraph(body);
        Map<Unit, Object> unitNodes = new HashMap<>();
        CfgBuilder.buildCfg(unitGraph, unitNodes);
        PdgBuilder.buildPdg(unitGraph, unitNodes);
    }

}
