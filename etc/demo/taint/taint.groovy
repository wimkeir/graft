import graft.traversal.__;

import static graft.Const.*;

source = __.hasLabel(CFG_NODE)                              \
        .has(NODE_TYPE, ASSIGN_STMT)                        \
        .where(                                             \
                __.getVal()                                 \
                .has(NODE_TYPE, EXPR)                       \
                .has(EXPR_TYPE, INVOKE_EXPR)                \
                .hasPattern(METHOD_SIG, ".*source.*")
        )

sink =  __.hasLabel(CFG_NODE)                               \
        .where(                                             \
                __.outE(AST_EDGE)                           \
                .inV()                                      \
                .has(NODE_TYPE, EXPR)                       \
                .has(EXPR_TYPE, INVOKE_EXPR)                \
                .hasPattern(METHOD_SIG, ".*sink.*")
        )

sanitizer = __.hasLabel(CFG_NODE)                           \
        .where(                                             \
                __.outE(AST_EDGE)                           \
                .inV()                                      \
                .has(NODE_TYPE, EXPR)                       \
                .has(EXPR_TYPE, INVOKE_EXPR)                \
                .hasPattern(METHOD_SIG, ".*sanitize.*")
        )