package graft.traversal;


import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.analysis.taint.SanitizerDescription;
import graft.analysis.taint.SourceDescription;
import graft.cpg.structure.VertexDescription;

import java.util.List;

import static graft.Const.*;
import static graft.traversal.__.*;

public class CpgTraversalSourceDsl extends GraphTraversalSource {

    public CpgTraversalSourceDsl(Graph graph, TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public CpgTraversalSourceDsl(Graph graph) {
        super(graph);
    }

    public CpgTraversal<Vertex, Vertex> getMatches(VertexDescription descr) {
        return getV().matches(descr);
    }

    /**
     * Get the tainted sources that taint the given variable argument of the given sink, according to the given source
     * description.
     *
     * @param sink the sensitive sink
     * @param varName the name of the sunk argument
     * @param sourceDescr the source description
     * @return a traversal containing the sources of taint for the given variable, if any
     */
    public CpgTraversal<Vertex, Vertex> getSourcesOfArg(Vertex sink, String varName, SourceDescription sourceDescr) {
        return (CpgTraversal<Vertex, Vertex>) V(sink)
                .inE(PDG_EDGE).has(VAR_NAME, varName).outV()
                .until(it -> sourceDescr.matches(it.get()))
                .repeat(inE(PDG_EDGE).outV());
    }

    /**
     * Get all paths from a given source to a given sink, where the given variable has not been reassigned or sanitized
     * according to the given sanitizer descriptions.
     *
     * Where the variable is sanitized or reassigned, the path is cut short but still returned. The return value is then
     * a collection of paths, each ending at either the sink, a reassignment of the variable, or a sanitizer node. Paths
     * ending at the sink are tainted paths.
     *
     * @param source the source of the given variable
     * @param sink the sink to which the variable propagates
     * @param sanitizers the sanitizer descriptions to check each node in the path against
     * @param varName the name of the tainted variable
     * @return
     */
    public CpgTraversal<Vertex, Path> sourceToSinkPaths(Vertex source, Vertex sink, List<SanitizerDescription> sanitizers, String varName) {
        return (CpgTraversal<Vertex, Path>) V(source).repeat(out(CFG_EDGE).simplePath())
                .until(or(
                        is(sink),
                        sanitizes(sanitizers, varName)
                        // TODO: this still causes issues with interproc example - why?
                        // it's because we're using the name of the variable being sunk, but we're starting at the source
                        // where the original tainted variable has a different name. along the path we're running into
                        // the point where the sunk variable is actually assigned for the first time.
                        // reassigns(varName)
                )).path();
    }

    /**
     * Get all invoke expressions that invoke a method matching the given signature pattern.
     *
     * @param sigPattern a regex specifying the method signature pattern
     * @return a traversal containing all invoke expressions nodes of the matching methods
     */
    @SuppressWarnings("unchecked")
    public CpgTraversal<Vertex, Vertex> getCallsTo(String sigPattern) {
        return (CpgTraversal<Vertex, Vertex>) getV()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, INVOKE_EXPR)
                .hasPattern(METHOD_SIG, sigPattern);
    }

    protected CpgTraversal<Vertex, Vertex> getV() {
        CpgTraversalSource clone = (CpgTraversalSource) this.clone();
        clone.getBytecode().addStep(GraphTraversal.Symbols.V);

        CpgTraversal<Vertex, Vertex> traversal = new DefaultCpgTraversal<>(clone);
        traversal.asAdmin().addStep(new GraphStep<>(traversal.asAdmin(), Vertex.class, true));

        return traversal;
    }

}
