package uk.me.bswales.tracker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RangeCalculator}.
 */
class RangeCalculatorTest {

    @Test
    void highestHighReturnsMaximumDayHigh() {
        List<DayRange> ranges = List.of(
                new DayRange(10.0, 5.0, 7.5),
                new DayRange(12.0, 7.0, 9.5),
                new DayRange(9.0, 4.0, 6.5),
                new DayRange(11.0, 6.0, 8.5),
                new DayRange(8.0, 3.0, 5.5)
        );

        assertEquals(12.0, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowReturnsMinimumDayLow() {
        List<DayRange> ranges = List.of(
                new DayRange(10.0, 5.0, 7.5),
                new DayRange(12.0, 7.0, 9.5),
                new DayRange(9.0, 4.0, 6.5),
                new DayRange(11.0, 6.0, 8.5),
                new DayRange(8.0, 3.0, 5.5)
        );

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
    void highestHighWithSingleElement() {
        List<DayRange> ranges = List.of(new DayRange(42.5, 40.0, 41.0));
        assertEquals(42.5, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowWithSingleElement() {
        List<DayRange> ranges = List.of(new DayRange(42.5, 40.0, 41.0));
        assertEquals(40.0, RangeCalculator.lowestLow(ranges), 1e-9);
    }

    @Test
    void highestHighWorksWithIdenticalValues() {
        List<DayRange> ranges = List.of(
                new DayRange(50.0, 30.0, 40.0),
                new DayRange(50.0, 20.0, 35.0),
                new DayRange(50.0, 10.0, 30.0)
        );
        assertEquals(50.0, RangeCalculator.highestHigh(ranges), 1e-9);
    }

    @Test
    void lowestLowWorksWithIdenticalValues() {
        List<DayRange> ranges = List.of(
                new DayRange(100.0, 25.0, 60.0),
                new DayRange(90.0, 25.0, 55.0),
                new DayRange(80.0, 25.0, 50.0)
        );
        assertEquals(25.0, RangeCalculator.lowestLow(ranges), 1e-9);
    }

    // --- averageDailyRange tests ---

    @Test
    void averageDailyRangeCalculatesCorrectPercentage() {
        // Day 1: high=110, low=90, close=100 => range% = (110-90)/100*100 = 20%
        // Day 2: high=105, low=95, close=100 => range% = (105-95)/100*100 = 10%
        // Average = (20 + 10) / 2 = 15%
        List<DayRange> ranges = List.of(
                new DayRange(110.0, 90.0, 100.0),
                new DayRange(105.0, 95.0, 100.0)
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
        List<DayRange> ranges = List.of(new DayRange(50.0, 40.0, 45.0));
        assertEquals(22.22222222222222, RangeCalculator.averageDailyRange(ranges), 1e-9);
    }

    @Test
    void averageDailyRangeWithZeroRange() {
        // high=close=low => range% = 0
        List<DayRange> ranges = List.of(
                new DayRange(100.0, 100.0, 100.0),
                new DayRange(50.0, 50.0, 50.0)
        );
        assertEquals(0.0, RangeCalculator.averageDailyRange(ranges), 1e-9);
    }
}