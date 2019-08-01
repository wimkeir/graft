package graft.analysis.taint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.analysis.AnalysisResult;
import graft.analysis.GraftAnalysis;
import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;

/**
 * This class performs a backwards taint analysis on the CPG, using the given source, sink and sanitizer descriptions.
 *
 * @author Wim Keirsgieter
 */
public class TaintAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(TaintAnalysis.class);

    private List<SourceDescription> sources;
    private List<SinkDescription> sinks;
    private List<SanitizerDescription> sanitizers;

    private List<AnalysisResult> results;

    public TaintAnalysis(List<SourceDescription> sources, List<SinkDescription> sinks, List<SanitizerDescription> sanitizers) {
        this.sources = sources;
        this.sinks = sinks;
        this.sanitizers = sanitizers;

        results = new ArrayList<>();
    }

    @Override
    public List<AnalysisResult> doAnalysis() {
        log.info("Running taint analysis...");
        if (log.isDebugEnabled()) {
            for (SourceDescription source : sources) log.debug(source.toString());
            for (SinkDescription sink : sinks) log.debug(sink.toString());
            for (SanitizerDescription san : sanitizers) log.debug(san.toString());
        }

        for (SinkDescription sinkDescr : sinks) {
            for (SourceDescription sourceDescr : sources) {
                backwardsTaintAnalysis(sinkDescr, sourceDescr);
            }
        }

        return results;
    }

    private void backwardsTaintAnalysis(SinkDescription sinkDescr, SourceDescription sourceDescr) {
        // a mapping of variables (args) to the sink invoke expression that "sunk" them
        Map<Vertex, Vertex> sunkVars = getSunkVars(sinkDescr);
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        // TODO: this can easily be parallelised
        for (Vertex localVertex : sunkVars.keySet()) {
            Vertex invokeVertex = sunkVars.get(localVertex);
            Vertex rootVertex = CpgUtil.getCfgRoot(invokeVertex);
            String varName = localVertex.value(NAME);         // NB: this assumes the arg is a local variable

            CpgTraversal sources = g.getSourcesOfArg(rootVertex, varName, sourceDescr);

            while (sources.hasNext()) {
                Vertex sourceVertex = (Vertex) sources.next();
                GraphTraversal paths = g.sourceToSinkPaths(sourceVertex, rootVertex, sanitizers, varName);
                while (paths.hasNext()) {
                    Path path = (Path) paths.next();
                    Vertex endVertex = path.get(path.size() - 1);

                    if (endVertex.equals(rootVertex)) {
                        AnalysisResult result = new TaintAnalysisResult(sourceVertex,
                                                                   rootVertex,
                                                                   varName,
                                                                   sourceDescr.sigPattern,
                                                                   sinkDescr.sigPattern);
                        if (!results.contains(result)) {
                            results.add(result);
                        }
                    }
                }
            }
        }
    }

    private Map<Vertex, Vertex> getSunkVars(SinkDescription sinkDescr) {
        Map<Vertex, Vertex> sunkVars = new HashMap<>();
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        CpgTraversal callsToSink = g.getCallsTo(sinkDescr.sigPattern);
        while (callsToSink.hasNext()) {
            Vertex invokeVertex = (Vertex) callsToSink.next();

            GraphTraversal sunkArgs = g.V(invokeVertex).sunkArgs(sinkDescr);

            while (sunkArgs.hasNext()) {
                Vertex sunkArg = (Vertex) sunkArgs.next();
                // TODO: handle refs
                // TODO: special case where call to source is arg to sink

                List<Vertex> locals = CpgUtil.getLocals(sunkArg);
                for (Vertex local : locals) {
                    sunkVars.put(local, invokeVertex);
                }
            }

        }

        return sunkVars;
    }

}
