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

    private final Connection connection;

    public DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            initialise();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to SQLite database", e);
        }
    }

    /**
     * Returns the underlying JDBC connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Loads the schema from the classpath resource and executes it.
     */
    private void initialise() throws SQLException {
        String sql = loadSchema();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
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
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}