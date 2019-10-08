package graft.phases;

import graft.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.utils.DotUtil;

import static graft.Const.*;

/**
 * This phase writes the CPG (as currently in the database) to a dot file for visualisation.
 *
 * @author Wim Keirsgieter
 */
public class DotPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    public DotPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running DotPhase...");
        String filename = Options.v().getString(OPT_GENERAL_DOT_FILE);
        DotUtil.cpgToDot(filename, "cpg");
        String details = String.format("| Written to dotfile %1$-77s |\n", filename);
        return new PhaseResult(this, true, details);
    }

}
