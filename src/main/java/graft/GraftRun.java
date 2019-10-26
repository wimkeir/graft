package graft;

import java.util.ArrayList;
import java.util.List;

import graft.phases.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;

/**
 * A single run of the Graft analysis tool.
 *
 * Graft phases are registered to a Graft run, and are executed in sequence.
 *
 * @author Wim Keirsgieter
 */
public class GraftRun {

    private static Logger log = LoggerFactory.getLogger(GraftRun.class);

    private List<GraftPhase> phases;

    public GraftRun() {
        phases = new ArrayList<>();

        for (String phaseName : Options.v().getStringArray(OPT_PHASES)) {
            switch (phaseName) {
                case "AmendCpgPhase":
                    register(new AmendCpgPhase());
                    break;
                case "BuildCpgPhase":
                    register(new BuildCpgPhase());
                    break;
                case "DotPhase":
                    register(new DotPhase());
                    break;
                case "DumpCpgPhase":
                    register(new DumpCpgPhase());
                    break;
                case "LoadCpgPhase":
                    register(new LoadCpgPhase());
                    break;
                case "InterprocPhase":
                    register(new InterprocPhase());
                    break;
                case "TaintAnalysisPhase":
                    register(new TaintAnalysisPhase());
                    break;
                case "AliasAnalysisPhase":
                    register(new AliasAnalysisPhase());
                    break;
                default:
                    log.warn("Unrecognised phase name '{}' in config", phaseName);
            }
        }
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
     * Run all registered phases in sequence.
     */
    public void run() {
        for (GraftPhase phase : phases) {
            phase.run();
        }
    }

}