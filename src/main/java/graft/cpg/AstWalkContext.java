package graft.cpg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;

/**
 * TODO: javadoc
 */
class AstWalkContext {

    private String currentFileName;
    private String currentFilePath;
    private String currentPackage;
    private String currentClass;
    private String currentMethod;

    private int paramIndex;

    private ClassOrInterfaceContext classOrInterfaceContext;
    private MethodContext methodContext;

    private Vertex astTail;
    private Vertex cfgTail;

    AstWalkContext() {
        currentFileName = UNKNOWN;
        currentFilePath = UNKNOWN;
        currentPackage = NONE;
        currentClass = NONE;
        currentMethod = NONE;
    }

    String currentFileName() {
        return currentFileName;
    }

    String currentFilePath() {
        return currentFilePath;
    }

    String currentPackage() {
        return currentPackage;
    }

    String currentClass() {
        return currentClass;
    }

    String currentMethod() {
        return currentMethod;
    }

    int getParamIndex() {
        return paramIndex;
    }

    void incrementParamIndex() {
        paramIndex++;
    }

    Vertex astTail() {
        return astTail;
    }

    Vertex cfgTail() {
        return cfgTail;
    }

    void setAstTail(Vertex tail) {
        astTail = tail;
    }

    void setCfgTail(Vertex tail) {
        cfgTail = tail;
    }

    void update(CompilationUnit cu) {
        Optional<CompilationUnit.Storage> optStorage = cu.getStorage();
        if (optStorage.isPresent()) {
            CompilationUnit.Storage storage = optStorage.get();
            Path srcRoot = storage.getSourceRoot();
            Path filePath = storage.getPath();
            currentFilePath = srcRoot.relativize(filePath).toString();
            currentFileName = storage.getFileName();
        } else {
            currentFilePath = UNKNOWN;
            currentFileName = UNKNOWN;
        }
    }

    void update(PackageDeclaration decl) {
        currentPackage = decl.getNameAsString();
    }

    void update(ClassOrInterfaceDeclaration decl) {
        classOrInterfaceContext = ClassOrInterfaceContext.fromClassOrInterfaceDecl(decl);
        currentClass = classOrInterfaceContext.simpleName;
    }

    void update(MethodDeclaration decl) {
        methodContext = MethodContext.fromMethodDecl(decl);
        currentMethod = methodContext.name;
        paramIndex = 0;
    }

    private static class ClassOrInterfaceContext {

        private String fullName;
        private String simpleName;
        private boolean isInterface;
        private List<Modifier> modifiers;
        private List<ClassOrInterfaceType> extendedTypes;
        private List<ClassOrInterfaceType> implementedTypes;

        private static ClassOrInterfaceContext fromClassOrInterfaceDecl(ClassOrInterfaceDeclaration decl) {
            ClassOrInterfaceContext context = new ClassOrInterfaceContext();
            context.simpleName = decl.getNameAsString();
            context.isInterface = decl.isInterface();

            context.modifiers = new ArrayList<>(decl.getModifiers());
            context.extendedTypes = new ArrayList<>(decl.getExtendedTypes());
            context.implementedTypes = new ArrayList<>(decl.getImplementedTypes());

            Optional<String> fullNameOpt = decl.getFullyQualifiedName();
            if (fullNameOpt.isPresent()) {
                context.fullName = fullNameOpt.get();
            } else {
                context.fullName = UNKNOWN;
            }

            return context;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            for (Modifier modifier : modifiers) {
                sb.append(modifier.getKeyword().toString());
            }

            if (isInterface) {
                sb.append("interface '");
            } else {
                sb.append("class '");
            }

            sb.append(simpleName).append("' (").append(fullName).append(")");

            if (extendedTypes.size() > 0) {
                sb.append(" extends ");
                for (ReferenceType type : extendedTypes) {
                    sb.append(type.asString()).append(", ");
                }
                sb.delete(sb.lastIndexOf(","), sb.length());
            }
            if (implementedTypes.size() > 0) {
                sb.append(" implements ");
                for (ReferenceType type : implementedTypes) {
                    sb.append(type.asString()).append(", ");
                }
                sb.delete(sb.lastIndexOf(","), sb.length());
            }

            return sb.toString();
        }
    }

    private static class MethodContext {

        private String name;
        private Type returnType;
        private List<Modifier> modifiers;
        private List<ReferenceType> exceptionsThrown;

        private static MethodContext fromMethodDecl(MethodDeclaration decl) {
            MethodContext context = new MethodContext();
            context.name = decl.getNameAsString();
            context.returnType = decl.getType();

            context.modifiers = new ArrayList<>(decl.getModifiers());
            context.exceptionsThrown = new ArrayList<>(decl.getThrownExceptions());

            return context;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            for (Modifier modifier : modifiers) {
                sb.append(modifier.getKeyword().asString());
            }

            sb.append(returnType).append(" ");
            sb.append(name);

            if (exceptionsThrown.size() > 0) {
                sb.append("throws ");
                for (ReferenceType exception : exceptionsThrown) {
                    sb.append(exception.asString()).append(", ");
                }
                sb.delete(sb.lastIndexOf(","), sb.length());
            }

            return sb.toString();
        }
    }
}
