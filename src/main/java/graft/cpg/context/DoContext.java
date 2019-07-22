package graft.cpg.context;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.DoStmt;

public class DoContext extends AstWalkContext {

    private int nrStmtsTotal;
    private int nrStmtsRemaining;
    private Expression condExpr;

    public DoContext(AstWalkContext outerContext, DoStmt stmt) {
        super(outerContext);
        inBlock = true;
        nrStmtsTotal = nrStmts(stmt.getBody());
        nrStmtsRemaining = nrStmtsTotal;
        condExpr = stmt.getCondition();
    }

    public int getStmtsTotal() {
        return nrStmtsTotal;
    }

    public Expression getConditionalExpr() {
        return condExpr;
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
