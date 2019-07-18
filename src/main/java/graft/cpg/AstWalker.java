package graft.cpg;

import java.util.function.Consumer;

import com.github.javaparser.ast.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: javadoc
 */
public class AstWalker implements Consumer<Node> {

    private static Logger log = LoggerFactory.getLogger(AstWalker.class);

    private AstWalkContext context;

    AstWalker() {
        this.context = new AstWalkContext();
    }

    @Override
    public void accept(Node node) {
        node.accept(new AstNodeVisitor(), context);
    }

}
