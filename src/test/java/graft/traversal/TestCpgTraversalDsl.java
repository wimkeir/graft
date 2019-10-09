package graft.traversal;

import graft.cpg.structure.CodePropertyGraph;
import graft.cpg.structure.VertexDescription;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graft.Const.*;
import static org.junit.Assert.*;

public class TestCpgTraversalDsl {

    private static final String CPG_PATH = "src/test/resources/simple_cpg.json";
    private static CodePropertyGraph cpg;

    @BeforeClass
    public static void setUpClass() {
        cpg = CodePropertyGraph.fromFile(CPG_PATH);
    }

    @Test
    public void testMatches() {
        String regex = "[a-zA-Z_]*";
        Map<String, String> props = new HashMap<>();
        props.put(NODE_TYPE, ENTRY);
        props.put(TEXT_LABEL, regex);
        VertexDescription descr = new VertexDescription("entry-nodes", CFG_NODE, props);

        List<Vertex> matches = cpg.traversal().V().matches(descr).toList();
        assertEquals(4, matches.size());

        for (Vertex match : matches) {
            assertEquals(ENTRY, match.value(NODE_TYPE));
            assertTrue(match.value(TEXT_LABEL).toString().matches(regex));
        }
    }

}
