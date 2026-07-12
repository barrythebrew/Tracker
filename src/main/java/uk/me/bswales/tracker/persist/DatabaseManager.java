package uk.me.bswales.tracker.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages the SQLite database connection and schema initialisation.
 * <p>
 * The schema is loaded from the classpath resource
 * {@code /uk/me/bswales/tracker/persist/schema.sql}.
 */
public class DatabaseManager implements AutoCloseable {

    private static final String DB_URL = "jdbc:sqlite:tracker.db";
    private static final String SCHEMA_RESOURCE = "/uk/me/bswales/tracker/persist/schema.sql";

    private boolean initialised = false;

    public DatabaseManager() {
        // Ensure schema is initialised on first use
    }

    /**
     * Returns a new JDBC connection.
     */
    public Connection getConnection() throws SQLException {
        if (!initialised) {
            initialise();
            initialised = true;
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Loads the schema from the classpath resource and executes it.
     */
    private void initialise() throws SQLException {
        String sql = loadSchema();
        // Split by semicolon to handle multiple statements
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private static String loadSchema() {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new RuntimeException("Schema resource not found: " + SCHEMA_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema: " + SCHEMA_RESOURCE, e);
        }
    }

    @Override
    public void close() {
        // No resources to close - connections are obtained per-operation
    }
}
