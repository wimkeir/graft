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

    private String descrFile;

    public TaintAnalysis(String descrFile) {
        this.descrFile = descrFile;
    }

    @Override
    public void doAnalysis() {
        log.info("Running taint analysis...");
        CpgTraversal source, sink, sanitizer;

        log.debug("Loading taint descriptions");
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);

        try {
            shell.evaluate(new File(descrFile));
            source = (CpgTraversal) shell.getVariable("source");
            sink = (CpgTraversal) shell.getVariable("sink");
            sanitizer = (CpgTraversal) shell.getVariable("sanitizer");
        } catch (IOException e) {
            log.error("Unable to read taint descriptions, failure", e);
            return;
        }

        List<Path> dataFlows = Graft.cpg().traversal()
                .dataFlowsBetween(source, sink)
                .toList();

        for (Path dataFlow : dataFlows) {
            if (!isSanitized(dataFlow, sanitizer)) {
                reportTaintedPath(dataFlow);
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

    private boolean isSanitized(Path pdgPath, CpgTraversal sanitizer) {
        log.debug("Checking path for sanitization: {}", pdgPath);
        for (int i = 0; i < pdgPath.size() - 2; i += 2) {
            List<Path> cfgPaths = Graft.cpg().traversal()
                    .pathsBetween(pdgPath.get(i), pdgPath.get(i + 2), CFG_EDGE)
                    .toList();
            for (Path cfgPath : cfgPaths) {
                boolean cfgSanitized = false;
                for (int j = 0; j < cfgPath.size(); j++) {
                    if (Graft.cpg().traversal()
                            .V(((Vertex) cfgPath.get(j)).id())
                            .where(sanitizer.copy())
                            .count().next() > 0) {
                        cfgSanitized = true;
                    }
                }
                if (!cfgSanitized) {
                    return false;
                }
            }
        }
        return true;
    }

}
