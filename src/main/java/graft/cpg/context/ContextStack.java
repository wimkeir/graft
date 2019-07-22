package graft.cpg.context;

import java.util.Stack;

public class ContextStack {

    private Stack<AstWalkContext> contextStack;

    public ContextStack() {
        contextStack = new Stack<>();
    }

    public void setCurrentContext(AstWalkContext context) {
        if (!contextStack.isEmpty()) {
            contextStack.pop();
        }
        contextStack.push(context);
    }

    public AstWalkContext getCurrentContext() {
        return contextStack.peek();
    }

    public AstWalkContext popCurrentContext() {
        return contextStack.pop();
    }

    public void pushNewContext(AstWalkContext context) {
        contextStack.push(context);
    }

    public int size() {
        return contextStack.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CONTEXT STACK (").append(size()).append(" contexts)\n");
        for (AstWalkContext context : contextStack) {
            sb.append(context.toString());
        }
        return sb.toString();
    }

}
