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
import static graft.cpg.CfgBuilder.*;

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

    private EnclosingStmtContext getEnclosingContext (String type, Vertex head, int nrStmts) {
        EnclosingStmtContext context = new EnclosingStmtContext();
        context.enclosingStmtType = type;
        context.enclosingStmtHead = head;
        context.stmtsRemaining = nrStmts;
        return context;
    }

    void enterStmt(WhileStmt whileStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = getEnclosingContext(WHILE_STMT, stmtVertex, nrStmts(whileStmt.getBody()));
        enclosingStmtContexts.push(context);
    }

    void enterStmt(IfStmt ifStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = getEnclosingContext(IF_STMT, stmtVertex, nrStmts(ifStmt.getThenStmt()));
        context.inThenBlock = true;

        ifStmt.getElseStmt().ifPresent(elseBody -> {
            EnclosingStmtContext elseContext = getEnclosingContext(IF_STMT, stmtVertex, nrStmts(elseBody) - 1);
            context.hasElseBlock = true;
            context.inThenBlock = false;
            enclosingStmtContexts.push(elseContext);
        });

        enclosingStmtContexts.push(context);
    }

    void enterStmt(ForStmt forStmt, Vertex stmtVertex) {
        EnclosingStmtContext context = getEnclosingContext(FOR_STMT, stmtVertex, nrStmts(forStmt.getBody()));
        enclosingStmtContexts.push(context);
    }

    void exitStmt(Statement stmt, Vertex stmtVertex) {
        // no enclosing statement - simply draw edge from tail to current vertex and update tail
        if (enclosingStmtContexts.isEmpty()) {
            genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
            cfgTail = stmtVertex;
            return;
        }

        EnclosingStmtContext context = enclosingStmtContexts.pop();

        // enclosing statement but statements remaining - update enclosing context, draw edge from
        // tail to current vertex and update tail
        if (context.stmtsRemaining > 0) {
            context.stmtsRemaining--;
            enclosingStmtContexts.push(context);
            genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
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
                    genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);

                    if (context.hasElseBlock) {
                        System.out.println("Leaving then block");
                        EnclosingStmtContext elseContext = enclosingStmtContexts.pop();
                        elseContext.thenTail = stmtVertex;
                        enclosingStmtContexts.push(elseContext);
                        cfgTail = context.enclosingStmtHead;
                    } else {
                        Vertex phi = genCfgNode(this, Optional.empty(), PHI, PHI);
                        genCfgEdge(stmtVertex, phi, EMPTY, EMPTY);
                        genCfgEdge(context.enclosingStmtHead, phi, EMPTY, EMPTY);
                        cfgTail = phi;
                    }
                } else {
                    // leaving an else block, TODO
                    System.out.println("Leaving else block");
                    genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                    Vertex phi = genCfgNode(this, Optional.empty(), PHI, PHI);
                    genCfgEdge(stmtVertex, phi, EMPTY, EMPTY);
                    genCfgEdge(context.thenTail, phi, EMPTY, EMPTY);
                    cfgTail = phi;
                }
                break;
            case WHILE_STMT:
                // leaving a while block, draw an edge back to the guard condition and set that as
                // the tail (after drawing a standard edge from the current tail to current vertex)
                genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                genCfgEdge(stmtVertex, context.enclosingStmtHead, EMPTY, EMPTY);
                cfgTail = context.enclosingStmtHead;
                break;
            case FOR_STMT:
                // leaving a for statement (handled the same as while statements)
                genCfgEdge(cfgTail, stmtVertex, EMPTY, EMPTY);
                genCfgEdge(stmtVertex, context.enclosingStmtHead, EMPTY, EMPTY);
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
