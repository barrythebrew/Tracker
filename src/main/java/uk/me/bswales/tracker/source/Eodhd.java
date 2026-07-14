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
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Data source that fetches ticker data from the EODHD API.
 */
public class Eodhd implements ISource {

    private static final String API_BASE_URL = "https://eodhd.com/api";

    private final String apiKey;

    /**
     * Creates an EODHD data source with the given configuration.
     *
     * @param config properties containing "apiKey"
     */
    public Eodhd(Properties config) {
        this.apiKey = config.getProperty("apiKey");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public TickerData fetch(String ticker) {
        try {
            String urlStr = buildUrl(ticker);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("EODHD API error for " + ticker + ": HTTP " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            String jsonResponse = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            conn.disconnect();

            return parseTickerData(ticker, jsonResponse);

        } catch (IOException e) {
            System.err.println("Failed to fetch EODHD data for " + ticker + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds the request URL for the given ticker.
     */
    private String buildUrl(String ticker) {
        return API_BASE_URL + "/eod/" + URLEncoder.encode(ticker, StandardCharsets.UTF_8)
                + "?api_token=" + apiKey
                + "&fmt=json";
    }

    /**
     * Parses the EODHD JSON response and populates a TickerData object.
     * <p>
     * The EODHD EOD endpoint returns a JSON array of daily price objects, each containing:
     * {@code date}, {@code open}, {@code high}, {@code low}, {@code close},
     * {@code adjusted_close}, and {@code volume}.
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

            // Parse all price data points (array is ordered oldest-first, newest-last)
            for (int i = 0; i < priceData.size(); i++) {
                JsonObject day = priceData.get(i).getAsJsonObject();
                String dateStr = day.get("date").getAsString();
                LocalDate date = LocalDate.parse(dateStr);
                double high = day.get("high").getAsDouble();
                double low = day.get("low").getAsDouble();
                double close = day.get("close").getAsDouble();
                long volume = day.get("volume").getAsLong();

                allRanges.add(new DayRange(date, high, low, close));

                // Most recent day's data (last element in oldest-first array)
                if (i == priceData.size() - 1) {
                    tickerData.setVolume((int) volume);
                }
            }

            if (allRanges.isEmpty()) {
                return null;
            }

            // Sort by date descending so the most recent record is first
            allRanges.sort(Comparator.comparing(DayRange::date).reversed());
            DayRange latest = allRanges.getFirst();
            tickerData.setPriceCurrent(BigDecimal.valueOf(latest.close()));
            tickerData.setClose(BigDecimal.valueOf(latest.close()));

            // 5-day high/low using RangeCalculator (uses the 5 most recent by date)
            tickerData.setFiveDayHigh(BigDecimal.valueOf(RangeCalculator.highestHigh(allRanges)));
            tickerData.setFiveDayLow(BigDecimal.valueOf(RangeCalculator.lowestLow(allRanges)));

            // Average daily range as a percentage using RangeCalculator (uses the 20 most recent by date)
            double adr = RangeCalculator.averageDailyRange(allRanges);
            tickerData.setAverageDailyRange((int) Math.round(adr));

            // Price at approximately 1 month ago (20 trading days)
            // Array is now newest-first (descending by date)
            int size = allRanges.size();
            if (size > 20) {
                tickerData.setOneMonthPrice(BigDecimal.valueOf(allRanges.get(20).close()));
            }

            // Price at approximately 3 months ago (60 trading days)
            if (size > 60) {
                tickerData.setThreeMonthPrice(BigDecimal.valueOf(allRanges.get(60).close()));
            }

            // Price at approximately 6 months ago (120 trading days)
            if (size > 120) {
                tickerData.setSixMonthPrice(BigDecimal.valueOf(allRanges.get(120).close()));
            }

            return tickerData;

        } catch (Exception e) {
            System.err.println("Failed to parse EODHD response for " + ticker + ": " + e.getMessage());
            return null;
        }
    }
}