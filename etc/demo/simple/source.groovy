import graft.traversal.__
import static graft.Const.*

cpg.traversal().V()
        .hasLabel(AST_NODE)
        .has(NODE_TYPE, INVOKE_EXPR)
        .hasPattern(METHOD_SIG, ".*source.*")
        .in(AST_EDGE)
        .until(__.hasLabel(CFG_NODE)).repeat(__.in(AST_EDGE))
        .has(NODE_TYPE, ASSIGN_STMT)