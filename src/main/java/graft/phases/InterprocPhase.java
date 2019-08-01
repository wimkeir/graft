package graft.phases;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.Interproc;

public class InterprocPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(GraftPhase.class);

    public InterprocPhase(Configuration options) {
        // TODO
    }

    @Override
    public PhaseResult run() {
        log.info("Running InterprocPhase...");
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
