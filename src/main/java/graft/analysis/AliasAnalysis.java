package graft.analysis;

import graft.cpg.structure.CodePropertyGraph;
import graft.traversal.CpgTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static graft.Const.*;

public class AliasAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(AliasAnalysis.class);

    private Map<String, Set<String>> pt;
    private Map<String, Set<String>> ptDelta;
    private Queue<String> worklist;

    private Map<String, Vertex> sources;

    public AliasAnalysis() {
        pt = new HashMap<>();
        ptDelta = new HashMap<>();
        worklist = new LinkedList<>();

        sources = new HashMap<>();
    }

    @Override
    public void doAnalysis(CodePropertyGraph cpg) {
        //aliasAnalysis(cpg);

        CpgTraversal newAssigns = cpg.traversal().getNewAssignStmts();
        log.debug("new assigns: {}", newAssigns.clone().count().next());
        while (newAssigns.hasNext()) {
            Vertex newAssign = (Vertex) newAssigns.next();
            Vertex source = cpg.traversal()
                    .V(newAssign)
                    .getTgt().next();
            String varName = source.value(NAME);
            pt.put(varName, new HashSet<>());

            log.debug("Source for {}: {}", varName, source.value(TEXT_LABEL));
            sources.put(varName, source);
        }

        CpgTraversal refAssigns = cpg.traversal().getRefAssignStmts();
        log.debug("ref assigns: {}", refAssigns.clone().count().next());
        while (refAssigns.hasNext()) {
            Vertex refAssign = (Vertex) refAssigns.next();
            Vertex target = cpg.traversal()
                    .V(refAssign)
                    .getTgt().next();
            String tgtName = target.value(NAME);
            String valName = cpg.traversal()
                    .V(refAssign)
                    .getVal()
                    .values(NAME)
                    .next().toString();

            Set<String> valSet = new HashSet<>();
            valSet.add(valName);

            // TODO: this will possibly overwrite things
            pt.put(tgtName, union(pt.get(tgtName), valSet));
            sources.put(tgtName, target);
        }

        for (String x : pt.keySet()) {
            Vertex source = sources.get(x);
            if (source == null) {
                log.debug("No source for {}", x);
                continue;
            }
            log.debug("Drawing may-alias edges for {}", x);
            for (String y : pt.get(x)) {
                log.debug("May alias {}", y);
                CpgTraversal uses = cpg.traversal().V()
                        .has(NODE_TYPE, LOCAL_VAR)
                        .has(NAME, y);
                while (uses.hasNext()) {
                    Vertex use = (Vertex) uses.next();
                    addMayAliasEdge(cpg, use, source);
                }
            }
        }
    }

    private void aliasAnalysis(CodePropertyGraph cpg) {
        handleNewAssignStmts(cpg);
        handleRefAssignStmts(cpg);

        while (!worklist.isEmpty()) {
            log.debug("{} items on worklist", worklist.size());
            String n = worklist.poll();
            assert n != null;

            handleOutEdges(cpg, n);
            handleStoreStmts(cpg, n);
            handleLoadStmts(cpg, n);

            pt.put(n, union(pt.get(n), ptDelta.get(n)));
            ptDelta.put(n, new HashSet<>());
        }
    }

    private void handleNewAssignStmts(CodePropertyGraph cpg) {
        CpgTraversal newAssigns = cpg.traversal().getNewAssignStmts();
        log.debug("newAssigns: {}", newAssigns.clone().count().next());

        while (newAssigns.hasNext()) {
            Vertex newAssign = (Vertex) newAssigns.next();
            log.debug(newAssign.value(TEXT_LABEL));

            Vertex tgt = cpg.traversal().V(newAssign).getTgt().next();
            // Vertex val = cpg.traversal().V(newAssign).getVal().next();

            // addToPtDelta(t, v);
            worklist.add(tgt.value(NAME));
        }
    }

    private void handleRefAssignStmts(CodePropertyGraph cpg) {
        CpgTraversal refAssigns = cpg.traversal().getRefAssignStmts();
        log.debug("refAssigns: {}", refAssigns.clone().count().next());

        while (refAssigns.hasNext()) {
            Vertex refAssign = (Vertex) refAssigns.next();
            log.debug(refAssign.value(TEXT_LABEL));

            Vertex tgt = cpg.traversal().V(refAssign).getTgt().next();
            Vertex val = cpg.traversal().V(refAssign).getVal().next();
            addMayAliasEdge(cpg, val, tgt);

            addToPtDelta(tgt.value(NAME), val.value(NAME));
            worklist.add(val.value(NAME));
        }
    }

    private void handleOutEdges(CodePropertyGraph cpg, String n) {
        CpgTraversal outEdges = cpg.traversal().V()
                .has(NODE_TYPE, LOCAL_VAR)
                .has(NAME, n)
                .outE(MAY_ALIAS);

        log.debug("{} out edges", outEdges.clone().count().next());
        while (outEdges.hasNext()) {
            Edge e = (Edge) outEdges.next();
            Vertex nPrime = e.inVertex();
            diffProp(ptDelta.get(n), nPrime.value(NAME));
        }
    }

    private void handleStoreStmts(CodePropertyGraph cpg, String n) {
        CpgTraversal storeStmts = cpg.traversal().getStoreStmts(n);
        log.debug("storeStmts: {}", storeStmts.clone().count().next());

        while (storeStmts.hasNext()) {
            Vertex storeStmt = (Vertex) storeStmts.next();
            Vertex value = cpg.traversal().V(storeStmt).getVal().next();

            Set<String> ptDeltaN = ptDelta.get(n) == null ? new HashSet<>() : ptDelta.get(n);
            for (String loc : ptDeltaN) {
                CpgTraversal fieldRefs = cpg.traversal().getFieldRefs(loc);
                while (fieldRefs.hasNext()) {
                    Vertex fieldRef = (Vertex) fieldRefs.next();
                    addMayAliasEdge(cpg, value, fieldRef);
                    // diffProp(pt.get(new VertexOrDescr(value)), fieldRef);
                }
            }
        }
    }

    private void handleLoadStmts(CodePropertyGraph cpg, String n) {
        CpgTraversal loadStmts = cpg.traversal().getLoadStmts(n);
        log.debug("loadStmts: {}", loadStmts.clone().count().next());

        while (loadStmts.hasNext()) {
            Vertex storeStmt = (Vertex) loadStmts.next();
            Vertex target = cpg.traversal().V(storeStmt).getTgt().next();

            Set<String> ptDeltaN = ptDelta.get(n) == null ? new HashSet<>() : ptDelta.get(n);
            for (String loc : ptDeltaN) {
                CpgTraversal fieldRefs = cpg.traversal().getFieldRefs(loc);
                while (fieldRefs.hasNext()) {
                    Vertex fieldRef = (Vertex) fieldRefs.next();
                    addMayAliasEdge(cpg, target, fieldRef);
                    // diffProp(pt.get(new VertexOrDescr(value)), fieldRef);
                }
            }
        }
    }

    private void diffProp(Set<String> srcSet, String n) {
        Set<String> newPtDeltaN = union(ptDelta.get(n), diff(srcSet, pt.get(n)));
        if (!newPtDeltaN.equals(ptDelta.get(n))) {
            worklist.add(n);
        }
        ptDelta.put(n, newPtDeltaN);

        log.debug("ptDelta({}):", n);
        if (log.isDebugEnabled()) {
            for (String s : ptDelta.get(n)) {
                log.debug(s);
            }
        }
    }

    private Set<String> union(Set<String> a, Set<String> b) {
        HashSet<String> union = new HashSet<>();
        if (a != null) {
            union.addAll(a);
        }
        if (b != null) {
            union.addAll(b);
        }
        return union;
    }

    private Set<String> diff(Set<String> a, Set<String> b) {
        HashSet<String> diff = new HashSet<>();
        if (a != null) {
            diff.addAll(a);
        }
        if (b != null) {
            diff.removeAll(b);
        }
        return diff;
    }

    private void addToPtDelta(String n, String nPrime) {
        Set<String> ptDeltaN = ptDelta.get(n);
        if (ptDeltaN == null) {
            ptDeltaN = new HashSet<>();
        }

        ptDeltaN.add(nPrime);
        ptDelta.put(n, ptDeltaN);
    }

    private void addMayAliasEdge(CodePropertyGraph cpg, Vertex from, Vertex to) {
        log.debug("Adding edge between {} and {}", from.value(TEXT_LABEL), to.value(TEXT_LABEL));
        cpg.traversal()
                .addE(MAY_ALIAS)
                .from(from).to(to)
                .property(EDGE_TYPE, MAY_ALIAS)
                .property(TEXT_LABEL, MAY_ALIAS)
                .iterate();
    }

}
