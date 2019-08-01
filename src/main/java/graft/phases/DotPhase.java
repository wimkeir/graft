package graft.phases;

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

    public DotPhase(String dotFile) {
        this.dotFile = dotFile;
    }

    @Override
    public PhaseResult run() {
        log.info("Running DotPhase...");
        DotUtil.cpgToDot(dotFile, "cpg");
        String details = String.format("| Written to dotfile %1$-77s |\n", dotFile);
        return new PhaseResult(this, true, details);
    }

}
