package de.wightman.minecraft.deathstats.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

import static de.wightman.minecraft.deathstats.DeathStats.DEFAULT_SESSION;
import static de.wightman.minecraft.deathstats.DeathStats.KEY_IS_VISIBLE;
import static org.junit.jupiter.api.Assertions.*;

public class DeathsDBTest {

    File dbFile;

    @BeforeEach
    public void setUp() throws IOException {
        dbFile = File.createTempFile("deaths_db_sqllite", ".db");
        dbFile.deleteOnExit();
    }

    @Test
    void createSession() throws Exception {
        var db = new DeathsDB(dbFile.toPath());

        assertEquals( db.path, dbFile.toPath() );
        assertNotNull( db.conn );

        db.startSession("default");

        int id = db.getActiveSessionId();
        assertEquals(1, id, "First id should be 1");

        db.endSession( "default ");

        id = db.getActiveSessionId();
        assertEquals(1, id, "First id should be 1");

        final String selectSql = "SELECT * FROM SESSION";
        try (final Statement stmt = db.conn.createStatement()) {
            final ResultSet rs = stmt.executeQuery(selectSql);

            var first = rs.next();
            assertEquals(1, rs.getObject("ID"));
            assertEquals("default", rs.getObject("NAME"));
            assertNull(rs.getObject("END"));
            var start = rs.getTimestamp("START");
            assertNotNull(start);
            assertTrue( start instanceof java.sql.Timestamp);

            assertFalse(rs.next());
        }

        db.endSession("default");

        try (final Statement stmt = db.conn.createStatement()) {
            final ResultSet rs = stmt.executeQuery(selectSql);

            var first = rs.next();
            assertEquals(1, rs.getObject("ID"));
            assertEquals("default", rs.getObject("NAME"));
            var start = rs.getTimestamp("START");
            assertNotNull(start);
            assertTrue( start instanceof java.sql.Timestamp);
            var o = rs.getTimestamp("END");
            assertNotNull(o);
            assertTrue( o instanceof java.sql.Timestamp);

            assertFalse(rs.next());
        }

        id = db.getActiveSessionId();
        assertEquals(-1, id);

        db.close();

        assertNull(db.conn);
    }

    @Test
    public void testMethodsOnClosedDB() throws Exception {
        var db = new DeathsDB(dbFile.toPath());
        db.close();

        db.startSession(DEFAULT_SESSION);
        db.endSession(DEFAULT_SESSION);

        db.getActiveSessionId();

        db.getConfig(KEY_IS_VISIBLE);

        db.setConfig(KEY_IS_VISIBLE, "false");

        db.debugConfigTable();
        db.debugSessionTable();
        db.debugDeathLogTable();

        db.getMaxDeathsPerSession(DEFAULT_SESSION);
        db.getDeathsPerSession(1);
    }

}
