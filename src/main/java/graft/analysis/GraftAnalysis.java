package graft.analysis;

import graft.cpg.structure.CodePropertyGraph;

/**
 * Base interface for analysis runs.
 *
 * @author Wim Keirsgieter
 */
public interface GraftAnalysis {

    void doAnalysis(CodePropertyGraph cpg);

}
