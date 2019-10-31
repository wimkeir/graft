package graft.traversal;

import graft.cpg.structure.CodePropertyGraph;
import graft.cpg.structure.VertexDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static graft.Const.*;
import static org.junit.Assert.*;

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
