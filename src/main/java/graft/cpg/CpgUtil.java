package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;

import graft.Graft;
import graft.cpg.visitors.TypeVisitor;
import graft.traversal.CpgTraversal;
import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Utility methods for CPG construction.
 *
 * @author Wim Keirsgieter
 */
public class CpgUtil {

    private static Logger log = LoggerFactory.getLogger(CpgUtil.class);

    public static String getFileName(Vertex v) {
        CpgTraversalSource g = Graft.cpg().traversal();
        return g.V(v)
                .until(hasLabel(CFG_NODE)).repeat(in())
                .until(has(NODE_TYPE, ENTRY)).repeat(in(CFG_EDGE))
                .until(has(NODE_TYPE, CLASS)).repeat(in(AST_EDGE))
                .values(FILE_PATH).next().toString();
    }

    /**
     * Get the number of nodes currently in the CPG.
     *
     * @return the number of nodes in the CPG
     */
    public static long getNodeCount() {
        CpgTraversalSource g = Graft.cpg().traversal();
        return g.V().count().next();
    }

    /**
     * Get the number of edges currently in the CPG.
     *
     * @return the number of edges in the CPG
     */
    public static long getEdgeCount() {
        CpgTraversalSource g = Graft.cpg().traversal();
        return g.E().count().next();
    }

    public static void dropCfg(SootMethod method) {
        log.debug("Dropping method '{}'", method.getName());
        CpgTraversalSource g = Graft.cpg().traversal();
        String methodSig = method.getSignature();

        // delete all method nodes and any interproc edges to/from them
        g.V().hasLabel(CFG_NODE)
                .has(METHOD_SIG, methodSig)
                .store("d")
                .repeat(out(AST_EDGE).store("d"))
                .cap("d").unfold()
                .drop()
                .iterate();
    }

    public static String getClassHash(String className) {
        CpgTraversalSource g = Graft.cpg().traversal();
        CpgTraversal t = g.V()
                .hasLabel(AST_NODE)
                .has(NODE_TYPE, CLASS)
                .has(FULL_NAME, className)
                .values(FILE_HASH);
        if (t.hasNext()) {
            return t.next().toString();
        } else {
            log.warn("Cannot get file hash of class '{}': no results", className);
            return "";
        }
    }

    /**
     * Get the string name of a Soot type using the type visitor.
     *
     * @param type the type to get the name of
     * @return the string name of the given type
     */
    public static String getTypeString(Type type) {
        TypeVisitor visitor = new TypeVisitor();
        type.apply(visitor);
        return visitor.getResult().toString();
    }

    /**
     * Get the CFG root of an AST node (returns the given vertex if it is already a CFG node).
     *
     * @param vertex the node to find the CFG root of
     * @return the CFG root of the node, or the node itself if it is already a CFG node
     */
    public static Vertex getCfgRoot(Vertex vertex) {
        if (vertex.label().equals(CFG_NODE))  {
            return vertex;
        }

        CpgTraversalSource g = Graft.cpg().traversal();
        return g.V(vertex)
                .repeat(in(AST_EDGE))
                .until(hasLabel(CFG_NODE))
                .next();
    }

    /**
     * Get a string representation of a vertex and its properties for debugging.
     *
     * @param vertex the vertex to get a string representation of
     * @return a string representation of the given vertex
     */
    public static String debugVertex(Vertex vertex) {
        return debugElement(vertex, NODE_TYPE);
    }

    /**
     * Get a string representation of an edge and its properties for debugging.
     *
     * @param edge the edge to get a string representation of
     * @return a string representation of the given edge
     */
    public static String debugEdge(Edge edge) {
        return debugElement(edge, EDGE_TYPE);
    }

    private static String debugElement(Element element, String typeKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(element.label()).append(" ");
        sb.append("(").append(element.value(typeKey).toString()).append(")");
        for (String key : element.keys()) {
            sb.append(" ");
            sb.append(key).append("='").append(element.value(key).toString()).append("'");
        }
        sb.append(">");
        return sb.toString();
    }

}
