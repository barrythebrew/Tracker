package uk.me.bswales.tracker.source;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import uk.me.bswales.tracker.TickerData;

import java.util.Properties;

/**
 * Data source that fetches ticker data from the Alpha Vantage API.
 */
public class AlphaAvantage implements ISource {

    private final int dayLimit;

    private int used = 0;

    public AlphaAvantage(Properties config) {
        Config cfg = Config.builder()
                .key(config.getProperty("apiKey"))
                .timeOut(10)
                .build();

        AlphaVantage.api().init(cfg);
        this.dayLimit = Integer.parseInt(config.getProperty("dayLimit", "25"));
    }

    @Override
    public boolean isAvailable() {
        return used < dayLimit;
    }

    @Override
    public TickerData fetch(String query) {
        // TODO: implement Alpha Vantage API call
        return null;
    }
}