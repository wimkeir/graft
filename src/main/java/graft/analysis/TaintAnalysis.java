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
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.traversal.__.*;
import static graft.utils.DisplayUtil.*;
import static graft.utils.FileUtil.*;

/**
 * This class performs a taint analysis on the CPG, using the given source, sink and sanitizer descriptions.
 *
 * @author Wim Keirsgieter
 */
public class TaintAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysis.class);

    private String descrFile;
    private int nrVulns;
    private Banner banner;
    private List<Banner> vulnBanners;

    /**
     * Instantiate a new taint analysis.
     *
     * @param descrFile the path of the taint descriptions file.
     */
    public TaintAnalysis(String descrFile) {
        this.descrFile = descrFile;
        banner = new Banner("Taint Analysis");
        vulnBanners = new ArrayList<>();
    }

    @Override
    public void doAnalysis() {
        log.info("Running taint analysis...");
        long start = System.currentTimeMillis();
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
                .repeat(timeLimit(50000).outE(PDG_EDGE).inV().simplePath())
                .until(sink)
                .path()
                .toList();
        int nrPotentials = dataFlows.size();
        log.info("{} data flows between sources and sinks", nrPotentials);
        banner.println(nrPotentials + " potentially tainted data flow paths");

        for (Path dataFlow : dataFlows) {
            if (!isSanitized(dataFlow, sanitizer)) {
                nrVulns++;
                reportTaintedPath(dataFlow);
            }
        }
        long end = System.currentTimeMillis();

        banner.println(nrVulns + " taint vulnerabilities found");
        banner.println();

        log.info("Taint analysis completed in {}", displayTime(end - start));
        banner.println("Elapsed time: " + displayTime(end - start));
        banner.display();

        for (Banner vulnBanner : vulnBanners) {
            vulnBanner.display();
        }
    }

    private void reportTaintedPath(Path pdgPath) {
        Vertex src = pdgPath.get(0);
        Vertex sink = pdgPath.get(pdgPath.size() - 1);
        Banner vulnBanner = new Banner("TAINT VULNERABILITY");

        String srcLoc = getClassName(getFileName(src));
        vulnBanner.println("Source: " + src.value(TEXT_LABEL));
        vulnBanner.println("(" + srcLoc + ", " + src.value(SRC_LINE_NO) + ")");
        vulnBanner.println();

        for (int i = 1; i < pdgPath.size() - 2; i += 2) {
            Edge varDep = pdgPath.get(i);
            Vertex propThrough = pdgPath.get(i + 1);
            String propLoc = getClassName(getFileName(propThrough));
            vulnBanner.println("Tainted var '" +
                    varDep.value(TEXT_LABEL) +
                    "' redefined at: " +
                    propThrough.value(TEXT_LABEL));
            vulnBanner.println("(" + propLoc + ", " + propThrough.value(SRC_LINE_NO) + ")");
        }
        vulnBanner.println();

        String sinkLoc = getClassName(getFileName(sink));
        vulnBanner.println("Sink: " + sink.value(TEXT_LABEL));
        vulnBanner.println("(" + sinkLoc + ", " + sink.value(SRC_LINE_NO) + ")");

        vulnBanners.add(vulnBanner);
    }

    private boolean isSanitized(Path pdgPath, CpgTraversal sanitizer) {
        log.debug("Checking path for sanitization: {}", pdgPath);

        for (int i = 0; i < pdgPath.size() - 2; i += 2) {
            Edge e = pdgPath.get(i + 1);

            String varName;
            try {
                varName = e.value(VAR_NAME);
            } catch (IllegalStateException exc) {
                varName = "";
            }

            Vertex v = pdgPath.get(i);
            Vertex w = pdgPath.get(i + 2);
            List<Path> cfgPaths = Graft.cpg().traversal()
                    .V(v)
                    .repeat(timeLimit(10000).out(CFG_EDGE).simplePath())
                    .until(is(w))
                    .path().dedup().toList();

            for (Path cfgPath : cfgPaths) {
                boolean cfgSanitized = false;
                for (int j = 0; j < cfgPath.size(); j++) {
                    //System.out.println(debugVertex(cfgPath.get(j)));
                    CpgTraversal sans = Graft.cpg().traversal()
                            .V(((Vertex) cfgPath.get(j)).id())
                            .where(or(
                                    sanitizer.copy()
                                    //defines(varName)
                            ));
                    if ((long) sans.count().next() > 0) {
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
