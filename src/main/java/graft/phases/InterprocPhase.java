package graft.phases;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import graft.cpg.Interproc;

public class InterprocPhase implements GraftPhase {

    public InterprocPhase(Configuration options) {
        // TODO
    }

    @Override
    public PhaseResult run() {
        Interproc.genInterprocEdges();
        PhaseResult result = new PhaseResult(this, true);
        return result;
    }

    public static Configuration getOptions(Configuration config) {
        Configuration options = new BaseConfiguration();
        // TODO
        return options;
    }
}
