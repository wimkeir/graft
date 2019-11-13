package graft.cpg.visitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;

import static graft.Const.*;

/**
 * Visitor applied to Soot types to get their string representation.
 *
 * @author Wim Keirsgieter
 */
public class TypeVisitor extends TypeSwitch {

    private static Logger log = LoggerFactory.getLogger(TypeVisitor.class);

    @Override
    public void caseArrayType(ArrayType type) {
        TypeVisitor typeVisitor = new TypeVisitor();
        type.baseType.apply(typeVisitor);
        StringBuilder sb = new StringBuilder(typeVisitor.getResult().toString());
        for (int i = 0; i < type.numDimensions; i++) {
            sb.append("[]");
        }
        setResult(sb.toString());
    }

    @Override
    public void caseBooleanType(BooleanType type) {
        setResult(BOOLEAN);
    }

    @Override
    public void caseByteType(ByteType type) {
        setResult(BYTE);
    }

    @Override
    public void caseCharType(CharType type) {
        setResult(CHAR);
    }

    @Override
    public void caseDoubleType(DoubleType type) {
        setResult(DOUBLE);
    }

    @Override
    public void caseFloatType(FloatType type) {
        setResult(FLOAT);
    }

    @Override
    public void caseIntType(IntType type) {
        setResult(INT);
    }

    @Override
    public void caseLongType(LongType type) {
        setResult(LONG);
    }

    @Override
    public void caseNullType(NullType type) {
        setResult(NULL);
    }

    @Override
    public void caseRefType(RefType type) {
        setResult(type.getClassName());
    }

    @Override
    public void caseVoidType(VoidType type) {
        setResult(VOID);
    }

    @Override
    public void caseShortType(ShortType type) {
        setResult(SHORT);
    }

    @Override
    public void defaultCase(Type type) {
        log.warn("Unrecognised Type class '{}'", type.toString());
        setResult(type.toString());
    }

}
