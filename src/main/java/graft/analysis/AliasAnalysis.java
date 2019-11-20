package graft.analysis;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import graft.Banner;
import graft.Graft;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.traversal.__.*;
import static graft.utils.DisplayUtil.*;

/**
 *
 */
public class AliasAnalysis implements GraftAnalysis {

    // TODO:
    // javadocs
    // run may-alias locally per method
    // get rid of traversal warnings?
    // nice banner output
    // define algorithm

    private static Logger log = LoggerFactory.getLogger(AliasAnalysis.class);

    /**
     *
     */
    public AliasAnalysis() {

    }

    @Override
    public void doAnalysis() {
        mayAlias();
    }

    private void mayAlias(Vertex entry) {
        // TODO
//        String methodSig = entry.value(METHOD_SIG);
//        log.debug("Running may-alias analysis for method {}", methodSig);
//
//        Map<String, Set<String>> pts = new HashMap<>();
    }

    private void mayAlias() {
        Banner banner = new Banner("Alias analysis");

        // TODO: do this locally within a method (use stmt edges)
        log.debug("Running mayAlias");
        Map<String, Set<String>> pts = new HashMap<>();

        long start = System.currentTimeMillis();
        CpgTraversal refAssigns = Graft.cpg().traversal().getRefAssignStmts();
        log.debug("{} ref assigns", refAssigns.clone().count().next());

        while (refAssigns.hasNext()) {
            Vertex refAssign = (Vertex) refAssigns.next();
            log.debug("Ref assign '{}'", refAssign.value(TEXT_LABEL).toString());
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

        banner.println(pts.keySet().size() + " refs analysed");
        int nrEdges = 0;
        for (String key : pts.keySet()) {
            log.debug("Points to set of {}", key);
            for (String val : pts.get(key)) {
                log.debug(val);
                log.debug("Adding may-alias edge between {} and {}", key, val);
                nrEdges++;
                Graft.cpg().traversal()
                        .locals(key).as("v")
                        .locals(val)
                        .coalesce(
                                inE(MAY_ALIAS).where(outV().as("v")),
                                addE(MAY_ALIAS)
                                .from("v")
                                .property(EDGE_TYPE, MAY_ALIAS)
                                .property(TEXT_LABEL, MAY_ALIAS)
                        ).iterate();
            }
        }

        banner.println(nrEdges + " may-alias edges added");
        banner.println("Time elapsed: " + displayTime(System.currentTimeMillis() - start));
        banner.display();
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
