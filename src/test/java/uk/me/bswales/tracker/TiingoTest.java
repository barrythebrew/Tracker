package uk.me.bswales.tracker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.me.bswales.tracker.source.ISource;
import uk.me.bswales.tracker.source.Tiingo;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that fetches real data from Tiingo for a single ticker
 * and saves the response to a file.
 */
class TiingoTest {

    private static final String TEST_TICKER = "TSCO_LON";
    private static final String OUTPUT_FILE = "build/tsco_lon_tiingo_test_output.csv";

    @Test
    void fetchAndSaveTescoData() throws IOException {
        // Load the real API key from the project properties
        Properties props = SourceFactory.loadProperties();
        Properties tiingoProps = SourceFactory.groupBySource(props).get("tiingo");

        assertNotNull(tiingoProps, "tiingo properties must be present in source.properties");
        assertNotNull(tiingoProps.getProperty("apiKey"), "apiKey must be configured");
        assertFalse(tiingoProps.getProperty("apiKey").equals("YOUR_TIINGO_API_KEY"),
                "Please configure a real Tiingo API key in source.properties");

        // Create the source and fetch data
        ISource source = new Tiingo(tiingoProps);
        assertTrue(source.isAvailable(), "Source should be available");

        TickerData data = source.fetch(TEST_TICKER);
        assertNotNull(data, "Fetched data should not be null for " + TEST_TICKER);
        assertEquals(TEST_TICKER, data.getTicker());
        assertEquals(Tiingo.class.getName(), data.getSource());

        // Verify key fields are populated
        assertNotNull(data.getPriceCurrent(), "priceCurrent should be set");
        assertNotNull(data.getClose(), "close should be set");
        assertTrue(data.getVolume() > 0, "volume should be positive");
        assertNotNull(data.getFiveDayHigh(), "fiveDayHigh should be set");
        assertNotNull(data.getFiveDayLow(), "fiveDayLow should be set");
        assertTrue(data.getAverageDailyRange() > 0, "averageDailyRange should be positive");
        assertNotNull(data.getOneMonthPrice(), "oneMonthPrice should be set");

        // Save the response data to a CSV file
        Path outputPath = Path.of(OUTPUT_FILE);
        Files.createDirectories(outputPath.getParent());

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.println(TickerData.csvHeader());
            writer.println(data.toCsvRow());
        }

        // Verify the file was written correctly
        assertTrue(Files.exists(outputPath), "Output file should exist");
        String content = Files.readString(outputPath);
        assertTrue(content.contains(TEST_TICKER), "Output should contain the ticker symbol");
        assertTrue(content.contains(Tiingo.class.getName()), "Output should contain the source class name");

        System.out.println("Data for " + TEST_TICKER + " saved to: " + outputPath.toAbsolutePath());
        System.out.println("--- CSV Output ---");
        System.out.println(content);
        System.out.println("--- End ---");
    }
}