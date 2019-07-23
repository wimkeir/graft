package graft.phases;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.utils.DotUtil;

/**
 * This phase writes the CPG (as currently in the database) to a dot file for visualisation.
 *
 * @author Wim Keirsgieter
 */
public class DotPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    private String dotFile;

    public DotPhase(Configuration options) {
        dotFile = options.getString("dot-file");
    }

    @Override
    public PhaseResult run() {
        log.info("Running DotPhase...");
        DotUtil.cpgToDot(dotFile, "cpg");
        // TODO: handle failure
        log.info("DotPhase complete");
        return new PhaseResult(this, true);
    }

    public static Configuration getOptions(Configuration config) {
        Configuration options = new BaseConfiguration();
        options.setProperty("dot-file", config.getString("general.dot-file"));
        return options;
    }

}
