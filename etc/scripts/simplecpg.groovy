import graft.cpg.structure.*;
import java.util.*;

import static graft.Const.*;
import static graft.traversal.__.*;

srcProps = new HashMap<String, String>()
srcProps.put(NODE_TYPE, ASSIGN_STMT)
srcDescr = new VertexDescription("source", CFG_NODE, srcProps)

sinkProps = new HashMap<String, String>()
sinkProps.put(NODE_TYPE, RETURN_STMT)
sinkDescr = new VertexDescription("sink", CFG_NODE, sinkProps)

sanProps = new HashMap<String, String>()
sanProps.put(NODE_TYPE, CONDITIONAL_STMT)
sanDescr = new VertexDescription("sanitizer", CFG_NODE, sanProps)

cpg = CodePropertyGraph.fromFile("etc/dumps/simple.json")

sources = cpg.traversal().getMatches(srcDescr).toList()
sinks = cpg.traversal().getMatches(sinkDescr).toList()

v = sources.get(0)
w = sinks.get(0)

// find paths between vertices
paths = cpg.traversal().V(v) \
    .repeat(out(CFG_EDGE).simplePath()) \
    .until(is(w)) \
    .path().toList()

def sanitized(cpg, path, sanDescr) {
    for (vertex in path) {
        if (cpg.traversal().V(vertex).matches(sanDescr)) {
            return true
        }
    }
    return false
}