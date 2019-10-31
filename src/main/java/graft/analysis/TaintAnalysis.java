package graft.analysis;

import java.io.File;
import java.io.IOException;
import java.util.*;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.Banner;
import graft.Graft;
import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.traversal.__.*;

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
            log.error("Unable to read taint descriptions, failure");
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            return;
        }

        List<Path> dataFlows = Graft.cpg().traversal()
                .V().where(source)
                .repeat(timeLimit(100).outE(PDG_EDGE).inV().simplePath())
                .until(sink)
                .path()
                .toList();
        log.info("{} data flows between sources and sinks", dataFlows.size());

        for (Path dataFlow : dataFlows) {
            if (!isSanitized(dataFlow, sanitizer)) {
                reportTaintedPath(dataFlow);
            }
        }

    }

    private void reportTaintedPath(Path pdgPath) {
        Vertex src = pdgPath.get(0);
        Vertex sink = pdgPath.get(pdgPath.size() - 1);
        Banner banner = new Banner("Taint vulnerability");
        banner.println("");
        banner.println("Source:");
        banner.println(src.value(TEXT_LABEL));
        banner.println("Location: " + CpgUtil.getFileName(src) + " (" + src.value(SRC_LINE_NO) + ")");
        banner.println();

        banner.println("TAINTED PATH");
        for (int i = 1; i < pdgPath.size() - 2; i += 2) {
            Edge varDep = pdgPath.get(i);
            Vertex propThrough = pdgPath.get(i + 1);
            banner.println("Tainted variable: " + varDep.value(TEXT_LABEL));
            banner.println("Taint propagates through: " + propThrough.value(TEXT_LABEL));
            banner.println("Location: " + CpgUtil.getFileName(propThrough) + " (" + propThrough.value(SRC_LINE_NO) + ")");
        }
        banner.println();

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
