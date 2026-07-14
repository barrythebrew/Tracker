package uk.me.bswales.tracker.persist;

import uk.me.bswales.tracker.TickerFileReader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@code ticker_code} table.
 * <p>
 * This table stores ticker symbols and their exchange location.
 */
public class TickerCodeRepository {

    private final DatabaseManager databaseManager;

    public TickerCodeRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Inserts a ticker code with the given location.
     */
    public void save(String ticker, String location) {
        String sql = "INSERT OR REPLACE INTO ticker_code (ticker, location) VALUES (?, ?)";
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticker);
            ps.setString(2, location);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ticker code: " + ticker, e);
        }
    }

    /**
     * Returns the location for the given ticker, or empty if not found.
     */
    public Optional<String> findLocation(String ticker) {
        String sql = "SELECT location FROM ticker_code WHERE ticker = ?";
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticker);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("location"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find location for ticker: " + ticker, e);
        }
        return Optional.empty();
    }

    /**
     * Returns all tickers in the table.
     */
    public List<String> findAllTickers() {
        List<String> tickers = new ArrayList<>();
        String sql = "SELECT ticker FROM ticker_code ORDER BY ticker";
        try (Connection conn = connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tickers.add(rs.getString("ticker"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list all tickers", e);
        }
        return tickers;
    }

    /**
     * Returns the count of tickers in the table.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM ticker_code";
        try (Connection conn = connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count tickers", e);
        }
    }

    /**
     * Deletes all rows from the ticker_code table.
     */
    public void truncate() {
        String sql = "DELETE FROM ticker_code";
        try (Connection conn = connection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to truncate ticker_code table", e);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Connection connection() {
        try {
            return databaseManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    /**
     * Loads tickers from a resource file and saves them with the given location.
     *
     * @param resourcePath the classpath resource path (e.g. "/uk/me/bswales/tracker/uk_tickers.txt")
     * @param location     the location to set for all tickers (e.g. "LON")
     * @throws IOException if the resource cannot be read
     */
    public void loadTickersFromFile(String resourcePath, String location) throws IOException {
        TickerFileReader reader = new TickerFileReader(resourcePath);
        List<String> tickers = reader.readTickers();
        for (String ticker : tickers) {
            save(ticker, location);
        }
    }
}