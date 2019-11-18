package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Type;

import graft.Graft;
import graft.cpg.visitors.TypeVisitor;
import graft.traversal.CpgTraversal;

import static graft.Const.*;
import static graft.traversal.__.*;

/**
 * Utility methods for CPG construction.
 *
 * @author Wim Keirsgieter
 */
public class CpgUtil {

    private static Logger log = LoggerFactory.getLogger(CpgUtil.class);

    /**
     * Given a vertex in the CPG, traverse up to its class node and return the source file.
     *
     * @param v the vertex
     * @return the source file containing the vertex
     */
    public static String getFileName(Vertex v) {
        return Graft.cpg().traversal().V(v)
                .until(has(NODE_TYPE, CLASS)).repeat(in(AST_EDGE))
                .values(FILE_PATH).next().toString();
    }

    /**
     * Drop the given method from the CPG.
     *
     * @param methodSig the signature of the method to drop
     */
    public static void dropMethod(String methodSig) {
        Graft.cpg().traversal()
                .entryOf(methodSig).store("d")
                .repeat(astOut().store("d"))
                .cap("d")
                .unfold().drop().iterate();
    }

    /**
     * Drop the given class from the CPG
     *
     * @param fullName the full name of the class to drop
     */
    public static void dropClass(String fullName) {
        Graft.cpg().traversal()
                .classOf(fullName).store("d")
                .repeat(astOut().store("d"))
                .cap("d")
                .unfold().drop().iterate();
    }

    /**
     * Get the stored hash of the given class file.
     *
     * @param className the name of the class
     * @return the stored hash of the class, if found (else empty string)
     */
    public static String getClassHash(String className) {
        CpgTraversal t = Graft.cpg().traversal().classOf(className).values(FILE_HASH);
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
