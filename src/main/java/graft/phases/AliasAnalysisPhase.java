package graft.phases;

import graft.Graft;
import graft.analysis.AliasAnalysis;

public class AliasAnalysisPhase implements GraftPhase {

    @Override
    public void run() {
        AliasAnalysis analysis = new AliasAnalysis();
        analysis.doAnalysis(Graft.cpg());
    }

}
