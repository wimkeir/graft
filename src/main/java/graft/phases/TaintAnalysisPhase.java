package graft.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.analysis.AnalysisResult;
import graft.analysis.taint.*;
import graft.Options;


/**
 * This phase handles the running of taint analyses.
 *
 * @author Wim Keirsgieter
 */
public class TaintAnalysisPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysisPhase.class);

    private List<SourceDescription> sources;
    private List<SinkDescription> sinks;
    private List<SanitizerDescription> sanitizers;

    public TaintAnalysisPhase() {
        sources = new ArrayList<>();
        sinks = new ArrayList<>();
        sanitizers = new ArrayList<>();

        // get source descriptions
        int nrSources = Options.v().getList("source.method").size();
        log.debug("{} sources specified", nrSources);
        for (int i = 0; i < nrSources; i++) {
            try {
                String methodSig = Options.v().getString(String.format("source(%d).method", i));
                boolean taintsRet = Options.v().getBoolean(String.format("source(%d).taints-ret", i));
                List<Integer> taintsArgs = new ArrayList<>();
                Object args = Options.v().getProperty(String.format("sanitizer(%d).args", i));
                if (args instanceof int[]) {
                    for (int arg : (int[]) args) {
                        taintsArgs.add(arg);
                    }
                } else if (args instanceof Integer){
                    taintsArgs.add((int) args);
                }
                sources.add(new SourceDescription(methodSig, taintsRet, taintsArgs));
            } catch (NoSuchElementException|NullPointerException e) {
                log.debug("Invalid source description in config, skipping", e);
            }
        }

        // get sink descriptions
        int nrSinks = Options.v().getList("sink.method").size();
        log.debug("{} sinks specified", nrSinks);
        for (int i = 0; i < nrSinks; i++) {
            try {
                String methodSig = Options.v().getString(String.format("sink(%d).method", i));
                List<Integer> sinksArgs = new ArrayList<>();
                Object args = Options.v().getProperty(String.format("sanitizer(%d).args", i));
                if (args instanceof int[]) {
                    for (int arg : (int[]) args) {
                        sinksArgs.add(arg);
                    }
                } else if (args instanceof Integer){
                    sinksArgs.add((int) args);
                }
                sinks.add(new SinkDescription(methodSig, sinksArgs));
            } catch (NoSuchElementException|NullPointerException e) {
                log.debug("Invalid sink description in config, skipping", e);
            }
        }

        // get sanitizer descriptions
        // TODO: handle conditional sanitizers!
        int nrSanitizers = Options.v().getList("sanitizer.method").size();
        log.debug("{} sanitizers specified", nrSanitizers);
        for (int i = 0; i < nrSanitizers; i++) {
            try {
                String methodSig = Options.v().getString(String.format("sanitizer(%d).method", i));
                List<Integer> sanitizesArgs = new ArrayList<>();
                Object args = Options.v().getProperty(String.format("sanitizer(%d).args", i));
                if (args instanceof int[]) {
                    for (int arg : (int[]) args) {
                        sanitizesArgs.add(arg);
                    }
                } else if (args instanceof Integer){
                    sanitizesArgs.add((int) args);
                }
                sanitizers.add(new MethodSanitizer(methodSig, sanitizesArgs));
            } catch (NoSuchElementException|NullPointerException e) {
                log.debug("Invalid sanitizer description in config, skipping", e);
            }
        }

    }

    @Override
    public PhaseResult run() {
        log.info("Running TaintAnalysisPhase...");

        try {
            TaintAnalysis taintAnalysis = new TaintAnalysis(sources, sinks, sanitizers);
            List<AnalysisResult> analysisResults = taintAnalysis.doAnalysis();
            StringBuilder sb = new StringBuilder();
            for (AnalysisResult res : analysisResults) {
                sb.append(res.toString());
            }
            return new PhaseResult(this, true, sb.toString());
        } catch (Exception e) {
            // TODO: better details
            return new PhaseResult(this, false, e.getMessage());
        }
    }

}
