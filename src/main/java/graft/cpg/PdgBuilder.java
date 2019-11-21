package graft.cpg;

import java.util.Iterator;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;

import graft.Graft;
import graft.GraftRuntimeException;
import graft.traversal.CpgTraversalSource;

/**
 * Handles construction of intraprocedural PDG edges.
 *
 * @author Wim Keirsgieter
 */
public class PdgBuilder {

    private static Logger log = LoggerFactory.getLogger(PdgBuilder.class);

    /**
     * Generate a PDG for the given unit graph, using Soot's local defs analysis.
     *
     * @param unitGraph the unit graph to generate the PDG for
     * @param unitNodes a mapping of units to their vertices in the CPG
     */
    public static void buildPdg(UnitGraph unitGraph, Map<Unit, Vertex> unitNodes) {
        log.debug("Building PDG for method '{}'", unitGraph.getBody().getMethod().getName());
        CpgTraversalSource g = Graft.cpg().traversal();

        LocalDefs localDefs = new SimpleLocalDefs(unitGraph);

        Iterator<Unit> units = unitGraph.iterator();
        while (units.hasNext()) {
            Unit unit = units.next();
            if (!unitNodes.containsKey(unit) || unitNodes.get(unit) ==  null) {
                continue;
            }
            Vertex unitVertex = unitNodes.get(unit);
            for (ValueBox valueBox : unit.getUseBoxes()) {
                Value value = valueBox.getValue();
                if (!(value instanceof Local)) {
                    continue;
                }
                Local local = (Local) value;
                for (Unit defSite : localDefs.getDefsOfAt(local, unit)) {
                    if (unitNodes.get(defSite) == null) {
                        log.warn("No def site for local '{}' in method '{}'",
                                local.getName(),
                                unitGraph.getBody().getMethod().getSignature());
                        throw new GraftRuntimeException();
                        //continue;
                    }
                    Vertex defVertex = g.V(unitNodes.get(defSite)).next();
                    Graft.cpg().traversal()
                            .addDataDepE(local.getName())
                            .from(defVertex).to(unitVertex)
                            .iterate();
                }
            }
        }
    }

}
