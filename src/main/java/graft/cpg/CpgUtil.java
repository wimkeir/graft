package graft.cpg;

import java.util.Optional;

import com.github.javaparser.Position;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import graft.traversal.CpgTraversalSource;

import static graft.Const.*;
import static graft.db.GraphUtil.graph;

class CpgUtil {

    static int lineNr(Optional<Position> pos) {
        if (pos.isPresent()) {
            return pos.get().line;
        }
        return -1;
    }

    static int colNr(Optional<Position> pos) {
        if (pos.isPresent()) {
            return pos.get().column;
        }
        return -1;
    }

    static Edge genAstEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        return genCpgEdge(AST_EDGE, from, to, edgeType, textLabel);
    }

    static Edge genCfgEdge(Vertex from, Vertex to, String edgeType, String textLabel) {
        return genCpgEdge(CFG_EDGE, from, to, edgeType, textLabel);
    }

    static Vertex genAstNode(AstWalkContext context, Optional<Position> pos, String nodeType, String textLabel) {
        return genCpgNode(context, AST_NODE, pos, nodeType, textLabel);
    }

    static Vertex genCfgNode(AstWalkContext context, Optional<Position> pos, String nodeType, String textLabel) {
        return genCpgNode(context, CFG_NODE, pos, nodeType, textLabel);
    }

    private static Edge genCpgEdge(String label, Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addE(label)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

    private static Vertex genCpgNode(AstWalkContext context,
                      String label,
                      Optional<Position> pos,
                      String nodeType,
                      String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addV(label)
                .property(NODE_TYPE, nodeType)
                .property(TEXT_LABEL, textLabel)
                .property(FILE_PATH, context.currentFilePath())
                .property(FILE_NAME, context.currentFileName())
                .property(PACKAGE_NAME, context.currentPackage())
                .property(CLASS_NAME, context.currentClass())
                .property(METHOD_NAME, context.currentMethod())
                .property(LINE_NO, lineNr(pos))
                .property(COL_NO, colNr(pos))
                .next();
    }
}
