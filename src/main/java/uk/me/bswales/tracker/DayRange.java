package uk.me.bswales.tracker;

/**
 * A record that holds the high, low, and close values for a single day's trading range.
 *
 * @param dayHigh the highest price of the day
 * @param dayLow  the lowest price of the day
 * @param close   the closing price of the day
 */
public record DayRange(double dayHigh, double dayLow, double close) {
}
