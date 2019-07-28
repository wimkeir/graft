package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.SootClass;

import graft.db.GraphUtil;
import graft.traversal.CpgTraversal;
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

        // get the class node if it already exists in the graph
        Vertex classNode;
        CpgTraversal classNodeTraversal = g.V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, CLASS)
                .has(FULL_NAME, cls.getName());
        if (classNodeTraversal.hasNext()) {
            classNode = (Vertex) classNodeTraversal.next();
        } else {
            classNode = AstBuilder.genAstNode(CLASS, cls.getShortName());
            CpgUtil.addNodeProperty(classNode, SHORT_NAME, cls.getShortName());
            CpgUtil.addNodeProperty(classNode, FULL_NAME, cls.getName());
        }

        CfgBuilder.buildCfg(classNode, body);
    }

}
