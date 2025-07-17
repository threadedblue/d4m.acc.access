package d4m.acc.access;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import d4m.acc.query.d4MQuery.AxisExpr;
import d4m.acc.query.d4MQuery.D4MQuery;
import d4m.acc.query.d4MQuery.QueryExpr;
import d4m.acc.query.d4MQuery.RowColWildcard;
import d4m.acc.query.d4MQuery.util.D4MQuerySwitch;

public class QueryExecutor extends D4MQuerySwitch<ObjectNode> {

    private final String tableName;

    public QueryExecutor(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public ObjectNode caseD4MQuery(D4MQuery query) {
        return doSwitch(query.getQuery()); // delegate to QueryExpr
    }

    @Override
    public ObjectNode caseQueryExpr(QueryExpr expr) {

        AxisExpr rowExpr = expr.getRow();
        AxisExpr colExpr = expr.getCol();

        boolean isRowWildcard = rowExpr instanceof RowColWildcard;
        boolean isColWildcard = colExpr instanceof RowColWildcard;

        if (isRowWildcard && isColWildcard) {
            throw new IllegalArgumentException("Both row and col cannot be ':' — ambiguous scan.");
        }

        String table = null;
        if (isRowWildcard) {
            table = tableName + "T";
            return AccumuloAccess.scanTable(colExpr, table);
        } else {
            table = tableName;
            return AccumuloAccess.scanTable(rowExpr, table);
        }
    }

    @Override
    public ObjectNode defaultCase(EObject object) {
        throw new UnsupportedOperationException("Unknown query component: " + object);
    }
}
