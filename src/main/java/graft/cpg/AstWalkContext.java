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
    }

    void enterStmt(WhileStmt whileStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = new EnclosingStmtContext();
        context.enclosingStmtHead = stmtVertex;
        context.enclosingStmtType = WHILE_STMT;
        context.stmtsRemaining = nrStmts(whileStmt.getBody());
        enclosingStmtContexts.push(context);
    }

    void enterStmt(IfStmt ifStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = new EnclosingStmtContext();
        context.enclosingStmtHead = stmtVertex;
        context.enclosingStmtType = IF_STMT;
        context.inThenBlock = true;
        context.stmtsRemaining = nrStmts(ifStmt.getThenStmt());

        if (ifStmt.hasElseBranch()) {
            context.hasElseBlock = true;
            EnclosingStmtContext elseContext = new EnclosingStmtContext();
            elseContext.enclosingStmtHead = stmtVertex;
            elseContext.enclosingStmtType = IF_STMT;
            elseContext.inThenBlock = false;
            // TODO: why the -1 here?
            elseContext.stmtsRemaining = nrStmts(ifStmt.getElseStmt().get()) - 1;
            enclosingStmtContexts.push(elseContext);
        }
        enclosingStmtContexts.push(context);
    }

    void enterStmt(ForStmt forStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = new EnclosingStmtContext();
        context.enclosingStmtHead = stmtVertex;
        context.enclosingStmtType = FOR_STMT;
        context.stmtsRemaining = nrStmts(forStmt.getBody());
        enclosingStmtContexts.push(context);
    }

    void exitStmt(Statement stmt, Vertex stmtVertex) {
        // no enclosing statement - simply draw edge from tail to current vertex and update tail
        if (enclosingStmtContexts.isEmpty()) {
            CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
            cfgTail = stmtVertex;
            return;
        }

        EnclosingStmtContext context = enclosingStmtContexts.pop();

        // enclosing statement but statements remaining - update enclosing context, draw edge from
        // tail to current vertex and update tail
        if (context.stmtsRemaining > 0) {
            context.stmtsRemaining--;
            enclosingStmtContexts.push(context);
            CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
            cfgTail = stmtVertex;
            return;
        }

        // no statements remaining (we're at the end of an enclosing statement)
        assert context.stmtsRemaining == 0;

        switch (context.enclosingStmtType) {
            case IF_STMT:
                if (context.inThenBlock) {
                    // leaving a then block, we set the tail to the statement head (the if condition)
                    // so that the next edge to either the phi node or else block is drawn from there
                    CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);

                    if (context.hasElseBlock) {
                        System.out.println("Leaving then block");
                        EnclosingStmtContext elseContext = enclosingStmtContexts.pop();
                        elseContext.thenTail = stmtVertex;
                        enclosingStmtContexts.push(elseContext);
                        cfgTail = context.enclosingStmtHead;
                    } else {
                        Vertex phi = CpgUtil.genCfgNode(this, Optional.empty(), PHI, PHI);
                        CpgUtil.genCfgEdge(stmtVertex, phi, EMPTY, EMPTY);
                        CpgUtil.genCfgEdge(context.enclosingStmtHead, phi, EMPTY, EMPTY);
                        cfgTail = phi;
                    }
                } else {
                    // leaving an else block, TODO
                    System.out.println("Leaving else block");
                    CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                    Vertex phi = CpgUtil.genCfgNode(this, Optional.empty(), PHI, PHI);
                    CpgUtil.genCfgEdge(stmtVertex, phi, EMPTY, EMPTY);
                    CpgUtil.genCfgEdge(context.thenTail, phi, EMPTY, EMPTY);
                    cfgTail = phi;
                }
                break;
            case WHILE_STMT:
                // leaving a while block, draw an edge back to the guard condition and set that as
                // the tail (after drawing a standard edge from the current tail to current vertex)
                CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                CpgUtil.genCfgEdge(stmtVertex, context.enclosingStmtHead, EMPTY, EMPTY);
                cfgTail = context.enclosingStmtHead;
                break;
            case FOR_STMT:
                // leaving a for statement (handled the same as while statements)
                CpgUtil.genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                CpgUtil.genCfgEdge(stmtVertex, context.enclosingStmtHead, EMPTY, EMPTY);
                cfgTail = context.enclosingStmtHead;
                break;
            default:
                // TODO
        }
    }

    private static class EnclosingStmtContext {
        String enclosingStmtType;
        Vertex enclosingStmtHead;
        int stmtsRemaining;

        // if statements
        boolean inThenBlock;
        boolean hasElseBlock;
        Vertex thenTail;
    }

    // if the statement is a block statement, returns the number of statements in the block
    // if the statement is a single statement, returns 1
    // if the statement is empty, returns 0
    private int nrStmts(Statement stmt) {
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
