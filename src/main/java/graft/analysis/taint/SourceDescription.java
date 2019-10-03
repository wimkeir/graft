package graft.analysis.taint;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.cpg.CpgUtil;
import graft.db.GraphUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

/**
 * A description of a tainted source (specifically a method call).
 *
 * The source can either taint its return value, or a specified set of arguments, or both (but not neither).
 *
 * @author Wim Keirsgieter
 */
public class SourceDescription {

    public String sigPattern;

    public List<Integer> taintsArgs;
    public boolean taintsRet;

    /**
     * Returns a new source description with patterns describing the signature of the methods (optionally
     * tainting their return values), and a list of args tainted by the matching methods.
     *
     * If no args are specified as tainted, then the source must taint its return value.
     *
     * @param sigPattern a regex describing the pattern of the method signature
     * @param taintsRet true if the source taints its return value
     * @param taintsArgs which args are tainted by the source
     */
    public SourceDescription(String sigPattern, boolean taintsRet, List<Integer> taintsArgs) {
        assert (taintsRet || taintsArgs.size() != 0);
        this.sigPattern = sigPattern;
        this.taintsRet = taintsRet;
        this.taintsArgs = taintsArgs;
    }

    /**
     * Check whether a given CFG node matches the source description.
     *
     * @param vertex the vertex to check against the description
     * @return true if the vertex matches the description, else false
     */
    public boolean matches(Vertex vertex) {
        // TODO: move this to DSL
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> invokeExprs = CpgUtil.getInvokeExprs(vertex);

        // TODO: this only checks for the presence of a source expression in the subtree
        for (Vertex expr : invokeExprs) {
            CpgTraversal match = g.V(expr)
                    .hasPattern(METHOD_SIG, sigPattern);
            if ((long) match.count().next() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return sigPattern;
    }

}
