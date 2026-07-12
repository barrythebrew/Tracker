package uk.me.bswales.tracker.persist;

import uk.me.bswales.tracker.TickerData;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CRUD operations on the {@code ticker_data} table.
 * <p>
 * This class handles all interactions with the {@code ticker_data} table
 * and is separate from the database connection/schema management
 * ({@link DatabaseManager}).
 */
public class TickerDataRepository {

    private final DatabaseManager databaseManager;

    public TickerDataRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Inserts a row for the given ticker with today's date as the retrieval date.
     *
     * @param data the ticker data to persist
     */
    public void save(TickerData data) {
        String sql = """
                INSERT INTO ticker_data (
                    ticker, source, average_daily_range, price_current,
                    five_day_high, five_day_low, volume, close,
                    one_month_price, three_month_price, six_month_price,
                    date_retrieved
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, data.getTicker());
            ps.setString(2, data.getSource());
            ps.setInt(3, data.getAverageDailyRange());
            setBigDecimal(ps, 4, data.getPriceCurrent());
            setBigDecimal(ps, 5, data.getFiveDayHigh());
            setBigDecimal(ps, 6, data.getFiveDayLow());
            ps.setInt(7, data.getVolume());
            setBigDecimal(ps, 8, data.getClose());
            setBigDecimal(ps, 9, data.getOneMonthPrice());
            setBigDecimal(ps, 10, data.getThreeMonthPrice());
            setBigDecimal(ps, 11, data.getSixMonthPrice());
            ps.setString(12, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ticker data for " + data.getTicker(), e);
        }
    }

    /**
     * Returns the most recently persisted {@link TickerData} for the given ticker,
     * ordered by {@code date_retrieved} descending, or empty if none exists.
     */
    public Optional<TickerData> loadLatest(String ticker) {
        String sql = """
                SELECT * FROM ticker_data
                WHERE ticker = ?
                ORDER BY date_retrieved DESC
                LIMIT 1
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, ticker);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ticker data for " + ticker, e);
        }

        return Optional.empty();
    }

    /**
     * Returns true if the database contains data for the given ticker retrieved
     * on the given date (or any later date).
     */
    public boolean hasDataForDate(String ticker, LocalDate date) {
        String sql = """
                SELECT COUNT(*) FROM ticker_data
                WHERE ticker = ?
                  AND date(date_retrieved) >= ?
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, ticker);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check ticker data for " + ticker, e);
        }
    }

    /**
     * Returns all persisted ticker data, ordered by ticker then date_retrieved descending.
     */
    public List<TickerData> loadAll() {
        List<TickerData> results = new ArrayList<>();
        String sql = "SELECT * FROM ticker_data ORDER BY ticker, date_retrieved DESC";

        try (Statement stmt = connection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all ticker data", e);
        }

        return results;
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

    private static TickerData mapRow(ResultSet rs) throws SQLException {
        TickerData data = new TickerData();
        data.setTicker(rs.getString("ticker"));
        data.setSource(rs.getString("source"));
        data.setAverageDailyRange(rs.getInt("average_daily_range"));
        data.setPriceCurrent(getBigDecimal(rs, "price_current"));
        data.setFiveDayHigh(getBigDecimal(rs, "five_day_high"));
        data.setFiveDayLow(getBigDecimal(rs, "five_day_low"));
        data.setVolume(rs.getInt("volume"));
        data.setClose(getBigDecimal(rs, "close"));
        data.setOneMonthPrice(getBigDecimal(rs, "one_month_price"));
        data.setThreeMonthPrice(getBigDecimal(rs, "three_month_price"));
        data.setSixMonthPrice(getBigDecimal(rs, "six_month_price"));
        return data;
    }

    private static void setBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.REAL);
        } else {
            ps.setDouble(index, value.doubleValue());
        }
    }

    private static BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : BigDecimal.valueOf(val);
    }
}