import graft.traversal.__
import static graft.Const.*

cpg.traversal().V()
        .hasLabel(AST_NODE)
        .has(NODE_TYPE, INVOKE_EXPR)
        .hasPattern(METHOD_SIG, ".*sink.*")
        .in(AST_EDGE)
        .until(__.hasLabel(CFG_NODE)).repeat(__.in(AST_EDGE))