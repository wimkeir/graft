package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootField;
import soot.Value;
import soot.jimple.*;

import graft.cpg.AstBuilder;

import static graft.Const.*;
import static graft.cpg.CpgUtil.*;

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
        Vertex refVertex = AstBuilder.genRefNode(ARRAY_REF, getTypeString(ref.getType()), ref.toString());

        Vertex baseVertex = AstBuilder.genValueNode(ref.getBase());
        AstBuilder.genAstEdge(refVertex, baseVertex, BASE, BASE);

        if (ref.getIndex() != null) {
            Vertex idxVertex = AstBuilder.genValueNode(ref.getIndex());
            AstBuilder.genAstEdge(refVertex, idxVertex, INDEX, INDEX);
        }

        setResult(refVertex);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef ref) {
        Vertex refVertex = AstBuilder.genRefNode(EXCEPTION_REF, getTypeString(ref.getType()), ref.toString());
        setResult(refVertex);
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
        caseFieldRef(ref.getField(), ref.getBase());
    }

    @Override
    public void caseParameterRef(ParameterRef ref) {
        Vertex refVertex = AstBuilder.genRefNode(PARAM_REF, getTypeString(ref.getType()), ref.toString());
        addNodeProperty(refVertex, INDEX, ref.getIndex());
        setResult(refVertex);
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
        caseFieldRef(ref.getField(), null);
    }

    @Override
    public void caseThisRef(ThisRef ref) {
        Vertex refVertex = AstBuilder.genRefNode(THIS_REF, getTypeString(ref.getType()), ref.toString());
        setResult(refVertex);
    }

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Ref type '{}', no AST node generated", obj.getClass());
    }

    // Generates an AST node for a field reference (and the base object for instance field refs)
    private void caseFieldRef(SootField field, Value base) {
        Vertex refVertex = AstBuilder.genFieldRefNode(field, field.toString());

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
