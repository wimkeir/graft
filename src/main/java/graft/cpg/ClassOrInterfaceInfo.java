package graft.cpg;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static graft.Const.*;

/**
 * An object for keeping track of information and properties relating to a Java class under analysis.
 */
public class ClassOrInterfaceInfo {

    private String fullName;
    private String simpleName;
    private List<String> modifiers;
    private List<String> extendedTypes;
    private List<String> implementedTypes;
    private boolean isInterface;

    // TODO: type params, annotations

    private ClassOrInterfaceInfo(String fullName,
                                 String simpleName,
                                 List<String> modifiers,
                                 List<String> extendedTypes,
                                 List<String> implementedTypes,
                                 boolean isInterface) {
        this.fullName = fullName;
        this.simpleName = simpleName;
        this.modifiers = new ArrayList<>(modifiers);
        this.extendedTypes = new ArrayList<>(extendedTypes);
        this.implementedTypes = new ArrayList<>(implementedTypes);
        this.isInterface = isInterface;
    }

    public String fullName() {
        return fullName;
    }

    public String simpleName() {
        return simpleName;
    }

    public List<String> modifiers() {
        return new ArrayList<>(modifiers);
    }

    public List<String> extendedTypes() {
        return new ArrayList<>(extendedTypes);
    }

    public List<String> implementedTypes() {
        return new ArrayList<>(implementedTypes);
    }

    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String modifier : modifiers) {
            sb.append(modifier);
        }

        if (isInterface) {
            sb.append("interface '");
        } else {
            sb.append("class '");
        }

        sb.append(simpleName).append("' (").append(fullName).append(")");

        if (extendedTypes.size() > 0) {
            sb.append(" extends ");
            for (String type : extendedTypes) {
                sb.append(type).append(", ");
            }
            sb.delete(sb.lastIndexOf(","), sb.length());
        }
        if (implementedTypes.size() > 0) {
            sb.append(" implements ");
            for (String type : implementedTypes) {
                sb.append(type).append(", ");
            }
            sb.delete(sb.lastIndexOf(","), sb.length());
        }

        return sb.toString();
    }

    public static ClassOrInterfaceInfo fromClassOrInterfaceDecl(ClassOrInterfaceDeclaration decl) {
        Optional<String> optFullName = decl.getFullyQualifiedName();
        String fullName;
        if (optFullName.isPresent()) {
            fullName = optFullName.get();
        } else {
            fullName = UNKNOWN;
        }

        List<String> modifiers = new ArrayList<>();
        List<String> extendedTypes = new ArrayList<>();
        List<String> implementedTypes = new ArrayList<>();
        for (Modifier modifier : decl.getModifiers()) {
            modifiers.add(modifier.toString());
        }
        for (ClassOrInterfaceType type : decl.getExtendedTypes()) {
            extendedTypes.add(type.getNameAsString());
        }
        for (ClassOrInterfaceType type : decl.getImplementedTypes()) {
            implementedTypes.add(type.getNameAsString());
        }

        return new ClassOrInterfaceInfo(fullName,
                                        decl.getNameAsString(),
                                        modifiers,
                                        extendedTypes,
                                        implementedTypes,
                                        decl.isInterface());
    }

}
