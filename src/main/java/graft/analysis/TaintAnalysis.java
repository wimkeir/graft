package graft.analysis;

import java.util.*;

import graft.Banner;
import graft.cpg.structure.CodePropertyGraph;
import graft.cpg.structure.VertexDescription;
import graft.utils.LogUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;

/**
 * This class performs a taint analysis on the CPG, using the given source, sink and sanitizer descriptions.
 *
 * @author Wim Keirsgieter
 */
public class TaintAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysis.class);

    private VertexDescription source;
    private VertexDescription sink;
    private VertexDescription sanitizer;

    public TaintAnalysis(VertexDescription source, VertexDescription sink, VertexDescription sanitizer) {
        this.source = source;
        this.sink = sink;
        this.sanitizer = sanitizer;
    }

    @Override
    public void doAnalysis(CodePropertyGraph cpg) {
        log.info("Running taint analysis...");
        // TODO: ideally we want the source vertices as well as the tainted vars in a map
        List<Vertex> sources = cpg.traversal().getMatches(source).toList();
        List<Vertex> sinks = cpg.traversal().getMatches(sink).toList();

        log.debug("{} sources found", sources.size());
        log.debug("{} sinks found", sinks.size());

        for (Vertex srcVertex : sources) {
            for (Vertex sinkVertex : sinks) {
                List<Path> pdgPaths = cpg.traversal()
                        .pathsBetween(srcVertex, sinkVertex, PDG_EDGE)
                        .toList();
                log.debug("{} PDG paths between vertex '{}' and vertex '{}'",
                        pdgPaths.size(),
                        srcVertex.value(TEXT_LABEL),
                        sinkVertex.value(TEXT_LABEL));
                if (pdgPaths.size() > 0) {
                    List<Path> cfgPaths = cpg.traversal()
                            .pathsBetween(srcVertex, sinkVertex, CFG_EDGE)
                            .toList();
                    for (Path path : cfgPaths) {
                        if (!isSanitized(cpg, path)) {
                            Banner banner = new Banner();
                            banner.println("Taint vulnerability found!");
                            banner.println("");
                            banner.println("Source: " + ((Vertex) path.get(0)).value(TEXT_LABEL));
                            banner.println("Sink: " + ((Vertex) path.get(path.size() - 1)).value(TEXT_LABEL));
                            banner.display();
                        }
                    }
                }
            }
        }
    }

    private boolean isSanitized(CodePropertyGraph cpg, Path path) {
        boolean[] sanitized = new boolean[]{ false };
        path.iterator().forEachRemaining(it -> {
            Vertex v = (Vertex) it;
            if (cpg.traversal().V(v).matches(sanitizer).hasNext()) {
                sanitized[0] = true;
            }
        });
        return sanitized[0];
    }

    // XXX
    public static void main(String[] args) {
        LogUtil.setLogLevel(DEBUG);
        CodePropertyGraph cpg = CodePropertyGraph.fromFile("etc/dumps/simple.json");

        VertexDescription srcDescr = new VertexDescription("source", CFG_NODE);
        srcDescr.setPropPattern(NODE_TYPE, ASSIGN_STMT);

        VertexDescription sinkDescr = new VertexDescription("sink", CFG_NODE);
        sinkDescr.setPropPattern(NODE_TYPE, INVOKE_STMT);

        VertexDescription sanDescr = new VertexDescription("sanitizer", CFG_NODE);
        sanDescr.setPropPattern(NODE_TYPE, CONDITIONAL_STMT);

        TaintAnalysis analysis = new TaintAnalysis(srcDescr, sinkDescr, sanDescr);
        analysis.doAnalysis(cpg);
    }
}
