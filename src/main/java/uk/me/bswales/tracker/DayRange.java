package uk.me.bswales.tracker;

import java.time.LocalDate;

/**
 * A record that holds the date, high, low, and close values for a single day's trading range.
 *
 * @param date    the date of the trading day
 * @param dayHigh the highest price of the day
 * @param dayLow  the lowest price of the day
 * @param close   the closing price of the day
 */
public record DayRange(LocalDate date, double dayHigh, double dayLow, double close) {
}
