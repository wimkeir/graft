package graft.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.Graft;
import graft.Options;
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
    public void run() {
        log.info("Running LoadCpgPhase...");
        String filename = Options.v().getString(OPT_GENERAL_GRAPH_FILE);
        Banner banner = new Banner();
        banner.println("LoadCpgPhase");
        banner.println("Filename: " + filename);
        try {
            Graft.cpg().traversal()
                    .io(filename)
                    .read()
                    .iterate();
            banner.println("CPG loaded successfully");
        } catch (Exception e) {
            log.error("Could not load CPG from file '{}'", filename, e);
            banner.println("CPG could not be loaded");
        }
        banner.display();
    }

}
