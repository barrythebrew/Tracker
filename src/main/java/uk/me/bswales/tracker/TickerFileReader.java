package uk.me.bswales.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads ticker symbols from a resource file.
 * <p>
 * The file is expected to contain one ticker symbol per line.
 * Blank lines are skipped.
 */
public class TickerFileReader {

    private final String resourcePath;

    /**
     * Creates a reader that loads tickers from the given classpath resource.
     *
     * @param resourcePath the classpath resource path (e.g. {@code "/uk/me/bswales/tracker/uk_tickers.txt"})
     */
    public TickerFileReader(String resourcePath) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath must not be null");
    }

    /**
     * Returns the classpath resource path this reader was configured with.
     *
     * @return the resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Reads all ticker symbols from the configured resource file.
     *
     * @return a list of ticker symbols, one per non-blank line in the file
     * @throws IOException if the resource cannot be found or read
     */
    public List<String> readTickers() throws IOException {
        List<String> tickers = new ArrayList<>();

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        tickers.add(trimmed);
                    }
                }
            }
        }

        return tickers;
    }
}