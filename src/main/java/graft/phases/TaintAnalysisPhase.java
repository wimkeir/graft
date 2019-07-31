package graft.phases;

import graft.analysis.taint.TaintAnalysis;
import org.apache.commons.configuration2.Configuration;

public class TaintAnalysisPhase implements GraftPhase {

    public TaintAnalysisPhase(Configuration options) {
        // TODO
    }

    @Override
    public PhaseResult run() {
        TaintAnalysis taintAnalysis = new TaintAnalysis();
        taintAnalysis.doAnalysis(null);
        return null;
    }

    public static Configuration getOptions(Configuration config) {
        return null;    // TODO
    }

}
