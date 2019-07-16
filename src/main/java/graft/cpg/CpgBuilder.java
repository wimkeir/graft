package graft.cpg;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

/**
 * TODO: javadoc
 */
public class CpgBuilder {

    private SourceRoot srcRoot;

    public CpgBuilder(String srcRoot) {
        this.srcRoot = new SourceRoot(Paths.get(srcRoot));
        System.out.println("New CpgBuilder (srcRoot=" + srcRoot + ")");
    }

    /**
     * TODO: javadoc
     */
    public void buildCpg() {
        System.out.println("Building CPG");

        List<ParseResult<CompilationUnit>> results;
        try {
            results = srcRoot.tryToParse();
        } catch (IOException e) {
            System.out.println("IOException in <CpgBuilder>.buildCpg");
            System.out.println(e.getMessage());
            return;
        }

        for (ParseResult<CompilationUnit> result : results) {
            if (result.isSuccessful()) {
                CompilationUnit cu = result.getResult().get();
                cu.findRootNode().walk(new AstWalker(null));
            } else {
                System.out.println("Problems with parse");
                List<Problem> problems = result.getProblems();
                for (Problem problem : problems) {
                    System.out.println(problem.getVerboseMessage());
                }
            }
        }
    }

}
