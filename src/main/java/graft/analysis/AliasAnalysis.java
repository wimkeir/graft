package graft.analysis;

import graft.cpg.CpgUtil;
import graft.cpg.structure.CodePropertyGraph;
import graft.traversal.CpgTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

import static graft.Const.*;

public class AliasAnalysis implements GraftAnalysis {

    private Map<Vertex, Set<Vertex>> pointsToSets;
    private Queue<Vertex> worklist;

    public AliasAnalysis() {
        pointsToSets = new HashMap<>();
        worklist = new LinkedList<>();
    }

    @Override
    public void doAnalysis(CodePropertyGraph cpg) {
        // TODO: first intraproc subgraphs, then extend
        pointsToAnalysis(cpg);

        // XXX debug
        System.out.println("WORKLIST");
        for (Vertex v : worklist) System.out.println(CpgUtil.debugVertex(v));

        System.out.println("POINTS-TO SETS");
        for (Vertex v : pointsToSets.keySet()) {
            System.out.println(CpgUtil.debugVertex(v));
            System.out.println("POINTS TO");
            for (Vertex w : pointsToSets.get(v)) System.out.println(CpgUtil.debugVertex(w));
        }
    }

    private void pointsToAnalysis(CodePropertyGraph subgraph) {
        CpgTraversal<Vertex, Vertex> newStmts = subgraph.traversal().getNewStmts();
        while (newStmts.hasNext()) {
            Vertex newStmt = newStmts.next();
            Vertex tgt = getTarget(subgraph, newStmt);
            Vertex val = getValue(subgraph, newStmt);
            addToSet(tgt, val);
            worklist.add(tgt);
        }

        CpgTraversal<Vertex, Vertex> refAssStmts = subgraph.traversal().getRefAssignStmts();
        while (refAssStmts.hasNext()) {
            Vertex refAssStmt = refAssStmts.next();
            // TODO
        }

        while (!worklist.isEmpty()) {
            Vertex n = worklist.poll();
            CpgTraversal outEdges = subgraph.traversal().V(n).outE("alias");
            while (outEdges.hasNext()) {
                Edge outEdge = (Edge) outEdges.next();
                diffProp(pointsToSets.get(n), outEdge.inVertex());
            }

            if (n.value(NODE_TYPE).equals(LOCAL_VAR)) {

            }
        }
    }

    private void diffProp(Set<Vertex> srcSet, Vertex n) {
        // TODO
    }

    private void addToSet(Vertex var, Vertex loc) {
        Set<Vertex> set = pointsToSets.get(var);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(loc);
        pointsToSets.put(var, set);
    }

    private static Vertex getTarget(CodePropertyGraph cpg, Vertex assignStmt) {
        assert assignStmt.value(NODE_TYPE).equals(ASSIGN_STMT);
        return cpg.traversal()
                .V(assignStmt)
                .outE(AST_EDGE).has(EDGE_TYPE, TARGET)
                .inV().next();
    }

    private static Vertex getValue(CodePropertyGraph cpg, Vertex assignStmt) {
        assert assignStmt.value(NODE_TYPE).equals(ASSIGN_STMT);
        return cpg.traversal()
                .V(assignStmt)
                .outE(AST_EDGE).has(EDGE_TYPE, VALUE)
                .inV().next();
    }

}
