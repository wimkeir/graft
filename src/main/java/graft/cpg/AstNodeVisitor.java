package graft.cpg;

import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.apache.tinkerpop.gremlin.structure.Graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<Graph> {

    private static Logger log = LoggerFactory.getLogger(AstNodeVisitor.class);

    // TODO: group these in context object
    private String currentFileName;         // the Java file currently being walked
    private String currentFilePath;         // relative to the source root
    private String currentPackage;
    private ClassOrInterfaceInfo currentClassOrInterface;
    private MethodInfo currentMethod;

    @Override
    public void defaultAction(Node node, Graph graph) {
        log.debug("Unsupported node type '{}', ignoring", node.getClass());
    }

    @Override
    public void defaultAction(NodeList list, Graph graph) {
        log.debug("NodeList visitor method not implemented");
    }

    // TODO: enums, union/intersection types, annotations

    @Override
    public void visit(CompilationUnit cu, Graph graph) {
        // a compilation unit represents a single Java source file, and consists of an optional
        // package declaration, zero or more import declarations, and zero or more type declarations
        log.trace("Visiting CompilationUnit");

        // reset current AST walk context
        currentPackage = NONE;
        currentClassOrInterface = null;
        currentMethod = null;

        // get source file path and filename
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
        log.debug("Walking AST of file '{}'", currentFilePath);
    }

    @Override
    public void visit(Modifier mod, Graph graph) {
        // a modifier like private, static, volatile etc.
        log.trace("Visiting Modifier");
    }

    @Override
    public void visit(Name name, Graph graph) {
        // a name that may consist of multiple dot-separated qualifiers and identifiers
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SimpleName name, Graph graph) {
        // a name consisting of a single identifier, ie. no dot qualifiers
        log.trace("Visiting SimpleName");
    }

    @Override
    public void visit(Parameter param, Graph graph) {
        // a parameter to a method declaration or lambda
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TypeParameter param, Graph graph) {
        // a generic type parameter
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // statements / blocks
    // ********************************************************************************************

    @Override
    public void visit(AssertStmt stmt, Graph graph) {
        // an assert statement with a check condition and optional message
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BlockStmt stmt, Graph graph) {
        // a block of statements enclosed between curly braces
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BreakStmt stmt, Graph graph) {
        // a break statement with an optional label or value
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CatchClause clause, Graph graph) {
        // the catch part of a try-catch-finally construct
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ContinueStmt stmt, Graph graph) {
        // a continue statement with an optional label
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(DoStmt stmt, Graph graph) {
        // a do-while loop with a condition and a body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(EmptyStmt stmt, Graph graph) {
        // a semicolon where a statement was expected
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt stmt, Graph graph) {
        // a call to super or this in a constructor
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ExpressionStmt stmt, Graph graph) {
        // a wrapper around an expression to allow it to be used as a statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ForEachStmt stmt, Graph graph) {
        // a for-each loop with a body and variable declaration
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ForStmt stmt, Graph graph) {
        // a for-loop with an initialization, comparison, update and body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(IfStmt stmt, Graph graph) {
        // an if-then statement with an optional else statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LocalClassDeclarationStmt stmt, Graph graph) {
        // a class declaration inside a method body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ReturnStmt stmt, Graph graph) {
        // a return statement with an optional return expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchEntry entry, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchStmt stmt, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SynchronizedStmt stmt, Graph graph) {
        // a usage of the synchronized keyword with an expression and a body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ThrowStmt stmt, Graph graph) {
        // a usage of the throw statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TryStmt stmt, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnparsableStmt stmt, Graph graph) {
        // a statement that caused parser errors
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(WhileStmt stmt, Graph graph) {
        // a while loop with a condition and body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // expressions
    // ********************************************************************************************

    @Override
    public void visit(ArrayAccessExpr expr, Graph graph) {
        // an array access (square brackets) with a name and index expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ArrayCreationExpr expr, Graph graph) {
        // an array creation expression consists of an element type, a list of one or more array
        // creation levels (containers for size expressions), and an optional initializer.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ArrayCreationLevel level, Graph graph) {
        // a container for an array dimension expression
    }

    @Override
    public void visit(ArrayInitializerExpr expr, Graph graph) {
        // a curly braces initialisation of an array literal, containing a list of expressions
        // which might be array initialisations themselves in the case of multi-dimensional arrays.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(AssignExpr expr, Graph graph) {
        // an assignment consisting of a target, value and assignment operator (see the
        // AssignExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BinaryExpr expr, Graph graph) {
        // a binary expression consisting of a left and right operand and binary operator (see
        // the BinaryExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CastExpr expr, Graph graph) {
        // a cast expression consisting of a type and operand
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ClassExpr expr, Graph graph) {
        // any access of type class, eg. ClassExpr.class
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ConditionalExpr expr, Graph graph) {
        // a ternary expression consisting of a condition, a "then" expression and an "else" expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(EnclosedExpr expr, Graph graph) {
        // an expression between brackets
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(FieldAccessExpr expr, Graph graph) {
        // access of a named field in an object or class (the scope)
        // TODO: how do we know if this is an instance or static access?
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(InstanceOfExpr expr, Graph graph) {
        // a use of the instanceof operator with a type and expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LambdaExpr expr, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodCallExpr expr, Graph graph) {
        // a method call on an object or class, with an optional scope and list of type arguments,
        // a name, and a (possibly empty) list of arguments
        // TODO: set scope to this when not present
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodReferenceExpr expr, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(NameExpr expr, Graph graph) {
        // a wrapper for simple names (ie. variables) used in expressions
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ObjectCreationExpr expr, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SuperExpr expr, Graph graph) {
        // any occurrence of the super keyword (with a possible type name)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchExpr expr, Graph graph) {
        // only supported in Java 12
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ThisExpr expr, Graph graph) {
        // any occurrence of the this keyword (with a possible type name)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TypeExpr expr, Graph graph) {
        // TODO: description (used w/ MethodReferenceExpr)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnaryExpr expr, Graph graph) {
        // a unary expression consisting of a single operand and a unary operator (see the
        // UnaryExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VariableDeclarationExpr expr, Graph graph) {
        // an expression containing a list of variable declarations and modifiers
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // literals
    // ********************************************************************************************

    @Override
    public void visit(BooleanLiteralExpr literal, Graph graph) {
        // a boolean literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CharLiteralExpr literal, Graph graph) {
        // a char literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(DoubleLiteralExpr literal, Graph graph) {
        // a float or double constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(IntegerLiteralExpr literal, Graph graph) {
        // an integer constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LongLiteralExpr literal, Graph graph) {
        // a long constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(NullLiteralExpr literal, Graph graph) {
        // a literal null value
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(StringLiteralExpr literal, Graph graph) {
        // a string literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // types
    // ********************************************************************************************

    @Override
    public void visit(ArrayType type, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ClassOrInterfaceType type, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(PrimitiveType type, Graph graph) {
        // a primitive type (see the PrimitiveType.Primitive enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnknownType type, Graph graph) {
        // the type of a lambda parameter with now explicit type declared
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VarType type, Graph graph) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VoidType type, Graph graph) {
        // the return type of a void method declaration
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(WildcardType type, Graph graph) {
        // a wildcard type argument, ie. <?> with an optional extended or super type
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // declarations
    // ********************************************************************************************

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Graph graph) {
        // the definition of a class or interface
        log.trace("Visiting ClassOrInterfaceDeclaration");
        currentClassOrInterface = ClassOrInterfaceInfo.fromClassOrInterfaceDecl(decl);
        log.debug("Walking AST of class '{}'", currentClassOrInterface.simpleName());
        log.debug(currentClassOrInterface.toString());
    }

    @Override
    public void visit(ConstructorDeclaration decl, Graph graph) {
        // a declaration of a constructor
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(FieldDeclaration decl, Graph graph) {
        // a declaration of a class field, with a list of modifiers and variable declarations
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodDeclaration decl, Graph graph) {
        // a full method declaration, with possibly empty lists of modifiers, annotations, type
        // parameters and regular parameters, and a return type, simple name, and body.
        log.trace("Visiting MethodDeclaration");
        currentMethod = MethodInfo.fromMethodDecl(decl);
        log.debug("Walking AST of method '{}'", currentMethod.name());
        log.debug(currentMethod.toString());

        // get position in source code
        // TODO: this can be factored out as a util function
        Optional<Position> posOpt = decl.getBegin();
        int line, col;
        if (posOpt.isPresent()) {
            Position pos = posOpt.get();
            line = pos.line;
            col = pos.column;
        } else {
            line = -1;
            col = -1;
        }

        // generate the method entry node
        CpgTraversalSource g = graph.traversal(CpgTraversalSource.class);
        // TODO: the base properties can be added in a util function given the walk context
        g.addV(CFG_NODE)
                .property(NODE_TYPE, ENTRY)
                .property(FILE_PATH, currentFilePath)
                .property(FILE_NAME, currentFileName)
                .property(PACKAGE_NAME, currentPackage)
                .property(CLASS_NAME, currentClassOrInterface.simpleName())
                .property(METHOD_NAME, currentMethod.name())
                .property(LINE_NO, line)
                .property(COL_NO, col)
                .iterate();
    }

    @Override
    public void visit(PackageDeclaration decl, Graph graph) {
        // a package declaration consisting of a possibly qualified package name
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VariableDeclarator decl, Graph graph) {
        // the declaration of a variable consisting of a type, name, and optional initializer
        // expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

}