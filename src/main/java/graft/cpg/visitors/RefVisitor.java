package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger log = LoggerFactory.getLogger(RefVisitor.class);

    private Object result;

    public Object getResult() {
        return result;
    }

    @Override
    public void caseArrayRef(ArrayRef ref) {
        // TODO: what happens if array is referenced without an index?
        Vertex refVertex = AstBuilder.genAstNode(ARRAY_REF, ref.toString());

        Vertex baseVertex = AstBuilder.genValueNode(ref.getBase());
        Vertex idxVertex = AstBuilder.genValueNode(ref.getIndex());
        AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);
        AstBuilder.genAstEdge(refVertex, idxVertex, INDEX, INDEX);

        setResult(refVertex);
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
        // TODO: decide how to treat these in PDG
        // TODO: scope, modifiers
        Vertex refVertex = AstBuilder.genAstNode(STATIC_FIELD_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));
        CpgUtil.addNodeProperty(refVertex, CLASS, ref.getField().getDeclaringClass().getName());
        setResult(refVertex);
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
        // TODO: scope, modifiers
        Vertex refVertex = AstBuilder.genAstNode(INSTANCE_FIELD_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));

        Vertex baseVertex = AstBuilder.genValueNode(ref.getBase());
        AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);

        setResult(refVertex);
    }

    @Override
    public void caseParameterRef(ParameterRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(PARAM_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));
        CpgUtil.addNodeProperty(refVertex, INDEX, ref.getIndex());
        setResult(refVertex);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef ref) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void caseThisRef(ThisRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(THIS_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));
        setResult(refVertex);
    }

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Ref type '{}', no AST node generated", obj.getClass());
    }

    private void setResult(Object object) {
        result = object;
    }

}
