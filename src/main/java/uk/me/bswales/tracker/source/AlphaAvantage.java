package uk.me.bswales.tracker.source;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import uk.me.bswales.tracker.DayRange;
import uk.me.bswales.tracker.RangeCalculator;
import uk.me.bswales.tracker.TickerData;
import uk.me.bswales.tracker.TickerDataValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
        return (dayLimit - used) > 1;
    }

    @Override
    public TickerData fetch(String ticker) {
        TimeSeriesResponse response = AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(ticker)
                .outputSize(OutputSize.COMPACT)
                .fetchSync();

        used++;

        if (response.getErrorMessage() != null) {
            System.err.println("API error for " + ticker + ": " + response.getErrorMessage());
            return null;
        }

        TickerData tickerData = populateTicker(ticker, response);

        // If the daily data didn't pass validation for sixMonthPrice,
        // fetch weekly data to get the 6-month ago close price
        if (tickerData != null && needsSixMonthPrice(tickerData)) {
            BigDecimal sixMonthPrice = fetchSixMonthPrice(ticker);
            if (sixMonthPrice != null) {
                tickerData.setSixMonthPrice(sixMonthPrice);
            }
        }

        return tickerData;
    }

    /**
     * Checks whether the ticker data still needs a sixMonthPrice populated.
     * Returns true if sixMonthPrice is null or if the validator flags it as failing
     * (meaning daily data didn't have enough history).
     */
    private static boolean needsSixMonthPrice(TickerData tickerData) {
        if (tickerData.getSixMonthPrice() == null) {
            return true;
        }
        // Run validation and see if the sixMonthPrice check failed
        return TickerDataValidator.validate(tickerData).stream()
                .filter(r -> r.getFieldName().contains("sixMonthPrice"))
                .noneMatch(TickerDataValidator.CheckResult::isPassed);
    }

    /**
     * Fetches weekly time series data and extracts the closing price
     * from approximately 6 months (26 weeks) ago.
     */
    private BigDecimal fetchSixMonthPrice(String ticker) {
        TimeSeriesResponse weeklyResponse = AlphaVantage.api()
                .timeSeries()
                .weekly()
                .forSymbol(ticker)
                .fetchSync();

        used++;

        if (weeklyResponse.getErrorMessage() != null) {
            System.err.println("Weekly API error for " + ticker + ": " + weeklyResponse.getErrorMessage());
            return null;
        }

        List<StockUnit> weeklyUnits = weeklyResponse.getStockUnits();
        if (weeklyUnits != null && !weeklyUnits.isEmpty() && weeklyUnits.size() > 26) {
            return BigDecimal.valueOf(weeklyUnits.get(26).getClose());
        }
        return null;
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

        List<DayRange> allRanges = new ArrayList<>(stockUnits.size());
        for (StockUnit unit : stockUnits) {
            LocalDate date = LocalDate.parse(unit.getDate());
            allRanges.add(new DayRange(date, unit.getHigh(), unit.getLow(), unit.getClose()));
        }

        // 5-day high/low using RangeCalculator (uses the 5 most recent by date)
        tickerData.setFiveDayHigh(BigDecimal.valueOf(RangeCalculator.highestHigh(allRanges)));
        tickerData.setFiveDayLow(BigDecimal.valueOf(RangeCalculator.lowestLow(allRanges)));

        // Average daily range as a percentage using RangeCalculator (uses the 20 most recent by date)
        double adr = RangeCalculator.averageDailyRange(allRanges);
        tickerData.setAverageDailyRange((int) Math.round(adr));

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

        // Price at approximately 6 months ago (120 trading days) — may be null
        // if daily data doesn't go back that far. The caller will fetch weekly
        // data to fill this in if validation fails.
        if (stockUnits.size() > 120) {
            tickerData.setSixMonthPrice(BigDecimal.valueOf(stockUnits.get(120).getClose()));
        }

        return tickerData;
    }
}