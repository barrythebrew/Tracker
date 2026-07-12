package uk.me.bswales.tracker;

import uk.me.bswales.tracker.persist.DatabaseManager;
import uk.me.bswales.tracker.persist.TickerCodeRepository;
import uk.me.bswales.tracker.source.ISource;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StockFinder {

    private static final String OUTPUT_FILE = "stock_data.csv";

    private final List<ISource> sources;
    private final TickerCodeRepository tickerCodeRepository;

    public StockFinder() {
        sources = SourceFactory.getSources();
        tickerCodeRepository = new TickerCodeRepository(new DatabaseManager());
    }

    public static void main(String[] args) {
        StockFinder stockFinder = new StockFinder();
        stockFinder.fetchAndSave();
    }

    /**
     * Reads all tickers from the database table, fetches data from the first
     * available source, and writes the results to a CSV file.
     */
    private void fetchAndSave() {
        // Read tickers from database
        List<String> tickers;
        try {
            tickers = tickerCodeRepository.findAllTickers();
        } catch (Exception e) {
            System.err.println("Failed to read tickers from database: " + e.getMessage());
            return;
        }

        System.out.println("Loaded " + tickers.size() + " tickers from database.");

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