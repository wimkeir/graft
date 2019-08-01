package graft.analysis;

import java.util.List;

/**
 * Base interface for analysis runs.
 *
 * @author Wim Keirsgieter
 */
public interface GraftAnalysis {

    List<AnalysisResult> doAnalysis();

}
