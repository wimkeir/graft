package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootField;
import soot.Value;
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
        Vertex refVertex = AstBuilder.genAstNode(ARRAY_REF, ref.toString());

        Vertex baseVertex = AstBuilder.genValueNode(ref.getBase());
        Vertex idxVertex = AstBuilder.genValueNode(ref.getIndex());
        AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);
        AstBuilder.genAstEdge(refVertex, idxVertex, INDEX, INDEX);

        setResult(refVertex);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(EXCEPTION_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));
        setResult(refVertex);
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
        caseFieldRef(ref.getField(), ref.getBase());
    }

    @Override
    public void caseParameterRef(ParameterRef ref) {
        Vertex refVertex = AstBuilder.genAstNode(PARAM_REF, ref.toString());
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(ref.getType()));
        CpgUtil.addNodeProperty(refVertex, INDEX, ref.getIndex());
        setResult(refVertex);
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
        caseFieldRef(ref.getField(), null);
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

    // Generates an AST node for a field reference (and the base object for instance field refs)
    private void caseFieldRef(SootField field, Value base) {
        Vertex refVertex;
        if (field.isStatic()) {
            refVertex = AstBuilder.genAstNode(STATIC_FIELD_REF, field.toString());
        } else {
            refVertex = AstBuilder.genAstNode(INSTANCE_FIELD_REF, field.toString());
        }
        CpgUtil.addNodeProperty(refVertex, JAVA_TYPE, CpgUtil.getTypeString(field.getType()));
        CpgUtil.addNodeProperty(refVertex, FIELD_NAME, field.getName());
        CpgUtil.addNodeProperty(refVertex, FIELD_SIG, field.getSignature());

        if (base != null) {
            Vertex baseVertex = AstBuilder.genValueNode(base);
            AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);
        }

        setResult(refVertex);
    }

    private void setResult(Object object) {
        result = object;
    }

}
