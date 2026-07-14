package uk.me.bswales.tracker.persist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TickerCodeRepository}.
 */
class TickerCodeRepositoryTest {

    private DatabaseManager dbm;
    private TickerCodeRepository repo;

    @BeforeEach
    void setUp() {
        dbm = new DatabaseManager("jdbc:sqlite:testTracker.db");
        repo = new TickerCodeRepository(dbm);
        repo.truncate();
    }

    @AfterEach
    void tearDown() {
        if (dbm != null) {
            dbm.close();
        }
    }

    @Test
    void saveAndFindLocation() {
        repo.save("TEST", "LON");

        Optional<String> location = repo.findLocation("TEST");
        assertTrue(location.isPresent());
        assertEquals("LON", location.get());
    }

    @Test
    void findLocationReturnsEmptyForUnknownTicker() {
        Optional<String> location = repo.findLocation("NONEXISTENT");
        assertFalse(location.isPresent());
    }

    @Test
    void findAllTickersReturnsAllSaved() {
        repo.save("AAA", "LON");
        repo.save("BBB", "NYSE");
        repo.save("CCC", "NASDAQ");

        List<String> tickers = repo.findAllTickers();
        assertTrue(tickers.contains("AAA"));
        assertTrue(tickers.contains("BBB"));
        assertTrue(tickers.contains("CCC"));
    }

    @Test
    void countReturnsCorrectNumber() {
        int countBefore = repo.count();
        repo.save("T1", "LON");
        repo.save("T2", "LON");
        repo.save("T3", "LON");

        assertEquals(countBefore + 3, repo.count());
    }

    @Test
    void loadTickersFromFileLoadsAllTickers() throws Exception {
        repo.loadTickersFromFile("/uk/me/bswales/tracker/uk_tickers.txt", "LON");

        int count = repo.count();
        assertTrue(count > 0, "Should have loaded tickers from file");

        // Verify a few known tickers
        assertTrue(repo.findLocation("AZN").isPresent());
        assertEquals("LON", repo.findLocation("AZN").get());
        assertTrue(repo.findLocation("LLOY").isPresent());
        assertEquals("LON", repo.findLocation("LLOY").get());
    }
}
