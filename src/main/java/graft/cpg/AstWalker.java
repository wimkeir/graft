package graft.cpg;

import java.util.function.Consumer;

import com.github.javaparser.ast.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.cpg.context.AstWalkContext;
import graft.cpg.context.ContextStack;


/**
 * TODO: javadoc
 */
public class AstWalker implements Consumer<Node> {

    private static Logger log = LoggerFactory.getLogger(AstWalker.class);

    private ContextStack contextStack;

    public AstWalker() {
        contextStack = new ContextStack();
        contextStack.pushNewContext(new AstWalkContext());

        log.debug("New AstWalker initialised");
        log.debug(contextStack.toString());
    }

    @Override
    public void accept(Node node) {
        node.removeComment();
        contextStack = node.accept(new AstNodeVisitor(), contextStack);
    }

}
