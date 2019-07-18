package graft.cpg;

import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.db.GraphUtil.graph;

/**
 * TODO: javadoc
 */
public class AstNodeVisitor extends VoidVisitorWithDefaults<AstWalkContext> {

    private static Logger log = LoggerFactory.getLogger(AstNodeVisitor.class);

    @Override
    public void defaultAction(Node node, AstWalkContext context) {
        log.debug("Unsupported node type '{}', ignoring", node.getClass());
    }

    @Override
    public void defaultAction(NodeList list, AstWalkContext context) {
        log.debug("NodeList visitor method not implemented");
    }

    // TODO: enums, union/intersection types, annotations

    @Override
    public void visit(CompilationUnit cu, AstWalkContext context) {
        log.trace("Visiting CompilationUnit");
        context.update(cu);
        log.debug("Walking AST of file '{}'", context.currentFileName());
    }

    @Override
    public void visit(Modifier mod, AstWalkContext context) {
        log.trace("Visiting Modifier");
    }

    @Override
    public void visit(Name name, AstWalkContext context) {
        // a name that may consist of multiple dot-separated qualifiers and identifiers
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SimpleName name, AstWalkContext context) {
        log.trace("Visiting SimpleName");
    }

    @Override
    public void visit(Parameter param, AstWalkContext context) {
        // a parameter to a method declaration or lambda
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TypeParameter param, AstWalkContext context) {
        // a generic type parameter
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // statements / blocks
    // ********************************************************************************************

    @Override
    public void visit(AssertStmt stmt, AstWalkContext context) {
        // an assert statement with a check condition and optional message
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BlockStmt stmt, AstWalkContext context) {
        // a block of statements enclosed between curly braces
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BreakStmt stmt, AstWalkContext context) {
        // a break statement with an optional label or value
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CatchClause clause, AstWalkContext context) {
        // the catch part of a try-catch-finally construct
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ContinueStmt stmt, AstWalkContext context) {
        // a continue statement with an optional label
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(DoStmt stmt, AstWalkContext context) {
        // a do-while loop with a condition and a body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(EmptyStmt stmt, AstWalkContext context) {
        // a semicolon where a statement was expected
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt stmt, AstWalkContext context) {
        // a call to super or this in a constructor
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ExpressionStmt stmt, AstWalkContext context) {
        // a wrapper around an expression to allow it to be used as a statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ForEachStmt stmt, AstWalkContext context) {
        // a for-each loop with a body and variable declaration
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ForStmt stmt, AstWalkContext context) {
        // a for-loop with an initialization, comparison, update and body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(IfStmt stmt, AstWalkContext context) {
        // an if-then statement with an optional else statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LocalClassDeclarationStmt stmt, AstWalkContext context) {
        // a class declaration inside a method body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ReturnStmt stmt, AstWalkContext context) {
        // a return statement with an optional return expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchEntry entry, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchStmt stmt, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SynchronizedStmt stmt, AstWalkContext context) {
        // a usage of the synchronized keyword with an expression and a body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ThrowStmt stmt, AstWalkContext context) {
        // a usage of the throw statement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TryStmt stmt, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnparsableStmt stmt, AstWalkContext context) {
        // a statement that caused parser errors
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(WhileStmt stmt, AstWalkContext context) {
        // a while loop with a condition and body
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // expressions
    // ********************************************************************************************

    @Override
    public void visit(ArrayAccessExpr expr, AstWalkContext context) {
        // an array access (square brackets) with a name and index expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ArrayCreationExpr expr, AstWalkContext context) {
        // an array creation expression consists of an element type, a list of one or more array
        // creation levels (containers for size expressions), and an optional initializer.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ArrayCreationLevel level, AstWalkContext context) {
        // a container for an array dimension expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ArrayInitializerExpr expr, AstWalkContext context) {
        // a curly braces initialisation of an array literal, containing a list of expressions
        // which might be array initialisations themselves in the case of multi-dimensional arrays.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(AssignExpr expr, AstWalkContext context) {
        // an assignment consisting of a target, value and assignment operator (see the
        // AssignExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(BinaryExpr expr, AstWalkContext context) {
        // a binary expression consisting of a left and right operand and binary operator (see
        // the BinaryExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CastExpr expr, AstWalkContext context) {
        // a cast expression consisting of a type and operand
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ClassExpr expr, AstWalkContext context) {
        // any access of type class, eg. ClassExpr.class
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ConditionalExpr expr, AstWalkContext context) {
        // a ternary expression consisting of a condition, a "then" expression and an "else" expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(EnclosedExpr expr, AstWalkContext context) {
        // an expression between brackets
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(FieldAccessExpr expr, AstWalkContext context) {
        // access of a named field in an object or class (the scope)
        // TODO: how do we know if this is an instance or static access?
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(InstanceOfExpr expr, AstWalkContext context) {
        // a use of the instanceof operator with a type and expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LambdaExpr expr, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodCallExpr expr, AstWalkContext context) {
        // a method call on an object or class, with an optional scope and list of type arguments,
        // a name, and a (possibly empty) list of arguments
        // TODO: set scope to this when not present
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodReferenceExpr expr, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(NameExpr expr, AstWalkContext context) {
        // a wrapper for simple names (ie. variables) used in expressions
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ObjectCreationExpr expr, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SuperExpr expr, AstWalkContext context) {
        // any occurrence of the super keyword (with a possible type name)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(SwitchExpr expr, AstWalkContext context) {
        // only supported in Java 12
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ThisExpr expr, AstWalkContext context) {
        // any occurrence of the this keyword (with a possible type name)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(TypeExpr expr, AstWalkContext context) {
        // TODO: description (used w/ MethodReferenceExpr)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnaryExpr expr, AstWalkContext context) {
        // a unary expression consisting of a single operand and a unary operator (see the
        // UnaryExpr.Operator enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VariableDeclarationExpr expr, AstWalkContext context) {
        // an expression containing a list of variable declarations and modifiers
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // literals
    // ********************************************************************************************

    @Override
    public void visit(BooleanLiteralExpr literal, AstWalkContext context) {
        // a boolean literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(CharLiteralExpr literal, AstWalkContext context) {
        // a char literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(DoubleLiteralExpr literal, AstWalkContext context) {
        // a float or double constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(IntegerLiteralExpr literal, AstWalkContext context) {
        // an integer constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(LongLiteralExpr literal, AstWalkContext context) {
        // a long constant
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(NullLiteralExpr literal, AstWalkContext context) {
        // a literal null value
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(StringLiteralExpr literal, AstWalkContext context) {
        // a string literal
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // types
    // ********************************************************************************************

    @Override
    public void visit(ArrayType type, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(ClassOrInterfaceType type, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(PrimitiveType type, AstWalkContext context) {
        // a primitive type (see the PrimitiveType.Primitive enum)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(UnknownType type, AstWalkContext context) {
        // the type of a lambda parameter with now explicit type declared
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VarType type, AstWalkContext context) {
        // TODO: description
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(VoidType type, AstWalkContext context) {
        // the return type of a void method declaration
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(WildcardType type, AstWalkContext context) {
        // a wildcard type argument, ie. <?> with an optional extended or super type
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ********************************************************************************************
    // declarations
    // ********************************************************************************************

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, AstWalkContext context) {
        log.trace("Visiting ClassOrInterfaceDeclaration");
        context.update(decl);
        log.debug("Walking AST of class '{}'", context.currentClass());
    }

    @Override
    public void visit(ConstructorDeclaration decl, AstWalkContext context) {
        // a declaration of a constructor
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(FieldDeclaration decl, AstWalkContext context) {
        // a declaration of a class field, with a list of modifiers and variable declarations
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(MethodDeclaration decl, AstWalkContext context) {
        log.trace("Visiting MethodDeclaration");
        context.update(decl);
        log.debug("Walking AST of method '{}'", context.currentMethod());

        String textLabel = decl.getDeclarationAsString(true, false, true);

        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        // TODO: the base properties can be added in a util function given the walk context, node and label
        g.addV(CFG_NODE)
                .property(NODE_TYPE, ENTRY)
                .property(FILE_PATH, context.currentFilePath())
                .property(FILE_NAME, context.currentFileName())
                .property(PACKAGE_NAME, context.currentPackage())
                .property(CLASS_NAME, context.currentClass())
                .property(METHOD_NAME, context.currentMethod())
                .property(TEXT_LABEL, textLabel)
                .property(LINE_NO, getLineNr(decl))
                .property(COL_NO, getColNr(decl))
                .iterate();
    }

    @Override
    public void visit(PackageDeclaration decl, AstWalkContext context) {
        // a package declaration consisting of a possibly qualified package name
        log.trace("Visiting PackageDeclaration");
        context.update(decl);
    }

    @Override
    public void visit(VariableDeclarator decl, AstWalkContext context) {
        // the declaration of a variable consisting of a type, name, and optional initializer
        // expression
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getLineNr(Node node) {
        Optional<Position> posOpt = node.getBegin();
        if (posOpt.isPresent()) {
            return posOpt.get().line;
        } else {
            return -1;
        }
    }

    private int getColNr(Node node) {
        Optional<Position> posOpt = node.getBegin();
        if (posOpt.isPresent()) {
            return posOpt.get().column;
        } else {
            return -1;
        }
    }

}