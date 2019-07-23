package graft.phases;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftException;
import graft.cpg.CpgBuilder;

/**
 * This phase handles the actual construction of the CPG.
 *
 * @author Wim Keirsgieter
 */
public class CpgBuildPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(CpgBuildPhase.class);

    private CpgBuilder cpgBuilder;

    public CpgBuildPhase(String srcRoot, Configuration options) {
        cpgBuilder = new CpgBuilder(srcRoot, options);
    }

    @Override
    public PhaseResult run() {
        log.info("Running CpgBuildPhase...");
        PhaseResult result;
        try {
            cpgBuilder.buildCpg();
            result = new PhaseResult(this, true);
        } catch (GraftException e) {
            result = new PhaseResult(this, false);
        }
        log.info("CpgBuildPhase complete");
        return result;
    }

    public static Configuration getOptions(Configuration config) {
        Configuration options = new BaseConfiguration();
        options.setProperty("exclusions", config.getList("cpg.exlusions"));
        return options;
    }

}
