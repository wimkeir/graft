package graft.utils;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import graft.db.GraphUtil;

import static graft.Const.*;

/**
 * Utilities for writing the CPG to a dot file.
 *
 * @author Wim Keirsgieter
 */
public class DotUtil {

    private static Logger log = LoggerFactory.getLogger(DotUtil.class);

    /**
     * Write the CPG (as currently in the database) to the given file in dot format.
     *
     * @param dotfile the dot file to write to
     * @param graphName the name of the graph
     */
    public static void cpgToDot(String dotfile, String graphName) {
        graphToDot(GraphUtil.graph(), dotfile, graphName);
    }

    public static void graphToDot(Graph graph, String filename, String graphName) {
        GraphTraversalSource g = graph.traversal();
        try {
            FileWriter out = new FileWriter(new File(filename));
            out.write("digraph " + graphName + "{\n");
            GraphTraversal nodes = g.V();
            while (nodes.hasNext()) {
                vertexToDot((Vertex) nodes.next(), out);
            }
            GraphTraversal edges = g.E();
            while (edges.hasNext()) {
                edgeToDot((Edge) edges.next(), out);
            }
            out.write("}");
            out.close();
        } catch (IOException e) {
            log.error("Unable to write graph '{}' to dotfile '{}'", graphName, filename, e);
        }
    }

    private static void vertexToDot(Vertex v, FileWriter out) throws IOException {
        out.write(v.id() + " [style=filled, shape=box");
        switch (v.label()) {
            case CFG_NODE:
                cfgNodeToDot(v, out);
                break;
            case AST_NODE:
                astNodeToDot(v, out);
                break;
            default:
                log.warn("There are nodes with unrecognized labels in the CPG: '{}'", v.label());
        }
    }

    private static void edgeToDot(Edge e, FileWriter out) throws IOException {
        out.write(e.outVertex().id() + " -> " + e.inVertex().id());
        out.write(" [style=bold");
        switch (e.label()) {
            case CFG_EDGE:
                cfgEdgeToDot(e, out);
                break;
            case AST_EDGE:
                astEdgeToDot(e, out);
                break;
            case PDG_EDGE:
                pdgEdgeToDot(e, out);
                break;
            default:
                log.warn("There are edges with unrecognized labels in the CPG: '{}'", e.label());
        }
    }

    private static void cfgNodeToDot(Vertex v, FileWriter out) throws IOException {
        out.write(", color=blue");
        out.write(", label=\"" + v.value(TEXT_LABEL).toString().replace("\"", "'"));
        out.write("\"];\n");
    }

    private static void astNodeToDot(Vertex v, FileWriter out) throws IOException {
        out.write(", color=green");
        out.write(", label=\"" + v.value(TEXT_LABEL).toString().replace("\"", "'"));
        out.write("\"];\n");
    }

    private static void cfgEdgeToDot(Edge e, FileWriter out) throws IOException {
        out.write(", color=blue");
        out.write(", label=\"" + e.value(TEXT_LABEL).toString());
        out.write("\"];\n");
    }

    private static void astEdgeToDot(Edge e, FileWriter out) throws IOException {
        out.write(", color=green, label=\"" + e.value(TEXT_LABEL) + "\"];\n");
    }

    private static void pdgEdgeToDot(Edge e, FileWriter out) throws IOException {
        out.write(", color=red, label=\"" + e.value(TEXT_LABEL) + "\"];\n");
    }

}
