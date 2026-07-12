package uk.me.bswales.tracker;

import uk.me.bswales.tracker.source.ISource;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StockFinder {

    private static final String TICKER_RESOURCE = "/uk/me/bswales/tracker/uk_tickers.txt";
    private static final String OUTPUT_FILE = "stock_data.csv";

    private final List<ISource> sources;

    public StockFinder() {
        sources = SourceFactory.getSources();
    }

    public static void main(String[] args) {
        StockFinder stockFinder = new StockFinder();
        stockFinder.fetchAndSave();
    }

    /**
     * Reads all tickers from the resource file, fetches data from the first
     * available source, and writes the results to a CSV file.
     */
    private void fetchAndSave() {
        // Read tickers
        TickerFileReader reader = new TickerFileReader(TICKER_RESOURCE);
        List<String> tickers;
        try {
            tickers = reader.readTickers();
        } catch (IOException e) {
            System.err.println("Failed to read tickers: " + e.getMessage());
            return;
        }

        System.out.println("Loaded " + tickers.size() + " tickers.");

        // Find an available source
        ISource source = null;
        for (ISource s : sources) {
            if (s.isAvailable()) {
                source = s;
                break;
            }
        }

        if (source == null) {
            System.err.println("No available data source.");
            return;
        }

        System.out.println("Using source: " + source.getClass().getSimpleName());

        // Fetch data and write to file
        try (PrintWriter writer = new PrintWriter(OUTPUT_FILE, StandardCharsets.UTF_8)) {
            writer.println(TickerData.csvHeader());

            int fetched = 0;
            int skipped = 0;

            for (String ticker : tickers) {
                if (!source.isAvailable()) {
                    System.out.println("Source exhausted after " + fetched + " fetches.");
                    break;
                }

                TickerData data = source.fetch(ticker);
                if (data != null) {
                    writer.println(data.toCsvRow());
                    fetched++;
                } else {
                    skipped++;
                }

                // Progress indicator
                if ((fetched + skipped) % 50 == 0) {
                    System.out.println("Processed " + (fetched + skipped) + "/" + tickers.size()
                            + " (fetched: " + fetched + ", skipped: " + skipped + ")");
                }
            }

            System.out.println("Done. Fetched: " + fetched + ", Skipped: " + skipped);
            System.out.println("Output written to: " + OUTPUT_FILE);

        } catch (IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }
}