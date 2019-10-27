package graft.cpg;

import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Graft;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Handles generations of interprocedural control and data flow edges.
 *
 * @author Wim Keirsgieter
 */
public class Interproc {

    private static Logger log = LoggerFactory.getLogger(Interproc.class);

    /**
     * Generate interprocedural call and return edges between call sites and method entries / returns.
     */
    public static void genInterprocEdges() {
        log.info("Generating interprocedural edges...");
        // TODO NB: context sensitivity
        CpgTraversalSource g = Graft.cpg().traversal();
        CpgTraversal invokeExprs = g.V()
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
            CpgTraversal ret = g.V(callSite).out(CFG_EDGE);
            Vertex retSite = null;
            if (ret.hasNext()) {
                retSite = (Vertex) ret.next();
            }

            // find entry node of method invoked
            CpgTraversal methodEntries = g.V()
                    .hasLabel(CFG_NODE)
                    .has(NODE_TYPE, ENTRY)
                    .has(METHOD_SIG, methodSig);

            if (methodEntries.hasNext()) {
                Vertex methodEntry = (Vertex) methodEntries.next();
                assert !methodEntries.hasNext();

                // generate call edge
                Graft.cpg().traversal()
                        .genCallEdge("")
                        .from(callSite).to(methodEntry)
                        .iterate();

                if (retSite != null) {
                    // generate ret edge
                    Vertex retStmt = Graft.cpg().traversal()
                            .V(methodEntry)
                            .repeat(out(CFG_EDGE)).until(has(NODE_TYPE, RETURN_STMT))
                            .next();
                    Graft.cpg().traversal()
                            .genRetEdge("")
                            .from(retStmt).to(retSite)
                            .iterate();
                } else {
                    log.warn("No ret site for call at vertex '{}'", callSite.value(TEXT_LABEL).toString());
                }

                genArgToParamEdges(callSite, methodEntry);
                genRetToCallEdges(callSite, methodEntry);
            }
        }
    }

    private static void genArgToParamEdges(Vertex callSite, Vertex methodEntry) {
        log.debug("Generating arg to param PDG edges for method '{}'...", methodEntry.value(METHOD_NAME).toString());
        CpgTraversalSource g = Graft.cpg().traversal();

        List<Vertex> params = new ArrayList<>();
        g.V().hasLabel(CFG_NODE)
                .has(NODE_TYPE, ASSIGN_STMT)
                .has(METHOD_SIG, methodEntry.value(METHOD_SIG).toString())
                .choose(
                        outE(AST_EDGE).has(EDGE_TYPE, VALUE).inV().values(NODE_TYPE).is(PARAM_REF),
                        sideEffect(x -> params.add(CpgUtil.getCfgRoot((Vertex) x.get())))
                ).iterate();

        for (Vertex param : params) {
            Graft.cpg().traversal()
                    .addArgDepE()
                    .from(callSite).to(param)
                    .iterate();
        }
    }

    private static void genRetToCallEdges(Vertex callSite, Vertex methodEntry) {
        log.debug("Generating ret to call PDG edges for method '{}'...", methodEntry.value(METHOD_NAME).toString());
        CpgTraversalSource g = Graft.cpg().traversal();

        List<Vertex> returns = g.V().hasLabel(CFG_NODE)
                .has(NODE_TYPE, RETURN_STMT)
                .has(METHOD_SIG, methodEntry.value(METHOD_SIG).toString())
                .toList();

        for (Vertex ret : returns) {
            if (g.V(ret).outE(AST_EDGE).count().next() > 0) {
                Graft.cpg().traversal()
                        .addRetDepE()
                        .from(ret).to(callSite)
                        .iterate();
            }
        }
    }

}
