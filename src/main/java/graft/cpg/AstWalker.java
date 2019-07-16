package graft.cpg;

import java.util.function.Consumer;

import com.github.javaparser.ast.Node;

import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * TODO: javadoc
 */
public class AstWalker implements Consumer<Node> {

    private Graph graph;

    public AstWalker(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void accept(Node node) {
        node.accept(new AstNodeVisitor(), graph);
    }

}
