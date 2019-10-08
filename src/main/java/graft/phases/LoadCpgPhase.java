package graft.phases;

import graft.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.db.GraphUtil;

import static graft.Const.*;

/**
 * This phase loads the CPG (as currently in the database) from a file.
 *
 * @author Wim Keirsgieter
 */
public class LoadCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    public LoadCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running LoadCpgPhase...");
        String filename = Options.v().getString(OPT_GENERAL_GRAPH_FILE);
        try {
            GraphUtil.graph().traversal()
                    .io(filename)
                    .read()
                    .iterate();

            String details = String.format("| CPG loaded from file %1$-75s |\n", filename);
            return new PhaseResult(this, true, details);

        } catch (Exception e) {
            log.error("Could not load CPG from file '{}'", filename, e);
            String details = String.format("| Could not load CPG from file %1$-67s |\n", filename);
            return new PhaseResult(this, false, details);
        }
    }

}
