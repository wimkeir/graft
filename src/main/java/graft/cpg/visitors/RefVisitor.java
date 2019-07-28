package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import soot.jimple.*;

import graft.cpg.CpgUtil;
import graft.cpg.AstBuilder;

import static graft.Const.*;

/**
 * Visitor applied to references to create AST nodes for them.
 *
 * @author Wim Keirsgieter
 */
public class RefVisitor extends AbstractRefSwitch {

    private Object result;

    private void setResult(Object object) {
        result = object;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public void caseArrayRef(ArrayRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(ARRAY_REF, ref.toString());

        Vertex baseVertex = AstBuilder.genValueNode(ref.getBase());
        Vertex idxVertex = AstBuilder.genValueNode(ref.getIndex());
        AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);
        AstBuilder.genAstEdge(refVertex, idxVertex, INDEX, INDEX);

        setResult(refVertex);
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseParameterRef(ParameterRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(PARAM_REF, ref.toString());

        TypeVisitor typeVisitor = new TypeVisitor();
        ref.getType().apply(typeVisitor);
        CpgUtil.addNodeProperty(refVertex, INDEX, ref.getIndex());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, typeVisitor.getResult().toString());

        setResult(refVertex);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef ref) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseThisRef(ThisRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(THIS_REF, ref.toString());
        setResult(refVertex);
    }

    @Override
    public void defaultCase(Object object) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
