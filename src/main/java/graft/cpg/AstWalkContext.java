package graft.cpg;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Stack;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

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
    private Vertex astTail;
    private Vertex cfgTail;
    private Stack<EnclosingStmtContext> enclosingStmtContexts;

    AstWalkContext() {
        currentFileName = UNKNOWN;
        currentFilePath = UNKNOWN;
        currentPackage = NONE;
        currentClass = NONE;
        currentMethod = NONE;
        enclosingStmtContexts = new Stack<>();
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

    // TODO: get rid of this (use enter/exit methods)
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
        currentClass = decl.getNameAsString();
    }

    void update(MethodDeclaration decl) {
        currentMethod = decl.getNameAsString();
        paramIndex = 0;
    }

    void enterStmt(Statement stmt, Vertex stmtVertex) {

        if (stmt instanceof WhileStmt) {
            EnclosingStmtContext context = new EnclosingStmtContext();
            context.enclosingStmtHead = stmtVertex;
            context.enclosingStmtType = WHILE_STMT;
            WhileStmt whileStmt = (WhileStmt) stmt;

            if (whileStmt.getBody() instanceof BlockStmt) {
                BlockStmt whileBlock = whileStmt.getBody().asBlockStmt();
                context.stmtsRemaining = whileBlock.getStatements().size();
            } else if (whileStmt.hasEmptyBody()) {
                context.stmtsRemaining = 0;
            } else {
                context.stmtsRemaining = 1;
            }
            enclosingStmtContexts.push(context);

        } else if (stmt instanceof IfStmt) {
            EnclosingStmtContext context = new EnclosingStmtContext();
            context.enclosingStmtHead = stmtVertex;
            context.enclosingStmtType = IF_STMT;
            context.inThenBlock = true;
            IfStmt ifStmt = (IfStmt) stmt;

            if (ifStmt.getThenStmt() instanceof BlockStmt) {
                BlockStmt thenBlock = ifStmt.getThenStmt().asBlockStmt();
                context.stmtsRemaining = thenBlock.getStatements().size();
            } else if (ifStmt.getThenStmt() instanceof EmptyStmt) {
                context.stmtsRemaining = 0;
            } else {
                context.stmtsRemaining = 1;
            }

            // TODO: explain this
            if (ifStmt.hasElseBranch()) {
                context.hasElseBlock = true;
                EnclosingStmtContext elseContext = new EnclosingStmtContext();
                elseContext.enclosingStmtHead = stmtVertex;
                elseContext.enclosingStmtType = IF_STMT;
                elseContext.inThenBlock = false;
                Statement elseBranch = ifStmt.getElseStmt().get();

                if (elseBranch instanceof BlockStmt) {
                    BlockStmt elseBlock = elseBranch.asBlockStmt();
                    elseContext.stmtsRemaining = elseBlock.getStatements().size();
                } else if (elseBranch instanceof EmptyStmt) {
                    elseContext.stmtsRemaining = 0;
                } else {
                    elseContext.stmtsRemaining = 1;
                }
                enclosingStmtContexts.push(elseContext);
            }
            enclosingStmtContexts.push(context);
        }
    }

    void exitStmt(Statement stmt, Vertex stmtVertex) {
        if (enclosingStmtContexts.isEmpty()) {
            cfgTail = stmtVertex;
            return;
        }

        EnclosingStmtContext context = enclosingStmtContexts.pop();
        if (context.stmtsRemaining > 0) {
            context.stmtsRemaining--;
            enclosingStmtContexts.push(context);
            cfgTail = stmtVertex;
            return;
        }
        assert context.stmtsRemaining == 0;

        if (context.enclosingStmtType.equals(IF_STMT)) {
            if (context.inThenBlock) {
                cfgTail = context.enclosingStmtHead;
                return;
            }
        } else if (context.enclosingStmtType.equals(WHILE_STMT)) {
            CpgUtil.genCfgEdge(stmtVertex, context.enclosingStmtHead, EMPTY, EMPTY);
            cfgTail = context.enclosingStmtHead;
            return;
        }

        cfgTail = stmtVertex;
    }

    private static class EnclosingStmtContext {
        String enclosingStmtType;
        Vertex enclosingStmtHead;
        int stmtsRemaining;

        // if statements
        boolean inThenBlock;
        boolean hasElseBlock;
    }

}
