package graft.utils;

import graft.db.GraphUtil;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static graft.Const.*;

public class DotUtil {

    private static Logger log = LoggerFactory.getLogger(DotUtil.class);

    public static void cpgToDot(String dotfile, String graphName) {
        log.info("Writing CPG to dotfile '{}'", dotfile);
        try {
            FileWriter out = new FileWriter(new File(dotfile));
            out.write("digraph " + graphName + "{\n");

            CpgTraversalSource g = GraphUtil.graph().traversal(CpgTraversalSource.class);
            CpgTraversal nodes = g.V();
            while (nodes.hasNext()) {
                vertexToDot((Vertex) nodes.next(), out);
            }
            CpgTraversal edges = g.E();
            while (edges.hasNext()) {
                edgeToDot((Edge) edges.next(), out);
            }

            out.write("}");
            out.close();
        } catch (IOException e) {
            log.error("Unable to write CPG to dotfile", e);
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
        String type = e.value(EDGE_TYPE).toString();
        out.write(", color=green, label=\"" + type + "\"];\n");
    }

}
