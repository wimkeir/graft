package graft.cpg;

import java.util.Iterator;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
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

import graft.db.GraphUtil;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

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
    public static void buildPdg(UnitGraph unitGraph, Map<Unit, Object> unitNodes) {
        log.debug("Building PDG for method '{}'", unitGraph.getBody().getMethod().getName());
        GraphTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);

        LocalDefs localDefs = new SimpleLocalDefs(unitGraph);

        Iterator<Unit> units = unitGraph.iterator();
        while (units.hasNext()) {
            Unit unit = units.next();
            if (!unitNodes.containsKey(unit) || unitNodes.get(unit) ==  null) {
                continue;
            }
            Vertex unitVertex = g.V(unitNodes.get(unit)).next();
            for (ValueBox valueBox : unit.getUseBoxes()) {
                Value value = valueBox.getValue();
                if (!(value instanceof Local)) {
                    continue;
                }
                Local local = (Local) value;
                for (Unit defSite : localDefs.getDefsOfAt(local, unit)) {
                    Vertex defVertex = g.V(unitNodes.get(defSite)).next();
                    genDataDepEdge(defVertex, unitVertex, local.getName(), local.getName());
                }
            }
        }
    }

    /**
     * Generate a data dependency PDG edge for the given variable, between the two given CFG nodes.
     *
     * @param from the start node of the edge
     * @param to the end node of the edge
     * @param varName the name of the dependent variable
     * @param textLabel a text label for the node
     * @return the generated PDG edge
     */
    public static Edge genDataDepEdge(Vertex from, Vertex to, String varName, String textLabel) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        Edge edge = g.addE(PDG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, DATA_DEP)
                .property(VAR_NAME, varName)
                .property(TEXT_LABEL, textLabel)
                .next();
        // GraphUtil.graph().tx().commit();
        return edge;
    }

}
