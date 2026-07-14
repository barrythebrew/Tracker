package uk.me.bswales.tracker;

import uk.me.bswales.tracker.source.AlphaAvantage;
import uk.me.bswales.tracker.source.Eodhd;
import uk.me.bswales.tracker.source.ISource;
import uk.me.bswales.tracker.source.Tiingo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Reads source configuration from {@code source.properties} and
 * instantiates the corresponding {@link ISource} implementations.
 * <p>
 * The properties file may contain multiple entries per source, each prefixed
 * with the source name then a dot. For example:
 * <pre>
 * alphaavantage.apiKey=ABC123
 * alphaavantage.dayLimit=25
 * </pre>
 * All properties sharing the same prefix are grouped together and passed
 * to the source's factory method.
 */
public class SourceFactory {

    private static final String PROPERTIES_PATH = "/uk/me/bswales/tracker/source/source.properties";

    /**
     * Loads all configured sources from the properties file.
     *
     * @return a list of configured {@link ISource} instances
     */
    public static List<ISource> getSources() {
        Properties props = loadProperties();
        Map<String, Properties> grouped = groupBySource(props);

        List<ISource> sources = new ArrayList<>();
        for (Map.Entry<String, Properties> entry : grouped.entrySet()) {
            ISource source = createSource(entry.getKey(), entry.getValue());
            if (source != null) {
                sources.add(source);
            }
        }

        return sources;
    }

    /**
     * Loads the raw properties from the classpath resource.
     *
     * @return the loaded Properties object
     */
    static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = SourceFactory.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + PROPERTIES_PATH);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + PROPERTIES_PATH, e);
        }
        return props;
    }

    /**
     * Groups flat property keys by their source name prefix.
     * <p>
     * For example, {@code alphaavantage.apiKey=ABC} and
     * {@code alphaavantage.dayLimit=25} both get grouped under
     * the source name {@code "alphaavantage"}.
     *
     * @param props the flat properties
     * @return a map from source name to its sub-properties (with the prefix stripped)
     */
    static Map<String, Properties> groupBySource(Properties props) {
        Map<String, Properties> grouped = new LinkedHashMap<>();

        for (String key : props.stringPropertyNames()) {
            int dot = key.indexOf('.');
            if (dot == -1) {
                // No prefix — treat the whole key as the source name with empty properties
                grouped.computeIfAbsent(key, k -> new Properties());
                continue;
            }

            String sourceName = key.substring(0, dot);
            String subKey = key.substring(dot + 1);
            String value = props.getProperty(key);

            grouped.computeIfAbsent(sourceName, k -> new Properties())
                    .setProperty(subKey, value);
        }

        return grouped;
    }

    /**
     * Creates an {@link ISource} for the given source name and its associated
     * properties.
     *
     * @param name   the source name (e.g. "alphaavantage")
     * @param config the configuration properties for this source
     * @return a configured ISource, or {@code null} if the name is unknown
     */
    static ISource createSource(String name, Properties config) {
        return switch (name.toLowerCase()) {
            case "alphaavantage" -> new AlphaAvantage(config);
            case "tiingo" -> new Tiingo(config);
            case "eodhd" -> new Eodhd(config);
            default -> {
                System.err.println("Unknown source: " + name);
                yield null;
            }
        };
    }
}