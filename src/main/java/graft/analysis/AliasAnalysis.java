package graft.analysis;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import graft.Banner;
import graft.Graft;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;
import static graft.traversal.__.*;
import static graft.utils.DisplayUtil.*;

/**
 * Basic, intraprocedural may-alias analysis.
 */
public class AliasAnalysis implements GraftAnalysis {

    private static Logger log = LoggerFactory.getLogger(AliasAnalysis.class);

    private Banner banner;

    /**
     * Instantiate a new alias analysis.
     */
    public AliasAnalysis() {
        banner = new Banner("Alias Analysis");
    }

    @Override
    public void doAnalysis() {
        CpgTraversal entries = Graft.cpg().traversal().entries();

        int nrMethods = 0;
        long start = System.currentTimeMillis();
        while (entries.hasNext()) {
            Vertex entry = (Vertex) entries.next();
            mayAlias(entry);
            nrMethods++;
        }
        long end = System.currentTimeMillis();

        banner.println("Alias analysis successfully performed on " + nrMethods + " methods");
        banner.println("Elapsed time: " + displayTime(end - start));
        banner.display();
    }

    @SuppressWarnings("unchecked")
    private void mayAlias(Vertex entry) {
        String methodSig = entry.value(METHOD_SIG);
        log.debug("Running mayAlias on method '{}'", methodSig);
        Map<String, Set<String>> pts = new HashMap<>();

        CpgTraversal refAssigns = Graft.cpg().traversal()
                .getRefAssignStmts()
                .where(astIn(STATEMENT).values(METHOD_SIG).is(methodSig));
        log.debug("{} ref assigns", refAssigns.clone().count().next());

        while (refAssigns.hasNext()) {
            Vertex refAssign = (Vertex) refAssigns.next();
            String tgtName = Graft.cpg().traversal()
                    .V(refAssign)
                    .getTgt()
                    .values(NAME).next().toString();
            String valName = Graft.cpg().traversal()
                    .V(refAssign)
                    .getVal()
                    .values(NAME).next().toString();
            addToSet(pts, tgtName, valName);
        }
        log.debug("Points-to sets filled");

        for (String key : pts.keySet()) {
            for (String val : pts.get(key)) {
                CpgTraversal keyLocals = astNodes(entry).locals(key);
                astNodes(entry).copy()
                        .locals(val)
                        .coalesce(
                                inE(MAY_ALIAS).where(outV().is(keyLocals.copy())),
                                addE(MAY_ALIAS)
                                .from(keyLocals.copy())
                                .property(EDGE_TYPE, MAY_ALIAS)
                                .property(TEXT_LABEL, MAY_ALIAS)
                        ).iterate();
            }
        }
    }

    private void addToSet(Map<String, Set<String>> map, String key, String val) {
        Set<String> set = map.get(key);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(val);
        map.put(key, set);
    }

}
