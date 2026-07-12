package uk.me.bswales.tracker;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TickerDataValidator}.
 */
class TickerDataValidatorTest {

    @Test
    void unsetFieldsAllPass() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        assertTrue(results.stream().allMatch(TickerDataValidator.CheckResult::isPassed),
                "All checks should pass when fields are unset");
    }

    @Test
    void averageDailyRangePassesWhenFiveOrMore() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setAverageDailyRange(5);

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult adr = findResult(results, "averageDailyRange");
        assertTrue(adr.isPassed());
    }

    @Test
    void averageDailyRangeFailsWhenBelowFive() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setAverageDailyRange(3);

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult adr = findResult(results, "averageDailyRange");
        assertFalse(adr.isPassed());
    }

    @Test
    void volumePassesWhenThreeMillionOrMore() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setVolume(3_000_000);

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult vol = findResult(results, "volume");
        assertTrue(vol.isPassed());
    }

    @Test
    void volumeFailsWhenBelowThreeMillion() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setVolume(1_000_000);

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult vol = findResult(results, "volume");
        assertFalse(vol.isPassed());
    }

    @Test
    void priceWithin15PercentOfFiveDayHighPasses() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(100));
        data.setFiveDayHigh(BigDecimal.valueOf(110));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs fiveDayHigh");
        assertTrue(check.isPassed());
    }

    @Test
    void priceWithin15PercentOfFiveDayHighFails() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(50));
        data.setFiveDayHigh(BigDecimal.valueOf(110));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs fiveDayHigh");
        assertFalse(check.isPassed());
    }

    @Test
    void priceWithin15PercentOfFiveDayLowPasses() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(100));
        data.setFiveDayLow(BigDecimal.valueOf(90));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs fiveDayLow");
        assertTrue(check.isPassed());
    }

    @Test
    void priceWithin15PercentOfFiveDayLowFails() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(200));
        data.setFiveDayLow(BigDecimal.valueOf(90));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs fiveDayLow");
        assertFalse(check.isPassed());
    }

    @Test
    void price25PercentHigherThanOneMonthPasses() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(125));
        data.setOneMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs oneMonthPrice");
        assertTrue(check.isPassed());
    }

    @Test
    void price25PercentHigherThanOneMonthFails() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(110));
        data.setOneMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs oneMonthPrice");
        assertFalse(check.isPassed());
    }

    @Test
    void price50PercentHigherThanThreeMonthPasses() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(150));
        data.setThreeMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs threeMonthPrice");
        assertTrue(check.isPassed());
    }

    @Test
    void price50PercentHigherThanThreeMonthFails() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(120));
        data.setThreeMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs threeMonthPrice");
        assertFalse(check.isPassed());
    }

    @Test
    void price100PercentHigherThanSixMonthPasses() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(200));
        data.setSixMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs sixMonthPrice");
        assertTrue(check.isPassed());
    }

    @Test
    void price100PercentHigherThanSixMonthFails() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(150));
        data.setSixMonthPrice(BigDecimal.valueOf(100));

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        TickerDataValidator.CheckResult check = findResult(results, "priceCurrent vs sixMonthPrice");
        assertFalse(check.isPassed());
    }

    @Test
    void summarizeReportsCorrectCounts() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setAverageDailyRange(3); // fail
        data.setVolume(1_000_000);    // fail

        List<TickerDataValidator.CheckResult> results = TickerDataValidator.validate(data);
        String summary = TickerDataValidator.summarize(results);
        assertTrue(summary.contains("passed"));
        assertTrue(summary.contains("failed"));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static TickerDataValidator.CheckResult findResult(List<TickerDataValidator.CheckResult> results, String fieldName) {
        return results.stream()
                .filter(r -> r.getFieldName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No result found for: " + fieldName));
    }
}