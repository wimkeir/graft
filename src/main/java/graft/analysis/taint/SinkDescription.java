package graft.analysis.taint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;

public class SinkDescription {

    private static Logger log = LoggerFactory.getLogger(SinkDescription.class);

    private String namePattern;
    private String scopePattern;
    private List<Integer> sinksArgs;

    public SinkDescription(String namePattern, String scopePattern, int... sinksArgs) {
        this.namePattern = namePattern;
        this.scopePattern = scopePattern;

        this.sinksArgs = new ArrayList<>();
        for (int arg : sinksArgs) {
            this.sinksArgs.add(arg);
        }
    }

    /**
     * Get a mapping of all CFG nodes that contain a sink invocation to the name of the variable being sunk.
     *
     * @return a mapping of CFG sink nodes to sunk variables
     */
    public Map<Vertex, String> getSunkVars() {
        Map<Vertex, String> sunkVars = new HashMap<>();
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        // get all invoke expressions that invoke the sink
        CpgTraversal sinkExprNodes = g.V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, INVOKE_EXPR)
                .hasPattern(METHOD_NAME, namePattern)
                .hasPattern(METHOD_SCOPE, scopePattern);

        while (sinkExprNodes.hasNext()) {
            Vertex sinkExprNode = (Vertex) sinkExprNodes.next();
            Vertex cfgRoot = CpgUtil.getCfgRoot(sinkExprNode);

            // if sink args are specified, we handle them here
            for (int arg : sinksArgs) {
                CpgTraversal sunkVar = g.V(sinkExprNode).ithArg(arg);

                if (sunkVar.hasNext()) {
                    String varName = ((Vertex) sunkVar.next()).value(NAME).toString();
                    sunkVars.put(cfgRoot, varName);
                }
            }

            // if not, we assume all args are sink args
            if (sinksArgs.size() == 0) {
                CpgTraversal sunkVar = g.V(sinkExprNode)
                        .outE(AST_EDGE)
                        .has(EDGE_TYPE, ARG)
                        .inV();

                while (sunkVar.hasNext()) {
                    String varName = ((Vertex) sunkVar.next()).value(NAME).toString();
                    sunkVars.put(cfgRoot, varName);
                }
            }
        }

        return sunkVars;
    }
}
