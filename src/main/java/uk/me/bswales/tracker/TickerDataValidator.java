package uk.me.bswales.tracker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that validates the data set on a {@link TickerData} instance.
 * <p>
 * If a field has not been set (i.e. is {@code null} for {@link BigDecimal}
 * or {@code 0} for {@code int}/{@code long}), that field is treated as a
 * pass and no check is performed.  Only fields that have been populated
 * are validated against the configured thresholds.
 */
public class TickerDataValidator {

    /**
     * A single check result.
     */
    public static class CheckResult {
        private final String fieldName;
        private final boolean passed;
        private final String message;

        CheckResult(String fieldName, boolean passed, String message) {
            this.fieldName = fieldName;
            this.passed = passed;
            this.message = message;
        }

        public String getFieldName() { return fieldName; }
        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
    }

    /**
     * Runs all applicable checks on the given {@link TickerData} instance.
     *
     * @param data the ticker data to validate
     * @return a list of check results, one per applicable field
     */
    public static List<CheckResult> validate(TickerData data) {
        List<CheckResult> results = new ArrayList<>();

        results.add(checkNotNull("ticker", data.getTicker()));
        results.add(checkNotNull("source", data.getSource()));
        results.add(checkAverageDailyRange(data.getAverageDailyRange()));
        results.add(checkPriceWithinPercent("priceCurrent vs fiveDayHigh",
                data.getPriceCurrent(), data.getFiveDayHigh(), 15));
        results.add(checkPriceWithinPercent("priceCurrent vs fiveDayLow",
                data.getPriceCurrent(), data.getFiveDayLow(), 15));
        results.add(checkVolume(data.getVolume()));
        results.add(checkPricePercentHigher("priceCurrent vs oneMonthPrice",
                data.getPriceCurrent(), data.getOneMonthPrice(), 25));
        results.add(checkPricePercentHigher("priceCurrent vs threeMonthPrice",
                data.getPriceCurrent(), data.getThreeMonthPrice(), 50));
        results.add(checkPricePercentHigher("priceCurrent vs sixMonthPrice",
                data.getPriceCurrent(), data.getSixMonthPrice(), 100));

        return results;
    }

    /**
     * Returns a summary line with pass/fail counts.
     */
    public static String summarize(List<CheckResult> results) {
        long passed = results.stream().filter(CheckResult::isPassed).count();
        long failed = results.stream().filter(r -> !r.isPassed()).count();
        return String.format("%d passed, %d failed", passed, failed);
    }

    // ---------------------------------------------------------------
    // Individual checks
    // ---------------------------------------------------------------

    private static CheckResult checkNotNull(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return new CheckResult(fieldName, true, "not set (pass)");
        }
        return new CheckResult(fieldName, true, "ok");
    }

    private static CheckResult checkAverageDailyRange(int adr) {
        if (adr == 0) {
            return new CheckResult("averageDailyRange", true, "not set (pass)");
        }
        boolean pass = adr >= 5;
        return new CheckResult("averageDailyRange", pass,
                pass ? "ok (" + adr + ")" : "too low: " + adr + " (min 5)");
    }

    private static CheckResult checkVolume(int vol) {
        if (vol == 0) {
            return new CheckResult("volume", true, "not set (pass)");
        }
        boolean pass = vol >= 3_000_000;
        return new CheckResult("volume", pass,
                pass ? "ok (" + vol + ")" : "too low: " + vol + " (min 3000000)");
    }

    /**
     * Checks that {@code actual} is within {@code percent}% of {@code target}.
     * For example, if percent = 15, then actual must be between
     * target * 0.85 and target * 1.15 (inclusive).
     */
    private static CheckResult checkPriceWithinPercent(String fieldName,
                                                        BigDecimal actual,
                                                        BigDecimal target,
                                                        int percent) {
        if (actual == null || target == null) {
            return new CheckResult(fieldName, true, "not set (pass)");
        }
        BigDecimal factor = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100));
        BigDecimal lower = target.multiply(BigDecimal.ONE.subtract(factor));
        BigDecimal upper = target.multiply(BigDecimal.ONE.add(factor));

        boolean pass = actual.compareTo(lower) >= 0 && actual.compareTo(upper) <= 0;
        return new CheckResult(fieldName, pass,
                pass ? "ok (" + actual + " within " + percent + "% of " + target + ")"
                        : "out of range: " + actual + " not within " + percent + "% of " + target);
    }

    /**
     * Checks that {@code actual} is at least {@code percent}% higher than {@code target}.
     * For example, if percent = 25, then actual >= target * 1.25.
     */
    private static CheckResult checkPricePercentHigher(String fieldName,
                                                       BigDecimal actual,
                                                       BigDecimal target,
                                                       int percent) {
        if (actual == null || target == null) {
            return new CheckResult(fieldName, true, "not set (pass)");
        }
        BigDecimal multiplier = BigDecimal.valueOf(100 + percent)
                .divide(BigDecimal.valueOf(100));
        BigDecimal required = target.multiply(multiplier);

        boolean pass = actual.compareTo(required) >= 0;
        return new CheckResult(fieldName, pass,
                pass ? "ok (" + actual + " >= " + required + ")"
                        : "too low: " + actual + " < " + required + " (needs " + percent + "% higher than " + target + ")");
    }
}