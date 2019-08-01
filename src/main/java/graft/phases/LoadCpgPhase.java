package graft.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.utils.GraphUtil;

/**
 * This phase loads the CPG (as currently in the database) from a file.
 *
 * @author Wim Keirsgieter
 */
public class LoadCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    private String cpgFile;

    public LoadCpgPhase(String cpgFile) {
        this.cpgFile = cpgFile;
    }

    @Override
    public PhaseResult run() {
        log.info("Running LoadCpgPhase...");
        try {
            GraphUtil.graph().traversal()
                    .io(cpgFile)
                    .read()
                    .iterate();

            String details = String.format("| CPG loaded from file %1$-75s |\n", cpgFile);
            return new PhaseResult(this, true, details);

        } catch (Exception e) {
            log.error("Could not load CPG from file '{}'", cpgFile, e);
            String details = String.format("| Could not load CPG from file %1$-67s |\n", cpgFile);
            return new PhaseResult(this, false, details);
        }
    }

}
