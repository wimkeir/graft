package graft.analysis.taint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.analysis.AnalysisResult;
import graft.analysis.GraftAnalysis;
import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;

public class TaintAnalysis implements GraftAnalysis {

    List<SourceDescription> sources;
    List<SinkDescription> sinks;
    List<SanitizerDescription> sanitizers;

    @Override
    public AnalysisResult doAnalysis(Configuration options) {
        sources = new ArrayList<>();
        sinks = new ArrayList<>();
        sanitizers = new ArrayList<>();

        // TODO: don't hard code this!
        sources.add(new SourceDescription("source", "Simple|SimpleInterproc", true));
        sinks.add(new SinkDescription("sink", "Simple|SimpleInterproc"));
        sanitizers.add(new MethodSanitizer("sanitizer", "Simple|SimpleInterproc"));

        for (SinkDescription sinkDescr : sinks) {
            for (SourceDescription sourceDescr : sources) {
                backwardsTaintAnalysis(sinkDescr, sourceDescr);
            }
        }

        return new AnalysisResult();
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
                    System.out.println("PATH ENDS WITH");
                    CpgUtil.debugVertex(endVertex);

                    if (endVertex.equals(rootVertex)) {
                        System.out.println("******************* TAINT **********************");
                    }
                }
            }
        }
    }

    private Map<Vertex, Vertex> getSunkVars(SinkDescription sinkDescr) {
        Map<Vertex, Vertex> sunkVars = new HashMap<>();
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        CpgTraversal callsToSink = g.getCallsTo(sinkDescr.namePattern, sinkDescr.scopePattern);
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
