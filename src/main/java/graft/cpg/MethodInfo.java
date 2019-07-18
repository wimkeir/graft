package graft.cpg;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ReferenceType;

import java.util.ArrayList;
import java.util.List;

/**
 * An object for keeping track of information and properties relating to a method under analysis.
 *
 * Note that no parameter information is included here as params are added to the CPG as they are
 * encountered during the AST walk.
 */
public class MethodInfo {

    private String name;
    private String returnType;
    private List<String> modifiers;
    private List<String> exceptionsThrown;

    // TODO: type params, annotations

    private MethodInfo(String name,
                       String returnType,
                       List<String> modifiers,
                       List<String> exceptionsThrown) {
        this.name = name;
        this.returnType = returnType;
        this.modifiers = new ArrayList<>(modifiers);
        this.exceptionsThrown = new ArrayList<>(exceptionsThrown);
    }

    public String name() {
        return name;
    }

    public String returnType() {
        return returnType;
    }

    public List<String> modifiers() {
        return new ArrayList<>(modifiers);
    }

    public List<String> exceptionsThrown() {
        return new ArrayList<>(exceptionsThrown);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String modifier : modifiers) {
            sb.append(modifier);
        }

        sb.append(returnType).append(" ");
        sb.append(name);

        if (exceptionsThrown.size() > 0) {
            sb.append("throws ");
            for (String exception : exceptionsThrown) {
                sb.append(exception).append(", ");
            }
            sb.delete(sb.lastIndexOf(","), sb.length());
        }

        return sb.toString();
    }

    public static MethodInfo fromMethodDecl(MethodDeclaration decl) {
        List<String> modifiers = new ArrayList<>();
        List<String> exceptionsThrown = new ArrayList<>();
        for (Modifier modifier : decl.getModifiers()) {
            modifiers.add(modifier.toString());
        }
        for (ReferenceType exception : decl.getThrownExceptions()) {
            exceptionsThrown.add(exception.toString());
        }
        return new MethodInfo(decl.getNameAsString(), decl.getTypeAsString(), modifiers, exceptionsThrown);
    }
}
