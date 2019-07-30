package graft.cpg;

import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;

import graft.traversal.CpgTraversalSource;
import graft.utils.GraphUtil;

import static graft.Const.*;

/**
 * Handles construction of intraprocedural PDG edges.
 *
 * @author Wim Keirsgieter
 */
public class PdgBuilder {

    private static Logger log = LoggerFactory.getLogger(PdgBuilder.class);
    private static Map<Value, Vertex> symbolTable = new HashMap<>();

    /**
     * Generate incoming PDG edges for the given vertex, and update the symbol table.
     *
     * @param unit the unit corresponding to the given vertex
     * @param vertex the vertex to handle
     */
    public static void handleCfgNode(Unit unit, Vertex vertex) {
        // TODO: test with redefined vars

        // For all local variable uses in the statement, draw PDG edges from their sources to the statement node
        for (ValueBox useBox : unit.getUseBoxes()) {
            Value value = useBox.getValue();
            Vertex source = symbolTable.get(value);
            if (source == null) {
                continue;
            }
            assert value instanceof Local;
            String varName = ((Local) value).getName();
            genDataDepEdge(source, vertex, varName, varName);
        }

        // For all definitions in the statement, update the local variable entries in the symbol table
        for (ValueBox defBox : unit.getDefBoxes()) {
            symbolTable.put(defBox.getValue(), vertex);
        }
    }

    private static Edge genDataDepEdge(Vertex from, Vertex to, String varName, String textLabel) {
        CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
        return g.addE(PDG_EDGE)
                .from(from).to(to)
                .property(EDGE_TYPE, DATA_DEP)
                .property(VAR_NAME, varName)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

}
