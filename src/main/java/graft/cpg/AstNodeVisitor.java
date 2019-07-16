package graft.cpg;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<Graph> {

    @Override
    public void defaultAction(Node node, Graph graph) {
        System.out.println(node.getClass() + ": " + node.toString());
    }

}