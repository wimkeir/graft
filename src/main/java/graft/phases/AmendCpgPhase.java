package graft.phases;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import graft.cpg.CpgBuilder;
import graft.cpg.CpgUtil;
import graft.utils.SootUtil;

public class AmendCpgPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(BuildCpgPhase.class);

    public AmendCpgPhase() { }

    @Override
    public PhaseResult run() {
        log.info("Running AmendCpgPhase...");
        PackManager.v().getPack("jtp").add(new Transform("jtp.cfg", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
                // XXX
                String CHANGED_CLASS = "A";

                SootClass cls = body.getMethod().getDeclaringClass();
                if (cls.getName().equals(CHANGED_CLASS)) {
                    String methodSig = body.getMethod().getSignature();
                    log.debug("{} phase: amending body of method '{}'", phaseName, methodSig);
                    CpgBuilder.amendCpg(body);
                }
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
