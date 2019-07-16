package graft.cpg;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<Graph> {

    private static Logger log = LoggerFactory.getLogger(AstNodeVisitor.class);

    @Override
    public void defaultAction(Node node, Graph graph) {
        log.debug(node.getClass() + ": " + node.toString());
    }

}