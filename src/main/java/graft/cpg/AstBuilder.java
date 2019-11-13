package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefType;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.Ref;

import graft.Graft;
import graft.cpg.visitors.ConstantVisitor;
import graft.cpg.visitors.ExprVisitor;
import graft.cpg.visitors.RefVisitor;

import java.util.HashMap;
import java.util.Map;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;

/**
 * Generate AST nodes and subtrees.
 *
 * @author Wim Keirsgieter
 */
public class AstBuilder {

    // TODO:
    // sort out locals business

    private static Logger log = LoggerFactory.getLogger(AstBuilder.class);

    private Map<Local, Vertex> locals;

    // ********************************************************************************************
    // public methods
    // ********************************************************************************************

    public AstBuilder() {
        this.locals = new HashMap<>();
    }

    /**
     * Generate an AST node (possibly a subtree) for the given Soot value.
     *
     * @param value the Soot value
     * @return the generated AST node
     */
    public Vertex genValueNode(Value value) {
        if (value instanceof Local) {
            return genLocalNode((Local) value);
        } else if (value instanceof Constant) {
            return genConstantNode((Constant) value);
        } else if (value instanceof Expr) {
            return genExprNode((Expr) value);
        } else if (value instanceof Ref) {
            return genRefNode((Ref) value);
        } else {
            log.warn("Unhandled Value type '{}', no AST node generated", value.getClass().getSimpleName());
            return null;
        }
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generates an AST node for a local variable
    private Vertex genLocalNode(Local local) {
        Vertex localNode = locals.get(local);
        if (localNode == null) {
            boolean refType = local.getType() instanceof RefType;
            localNode = (Vertex) Graft.cpg().traversal()
                    .addLocalNode(getTypeString(local.getType()), local.toString(), local.getName())
                    .property(REF_TYPE, refType)
                    .next();
//            locals.put(local, localNode);
        }
        return localNode;
    }

    // Generates an AST node for a constant value using the ConstantVisitor
    private Vertex genConstantNode(Constant constant) {
        ConstantVisitor visitor = new ConstantVisitor();
        constant.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for an expression using the ExprVisitor
    private Vertex genExprNode(Expr expr) {
        ExprVisitor visitor = new ExprVisitor(this);
        expr.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for a reference using the RefVisitor
    private Vertex genRefNode(Ref ref) {
        RefVisitor visitor = new RefVisitor(this);
        ref.apply(visitor);
        return (Vertex) visitor.getResult();
    }

}
