package d4m.acc.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import d4m.acc.query.d4MQuery.*;
import d4m.acc.query.d4MQuery.util.D4MQuerySwitch;

import org.apache.accumulo.core.data.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanCriteriaBuilder extends D4MQuerySwitch<List<Range>> { 
    
	private static final Logger log = LoggerFactory.getLogger(ScanCriteriaBuilder.class);

    @Override
    public List<Range> caseLiteralExpr(LiteralExpr expr) {
        log.trace("caseLiteralExpr=={}", expr.getValue());
        return List.of(new Range(expr.getValue()));
    }

    @Override
    public List<Range> caseRangeExpr(RangeExpr expr) {
        log.trace("caseRangeExpr=={}..{}", expr.getFrom(), expr.getTo());
        return List.of(new Range(expr.getFrom(), expr.getTo()));
    }

    @Override
    public List<Range> caseListExpr(ListExpr expr) {
        List<Range> ranges = new ArrayList<>();
        for (String val : expr.getValues()) {
            ranges.add(new Range(val));
        }
        log.trace("caseListExpr=={}", expr.getValues());
        return ranges;
    }

    @Override
    public List<Range> caseRowColWildcard(RowColWildcard expr) {
        // Return null or empty list to signify full scan — up to you
        log.trace("caseRowColWildcard=={}", expr.toString());
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
