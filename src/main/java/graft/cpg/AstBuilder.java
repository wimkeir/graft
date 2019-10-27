package graft.cpg;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.Ref;

import graft.Graft;
import graft.cpg.visitors.ConstantVisitor;
import graft.cpg.visitors.ExprVisitor;
import graft.cpg.visitors.RefVisitor;

import static graft.cpg.CpgUtil.*;

/**
 * Generate AST nodes and subtrees.
 *
 * @author Wim Keirsgieter
 */
public class AstBuilder {

    private static Logger log = LoggerFactory.getLogger(AstBuilder.class);

    // ********************************************************************************************
    // public methods
    // ********************************************************************************************

    /**
     * Generate an AST node (possibly a subtree) for the given Soot value.
     *
     * @param value the Soot value
     * @return the generated AST node
     */
    public static Vertex genValueNode(Value value) {
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
    private static Vertex genLocalNode(Local local) {
        return (Vertex) Graft.cpg().traversal()
                .addLocalNode(getTypeString(local.getType()), local.toString(), local.getName())
                .next();
    }

    // Generates an AST node for a constant value using the ConstantVisitor
    private static Vertex genConstantNode(Constant constant) {
        ConstantVisitor visitor = new ConstantVisitor();
        constant.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for an expression using the ExprVisitor
    private static Vertex genExprNode(Expr expr) {
        ExprVisitor visitor = new ExprVisitor();
        expr.apply(visitor);
        return (Vertex) visitor.getResult();
    }

    // Generates an AST node for a reference using the RefVisitor
    private static Vertex genRefNode(Ref ref) {
        RefVisitor visitor = new RefVisitor();
        ref.apply(visitor);
        return (Vertex) visitor.getResult();
    }

}
