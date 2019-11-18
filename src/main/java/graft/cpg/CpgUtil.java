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

    // TODO
    // javadocs
    // more refactoring, convenience methods

    private static Logger log = LoggerFactory.getLogger(CpgUtil.class);

    public static String getFileName(Vertex v) {
        return Graft.cpg().traversal().V(v)
                .until(has(NODE_TYPE, CLASS)).repeat(in(AST_EDGE))
                .values(FILE_PATH).next().toString();
    }

    public static void dropMethod(String methodSig) {
        Graft.cpg().traversal()
                .entryOf(methodSig).store("d")
                .repeat(astOut().store("d"))
                .cap("d")
                .unfold().drop().iterate();
    }

    public static void dropClass(String fullName) {
        Graft.cpg().traversal()
                .classOf(fullName).store("d")
                .repeat(astOut().store("d"))
                .cap("d")
                .unfold().drop().iterate();
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
            log.debug("Cannot get file hash of class '{}': no results", className);
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
