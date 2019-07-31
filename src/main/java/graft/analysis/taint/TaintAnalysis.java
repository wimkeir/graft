package graft.analysis.taint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.analysis.AnalysisResult;
import graft.analysis.GraftAnalysis;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;
import static graft.traversal.__.*;

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
        sources.add(new SourceDescription("source", "Simple", true));
        sinks.add(new SinkDescription("sink", "Simple"));
        sanitizers.add(new MethodSanitizer("sanitizer", "Simple"));

        // TODO: run these as lists
        Map<Vertex, String> sunkVars = sinks.get(0).getSunkVars();
        List<Vertex> sourceNodes = getSources(sunkVars, sources.get(0));

        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        for (Vertex sourceNode : sourceNodes) {
            for (Vertex sinkNode: sunkVars.keySet()) {
                String varName = sunkVars.get(sinkNode);
                GraphTraversal paths = g.V(sourceNode)
                        .repeat(out(CFG_EDGE).simplePath())
                        .until(or(

                                // stop if we reach the sink...
                                is(sinkNode),

                                // if we reach a sanitizer...
                                filter(x -> {
                                    Vertex v = (Vertex) x.get();
                                    return sanitizers.get(0).sanitizes(v, varName);
                                }),

                                // or if the variable is reassigned
                                and(
                                        has(NODE_TYPE, ASSIGN_STMT),
                                        outE(AST_EDGE).has(EDGE_TYPE, TARGET).inV().has(NAME, varName)
                                )
                        ))
                        .path();

                System.out.println("Paths:");
                while(paths.hasNext()) {
                    // TODO!!!!!!
                    Path path = (Path) paths.next();
                    Vertex endVertex = path.get(path.size() - 1);
                    if (endVertex.id().equals(sinkNode.id())) {
                        System.out.println("TAAAAAAAAAAAAAINT");
                    }
                }
            }
        }

        return new AnalysisResult();
    }

    private List<Vertex> getSources(Map<Vertex, String> sunkNodes, SourceDescription sourceDescr) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> sources = new ArrayList<>();

        for (Vertex sinkNode : sunkNodes.keySet()) {
            String varName = sunkNodes.get(sinkNode);

            // get all tainted sources of variables that propagate to the sinks
            CpgTraversal sourceTraversal = g.V(sinkNode)
                    .inE(PDG_EDGE)
                    .has(VAR_NAME, varName)
                    .outV()
                    .until(x -> sourceDescr.matches(x.get()))
                    .repeat(inE(PDG_EDGE).outV());

            while (sourceTraversal.hasNext()) {
                sources.add((Vertex) sourceTraversal.next());
            }
        }

        return sources;
    }

}
