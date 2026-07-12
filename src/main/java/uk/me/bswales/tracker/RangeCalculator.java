package uk.me.bswales.tracker;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class that performs calculations on {@link DayRange} values.
 */
public class RangeCalculator {

    /**
     * Returns the highest {@code dayHigh} value among the 5 most recent {@link DayRange}s
     * based on the {@code date} field.
     *
     * @param ranges a list of DayRange values
     * @return the maximum dayHigh value, or {@code Double.NaN} if the list is empty
     */
    public static double highestHigh(List<DayRange> ranges) {
        return getTheMostRecentRecords(ranges, 5).stream()
                .mapToDouble(DayRange::dayHigh)
                .max()
                .orElse(Double.NaN);
    }

    /**
     * Returns the lowest {@code dayLow} value among the 5 most recent {@link DayRange}s
     * based on the {@code date} field.
     *
     * @param ranges a list of DayRange values
     * @return the minimum dayLow value, or {@code Double.NaN} if the list is empty
     */
    public static double lowestLow(List<DayRange> ranges) {
        return getTheMostRecentRecords(ranges, 5).stream()
                .mapToDouble(DayRange::dayLow)
                .min()
                .orElse(Double.NaN);
    }

    /**
     * Calculates the average daily range as a percentage of the closing price
     * over the 20 most recent {@link DayRange}s (based on the {@code date} field).
     * <p>
     * For each {@link DayRange}, the daily range is {@code (dayHigh - dayLow) / close * 100}.
     * The result is the arithmetic mean of those percentages.
     *
     * @param ranges a list of DayRange values
     * @return the average daily range as a percentage, or {@code Double.NaN} if the list is empty
     */
    public static double averageDailyRange(List<DayRange> ranges) {
        return getTheMostRecentRecords(ranges, 20).stream()
                .mapToDouble(r -> (r.dayHigh() - r.dayLow()) / r.close() * 100.0)
                .average()
                .orElse(Double.NaN);
    }

    /**
     * Returns the <em>numberToGet</em> most recent {@link DayRange}s based on their {@code date} field.
     *
     * @param ranges a list of DayRange values
     * @param numberToGet      the number of most recent records to return
     * @return the most recent {@code numberToGet} records, or the whole list if it is shorter than {@code numberToGet}
     */
    private static List<DayRange> getTheMostRecentRecords(List<DayRange> ranges, int numberToGet) {
        return ranges.stream()
                .sorted(Comparator.comparing(DayRange::date).reversed())
                .limit(numberToGet)
                .toList();
    }
}
