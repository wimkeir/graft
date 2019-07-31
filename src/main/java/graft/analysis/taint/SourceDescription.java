package graft.analysis.taint;

import java.util.ArrayList;
import java.util.List;

import graft.cpg.CpgUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;

public class SourceDescription {

    String namePattern;
    String scopePattern;

    List<Integer> taintsArgs;
    boolean taintsRet;

    public SourceDescription(String namePattern, String scopePattern, boolean taintsRet, int... taintsArgs) {
        this.namePattern = namePattern;
        this.scopePattern = scopePattern;
        this.taintsRet = taintsRet;

        this.taintsArgs = new ArrayList<>();
        for (int arg : taintsArgs) {
            this.taintsArgs.add(arg);
        }
    }

    /**
     * Check whether a given CFG node sanitizes the source description.
     *
     * @param vertex the vertex to check against the description
     * @return true if the vertex sanitizes the description, else false
     */
    public boolean matches(Vertex vertex) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> invokeExprs = CpgUtil.getInvokeExprs(vertex);

        // TODO: this is very naive
        for (Vertex expr : invokeExprs) {
            CpgTraversal match = g.V(expr)
                    .hasPattern(METHOD_NAME, namePattern)
                    .hasPattern(METHOD_SCOPE, scopePattern);
            if ((long) match.count().next() > 0) {
                return true;
            }
        }
        return false;
    }

}
