package d4m.acc.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import d4m.acc.query.d4MQuery.*;
import d4m.acc.query.d4MQuery.util.D4MQuerySwitch;

import org.apache.accumulo.core.data.Range;

public class ScanCriteriaBuilder extends D4MQuerySwitch<List<Range>> { 
    
    @Override
    public List<Range> caseLiteralExpr(LiteralExpr expr) {
        return List.of(new Range(expr.getValue()));
    }

    @Override
    public List<Range> caseRangeExpr(RangeExpr expr) {
        return List.of(new Range(expr.getFrom(), expr.getTo()));
    }

    @Override
    public List<Range> caseListExpr(ListExpr expr) {
        List<Range> ranges = new ArrayList<>();
        for (String val : expr.getValues()) {
            ranges.add(new Range(val));
        }
        return ranges;
    }

    @Override
    public List<Range> caseRowColWildcard(RowColWildcard expr) {
        // Return null or empty list to signify full scan — up to you
        return Collections.emptyList(); // interpreted as a wildcard/full-scan by caller
    }

    // Add StartsWithExpr or RegexExpr if needed — for now you can throw if unsupported
    @Override
    public List<Range> caseStartsWithExpr(StartsWithExpr expr) {
        throw new UnsupportedOperationException("StartsWithExpr not yet supported");
    }

    @Override
    public List<Range> caseRegexExpr(RegexExpr expr) {
        throw new UnsupportedOperationException("RegexExpr not yet supported");
    }
}
