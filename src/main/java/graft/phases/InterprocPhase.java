package graft.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.Interproc;

/**
 * This phase handles the generation of interprocedural CFG and PDG edges.
 *
 * @author Wim Keirsgieter
 */
public class InterprocPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(GraftPhase.class);

    public InterprocPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running InterprocPhase...");
        Interproc.genInterprocEdges();
        return new PhaseResult(this, true, "");
    }

}
