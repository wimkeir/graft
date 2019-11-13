package graft.cpg;

import java.util.NoSuchElementException;

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

    // TODO
    // this is disgusting and needs some serious refactoring
    // getting it to run faster would be great too (use traversals instead of loops)

    private static Logger log = LoggerFactory.getLogger(Interproc.class);

    private static void callsTo(Vertex calleeEntry) {
        String calleeSig = calleeEntry.value(METHOD_SIG);
        log.warn("Generating interproc edges for calls to {}", calleeSig);

        // call sites
        CpgTraversal calls = Graft.cpg().traversal()
                .invokesOf(calleeSig)
                .repeat(astIn()).until(label().is(CFG_NODE));
        long nrCalls = (long) calls.copy().count().next();
        log.warn("{} calls", nrCalls);
        if (nrCalls == 0) {
            // method is never invoked
            return;
        }

        // method return sites
        CpgTraversal rets = Graft.cpg().traversal().returnsOf(calleeSig);
        long nrReturns = (long) rets.copy().count().next();

        if (nrReturns == 0) {
            log.warn("No returns for method {}", calleeSig);
            //throw new GraftRuntimeException("No returns for method " + methodSig);
        }

        // the next node after the caller in the CFG
        CpgTraversal retSites = calls.copy().cfgOut(false);
        long nrRetSites = (long) retSites.copy().count().next();

        if (nrRetSites == 0) {
            // TODO: is this a problem?
            log.debug("No ret sites for method " + calleeSig);
        }

        callEdges(calleeEntry, calls.copy());
        argDepEdges(calleeEntry, calls.copy());
        retDepEdges(rets.copy(), calls.copy());
        if (nrRetSites > 0 && nrReturns > 0) {
            returnEdges(rets.copy(), retSites.copy());
        }
    }

    private static void handleCall(Vertex invokeExpr) {
        String calleeSig = invokeExpr.value(METHOD_SIG);

        // call site statement node
        Vertex callSite = Graft.cpg().traversal().V(invokeExpr)
                .repeat(astIn())
                .until(label().is(CFG_NODE))
                .next();

        // ret site statement node (or none)
        Vertex retSite;
        try {
            retSite = Graft.cpg().traversal().V(callSite).cfgOut()
                    .cfgOut()
                    .next();
        } catch (NoSuchElementException e) {
            log.warn("No ret site");
            retSite = null;
        }

        // callee entry node (return if DNE)
        Vertex calleeEntry;
        try {
            calleeEntry = (Vertex) Graft.cpg().traversal()
                    .entryOf(calleeSig)
                    .next();
        } catch (NoSuchElementException e) {
            // no callee entry - probably a library method
            return;
        }

        log.info("Handling call to {}", calleeSig);

        // call edge
        Graft.cpg().traversal()
                .genCallEdge()
                .from(callSite).to(calleeEntry)
                .iterate();

        // ret edges
        if (retSite != null) {
            Graft.cpg().traversal()
                    .returnsOf(calleeSig).as("rets")
                    .genRetEdge()
                    .from("rets").to(retSite)
                    .iterate();
        }

        // arg edges
        Graft.cpg().traversal()
                .paramsOf(calleeSig).as("params")
                .addArgDepE()
                .from(callSite).to("params")
                .iterate();

        // ret value edges
        Graft.cpg().traversal()
                .returnsOf(calleeSig).where(astOut().count().is(P.gt(0))).as("rets")
                .addRetDepE()
                .from("rets").to(callSite)
                .iterate();
    }

    @SuppressWarnings("unchecked")
    private static void callEdges(Vertex entry, CpgTraversal calls) {
        Graft.cpg().traversal()
                .genCallEdge()
                .from(calls).to(entry)
                .iterate();
    }

    @SuppressWarnings("unchecked")
    private static void returnEdges(CpgTraversal returns, CpgTraversal retSites) {
        Graft.cpg().traversal()
                .genRetEdge()
                .from(returns).to(retSites)
                .iterate();
    }

    private static void argDepEdges(Vertex entry, CpgTraversal calls) {
        Graft.cpg().traversal().V(entry)
                .astOut(STATEMENT).has(NODE_TYPE, ASSIGN_STMT)
                .where(getVal().and(
                        values(NODE_TYPE).is(REF),
                        values(REF_TYPE).is(PARAM_REF)
                )).as("params")
                .addArgDepE()
                .from(calls).to("params")
                .iterate();
    }

    @SuppressWarnings("unchecked")
    private static void retDepEdges(CpgTraversal returns, CpgTraversal calls) {
        returns.where(astOut().count().is(P.gt(0))).as("rets")
                .addRetDepE()
                .from("rets").to(calls)
                .iterate();
    }

    /**
     * Generate interprocedural call and return edges between call sites and method entries / returns.
     */
    public static void genInterprocEdges() {
        log.info("Generating interprocedural edges...");

        CpgTraversal entries = Graft.cpg().traversal().entries();
        while (entries.hasNext()) {
            callsTo((Vertex) entries.next());
        }

    }

    public static void genInterprocEdges(Vertex callerEntry) {
        String callerSig = callerEntry.value(METHOD_SIG);
        log.info("Generating interprocedural edges for method {}...", callerSig);

        CpgTraversal calls = Graft.cpg().traversal()
                .invokeExprs()
                .where(
                        repeat(astIn()).until(values(NODE_TYPE).is(ENTRY))
                        .values(METHOD_SIG).is(callerSig)
                );
        System.out.println("Calls from method: " + calls.copy().count().next());

        callsTo(callerEntry);

        while (calls.hasNext()) {
            Vertex call = (Vertex) calls.next();
            handleCall(call);
        }
    }

}
