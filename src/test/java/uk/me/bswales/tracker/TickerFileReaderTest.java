package uk.me.bswales.tracker;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TickerFileReader}.
 */
class TickerFileReaderTest {

    @Test
    void readsKnownTickersFromResourceFile() throws IOException {
        TickerFileReader reader =
                new TickerFileReader("/uk/me/bswales/tracker/uk_tickers.txt");

        List<String> tickers = reader.readTickers();

        // The file starts with TIDM, III, 3IN ...
        assertEquals("TIDM", tickers.get(0));
        assertEquals("III", tickers.get(1));
        assertEquals("3IN", tickers.get(2));

        // The file ends with ZOYO
        assertEquals("ZOYO", tickers.getLast());
    }

    @Test
    void readTickers_returnsAllNonBlankLines() throws IOException {
        TickerFileReader reader =
                new TickerFileReader("/uk/me/bswales/tracker/uk_tickers.txt");

        List<String> tickers = reader.readTickers();

        // Known file has 1488 lines, all non-blank
        assertEquals(1488, tickers.size());
    }

    @Test
    void readTickers_doesNotReturnNullOrEmptyLines() throws IOException {
        TickerFileReader reader =
                new TickerFileReader("/uk/me/bswales/tracker/uk_tickers.txt");

        List<String> tickers = reader.readTickers();

        assertFalse(tickers.isEmpty());
        for (String ticker : tickers) {
            assertNotNull(ticker);
            assertFalse(ticker.trim().isEmpty(), "Ticker should not be blank");
        }
    }

    @Test
    void constructor_throwsOnNullPath() {
        assertThrows(NullPointerException.class,
                () -> new TickerFileReader(null));
    }

    @Test
    void getResourcePath_returnsConfiguredPath() {
        TickerFileReader reader =
                new TickerFileReader("/uk/me/bswales/tracker/uk_tickers.txt");
        assertEquals("/uk/me/bswales/tracker/uk_tickers.txt", reader.getResourcePath());
    }

    @Test
    void readTickers_throwsOnMissingResource() {
        TickerFileReader reader =
                new TickerFileReader("/nonexistent/resource.txt");

        IOException ex = assertThrows(IOException.class, reader::readTickers);
        assertTrue(ex.getMessage().contains("Resource not found"),
                "Exception message should indicate resource not found");
    }
}