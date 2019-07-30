package graft.cpg;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Handles generations of interprocedural control and data flow edges.
 *
 * @author Wim Keirsgieter
 */
public class Interproc {

    /**
     * Generate interprocedural call and return edges between call sites and method entries / returns.
     */
    public static void genInterprocCfgEdges() {
        // TODO NB: context sensitivity
        GraphTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        GraphTraversal invokeExprs = g.V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, INVOKE_EXPR);

        while (invokeExprs.hasNext()) {
            Vertex invokeExpr = (Vertex) invokeExprs.next();
            String methodSig = invokeExpr.value(METHOD_SIG).toString();

            // find CFG root of invoke expression
            Vertex callSite = g.V(invokeExpr)
                    .repeat(in(AST_EDGE))
                    .until(hasLabel(CFG_NODE))
                    .next();
            Vertex retSite = g.V(callSite).out(CFG_EDGE).next();

            // find entry node of method invoked
            GraphTraversal methodEntries = g.V()
                    .hasLabel(CFG_NODE)
                    .has(NODE_TYPE, ENTRY)
                    .has(METHOD_SIG, methodSig);

            if (methodEntries.hasNext()) {
                Vertex methodEntry = (Vertex) methodEntries.next();
                assert !methodEntries.hasNext();

                // generate call edge
                CfgBuilder.genCfgEdge(callSite, methodEntry, CALL, CALL);

                // generate ret edge
                Vertex retStmt = g.V(methodEntry).repeat(out(CFG_EDGE)).until(has(NODE_TYPE, RETURN_STMT)).next();
                CfgBuilder.genCfgEdge(retStmt, retSite, RET, RET);
            }
        }
    }
}
