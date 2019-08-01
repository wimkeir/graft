package graft.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.analysis.AnalysisResult;
import graft.analysis.taint.*;

public class TaintAnalysisPhase implements GraftPhase {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysisPhase.class);

    private List<SourceDescription> sources;
    private List<SinkDescription> sinks;
    private List<SanitizerDescription> sanitizers;

    public TaintAnalysisPhase(Configuration options) {
        sources = new ArrayList<>();
        sinks = new ArrayList<>();
        sanitizers = new ArrayList<>();

        int nrSources = options.getList("source.method").size();
        log.debug("{} sources specified", nrSources);
        for (int i = 0; i < nrSources; i++) {
            try {
                String methodSig = options.getString(String.format("source(%d).method", i));
                boolean taintsRet = options.getBoolean(String.format("source(%d).taints-ret", i));
                List<Integer> taintsArgs = new ArrayList<>();
                Object args = options.getProperty(String.format("sanitizer(%d).args", i));
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

        int nrSinks = options.getList("sink.method").size();
        log.debug("{} sinks specified", nrSinks);
        for (int i = 0; i < nrSinks; i++) {
            try {
                String methodSig = options.getString(String.format("sink(%d).method", i));
                List<Integer> sinksArgs = new ArrayList<>();
                Object args = options.getProperty(String.format("sanitizer(%d).args", i));
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

        // TODO: handle conditional sanitizers!
        int nrSanitizers = options.getList("sanitizer.method").size();
        log.debug("{} sanitizers specified", nrSanitizers);
        for (int i = 0; i < nrSanitizers; i++) {
            try {
                String methodSig = options.getString(String.format("sanitizer(%d).method", i));
                List<Integer> sanitizesArgs = new ArrayList<>();
                Object args = options.getProperty(String.format("sanitizer(%d).args", i));
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

    public static Configuration getOptions(Configuration config) {
        return config.subset("taint-analysis");
    }

}
