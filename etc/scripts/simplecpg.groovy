import graft.cpg.structure.*;
import java.util.*;

import static graft.Const.*;

srcProps = [NODE_TYPE:ASSIGN_STMT]
sinkProps = [NODE_TYPE:INVOKE_STMT]

srcDescr = new VertexDescription("source", CFG_NODE, srcProps)
sinkDescr = new VertexDescription("sink", CFG_NODE, sinkProps)

cpg = CodePropertyGraph.fromFile("etc/dumps/simple.json")

sources = cpg.traversal().getMatches(srcDescr)
sinks = cpg.traversal().getMatches(sinkDescr)