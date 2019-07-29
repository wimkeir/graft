package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;
import soot.jimple.*;

import graft.cpg.AstBuilder;
import graft.cpg.CpgUtil;

import static graft.Const.*;

/**
 * Visitor applied to expressions to create AST nodes (or subtrees) for them.
 *
 * @author Wim Keirsgieter
 */
public class ExprVisitor extends AbstractExprSwitch {

    private static Logger log = LoggerFactory.getLogger(ExprVisitor.class);

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
        Vertex exprVertex = AstBuilder.genAstNode(NEW_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, BASE_TYPE, CpgUtil.getTypeString(expr.getBaseType()));
        setResult(exprVertex);
    }

    @Override
    public void caseNewArrayExpr(NewArrayExpr expr) {
        Vertex exprVertex = AstBuilder.genAstNode(NEW_ARRAY_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, BASE_TYPE, CpgUtil.getTypeString(expr.getBaseType()));

        Vertex sizeVertex = AstBuilder.genValueNode(expr.getSize());
        Edge sizeEdge = AstBuilder.genAstEdge(exprVertex, sizeVertex, SIZE, SIZE);
        CpgUtil.addEdgeProperty(sizeEdge, DIM, 0);

        setResult(exprVertex);
    }

    @Override
    public void caseNewMultiArrayExpr(NewMultiArrayExpr expr) {
        Vertex exprVertex = AstBuilder.genAstNode(NEW_ARRAY_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, BASE_TYPE, CpgUtil.getTypeString(expr.getBaseType()));

        int i = 0;
        for (Value size : expr.getSizes()) {
            Vertex sizeVertex = AstBuilder.genValueNode(size);
            Edge sizeEdge = AstBuilder.genAstEdge(exprVertex, sizeVertex, SIZE, SIZE);
            CpgUtil.addEdgeProperty(sizeEdge, DIM, i++);
        }

        setResult(exprVertex);
    }

    @Override
    public void caseInstanceOfExpr(InstanceOfExpr expr) {
        Vertex exprVertex = AstBuilder.genAstNode(INSTANCEOF_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, CHECK_TYPE, CpgUtil.getTypeString(expr.getCheckType()));

        Vertex opVertex = AstBuilder.genValueNode(expr.getOp());
        AstBuilder.genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);

        setResult(exprVertex);
    }

    @Override
    public void caseCastExpr(CastExpr expr) {
        Vertex exprVertex = AstBuilder.genAstNode(CAST_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, CAST_TYPE, CpgUtil.getTypeString(expr.getCastType()));

        Vertex opVertex = AstBuilder.genValueNode(expr.getOp());
        AstBuilder.genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);

        setResult(exprVertex);
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
        Vertex exprVertex = AstBuilder.genAstNode(BINARY_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, OPERATOR, operator);
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));

        Vertex lopVertex = AstBuilder.genValueNode(expr.getOp1());
        Vertex ropVertex = AstBuilder.genValueNode(expr.getOp2());
        AstBuilder.genAstEdge(exprVertex, lopVertex, LEFT_OPERAND, LEFT_OPERAND);
        AstBuilder.genAstEdge(exprVertex, ropVertex, RIGHT_OPERAND, RIGHT_OPERAND);

        setResult(exprVertex);
    }

    // Generates an AST subtree for a unary expression and its operand
    private void caseUnaryExpr(UnopExpr expr, String operator) {
        Vertex exprVertex = AstBuilder.genAstNode(UNARY_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, OPERATOR, operator);
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));

        Vertex opVertex = AstBuilder.genValueNode(expr.getOp());
        AstBuilder.genAstEdge(exprVertex, opVertex, LEFT_OPERAND, LEFT_OPERAND);

        setResult(exprVertex);
    }

    // Generates an AST subtree for an invoke expression and possibly its base and args
    private void caseInvokeExpr(InvokeExpr expr, String invokeType, Value base) {
        Vertex exprVertex = AstBuilder.genAstNode(INVOKE_EXPR, expr.toString());
        CpgUtil.addNodeProperty(exprVertex, JAVA_TYPE, CpgUtil.getTypeString(expr.getType()));
        CpgUtil.addNodeProperty(exprVertex, INVOKE_TYPE, invokeType);
        CpgUtil.addNodeProperty(exprVertex, METHOD_SIG, expr.getMethod().getSignature());
        CpgUtil.addNodeProperty(exprVertex, METHOD_NAME, expr.getMethod().getName());
        CpgUtil.addNodeProperty(exprVertex, METHOD_SCOPE, expr.getMethod().getDeclaringClass().getName());

        if (base != null) {
            Vertex baseVertex = AstBuilder.genValueNode(base);
            AstBuilder.genAstEdge(exprVertex, baseVertex, BASE, BASE);
        }

        int i = 0;
        for (Value arg : expr.getArgs()) {
            Vertex argVertex = AstBuilder.genValueNode(arg);
            Edge argEdge = AstBuilder.genAstEdge(exprVertex, argVertex, ARG, ARG);
            CpgUtil.addEdgeProperty(argEdge, INDEX, i++);
        }

        setResult(exprVertex);
    }

}
