package uk.me.bswales.tracker;

import java.util.List;

/**
 * Utility class that performs calculations on {@link DayRange} values.
 */
public class RangeCalculator {

    /**
     * Returns the highest {@code dayHigh} value from the given list of {@link DayRange}s.
     *
     * @param ranges a list of DayRange values (expected to contain 5 entries)
     * @return the maximum dayHigh value, or {@code Double.NaN} if the list is empty
     */
    public static double highestHigh(List<DayRange> ranges) {
        return ranges.stream()
                .mapToDouble(DayRange::dayHigh)
                .max()
                .orElse(Double.NaN);
    }

    /**
     * Returns the lowest {@code dayLow} value from the given list of {@link DayRange}s.
     *
     * @param ranges a list of DayRange values (expected to contain 5 entries)
     * @return the minimum dayLow value, or {@code Double.NaN} if the list is empty
     */
    public static double lowestLow(List<DayRange> ranges) {
        return ranges.stream()
                .mapToDouble(DayRange::dayLow)
                .min()
                .orElse(Double.NaN);
    }

    /**
     * Calculates the average daily range as a percentage of the closing price.
     * <p>
     * For each {@link DayRange}, the daily range is {@code (dayHigh - dayLow) / close * 100}.
     * The result is the arithmetic mean of those percentages across all entries.
     *
     * @param ranges a list of DayRange values
     * @return the average daily range as a percentage, or {@code Double.NaN} if the list is empty
     */
    public static double averageDailyRange(List<DayRange> ranges) {
        return ranges.stream()
                .mapToDouble(r -> (r.dayHigh() - r.dayLow()) / r.close() * 100.0)
                .average()
                .orElse(Double.NaN);
    }
}
