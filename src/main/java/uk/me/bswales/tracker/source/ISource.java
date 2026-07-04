package uk.me.bswales.tracker.source;

import uk.me.bswales.tracker.TickerData;

/**
 * Interface for data sources that provide ticker information.
 */
public interface ISource {

    /**
     * Checks whether this data source is currently available.
     *
     * @return {@code true} if the source is available and ready to query
     */
    boolean isAvailable();

    /**
     * Fetches ticker data for the given query string.
     *
     * @param query the ticker symbol or other identifying string
     * @return {@link TickerData} containing financial information for the queried ticker
     */
    TickerData fetch(String query);
}