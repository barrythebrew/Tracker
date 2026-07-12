package uk.me.bswales.tracker.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.me.bswales.tracker.DayRange;
import uk.me.bswales.tracker.RangeCalculator;
import uk.me.bswales.tracker.TickerData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Data source that fetches ticker data from the Tiingo API.
 */
public class Tiingo implements ISource {

    private static final String API_BASE_URL = "https://api.tiingo.com/tiingo/daily";

    private final String apiKey;
    private final int uniqueSymbols;
    private final int requestPerHour;
    private final int requestPerDay;
    private final String maxBandwidthPerMonth;
    private int used = 0;

    /**
     * Creates a Tiingo data source with the given configuration.
     *
     * @param config properties containing "apiKey", "dayLimit", "uniqueSymbols",
     *               "requestPerHour", "requestPerDay", and "maxBandwidthPerMonth"
     */
    public Tiingo(Properties config) {
        this.apiKey = config.getProperty("apiKey");
        this.uniqueSymbols = Integer.parseInt(config.getProperty("uniqueSymbols", "500"));
        this.requestPerHour = Integer.parseInt(config.getProperty("requestPerHour", "50"));
        this.requestPerDay = Integer.parseInt(config.getProperty("requestPerDay", "1000"));
        this.maxBandwidthPerMonth = config.getProperty("maxBandwidthPerMonth", "1GB");
    }

    @Override
    public boolean isAvailable() {
        // TODO set limits
        return true;
    }

    @Override
    public TickerData fetch(String ticker) {
        try {
            String urlStr = API_BASE_URL + "/" + URLEncoder.encode(ticker, StandardCharsets.UTF_8) + "/prices?token=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Tiingo API error for " + ticker + ": HTTP " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            String jsonResponse = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
            reader.close();
            conn.disconnect();

            used++;

            return parseTickerData(ticker, jsonResponse);

        } catch (IOException e) {
            System.err.println("Failed to fetch data for " + ticker + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses the Tiingo JSON response and populates a TickerData object.
     */
    private TickerData parseTickerData(String ticker, String jsonResponse) {
        try {
            JsonArray priceData = JsonParser.parseString(jsonResponse).getAsJsonArray();
            if (priceData == null || priceData.isEmpty()) {
                return null;
            }

            TickerData tickerData = new TickerData();
            tickerData.setTicker(ticker);
            tickerData.setSource(this.getClass().getName());

            List<DayRange> allRanges = new ArrayList<>();

            // Parse all price data points
            for (int i = 0; i < priceData.size(); i++) {
                JsonObject day = priceData.get(i).getAsJsonObject();
                String dateStr = day.get("date").getAsString();
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10)); // Handle datetime format
                double high = day.get("high").getAsDouble();
                double low = day.get("low").getAsDouble();
                double close = day.get("close").getAsDouble();
                long volume = day.get("volume").getAsLong();

                allRanges.add(new DayRange(date, high, low, close));

                // Most recent day's data (first in array)
                if (i == 0) {
                    tickerData.setPriceCurrent(BigDecimal.valueOf(close));
                    tickerData.setClose(BigDecimal.valueOf(close));
                    tickerData.setVolume((int) volume);
                }
            }

            if (allRanges.isEmpty()) {
                return null;
            }

            // 5-day high/low using RangeCalculator (uses the 5 most recent by date)
            tickerData.setFiveDayHigh(BigDecimal.valueOf(RangeCalculator.highestHigh(allRanges)));
            tickerData.setFiveDayLow(BigDecimal.valueOf(RangeCalculator.lowestLow(allRanges)));

            // Average daily range as a percentage using RangeCalculator (uses the 20 most recent by date)
            double adr = RangeCalculator.averageDailyRange(allRanges);
            tickerData.setAverageDailyRange((int) Math.round(adr));

            // Price at approximately 1 month ago (20 trading days)
            if (allRanges.size() > 20) {
                tickerData.setOneMonthPrice(BigDecimal.valueOf(allRanges.get(20).close()));
            } else if (allRanges.size() > 1) {
                tickerData.setOneMonthPrice(BigDecimal.valueOf(allRanges.get(allRanges.size() - 1).close()));
            }

            // Price at approximately 3 months ago (60 trading days)
            if (allRanges.size() > 60) {
                tickerData.setThreeMonthPrice(BigDecimal.valueOf(allRanges.get(60).close()));
            }

            // Price at approximately 6 months ago (120 trading days)
            if (allRanges.size() > 120) {
                tickerData.setSixMonthPrice(BigDecimal.valueOf(allRanges.get(120).close()));
            }

            return tickerData;

        } catch (Exception e) {
            System.err.println("Failed to parse Tiingo response for " + ticker + ": " + e.getMessage());
            return null;
        }
    }
}