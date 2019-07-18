package graft.cpg;

import java.util.function.Consumer;

import com.github.javaparser.ast.Node;

import org.apache.tinkerpop.gremlin.structure.Graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: javadoc
 */
public class AstWalker implements Consumer<Node> {

    private static Logger log = LoggerFactory.getLogger(AstWalker.class);

    private Graph graph;
    private AstNodeVisitor visitor;

    public AstWalker(Graph graph) {
        this.graph = graph;
        visitor = new AstNodeVisitor();
    }

    @Override
    public void accept(Node node) {
        node.accept(visitor, graph);
    }

}
