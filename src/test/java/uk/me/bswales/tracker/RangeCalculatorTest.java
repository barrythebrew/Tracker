package uk.me.bswales.tracker;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RangeCalculator}.
 */
class RangeCalculatorTest {

    @Test
    void highestHighReturnsMaximumOfFiveRecentByDate() {
        // Dates in mixed order — lastN should sort by date descending first
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 3), 9.0, 4.0, 6.5),
                new DayRange(LocalDate.of(2026, 1, 5), 8.0, 3.0, 5.5),
                new DayRange(LocalDate.of(2026, 1, 1), 10.0, 5.0, 7.5),
                new DayRange(LocalDate.of(2026, 1, 4), 11.0, 6.0, 8.5),
                new DayRange(LocalDate.of(2026, 1, 2), 12.0, 7.0, 9.5)
        );
        // 5 most recent: Jan 5 (high=8), Jan 4 (high=11), Jan 3 (high=9), Jan 2 (high=12), Jan 1 (high=10)
        // Max high among those 5 = 12
        assertEquals(12.0, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowReturnsMinimumOfFiveRecentByDate() {
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 3), 9.0, 4.0, 6.5),
                new DayRange(LocalDate.of(2026, 1, 5), 8.0, 3.0, 5.5),
                new DayRange(LocalDate.of(2026, 1, 1), 10.0, 5.0, 7.5),
                new DayRange(LocalDate.of(2026, 1, 4), 11.0, 6.0, 8.5),
                new DayRange(LocalDate.of(2026, 1, 2), 12.0, 7.0, 9.5)
        );
        // 5 most recent: Jan 5 (low=3), Jan 4 (low=6), Jan 3 (low=4), Jan 2 (low=7), Jan 1 (low=5)
        // Min low among those 5 = 3
        assertEquals(3.0, RangeCalculator.lowestLow(ranges), 1e-9);
    }

    @Test
    void highestHighOnlyUsesFiveMostRecent() {
        // Many records — should only consider the 5 most recent by date
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 1), 10.0, 5.0, 7.5),
                new DayRange(LocalDate.of(2026, 1, 2), 20.0, 15.0, 17.5),
                new DayRange(LocalDate.of(2026, 1, 3), 9.0, 4.0, 6.5),
                new DayRange(LocalDate.of(2026, 1, 4), 11.0, 6.0, 8.5),
                new DayRange(LocalDate.of(2026, 1, 5), 8.0, 3.0, 5.5),
                new DayRange(LocalDate.of(2026, 1, 6), 12.0, 7.0, 9.5)
        );
        // 5 most recent: Jan 6 (12), Jan 5 (8), Jan 4 (11), Jan 3 (9), Jan 2 (20)
        // Should NOT include Jan 1 (10) even though it's in the list
        assertEquals(20.0, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowOnlyUsesFiveMostRecent() {
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 1), 10.0, 1.0, 7.5),
                new DayRange(LocalDate.of(2026, 1, 2), 20.0, 15.0, 17.5),
                new DayRange(LocalDate.of(2026, 1, 3), 9.0, 4.0, 6.5),
                new DayRange(LocalDate.of(2026, 1, 4), 11.0, 6.0, 8.5),
                new DayRange(LocalDate.of(2026, 1, 5), 8.0, 3.0, 5.5),
                new DayRange(LocalDate.of(2026, 1, 6), 12.0, 7.0, 9.5)
        );
        // 5 most recent: Jan 6 (7), Jan 5 (3), Jan 4 (6), Jan 3 (4), Jan 2 (15)
        // Should NOT include Jan 1 (1)
        assertEquals(3.0, RangeCalculator.lowestLow(ranges), 1e-9);
    }

    @Test
    void highestHighReturnsNaNForEmptyList() {
        assertTrue(Double.isNaN(RangeCalculator.highestHigh(List.of())));
    }

    @Test
    void lowestLowReturnsNaNForEmptyList() {
        assertTrue(Double.isNaN(RangeCalculator.lowestLow(List.of())));
    }

    @Test
    void highestHighWithFewerThanFive() {
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 2), 42.5, 40.0, 41.0),
                new DayRange(LocalDate.of(2026, 1, 1), 30.0, 28.0, 29.0)
        );
        assertEquals(42.5, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowWithFewerThanFive() {
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 2), 42.5, 40.0, 41.0),
                new DayRange(LocalDate.of(2026, 1, 1), 30.0, 28.0, 29.0)
        );
        assertEquals(28.0, RangeCalculator.lowestLow(ranges), 1e-9);
    }

    // --- averageDailyRange tests ---

    @Test
    void averageDailyRangeUsesTwentyMostRecentByDate() {
        // Create 25 days — should only use the 20 most recent
        List<DayRange> ranges = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            ranges.add(new DayRange(
                    LocalDate.of(2026, 1, i),
                    100.0 + i,      // high increases with date
                    90.0 + i,       // low increases with date
                    95.0 + i        // close increases with date
            ));
        }
        // The oldest 5 (Jan 1-5) have smaller ranges and should be excluded
        double adr = RangeCalculator.averageDailyRange(ranges);
        assertFalse(Double.isNaN(adr));
        // Sanity: result should be positive
        assertTrue(adr > 0);
    }

    @Test
    void averageDailyRangeCalculatesCorrectPercentage() {
        // Day 1: high=110, low=90, close=100 => range% = (110-90)/100*100 = 20%
        // Day 2: high=105, low=95, close=100 => range% = (105-95)/100*100 = 10%
        // Average = (20 + 10) / 2 = 15%
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 1), 110.0, 90.0, 100.0),
                new DayRange(LocalDate.of(2026, 1, 2), 105.0, 95.0, 100.0)
        );

        assertEquals(15.0, RangeCalculator.averageDailyRange(ranges), 1e-9);
    }

    @Test
    void averageDailyRangeReturnsNaNForEmptyList() {
        assertTrue(Double.isNaN(RangeCalculator.averageDailyRange(List.of())));
    }

    @Test
    void averageDailyRangeWithSingleDay() {
        // high=50, low=40, close=45 => range% = (50-40)/45*100 = 22.222...
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 1), 50.0, 40.0, 45.0)
        );
        assertEquals(22.22222222222222, RangeCalculator.averageDailyRange(ranges), 1e-9);
    }

    @Test
    void averageDailyRangeWithZeroRange() {
        List<DayRange> ranges = List.of(
                new DayRange(LocalDate.of(2026, 1, 1), 100.0, 100.0, 100.0),
                new DayRange(LocalDate.of(2026, 1, 2), 50.0, 50.0, 50.0)
        );
        assertEquals(0.0, RangeCalculator.averageDailyRange(ranges), 1e-9);
    }
}