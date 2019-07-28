package graft.cpg.visitors;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.*;

import graft.cpg.AstBuilder;
import graft.cpg.CpgUtil;

import static graft.Const.*;

/**
 * Visitor applied to constant values to create AST nodes for them.
 *
 * @author Wim Keirsgieter
 */
public class ConstantVisitor extends AbstractConstantSwitch {

    private static Logger log = LoggerFactory.getLogger(ConstantVisitor.class);

    // Helper method to create AST nodes for literal constant values
    private void literalConstant(String type, String textLabel, Object value) {
        Vertex constVertex = AstBuilder.genAstNode(LITERAL, textLabel);
        CpgUtil.addNodeProperty(constVertex, JAVA_TYPE, type);
        CpgUtil.addNodeProperty(constVertex, VALUE, value);
        setResult(constVertex);
    }

    @Override
    public void caseDoubleConstant(DoubleConstant constant) {
        literalConstant(DOUBLE, constant.toString(), constant.value);
    }

    @Override
    public void caseFloatConstant(FloatConstant constant) {
        literalConstant(FLOAT, constant.toString(), constant.value);
    }

    @Override
    public void caseIntConstant(IntConstant constant) {
        literalConstant(INT, constant.toString(), constant.value);
    }

    @Override
    public void caseLongConstant(LongConstant constant) {
        literalConstant(LONG, constant.toString(), constant.value);
    }

    @Override
    public void caseNullConstant(NullConstant constant) {
        TypeVisitor typeVisitor = new TypeVisitor();
        constant.getType().apply(typeVisitor);
        String type = typeVisitor.getResult().toString();
        literalConstant(type, NULL, NULL);
    }

    @Override
    public void caseStringConstant(StringConstant constant) {
        literalConstant(STRING, constant.toString(), constant.value);
    }

    @Override
    public void caseClassConstant(ClassConstant constant) {
        TypeVisitor typeVisitor = new TypeVisitor();
        constant.getType().apply(typeVisitor);
        String type = typeVisitor.getResult().toString();
        literalConstant(type, constant.toString(), constant.value);
    }

    @Override
    public void defaultCase(Object object) {
        log.warn("Unrecognised Constant type '{}'", object.getClass());
        Constant constant = (Constant) object;
        literalConstant(UNKNOWN, constant.toString(), UNKNOWN);
    }

}
