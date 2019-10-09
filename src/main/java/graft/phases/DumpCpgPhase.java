package graft.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.Graft;
import graft.Options;

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
    public void run() {
        log.info("Running DumpCpgPhase...");
        String filename = Options.v().getString(OPT_DB_DUMP_TO);
        Banner banner = new Banner();
        banner.println("DumpCpgPhase");
        banner.println("Filename: " + filename);
        try {
            Graft.cpg().traversal()
                    .io(filename)
                    .write()
                    .iterate();
            banner.println("CPG dumped to file");
        } catch (Exception e) {
            log.error("Could not write CPG to file '{}'", filename, e);
            banner.println("CPG could not be written to file");
        }
        banner.display();
    }

}
