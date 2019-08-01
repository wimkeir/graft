package graft.cpg;

import java.util.ArrayList;
import java.util.List;

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
    public static void genInterprocEdges() {
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

                genArgToParamEdges(callSite, methodEntry);
                genRetToCallEdges(callSite, methodEntry);
            }
        }
    }

    private static void genArgToParamEdges(Vertex callSite, Vertex methodEntry) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        List<Vertex> params = new ArrayList<>();
        g.V(methodEntry)
                .repeat(
                        out(CFG_EDGE)
                        .choose(
                                values(NODE_TYPE).is(ASSIGN_STMT),
                                choose(
                                        outE(AST_EDGE).has(EDGE_TYPE, VALUE).inV().values(NODE_TYPE).is(PARAM_REF),
                                        sideEffect(x -> params.add(CpgUtil.getCfgRoot(x.get())))
                                )
                        )
                ).until(values(NODE_TYPE).is(RETURN_STMT)).iterate();

        for (int i = 0; i < params.size(); i++) {
            PdgBuilder.genDataDepEdge(callSite, params.get(i), ARG, ARG);
        }
    }

    private static void genRetToCallEdges(Vertex callSite, Vertex methodEntry) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        List<Vertex> returns = g.V(methodEntry)
                .repeat(out(CFG_EDGE))
                .until(values(NODE_TYPE).is(RETURN_STMT))
                .toList();

        for (Vertex ret : returns) {
            if (g.V(ret).outE(AST_EDGE).count().next() > 0) {
                PdgBuilder.genDataDepEdge(ret, callSite, RET, RET);
            }
        }
    }

}
