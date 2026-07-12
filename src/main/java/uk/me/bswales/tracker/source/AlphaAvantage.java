package uk.me.bswales.tracker.source;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import uk.me.bswales.tracker.TickerData;

import java.math.BigDecimal;
import java.util.List;
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
    public TickerData fetch(String ticker) {
        TimeSeriesResponse response = AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(ticker)
                .outputSize(OutputSize.COMPACT)
                .fetchSync();

        if (response.getErrorMessage() != null) {
            System.err.println("API error for " + ticker + ": " + response.getErrorMessage());
            return null;
        }

        used++;
        return populateTicker(ticker, response);
    }

    private TickerData populateTicker(String tickerCode, TimeSeriesResponse timeSeriesResponse) {
        List<StockUnit> stockUnits = timeSeriesResponse.getStockUnits();
        if (stockUnits == null || stockUnits.isEmpty()) {
            return null;
        }

        TickerData tickerData = new TickerData();
        tickerData.setTicker(tickerCode);
        tickerData.setSource(this.getClass().getName());

        // Most recent day's data
        StockUnit latest = stockUnits.getFirst();
        tickerData.setPriceCurrent(BigDecimal.valueOf(latest.getClose()));
        tickerData.setClose(BigDecimal.valueOf(latest.getClose()));
        tickerData.setVolume((int) latest.getVolume());

        // Compute 5-day high/low from the first 5 stock units
        int limit = Math.min(5, stockUnits.size());
        double fiveDayHigh = Double.MIN_VALUE;
        double fiveDayLow = Double.MAX_VALUE;
        for (int i = 0; i < limit; i++) {
            StockUnit unit = stockUnits.get(i);
            if (unit.getHigh() > fiveDayHigh) fiveDayHigh = unit.getHigh();
            if (unit.getLow() < fiveDayLow) fiveDayLow = unit.getLow();
        }
        tickerData.setFiveDayHigh(BigDecimal.valueOf(fiveDayHigh));
        tickerData.setFiveDayLow(BigDecimal.valueOf(fiveDayLow));

        // Approximate average daily range over available data
        if (stockUnits.size() > 1) {
            double totalRange = 0;
            int count = Math.min(20, stockUnits.size());
            for (int i = 0; i < count; i++) {
                StockUnit unit = stockUnits.get(i);
                totalRange += unit.getHigh() - unit.getLow();
            }
            tickerData.setAverageDailyRange((int) Math.round(totalRange / count));
        }

        // Price at approximately 1 month ago (20 trading days)
        if (stockUnits.size() > 20) {
            tickerData.setOneMonthPrice(BigDecimal.valueOf(stockUnits.get(20).getClose()));
        } else {
            tickerData.setOneMonthPrice(BigDecimal.valueOf(stockUnits.getLast().getClose()));
        }

        // Price at approximately 3 months ago (60 trading days)
        if (stockUnits.size() > 60) {
            tickerData.setThreeMonthPrice(BigDecimal.valueOf(stockUnits.get(60).getClose()));
        }

        // Price at approximately 6 months ago (120 trading days)
        if (stockUnits.size() > 120) {
            tickerData.setSixMonthPrice(BigDecimal.valueOf(stockUnits.get(120).getClose()));
        }

        return tickerData;
    }
}