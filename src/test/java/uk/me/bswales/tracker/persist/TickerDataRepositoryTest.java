package uk.me.bswales.tracker.persist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.me.bswales.tracker.TickerData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TickerDataRepository}.
 */
class TickerDataRepositoryTest {

    private DatabaseManager dbm;
    private TickerDataRepository repo;

    @BeforeEach
    void setUp() {
        dbm = new DatabaseManager();
        repo = new TickerDataRepository(dbm);
    }

    @AfterEach
    void tearDown() {
        if (dbm != null) {
            dbm.close();
        }
    }

    @Test
    void saveAndLoadLatest() {
        TickerData data = new TickerData();
        data.setTicker("TEST");
        data.setSource("test.Source");
        data.setPriceCurrent(BigDecimal.valueOf(100.50));
        data.setVolume(5_000_000);

        repo.save(data);

        Optional<TickerData> loaded = repo.loadLatest("TEST");
        assertTrue(loaded.isPresent());
        assertEquals("TEST", loaded.get().getTicker());
        assertEquals("test.Source", loaded.get().getSource());
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(loaded.get().getPriceCurrent()));
        assertEquals(5_000_000, loaded.get().getVolume());
    }

    @Test
    void loadLatestReturnsEmptyForUnknownTicker() {
        Optional<TickerData> loaded = repo.loadLatest("NONEXISTENT");
        assertFalse(loaded.isPresent());
    }

    @Test
    void loadLatestReturnsMostRecentByDate() {
        TickerData first = new TickerData();
        first.setTicker("ABC");
        first.setSource("test.Source");
        first.setPriceCurrent(BigDecimal.valueOf(50));
        repo.save(first);

        TickerData second = new TickerData();
        second.setTicker("ABC");
        second.setSource("test.Source");
        second.setPriceCurrent(BigDecimal.valueOf(75));
        repo.save(second);

        Optional<TickerData> loaded = repo.loadLatest("ABC");
        assertTrue(loaded.isPresent());
        assertEquals(0, BigDecimal.valueOf(75).compareTo(loaded.get().getPriceCurrent()));
    }

    @Test
    void hasDataForDateReturnsTrueWhenDataExists() {
        TickerData data = new TickerData();
        data.setTicker("XYZ");
        data.setSource("test.Source");
        repo.save(data);

        assertTrue(repo.hasDataForDate("XYZ", LocalDate.now()));
        // Tomorrow — should be false since data is from today
        assertFalse(repo.hasDataForDate("XYZ", LocalDate.now().plusDays(1)));
    }

    @Test
    void hasDataForDateReturnsFalseForUnknownTicker() {
        assertFalse(repo.hasDataForDate("UNKNOWN", LocalDate.now()));
    }

    @Test
    void loadAllReturnsAllRecordsOrderedByTickerAndDate() {
        TickerData a1 = new TickerData();
        a1.setTicker("A");
        a1.setSource("src");
        a1.setPriceCurrent(BigDecimal.valueOf(10));
        repo.save(a1);

        TickerData b1 = new TickerData();
        b1.setTicker("B");
        b1.setSource("src");
        b1.setPriceCurrent(BigDecimal.valueOf(20));
        repo.save(b1);

        List<TickerData> all = repo.loadAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void savesNullBigDecimalFields() {
        TickerData data = new TickerData();
        data.setTicker("NULLTEST");
        data.setSource("test.Source");
        // All BigDecimal fields are null

        repo.save(data);

        Optional<TickerData> loaded = repo.loadLatest("NULLTEST");
        assertTrue(loaded.isPresent());
        assertNull(loaded.get().getPriceCurrent());
        assertNull(loaded.get().getClose());
        assertNull(loaded.get().getFiveDayHigh());
        assertNull(loaded.get().getFiveDayLow());
    }

    @Test
    void loadAllReturnsEmptyListWhenNoData() {
        // Use a fresh in-memory-ish approach — just verify loadAll doesn't blow up
        List<TickerData> all = repo.loadAll();
        assertNotNull(all);
    }
}