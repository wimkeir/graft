package graft;

import java.util.ArrayList;
import java.util.List;

import graft.phases.*;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private List<GraftPhase> phases;
    private int nrDotPhases = 0;
    private int nrDumpPhases = 0;

    public GraftRun(Configuration config) {
        phases = new ArrayList<>();

        for (String phaseName : config.getStringArray("phases.phase")) {
            switch (phaseName) {
                case "BuildCpgPhase":
                    register(new BuildCpgPhase(BuildCpgPhase.getOptions(config)));
                    break;
                case "DotPhase":
                    register(new DotPhase(getDotFilename(config.getString("general.dot-file"))));
                    break;
                case "DumpCpgPhase":
                    register(new DumpCpgPhase(getDumpFilename(config.getString("general.graph-file"))));
                    break;
                case "LoadCpgPhase":
                    register(new LoadCpgPhase(config.getString("general.graph-file")));
                    break;
                case "InterprocPhase":
                    register(new InterprocPhase(InterprocPhase.getOptions(config)));
                    break;
                case "TaintAnalysisPhase":
                    register(new TaintAnalysisPhase(TaintAnalysisPhase.getOptions(config)));
                    break;
                default:
                    log.warn("Unrecognised phase name '{}' in config");
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

    private String getDotFilename(String dotFile) {
        if (nrDotPhases == 0) {
            nrDotPhases++;
            return dotFile;
        }

        String[] splitFile = dotFile.split("[.]");
        splitFile[splitFile.length - 2] += "_" + nrDotPhases++ + ".";
        StringBuilder sb = new StringBuilder();
        for (String s : splitFile) sb.append(s);
        return sb.toString();
    }

    private String getDumpFilename(String graphFile) {
        if (nrDumpPhases == 0) {
            nrDumpPhases++;
            return graphFile;
        }

        String[] splitFile = graphFile.split("[.]");
        splitFile[splitFile.length - 2] += "_" + nrDotPhases++ + ".";
        StringBuilder sb = new StringBuilder();
        for (String s : splitFile) sb.append(s);
        return sb.toString();
    }

}