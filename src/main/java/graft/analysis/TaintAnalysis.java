package graft.analysis;

import java.io.File;
import java.io.IOException;
import java.util.*;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.Graft;
import graft.Options;
import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;

import static graft.Const.*;

/**
 * This class performs a taint analysis on the CPG, using the given source, sink and sanitizer descriptions.
 *
 * @author Wim Keirsgieter
 */
public class TaintAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysis.class);

    private CpgTraversal sourceDescr;
    private CpgTraversal sinkDescr;
    private CpgTraversal sanDescr;

    private Set<Vertex> sanitizers;

    public TaintAnalysis() { }

    @Override
    public void doAnalysis() {
        log.info("Running taint analysis...");

        log.debug("Loading taint descriptions");
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        binding.setProperty("cpg", Graft.cpg());

        try {
            sourceDescr = (CpgTraversal) shell.evaluate(new File(Options.v().getString(OPT_TAINT_SOURCE)));
            sinkDescr = (CpgTraversal) shell.evaluate(new File(Options.v().getString(OPT_TAINT_SINK)));
            sanDescr = (CpgTraversal) shell.evaluate(new File(Options.v().getString(OPT_TAINT_SANITIZER)));
        } catch (IOException e) {
            log.error("Unable to read taint descriptions, failure", e);
            return;
        }

        sanitizers = sanDescr.toSet();

        // TODO: ideally we want the source vertices as well as the tainted vars in a map
        List<Vertex> sources = sourceDescr.toList();
        List<Vertex> sinks = sinkDescr.toList();

        log.debug("{} potential sources found", sources.size());
        log.debug("{} potential sinks found", sinks.size());

        for (Vertex srcVertex : sources) {
            for (Vertex sinkVertex : sinks) {
                List<Path> pdgPaths = Graft.cpg().traversal()
                        .pathsBetween(srcVertex, sinkVertex, PDG_EDGE)
                        .toList();
                log.debug("{} PDG paths between vertex '{}' and vertex '{}'",
                        pdgPaths.size(),
                        srcVertex.value(TEXT_LABEL),
                        sinkVertex.value(TEXT_LABEL));
                for (Path pdgPath : pdgPaths) {
                    if (!isSanitized(pdgPath)) {
                        reportTaintedPath(pdgPath);
                    } else {
                        log.debug("Path is sanitized");
                    }
                }
            }
        }
    }

    private void reportTaintedPath(Path pdgPath) {
        Vertex src = pdgPath.get(0);
        Vertex sink = pdgPath.get(pdgPath.size() - 1);
        Banner banner = new Banner();
        banner.println("Taint vulnerability found!");
        banner.println("");
        banner.println("Source:");
        banner.println(src.value(TEXT_LABEL));
        banner.println("Location: " + CpgUtil.getFileName(src) + " (" + src.value(SRC_LINE_NO) + ")");
        banner.println("Sink:");
        banner.println(sink.value(TEXT_LABEL));
        banner.println("Location: " + CpgUtil.getFileName(sink) + " (" + sink.value(SRC_LINE_NO) + ")");
        banner.display();

    }

    private boolean isSanitized(Path pdgPath) {
        log.debug("Checking path for sanitization");
        for (int i = 0; i < pdgPath.size() - 1; i++) {
            List<Path> cfgPaths = Graft.cpg().traversal()
                    .pathsBetween(pdgPath.get(i), pdgPath.get(i + 1), CFG_EDGE)
                    .toList();
            for (Path cfgPath : cfgPaths) {
                for (int j = 0; j < cfgPath.size(); j++) {
                    if (sanitizers.contains(cfgPath.get(j))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
