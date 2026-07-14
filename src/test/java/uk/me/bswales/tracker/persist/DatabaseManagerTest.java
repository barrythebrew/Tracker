package uk.me.bswales.tracker.persist;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DatabaseManager}.
 */
class DatabaseManagerTest {

    @Test
    void createsSchemaAndProvidesConnection() {
        try (DatabaseManager dbm = new DatabaseManager("jdbc:sqlite:testTracker.db")) {
            Connection conn = dbm.getConnection();
            assertNotNull(conn);

            // Verify the ticker_data table was created
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='ticker_data'")) {
                assertTrue(rs.next(), "ticker_data table should exist");
                assertEquals("ticker_data", rs.getString("name"));
            }
        } catch (SQLException e) {
            fail("DatabaseManager should initialise without error", e);
        }
    }

    @Test
    void schemaIsIdempotent() {
        // Running twice should not cause errors
        try (DatabaseManager dbm1 = new DatabaseManager("jdbc:sqlite:testTracker.db");
             DatabaseManager dbm2 = new DatabaseManager("jdbc:sqlite:testTracker.db")) {
            assertNotNull(dbm1.getConnection());
            assertNotNull(dbm2.getConnection());
        } catch (SQLException e) {
            fail("DatabaseManager should initialise without error", e);
        }
    }
}
