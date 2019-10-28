package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;
import soot.jimple.*;

import graft.Graft;
import graft.cpg.AstBuilder;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;

/**
 * Visitor applied to expressions to create AST nodes (or subtrees) for them.
 *
 * @author Wim Keirsgieter
 */
public class ExprVisitor extends AbstractExprSwitch {

    private static Logger log = LoggerFactory.getLogger(ExprVisitor.class);

    private AstBuilder astBuilder;

    public ExprVisitor(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }

    // ********************************************************************************************
    // binary expressions
    // ********************************************************************************************

    @Override
    public void caseAddExpr(AddExpr expr) {
        caseBinaryExpr(expr, PLUS);
    }

    @Override
    public void caseAndExpr(AndExpr expr) {
        caseBinaryExpr(expr, AND);
    }

    @Override
    public void caseCmpExpr(CmpExpr expr) {
        caseBinaryExpr(expr, CMP);
    }

    @Override
    public void caseCmpgExpr(CmpgExpr expr) {
        caseBinaryExpr(expr, CMPG);
    }

    @Override
    public void caseCmplExpr(CmplExpr expr) {
        caseBinaryExpr(expr, CMPL);
    }

    @Override
    public void caseDivExpr(DivExpr expr) {
        caseBinaryExpr(expr, DIVIDE);
    }

    @Override
    public void caseEqExpr(EqExpr expr) {
        caseBinaryExpr(expr, EQUALS);
    }

    @Override
    public void caseNeExpr(NeExpr expr) {
        caseBinaryExpr(expr, NOT_EQUALS);
    }

    @Override
    public void caseGeExpr(GeExpr expr) {
        caseBinaryExpr(expr, GREATER_EQUALS);
    }

    @Override
    public void caseGtExpr(GtExpr expr) {
        caseBinaryExpr(expr, GREATER);
    }

    @Override
    public void caseLeExpr(LeExpr expr) {
        caseBinaryExpr(expr, LESS_EQUALS);
    }

    @Override
    public void caseLtExpr(LtExpr expr) {
        caseBinaryExpr(expr, LESS);
    }

    @Override
    public void caseMulExpr(MulExpr expr) {
        caseBinaryExpr(expr, MULTIPLY);
    }

    @Override
    public void caseOrExpr(OrExpr expr) {
        caseBinaryExpr(expr, OR);
    }

    @Override
    public void caseRemExpr(RemExpr expr) {
        caseBinaryExpr(expr, REMAINDER);
    }

    @Override
    public void caseShlExpr(ShlExpr expr) {
        caseBinaryExpr(expr, LEFT_SHIFT);
    }

    @Override
    public void caseShrExpr(ShrExpr expr) {
        caseBinaryExpr(expr, SIGNED_RIGHT_SHIFT);
    }

    @Override
    public void caseUshrExpr(UshrExpr expr) {
        caseBinaryExpr(expr, UNSIGNED_RIGHT_SHIFT);
    }

    @Override
    public void caseSubExpr(SubExpr expr) {
        caseBinaryExpr(expr, MINUS);
    }

    @Override
    public void caseXorExpr(XorExpr expr) {
        caseBinaryExpr(expr, XOR);
    }

    // ********************************************************************************************
    // unary expressions
    // ********************************************************************************************

    @Override
    public void caseLengthExpr(LengthExpr expr) {
        caseUnaryExpr(expr, LENGTH);
    }

    @Override
    public void caseNegExpr(NegExpr expr) {
        caseUnaryExpr(expr, NEGATION);
    }

    // ********************************************************************************************
    // invoke expressions
    // ********************************************************************************************

