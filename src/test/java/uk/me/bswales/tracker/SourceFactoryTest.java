package uk.me.bswales.tracker;

import uk.me.bswales.tracker.source.AlphaAvantage;
import uk.me.bswales.tracker.source.ISource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SourceFactory}.
 */
class SourceFactoryTest {

    @Test
    void loadProperties_loadsFromClasspath() {
        Properties props = SourceFactory.loadProperties();
        assertNotNull(props);
        assertNotNull(props.getProperty("alphaavantage.apiKey"));
        assertFalse(props.getProperty("alphaavantage.apiKey").isBlank());
        assertEquals("25", props.getProperty("alphaavantage.dayLimit"));
    }

    @Test
    void groupBySource_groupsPrefixedProperties() {
        Properties flat = new Properties();
        flat.setProperty("alphaavantage.apiKey", "ABC123");
        flat.setProperty("alphaavantage.dayLimit", "25");
        flat.setProperty("someother.host", "example.com");

        Map<String, Properties> grouped = SourceFactory.groupBySource(flat);

        assertEquals(2, grouped.size());
        assertTrue(grouped.containsKey("alphaavantage"));
        assertTrue(grouped.containsKey("someother"));

        Properties alphaProps = grouped.get("alphaavantage");
        assertEquals("ABC123", alphaProps.getProperty("apiKey"));
        assertEquals("25", alphaProps.getProperty("dayLimit"));
    }

    @Test
    void groupBySource_handlesKeyWithoutDot() {
        Properties flat = new Properties();
        flat.setProperty("barekey", "value");

        Map<String, Properties> grouped = SourceFactory.groupBySource(flat);

        assertTrue(grouped.containsKey("barekey"));
        // Properties should be empty (no sub-keys)
        assertEquals(0, grouped.get("barekey").size());
    }

    @Test
    void groupBySource_handlesEmptyProperties() {
        Map<String, Properties> grouped = SourceFactory.groupBySource(new Properties());
        assertTrue(grouped.isEmpty());
    }

    @Test
    void createSource_returnsAlphaAvantage() {
        Properties config = new Properties();
        config.setProperty("apiKey", "TESTKEY");
        config.setProperty("dayLimit", "10");

        ISource source = SourceFactory.createSource("alphaavantage", config);

        assertNotNull(source);
        assertInstanceOf(AlphaAvantage.class, source);
        assertTrue(source.isAvailable());
    }

    @Test
    void createSource_returnsNullForUnknown() {
        ISource source = SourceFactory.createSource("unknown", new Properties());
        assertNull(source);
    }

    @Test
    void createSource_isCaseInsensitive() {
        Properties config = new Properties();
        config.setProperty("apiKey", "KEY");

        assertNotNull(SourceFactory.createSource("ALPHAVANTAGE", config));
        assertNotNull(SourceFactory.createSource("AlphaVantage", config));
    }

    @Test
    void getSources_returnsConfiguredSources() {
        List<ISource> sources = SourceFactory.getSources();

        assertFalse(sources.isEmpty());
        assertEquals(1, sources.size());
        assertInstanceOf(AlphaAvantage.class, sources.getFirst());
        assertTrue(sources.getFirst().isAvailable());
    }
}