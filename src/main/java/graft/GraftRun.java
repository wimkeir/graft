package graft;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.phases.GraftPhase;
import graft.phases.PhaseResult;

/**
 * A single run of the Graft analysis tool.
 *
 * Graft phases are registered to a Graft run, and are executed in sequence with their results being
 * gathered in the Graft run's result.
 *
 * @author Wim Keirsgieter
 */
public class GraftRun {

    private static Logger log = LoggerFactory.getLogger(GraftRun.class);

    private GraftConfig config;
    private List<GraftPhase> phases;

    public GraftRun(GraftConfig config) {
        this.config = config;
        phases = new ArrayList<>();
    }

    /**
     * Register phases to be run in sequence.
     *
     * @param phases the phases to be run
     */
    public void register(GraftPhase... phases) {
        for (GraftPhase phase : phases) {
            log.info("Registering new {}", phase.getClass().getSimpleName());
            this.phases.add(phase);
        }
    }

    /**
     * Run all registered phases in sequence, gathering their individual phase results in the Graft
     * result returned.
     *
     * @return the result of the Graft run
     */
    public GraftResult run() {
        GraftResult result = new GraftResult();
        for (GraftPhase phase : phases) {
            PhaseResult phaseResult = phase.run();
            result.addPhaseResult(phaseResult);
        }
        return result;
    }

}