    @Override
    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr invokeExpr) {
        caseInvokeExpr(invokeExpr, INTERFACE, invokeExpr.getBase());
    }

        @Override
    public void caseSpecialInvokeExpr(SpecialInvokeExpr invokeExpr) {
        caseInvokeExpr(invokeExpr, SPECIAL, invokeExpr.getBase());
    }

    @Override
    public void caseStaticInvokeExpr(StaticInvokeExpr invokeExpr) {
        caseInvokeExpr(invokeExpr, STATIC, null);
    }

    @Override
    public void caseVirtualInvokeExpr(VirtualInvokeExpr invokeExpr) {
        caseInvokeExpr(invokeExpr, VIRTUAL, invokeExpr.getBase());
    }

    @Override
    public void caseDynamicInvokeExpr(DynamicInvokeExpr invokeExpr) {
        caseInvokeExpr(invokeExpr, DYNAMIC, null);
    }

    // ********************************************************************************************
    // other expressions
    // ********************************************************************************************

    @Override
    public void caseNewExpr(NewExpr expr) {
        setResult(Graft.cpg().traversal()
            .addExprNode(NEW_EXPR, expr.toString(), getTypeString(expr.getType()))
            .property(NEW_EXPR_TYPE, NEW_EXPR)
            .next()
        );
    }

    @Override
    public void caseNewArrayExpr(NewArrayExpr expr) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(NEW_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(NEW_EXPR_TYPE, NEW_ARRAY_EXPR)
                .property(BASE_TYPE, getTypeString(expr.getBaseType()))
                .next();

        Graft.cpg().traversal()
                .addAstE(SIZE, SIZE)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getSize()))
                .iterate();

        setResult(exprNode);
    }

    @Override
    public void caseNewMultiArrayExpr(NewMultiArrayExpr expr) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(NEW_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(NEW_EXPR_TYPE, NEW_ARRAY_EXPR)
                .property(BASE_TYPE, getTypeString(expr.getBaseType()))
                .next();

        int i = 0;
        for (Value size : expr.getSizes()) {
            Graft.cpg().traversal()
                    .addAstE(SIZE, SIZE)
                    .from(exprNode)
                    .to(astBuilder.genValueNode(size))
                    .property(DIM, i++)
                    .iterate();
        }

        setResult(exprNode);
    }

    @Override
    public void caseInstanceOfExpr(InstanceOfExpr expr) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(INSTANCEOF_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(CHECK_TYPE, getTypeString(expr.getCheckType()))
                .next();

        Graft.cpg().traversal()
                .addAstE(OPERAND, OPERAND)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getOp()))
                .iterate();

        setResult(exprNode);
    }

    @Override
    public void caseCastExpr(CastExpr expr) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(CAST_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(CAST_TYPE, getTypeString(expr.getCastType()))
                .next();

        Graft.cpg().traversal()
                .addAstE(OPERAND, OPERAND)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getOp()))
                .iterate();

        setResult(exprNode);
    }

    // ********************************************************************************************
    // default
    // ********************************************************************************************

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Expr type '{}', no AST node generated", obj.getClass());
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    // Generates an AST subtree for a binary expression and its operands
    private void caseBinaryExpr(BinopExpr expr, String operator) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(BINARY_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(OPERATOR, operator)
                .next();

        Graft.cpg().traversal()
                .addAstE(LEFT_OPERAND, LEFT_OPERAND)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getOp1()))
                .iterate();

        Graft.cpg().traversal()
                .addAstE(RIGHT_OPERAND, RIGHT_OPERAND)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getOp2()))
                .iterate();

        setResult(exprNode);
    }

    // Generates an AST subtree for a unary expression and its operand
    private void caseUnaryExpr(UnopExpr expr, String operator) {
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(UNARY_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(OPERATOR, operator)
                .next();

        Graft.cpg().traversal()
                .addAstE(LEFT_OPERAND, LEFT_OPERAND)
                .from(exprNode)
                .to(astBuilder.genValueNode(expr.getOp()))
                .iterate();

        setResult(exprNode);
    }

    // Generates an AST subtree for an invoke expression and possibly its base and args
    private void caseInvokeExpr(InvokeExpr expr, String invokeType, Value base) {
        // TODO: use INVOKES instead of METHOD_SIG
        Vertex exprNode = (Vertex) Graft.cpg().traversal()
                .addExprNode(INVOKE_EXPR, expr.toString(), getTypeString(expr.getType()))
                .property(INVOKE_TYPE, invokeType)
                .property(METHOD_SIG, expr.getMethod().getSignature())
                .next();

        if (base != null) {
            Graft.cpg().traversal()
                    .addAstE(BASE, BASE)
                    .from(exprNode)
                    .to(astBuilder.genValueNode(base))
                    .iterate();
        }

        int i = 0;
        for (Value arg : expr.getArgs()) {
            Graft.cpg().traversal()
                    .addAstE(ARG, ARG)
                    .from(exprNode)
                    .to(astBuilder.genValueNode(arg))
                    .property(INDEX, i++)
                    .iterate();
        }

        setResult(exprNode);
    }

}
