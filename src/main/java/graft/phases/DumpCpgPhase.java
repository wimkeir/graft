package graft.phases;

import graft.utils.GraphUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This phase dumps the CPG (as currently in the database) to a file.
 *
 * @author Wim Keirsgieter
 */
public class DumpCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(DotPhase.class);

    private String cpgFile;

    public DumpCpgPhase(String cpgFile) {
        this.cpgFile = cpgFile;
    }

    @Override
    public PhaseResult run() {
        log.info("Running DumpCpgPhase...");
        try {
            GraphUtil.graph().traversal()
                    .io(cpgFile)
                    .write()
                    .iterate();

            String details = String.format("| CPG written from file %1$-74s |\n", cpgFile);
            return new PhaseResult(this, true, details);

        } catch (Exception e) {
            log.error("Could not write CPG to file '{}'", cpgFile, e);
            String details = String.format("| Could not write CPG to file %1$-68s |\n", cpgFile);
            return new PhaseResult(this, false, details);
        }
    }

}
