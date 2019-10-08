package graft.phases;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;

import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;
import graft.utils.SootUtil;

/**
 * This phase handles the actual construction of the (intraprocedural) CPG, using Soot.
 *
 * @author Wim Keirsgieter
 */
public class BuildCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    public BuildCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running BuildCpgPhase");

        PackManager.v().getPack("jtp").add(new Transform("jtp.cfg", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                String methodSig = body.getMethod().getSignature();
                log.debug("{} phase: transforming body of method '{}'", phaseName, methodSig);
                CpgBuilder.buildCpg(body);
            }
        }));
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");

        List<String> sootOptions = SootUtil.getSootOptions();
        String[] sootArgs = new String[(sootOptions).size()];

        log.debug("Running soot with options: ");
        int i = 0;
        for (String option : sootOptions) {
            log.debug(option);
            sootArgs[i++] = option;
        }
        soot.Main.main(sootArgs);

        String details = String.format("| Nodes: %1$-89d |\n", CpgUtil.getNodeCount());
        details += String.format("| Edges: %1$-89d |\n", CpgUtil.getEdgeCount());
        return new PhaseResult(this, true, details);
    }

}
