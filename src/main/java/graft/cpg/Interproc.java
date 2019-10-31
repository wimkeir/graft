package graft.cpg;

import graft.GraftRuntimeException;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Graft;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Handles generations of interprocedural control and data flow edges.
 *
 * @author Wim Keirsgieter
 */
public class Interproc {

    private static Logger log = LoggerFactory.getLogger(Interproc.class);

    private static void callsTo(Vertex entry) {
        String methodSig = entry.value(METHOD_SIG);
        log.debug("Generating interproc edges for calls to {}", methodSig);

        // call sites
        CpgTraversal calls = Graft.cpg().traversal()
                .invokesOf(methodSig)
                .repeat(astIn()).until(label().is(CFG_NODE));
        long nrCalls = (long) calls.copy().count().next();

        if (nrCalls == 0) {
            // method is never invoked
            return;
        }

        // method return sites
        CpgTraversal returns = Graft.cpg().traversal()
                .returnsOf(methodSig);
        long nrReturns = (long) returns.copy().count().next();

        if (nrReturns == 0) {
            // this shouldn't happen
            return;
//            throw new GraftRuntimeException("No returns for method " + methodSig);
        }

        // the next node after the caller in the CFG
        CpgTraversal retSites = calls.copy().cfgOut(false);
        long nrRetSites = (long) retSites.copy().count().next();

        if (nrRetSites == 0) {
            // TODO: is this a problem?
            log.debug("No ret sites for method " + methodSig);
        }

        // generate call edges from callers to entry
        Graft.cpg().traversal()
                .genCallEdge("")
                .from(calls.copy())
                .to(entry)
                .iterate();

        // generate return edges from returns to ret sites
        if (nrRetSites > 0) {
            Graft.cpg().traversal()
                    .genRetEdge("")
                    .from(returns.copy())
                    .to(retSites.copy())
                    .iterate();
        }

        // generate arg to param edges
        Graft.cpg().traversal().V(entry)
                .astOut(STATEMENT)
                .has(NODE_TYPE, ASSIGN_STMT)
                .where(getVal().and(
                        values(NODE_TYPE).is(REF),
                        values(REF_TYPE).is(PARAM_REF)
                )).as("params")
                .addArgDepE()
                .from(calls.copy()).to("params")
                .iterate();

        // generate return value to call site edges
        returns.copy().where(astOut().count().is(P.gt(0)))
                .as("rets")
                .addRetDepE()
                .from("rets").to(calls.copy())
                .iterate();
    }

    /**
     * Generate interprocedural call and return edges between call sites and method entries / returns.
     */
    public static void genInterprocEdges() {
        log.info("Generating interprocedural edges...");
        // TODO NB: context sensitivity
        CpgTraversal entries = Graft.cpg().traversal().entries();
        while (entries.hasNext()) {
            callsTo((Vertex) entries.next());
        }
    }

}
