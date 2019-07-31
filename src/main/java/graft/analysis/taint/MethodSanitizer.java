package graft.analysis.taint;

import java.util.ArrayList;
import java.util.List;

import graft.cpg.CpgUtil;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;

public class MethodSanitizer extends SanitizerDescription {

    String namePattern;
    String scopePattern;

    List<Integer> sanitizesArgs;

    public MethodSanitizer(String namePattern, String scopePattern, int... sanitizesArgs) {
        this.namePattern = namePattern;
        this.scopePattern = scopePattern;

        this.sanitizesArgs = new ArrayList<>();
        for (int arg : sanitizesArgs) {
            this.sanitizesArgs.add(arg);
        }
    }

    /**
     * Checks if the given node sanitizes the given variable, according to the sanitizer description.
     *
     * @param vertex the node to check
     * @param varName the variable name to sanitize
     * @return true if the given node sanitizes the variable, otherwise false
     */
    @Override
    public boolean sanitizes(Vertex vertex, String varName) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        List<Vertex> invokeExprs = CpgUtil.getInvokeExprs(vertex, namePattern, scopePattern);

        for (Vertex invokeExpr : invokeExprs) {

            // if sink args are specified, we handle them here
            for (int arg : sanitizesArgs) {
                CpgTraversal sanitizedVar = g.V(invokeExpr).ithArg(arg);

                if (sanitizedVar.hasNext()) {
                    String sanVarName = ((Vertex) sanitizedVar.next()).value(NAME).toString();
                    if (sanVarName.equals(varName)) {
                        return true;
                    }
                }
            }

            // if not, we assume all args are sink args
            if (sanitizesArgs.size() == 0) {
                CpgTraversal sanVars = g.V(invokeExpr)
                        .outE(AST_EDGE)
                        .has(EDGE_TYPE, ARG)
                        .inV();

                while (sanVars.hasNext()) {
                    String sanVarName = ((Vertex) sanVars.next()).value(NAME).toString();
                    if (sanVarName.equals(varName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
