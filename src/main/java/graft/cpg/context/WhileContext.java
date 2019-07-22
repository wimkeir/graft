package graft.cpg.context;

import com.github.javaparser.ast.stmt.WhileStmt;

import org.apache.tinkerpop.gremlin.structure.Vertex;


public class WhileContext extends AstWalkContext {

    private int nrStmtsTotal;
    private int nrStmtsRemaining;
    private Vertex conditional;

    public WhileContext(AstWalkContext outerContext, WhileStmt stmt, Vertex conditional) {
        super(outerContext);
        inBlock = true;
        nrStmtsTotal = nrStmts(stmt.getBody());
        nrStmtsRemaining = nrStmtsTotal;
        this.conditional = conditional;
    }

    public int getStmtsTotal() {
        return nrStmtsTotal;
    }

    public Vertex getConditional() {
        return conditional;
    }

    @Override
    public int getStmtsRemaining() {
        return nrStmtsRemaining;
    }

    @Override
    public void decrStmtsRemaining() {
        assert nrStmtsRemaining > 0;
        nrStmtsRemaining--;
    }

}
