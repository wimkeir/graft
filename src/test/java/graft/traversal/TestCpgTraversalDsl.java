package graft.traversal;

import graft.cpg.structure.CodePropertyGraph;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCpgTraversalDsl {

    private static final String CPG_PATH = "src/test/resources/simple.json";
    private static CodePropertyGraph cpg;

    @BeforeClass
    public static void setUpClass() {
        cpg = CodePropertyGraph.fromFile(CPG_PATH);
    }

    @Test
    public void testMatches() {

    }

}
