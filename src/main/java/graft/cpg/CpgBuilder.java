package graft.cpg;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import graft.GraftException;
import graft.traversal.CpgTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;
import static graft.db.GraphUtil.graph;
import static graft.cpg.CpgUtil.*;

/**
 * TODO: javadoc
 */
public class CpgBuilder {

    private static Logger log = LoggerFactory.getLogger(CpgBuilder.class);

    private SourceRoot srcRoot;

    public CpgBuilder(String srcRoot) {
        this.srcRoot = new SourceRoot(Paths.get(srcRoot));
        log.debug("New CpgBuilder (srcRoot=" + srcRoot + ")");
    }

    /**
     * TODO: javadoc
     */
    public void buildCpg() throws GraftException {
        log.info("Building CPG");

        List<ParseResult<CompilationUnit>> results;
        try {
            results = srcRoot.tryToParse();
        } catch (IOException e) {
            log.error("IOException in <CpgBuilder>.buildCpg", e);
            return;
        }

        for (ParseResult<CompilationUnit> result : results) {
            if (result.isSuccessful()) {
                CompilationUnit cu = result.getResult().get();
                cu.findRootNode().walk(new AstWalker());
            } else {
                log.error("Problems with parse");
                List<Problem> problems = result.getProblems();
                for (Problem problem : problems) {
                    log.error(problem.getVerboseMessage());
                }
            }
        }
    }

    static Edge genCpgEdge(String label, Vertex from, Vertex to, String edgeType, String textLabel) {
        CpgTraversalSource g = graph().traversal(CpgTraversalSource.class);
        return g.addE(label)
                .from(from).to(to)
                .property(EDGE_TYPE, edgeType)
                .property(TEXT_LABEL, textLabel)
                .next();
    }

    static Vertex genCpgNode(AstWalkContext context,
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
