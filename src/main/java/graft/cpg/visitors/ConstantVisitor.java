package graft.cpg.visitors;

import graft.Graft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.*;

import graft.cpg.CpgUtil;

import static graft.Const.*;

/**
 * Visitor applied to constant values to create AST nodes for them.
 *
 * @author Wim Keirsgieter
 */
public class ConstantVisitor extends AbstractConstantSwitch {

    private static Logger log = LoggerFactory.getLogger(ConstantVisitor.class);

    @Override
    public void caseDoubleConstant(DoubleConstant constant) {
        caseConstant(DOUBLE, constant.value);
    }

    @Override
    public void caseFloatConstant(FloatConstant constant) {
        caseConstant(FLOAT, constant.value);
    }

    @Override
    public void caseIntConstant(IntConstant constant) {
        caseConstant(INT, constant.value);
    }

    @Override
    public void caseLongConstant(LongConstant constant) {
        caseConstant(LONG, constant.value);
    }

    @Override
    public void caseNullConstant(NullConstant constant) {
        caseConstant(NULL, NULL);
    }

    @Override
    public void caseStringConstant(StringConstant constant) {
        caseConstant(STRING, constant.value);
    }

    @Override
    public void caseClassConstant(ClassConstant constant) {
        caseConstant(CLASS, constant.getValue());
    }

    @Override
    public void defaultCase(Object obj) {
        log.warn("Unrecognised Constant type '{}'", obj.getClass());
        Constant constant = (Constant) obj;
        caseConstant(CpgUtil.getTypeString(constant.getType()), UNKNOWN);
    }

    // Generate an AST node for a constant value
    private void caseConstant(String type, Object value) {
        setResult(Graft.cpg().traversal()
            .addConstNode(type, value.toString(), value.toString())
            .next()
        );
    }

}
