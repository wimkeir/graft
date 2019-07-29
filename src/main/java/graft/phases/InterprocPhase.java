package graft.phases;

import graft.interproc.Interproc;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

public class InterprocPhase implements GraftPhase {

    public InterprocPhase(Configuration options) {
        // TODO
    }

    @Override
    public PhaseResult run() {
        Interproc.genInterprocCfgEdges();
        PhaseResult result = new PhaseResult(this, true);
        return result;
    }

    public static Configuration getOptions(Configuration config) {
        Configuration options = new BaseConfiguration();
        // TODO
        return options;
    }
}
