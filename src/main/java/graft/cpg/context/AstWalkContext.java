package graft.cpg.context;

import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;

public class AstWalkContext {

    private static Logger log = LoggerFactory.getLogger(AstWalkContext.class);

    private String currentFileName;
    private String currentFilePath;
    private String currentPackage;
    private String currentClass;
    private String currentMethod;

    private Vertex classNode;
    private Vertex cfgTail;
    boolean inBlock;

    public AstWalkContext() {
        log.trace("Creating new context");
        currentFileName = UNKNOWN;
        currentFilePath = UNKNOWN;
        currentPackage = NONE;
        currentClass = NONE;
        currentMethod = NONE;
        inBlock = false;
        cfgTail = null;
    }

    public AstWalkContext(AstWalkContext outerContext) {
        log.trace("Creating new context");
        currentFileName = outerContext.currentFileName();
        currentFilePath = outerContext.currentFilePath();
        currentPackage = outerContext.currentPackage();
        currentClass = outerContext.currentClass();
        currentMethod = outerContext.currentMethod();
        inBlock = false;
        classNode = outerContext.getClassNode();
        cfgTail = outerContext.cfgTail();
    }

    public boolean inBlock() {
        return inBlock;
    }

    public String currentFileName() {
        return currentFileName;
    }

    public String currentFilePath() {
        return currentFilePath;
    }

    public String currentPackage() {
        return currentPackage;
    }

    public String currentClass() {
        return currentClass;
    }

    public String currentMethod() {
        return currentMethod;
    }

    public Vertex cfgTail() {
        return cfgTail;
    }

    public void updateCfgTail(Vertex tail) {
        cfgTail = tail;
    }

    public Vertex getClassNode() {
        return classNode;
    }

    public void update(CompilationUnit cu) {
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

    public void update(PackageDeclaration decl) {
        currentPackage = decl.getNameAsString();
    }

    public void update(ClassOrInterfaceDeclaration decl, Vertex classNode) {
        this.classNode = classNode;
        currentClass = decl.getNameAsString();
    }

    public void update(ConstructorDeclaration decl, Vertex entryNode) {
        currentMethod = decl.getNameAsString();
        cfgTail = entryNode;
    }

    public void update(MethodDeclaration decl, Vertex entryNode) {
        currentMethod = decl.getNameAsString();
        cfgTail = entryNode;
    }

    /**
     * This method should always be overridden.
     *
     * @return statements remaining in the current block
     */
    public int getStmtsRemaining() {
        return -1;
    }

    /**
     * This method should always be overridden.
     */
    public void decrStmtsRemaining() {

    }

    @Override
    public String toString() {
        return "AstWalkContext (" + getClass().getSimpleName() + ")\n" +
                "file-name = " + currentFileName + "\n" +
                "file-path = " + currentFilePath + "\n" +
                "package = " + currentPackage + "\n" +
                "class = " + currentClass + "\n" +
                "method = " + currentMethod + "\n" +
                getStmtsRemaining() + " stmts remaining";
    }

    int nrStmts(Statement stmt) {
        if (stmt instanceof BlockStmt) {
            BlockStmt block = stmt.asBlockStmt();
            return block.getStatements().size();
        } else if (stmt instanceof EmptyStmt) {
            return 0;
        } else {
            return 1;
        }
    }

}
