package graft.cpg;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import org.apache.commons.configuration2.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftException;

/**
 * Handles the actual construction of the CPG.
 *
 * @author Wim Keirsgieter
 */
public class CpgBuilder {

    private static Logger log = LoggerFactory.getLogger(CpgBuilder.class);

    private SourceRoot srcRoot;
    private Configuration options;

    public CpgBuilder(String srcRoot, Configuration options) {
        this.srcRoot = new SourceRoot(Paths.get(srcRoot));
        this.options = options;
    }

    /**
     * Generate and store the CPG in the graph database.
     */
    public void buildCpg() throws GraftException {
        log.info("Building CPG");

        List<ParseResult<CompilationUnit>> results;
        try {
            results = srcRoot.tryToParse();
        } catch (IOException e) {
            log.debug("IOException in <CpgBuilder>.buildCpg", e);
            throw new GraftException("Unable to parse source root (see debug logs)");
        }

        for (ParseResult<CompilationUnit> result : results) {
            if (result.isSuccessful()) {
                CompilationUnit cu = result.getResult().get();
                cu.findRootNode().walk(new AstWalker());
            } else {
                log.debug("Problems with parse");
                List<Problem> problems = result.getProblems();
                for (Problem problem : problems) {
                    log.debug(problem.getVerboseMessage());
                }
                throw new GraftException("Problems encountered while parsing source root (see debug logs)");
            }
        }
    }

}
