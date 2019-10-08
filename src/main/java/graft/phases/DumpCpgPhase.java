package graft.phases;

import graft.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.db.GraphUtil;

import static graft.Const.*;

/**
 * This phase dumps the CPG (as currently in the database) to a file.
 *
 * @author Wim Keirsgieter
 */
public class DumpCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    public DumpCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running DumpCpgPhase...");
        String filename = Options.v().getString(OPT_GENERAL_GRAPH_FILE);
        try {
            GraphUtil.graph().traversal()
                    .io(filename)
                    .write()
                    .iterate();

            String details = String.format("| CPG written to file %1$-76s |\n", filename);
            return new PhaseResult(this, true, details);

        } catch (Exception e) {
            log.error("Could not write CPG to file '{}'", filename, e);
            String details = String.format("| Could not write CPG to file %1$-68s |\n", filename);
            return new PhaseResult(this, false, details);
        }
    }

}
