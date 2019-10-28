package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootField;
import soot.Value;
import soot.jimple.*;

import graft.Graft;
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
    private AstBuilder astBuilder;

    public RefVisitor(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public void caseArrayRef(ArrayRef ref) {

        Vertex refNode = (Vertex) Graft.cpg().traversal()
                .addRefNode(ARRAY_REF, ref.toString(), getTypeString(ref.getType()))
                .next();

        Graft.cpg().traversal()
                .addAstE(BASE, BASE)
                .from(refNode)
                .to(astBuilder.genValueNode(ref.getBase()))
                .iterate();

        if (ref.getIndex() != null) {
            Graft.cpg().traversal()
                    .addAstE(INDEX, INDEX)
                    .from(refNode)
                    .to(astBuilder.genValueNode(ref.getIndex()))
                    .iterate();
        }

        setResult(refNode);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef ref) {
        setResult(Graft.cpg().traversal()
                .addRefNode(EXCEPTION_REF, ref.toString(), getTypeString(ref.getType()))
                .next()
        );
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
        caseFieldRef(ref.getField(), ref.getBase());
    }

    @Override
    public void caseParameterRef(ParameterRef ref) {
        setResult(Graft.cpg().traversal()
                .addRefNode(PARAM_REF, ref.toString(), getTypeString(ref.getType()))
                .property(INDEX, ref.getIndex())
                .next()
        );
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
        caseFieldRef(ref.getField(), null);
    }

    @Override
    public void caseThisRef(ThisRef ref) {
        setResult(Graft.cpg().traversal()
                .addRefNode(THIS_REF, ref.toString(), getTypeString(ref.getType()))
                .next()
        );
    }

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Ref type '{}', no AST node generated", obj.getClass());
    }

    // Generates an AST node for a field reference (and the base object for instance field refs)
    private void caseFieldRef(SootField field, Value base) {
        String fieldType = field.isStatic() ? STATIC_FIELD_REF : INSTANCE_FIELD_REF;
        Vertex refNode = (Vertex) Graft.cpg().traversal()
                .addRefNode(FIELD_REF, field.toString(), getTypeString(field.getType()))
                .property(FIELD_REF_TYPE, fieldType)
                .property(FIELD_NAME, field.getName())
                .property(FIELD_SIG, field.getSignature())
                .next();

        if (base != null) {
            Graft.cpg().traversal()
                    .addAstE(BASE, BASE)
                    .from(refNode)
                    .to(astBuilder.genValueNode(base))
                    .iterate();
        }

        setResult(refNode);
    }

    private void setResult(Object object) {
        result = object;
    }

}
