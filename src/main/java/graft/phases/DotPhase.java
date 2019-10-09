package graft.phases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.Graft;
import graft.Options;
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
    public void run() {
        log.info("Running DotPhase...");
        String filename = Options.v().getString(OPT_GENERAL_DOT_FILE);
        Banner banner = new Banner();
        banner.println("DotPhase");
        banner.println("Filename: " + filename);
        DotUtil.graphToDot(Graft.cpg(), filename, "cpg");
        banner.println("CPG written to dotfile " + filename);
        banner.display();
    }

}
