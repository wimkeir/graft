package graft.cpg.visitors;

import java.util.List;

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
 */
public class ExprVisitor extends AbstractExprSwitch {

    private static Logger log = LoggerFactory.getLogger(ExprVisitor.class);

    // Helper method to create an AST node for binary expressions, with nodes for each operand
    private Vertex binaryExpr(String operator, Value lop, Value rop, String textLabel) {
        Vertex exprVertex = AstBuilder.genAstNode(BINARY_EXPR, textLabel);
        CpgUtil.addNodeProperty(exprVertex, OPERATOR, operator);

        Vertex lopVertex = AstBuilder.genValueNode(lop);
        Vertex ropVertex = AstBuilder.genValueNode(rop);
        AstBuilder.genAstEdge(exprVertex, lopVertex, LEFT_OPERAND, LEFT_OPERAND);
        AstBuilder.genAstEdge(exprVertex, ropVertex, RIGHT_OPERAND, RIGHT_OPERAND);

        return exprVertex;
    }

    // Helper method to create an AST node for unary expressions, with a node for the operand
    private Vertex unaryExpr(String operator, Value op, String textLabel) {
        Vertex exprVertex = AstBuilder.genAstNode(UNARY_EXPR, textLabel);
        CpgUtil.addNodeProperty(exprVertex, OPERATOR, operator);

        Vertex opVertex = AstBuilder.genValueNode(op);
        AstBuilder.genAstEdge(exprVertex, opVertex, OPERAND, OPERAND);

        return exprVertex;
    }

    // Helper method to create an AST node for invoke expressions, with possible nodes for arguments
    private Vertex invokeExpr(String invokeType, String method, List<Value> args, String textLabel) {
        Vertex exprVertex = AstBuilder.genAstNode(INVOKE_EXPR, textLabel);
        CpgUtil.addNodeProperty(exprVertex, INVOKES, method);
        CpgUtil.addNodeProperty(exprVertex, INVOKE_TYPE, invokeType);

        int i = 0;
        for (Value arg : args) {
            Vertex argVertex = AstBuilder.genValueNode(arg);
            Edge argEdge = AstBuilder.genAstEdge(exprVertex, argVertex, ARG, ARG);
            CpgUtil.addEdgeProperty(argEdge, INDEX, i);
        }

        return exprVertex;
    }

    // ********************************************************************************************
    // binary expressions
    // ********************************************************************************************

    @Override
    public void caseAddExpr(AddExpr expr) {
        setResult(binaryExpr(PLUS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseAndExpr(AndExpr expr) {
        setResult(binaryExpr(AND, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseCmpExpr(CmpExpr expr) {
        // TODO: what is this?
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseCmpgExpr(CmpgExpr expr) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseCmplExpr(CmplExpr expr) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseDivExpr(DivExpr expr) {
        setResult(binaryExpr(DIVIDE, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseEqExpr(EqExpr expr) {
        setResult(binaryExpr(EQUALS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseNeExpr(NeExpr expr) {
        setResult(binaryExpr(NOT_EQUALS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseGeExpr(GeExpr expr) {
        setResult(binaryExpr(GREATER_EQUALS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseGtExpr(GtExpr expr) {
        setResult(binaryExpr(GREATER, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseLeExpr(LeExpr expr) {
        setResult(binaryExpr(LESS_EQUALS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseLtExpr(LtExpr expr) {
        setResult(binaryExpr(LESS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseMulExpr(MulExpr expr) {
        setResult(binaryExpr(MULTIPLY, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseOrExpr(OrExpr expr) {
        setResult(binaryExpr(OR, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseRemExpr(RemExpr expr) {
        setResult(binaryExpr(REMAINDER, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseShlExpr(ShlExpr expr) {
        setResult(binaryExpr(LEFT_SHIFT, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseShrExpr(ShrExpr expr) {
        setResult(binaryExpr(SIGNED_RIGHT_SHIFT, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseUshrExpr(UshrExpr expr) {
        setResult(binaryExpr(UNSIGNED_RIGHT_SHIFT, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseSubExpr(SubExpr expr) {
        setResult(binaryExpr(MINUS, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    @Override
    public void caseXorExpr(XorExpr expr) {
        setResult(binaryExpr(XOR, expr.getOp1(), expr.getOp2(), expr.toString()));
    }

    // ********************************************************************************************
    // unary expressions
    // ********************************************************************************************

    @Override
    public void caseCastExpr(CastExpr expr) {
        Vertex exprVertex = unaryExpr(CAST, expr.getOp(), expr.toString());

        TypeVisitor typeVisitor = new TypeVisitor();
        expr.getCastType().apply(typeVisitor);
        CpgUtil.addNodeProperty(exprVertex, CAST_TYPE, typeVisitor.getResult().toString());

        setResult(exprVertex);
    }

    @Override
    public void caseInstanceOfExpr(InstanceOfExpr expr) {
        Vertex exprVertex = unaryExpr(INSTANCEOF_EXPR, expr.getOp(), expr.toString());

        TypeVisitor typeVisitor = new TypeVisitor();
        expr.getCheckType().apply(typeVisitor);
        CpgUtil.addNodeProperty(exprVertex, CHECK_TYPE, typeVisitor.getResult().toString());

        setResult(exprVertex);
    }

    @Override
    public void caseNewArrayExpr(NewArrayExpr expr) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseNewMultiArrayExpr(NewMultiArrayExpr expr) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseNewExpr(NewExpr expr) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseLengthExpr(LengthExpr expr) {
        setResult(unaryExpr(LENGTH, expr.getOp(), expr.toString()));
    }

    @Override
    public void caseNegExpr(NegExpr expr) {
        setResult(unaryExpr(NEGATION, expr.getOp(), expr.toString()));
    }

    // ********************************************************************************************
    // invoke expressions
    // ********************************************************************************************

    @Override
    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr invokeExpr) {
        setResult(invokeExpr(INTERFACE, invokeExpr.getMethod().getName(), invokeExpr.getArgs(), invokeExpr.toString()));
    }

    @Override
    public void caseSpecialInvokeExpr(SpecialInvokeExpr invokeExpr) {
        setResult(invokeExpr(SPECIAL, invokeExpr.getMethod().getName(), invokeExpr.getArgs(), invokeExpr.toString()));
    }

    @Override
    public void caseStaticInvokeExpr(StaticInvokeExpr invokeExpr) {
        setResult(invokeExpr(STATIC, invokeExpr.getMethod().getName(), invokeExpr.getArgs(), invokeExpr.toString()));
    }

    @Override
    public void caseVirtualInvokeExpr(VirtualInvokeExpr invokeExpr) {
        setResult(invokeExpr(VIRTUAL, invokeExpr.getMethod().getName(), invokeExpr.getArgs(), invokeExpr.toString()));
    }

    @Override
    public void caseDynamicInvokeExpr(DynamicInvokeExpr invokeExpr) {
        setResult(invokeExpr(DYNAMIC, invokeExpr.getMethod().getName(), invokeExpr.getArgs(), invokeExpr.toString()));
    }

    // ********************************************************************************************
    // default
    // ********************************************************************************************

    @Override
    public void defaultCase(Object object) {
        log.warn("Unrecognised Expr type '{}'", object.getClass());
        // TODO
    }

}
