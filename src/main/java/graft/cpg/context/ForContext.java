package graft.cpg.context;

import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ForStmt;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class ForContext extends AstWalkContext {

    private List<Expression> updates;
    private Expression check;
    private int nrStmtsTotal;
    private int nrStmtsRemaining;
    private Vertex lastInit;

    public ForContext(AstWalkContext outerContext, ForStmt stmt, Vertex lastInit) {
        super(outerContext);
        inBlock = true;
        updates = stmt.getUpdate();
        check = null;
        stmt.getCompare().ifPresent(check -> this.check = check);
        nrStmtsTotal = nrStmts(stmt.getBody());
        nrStmtsRemaining = nrStmtsTotal;
        this.lastInit = lastInit;
    }

    public List<Expression> getUpdates() {
        return updates;
    }

    public Expression getCheck() {
        return check;
    }

    public int getStmtsTotal() {
        return nrStmtsTotal;
    }

    public Vertex getLastInit() {
        return lastInit;
    }

    @Override
    public int getStmtsRemaining() {
        return nrStmtsRemaining;
    }

    @Override
    public void decrStmtsRemaining() {
        nrStmtsRemaining--;
    }

}
