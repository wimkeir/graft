package graft.cpg.context;

import com.github.javaparser.ast.stmt.IfStmt;

import org.apache.tinkerpop.gremlin.structure.Vertex;


public class IfContext extends AstWalkContext {

    private boolean inThen;
    private int nrStmtsInThenTotal;
    private int nrStmtsInElseTotal;
    private int nrStmtsInThenRemaining;
    private int nrStmtsInElseRemaining;

    private Vertex conditional;
    private Vertex thenTail;
    private Vertex elseTail;

    public IfContext(AstWalkContext outerContext, IfStmt stmt, Vertex conditional) {
        super(outerContext);
        inBlock = true;
        inThen = true;

        nrStmtsInThenTotal = nrStmts(stmt.getThenStmt());
        nrStmtsInThenRemaining = nrStmtsInThenTotal;

        if (stmt.hasElseBranch()) {
            nrStmtsInElseTotal = nrStmts(stmt.getElseStmt().get());
            nrStmtsInElseRemaining = nrStmtsInElseTotal;
        } else {
            nrStmtsInElseTotal = 0;
            nrStmtsInElseRemaining = 0;
        }

        this.conditional = conditional;
        updateCfgTail(this.conditional);
        thenTail = this.conditional;
        elseTail = this.conditional;
    }

    public Vertex thenTail() {
        return thenTail;
    }

    public Vertex elseTail() {
        return elseTail;
    }

    public boolean inThen() {
        return inThen;
    }

    public void enterElse() {
        inThen = false;
    }

    public int getStmtsTotal() {
        if (inThen) {
            return nrStmtsInThenTotal;
        } else {
            return nrStmtsInElseTotal;
        }
    }

    public Vertex getConditional() {
        return conditional;
    }

    @Override
    public int getStmtsRemaining() {
        if (inThen) {
            return nrStmtsInThenRemaining;
        } else {
            return nrStmtsInElseRemaining;
        }
    }

    @Override
    public void decrStmtsRemaining() {
        if (inThen) {
            assert nrStmtsInThenRemaining > 0;
            nrStmtsInThenRemaining--;
        } else {
            assert nrStmtsInElseRemaining > 0;
            nrStmtsInThenRemaining--;
        }
    }

    @Override
    public void updateCfgTail(Vertex tail) {
        super.updateCfgTail(tail);
        if (inThen) {
            thenTail = tail;
        } else {
            elseTail = tail;
        }
    }
}
