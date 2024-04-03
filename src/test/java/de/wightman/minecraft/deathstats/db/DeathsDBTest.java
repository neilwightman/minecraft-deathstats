package de.wightman.minecraft.deathstats.db;

import de.wightman.minecraft.deathstats.record.DeathRecord;
import de.wightman.minecraft.deathstats.util.WeightedRandomBag;
import de.wightman.minecraft.deathstats.util.WeightedRandomBagTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import static de.wightman.minecraft.deathstats.DeathStats.DEFAULT_SESSION;
import static de.wightman.minecraft.deathstats.DeathStats.KEY_IS_VISIBLE;
import static de.wightman.minecraft.deathstats.record.DeathRecord.NOT_SET;
import static org.junit.jupiter.api.Assertions.*;

public class DeathsDBTest {

    File dbFile;

    @BeforeEach
    public void setUp() throws IOException {
        dbFile = File.createTempFile("deaths_db_sqllite", ".db");
        dbFile.deleteOnExit();
    }

    @AfterEach
    public void tearDown() throws IOException {
        dbFile.delete();
        dbFile = null;
    }

    @Test
    void createSession() throws Exception {
        var db = new DeathsDB(dbFile.toPath());

        assertEquals( db.path, dbFile.toPath() );
        assertNotNull( db.conn );

        int createId = db.startSession("default");
        assertEquals(1, createId, "First id should be 1");

        int id = db.getActiveSessionId(DEFAULT_SESSION);
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

        id = db.getActiveSessionId(DEFAULT_SESSION);
        assertEquals(-1, id);

        var sessions = db.getSessions(DEFAULT_SESSION, 25, 1);
        System.out.println(sessions);

        db.close();

        assertNull(db.conn);
    }

    @Test
    void createMultipleSession() throws Exception {
        var db = new DeathsDB(dbFile.toPath());

        assertEquals( db.path, dbFile.toPath() );
        assertNotNull( db.conn );

        // Wednesday, 3 January 2024 12:00:00 GMT+01:00
        long start = 1704279600;

        int id = db.startSession("default");
        setSessionStartDate(db.conn, id, start);
        db.endSession( "default ");

        for (int i=1; i<10;i++) {
            id = db.startSession("default");
            setSessionStartDate(db.conn, id, start + 10000 * i);
            db.endSession("default");
        }

        var sessions = db.getSessions(DEFAULT_SESSION, 3, id);
        assertEquals(3, sessions.size());

        assertEquals(10, sessions.get(0).id());
        assertEquals(9, sessions.get(1).id());
        assertEquals(8, sessions.get(2).id());

        sessions = db.getSessions(DEFAULT_SESSION, 5, id);
        assertEquals(5, sessions.size());

        assertEquals(10, sessions.get(0).id());
        assertEquals(9, sessions.get(1).id());
        assertEquals(8, sessions.get(2).id());
        assertEquals(7, sessions.get(3).id());
        assertEquals(6, sessions.get(4).id());

        sessions = db.getSessions(DEFAULT_SESSION, 5, 5);
        assertEquals(5, sessions.size());

        assertEquals(5, sessions.get(0).id());
        assertEquals(4, sessions.get(1).id());
        assertEquals(3, sessions.get(2).id());
        assertEquals(2, sessions.get(3).id());
        assertEquals(1, sessions.get(4).id());

        id = db.startSession("stream");
        setSessionStartDate(db.conn, id, start + 10000 * 15);
        db.endSession("stream");

        sessions = db.getSessions("stream", 5, id);
        assertEquals(1, sessions.size());
        assertEquals(11, sessions.get(0).id());

        sessions = db.getSessions(DEFAULT_SESSION, 1, id);
        assertEquals(1, sessions.size());
        assertEquals(10, sessions.get(0).id());

        db.close();
        assertNull(db.conn);
    }

    @Test
    public void testMethodsOnClosedDB() throws Exception {
        var db = new DeathsDB(dbFile.toPath());
        db.close();

        db.startSession(DEFAULT_SESSION);
        db.endSession(DEFAULT_SESSION);

        db.getActiveSessionId(DEFAULT_SESSION);

        db.getConfig(KEY_IS_VISIBLE);

        db.setConfig(KEY_IS_VISIBLE, "false");

        db.debugConfigTable();
        db.debugSessionTable();
        db.debugDeathLogTable();

        db.getMaxDeathsPerSession(DEFAULT_SESSION);
        db.getTimeOfDeathsPerSession(1);
    }

    @Test
    /**
     * Deaths based on MisterWiggly [Minecraft] FTB Stoneblock 3 | Day 12
     * https://www.youtube.com/watch?v=9v-8ctsMxEA
     * */
    public void testMisterWiggly514() throws Exception {
        var db = new DeathsDB(dbFile.toPath());

        int sessionId = db.startSession("default");

        // patch start date.
        Statement stmt = db.conn.createStatement();
        stmt.execute("UPDATE SESSION SET START = DateTime('2023-05-08 08:00:00.000', 'utc') WHERE NAME = 'default' AND END IS NULL");

        stmt.close();

        db.debugSessionTable();

        // 13|2023-11-06 09:01:55|New World|overworld|death.attack.mob|entity.minecraft.zombie||16777215

        DeathRecord record = new DeathRecord(NOT_SET, "New World", "overworld",
                "death.attack.mob","entity.minecraft.zombie", null, 16777215, System.currentTimeMillis() / 1000 );
        db.newDeath(record);

        long cnt = db.getActiveDeathsSession("default");
        assertEquals(1, cnt, "Should have 1 death");

        stmt = db.conn.createStatement();
        stmt.execute("DELETE FROM DEATH_LOG");
        stmt.close();

        // Assume deaths are caused by 2 raids of 100, 50 subscriptions and 25 bit donors
        WeightedRandomBag<String> bag = createTwitchUserMap(2, 50, 25);

        cnt = db.getActiveDeathsSession("default");
        assertEquals(0, cnt, "Should have 0 death");

        // insert 514 deaths over a 5:11 hour period
        final int DEATHS = 514;

        int seconds = (5 * 60 * 60) + (11 * 60);
        int delta = seconds / DEATHS;

        long startTs = 1683525600;

        long start = System.nanoTime();
        for (int i = 1; i <= DEATHS; i++) {
            long offset = startTs + (delta * i);
            // TODO use random player based on stream names so we can test high score query.
            DeathRecord deathRecord = new DeathRecord(NOT_SET, "New World", "overworld",
                    "death.attack.mob",null, bag.getRandom(), 16777215, offset );
            db.newDeath(deathRecord);
        }
        long end = System.nanoTime();
        float insertTimeSeconds = ((float)end - (float)start) / 1000000000.0f;

        db.debugDeathLogTable();

        start = System.nanoTime();
        cnt = db.getActiveDeathsSession("default");
        end = System.nanoTime();
        assertEquals(514, cnt, "Should have 514 death");

        float activeSessionTimeSeconds = ((float)end - (float)start) / 1000000000.0f;

        start = System.nanoTime();
        var deaths = db.getTimeOfDeathsPerSession(sessionId);
        end = System.nanoTime();
        assertEquals(514, deaths.size(), "Should have 514 deaths");
        float timeOfDeathsPerSessionSeconds = ((float)end - (float)start) / 1000000000.0f;

        System.out.println("Insert = " + String.format("%.8f", insertTimeSeconds) + "s");
        System.out.println("getActiveDeathsSession = " + String.format("%.8f", activeSessionTimeSeconds) + "s");
        System.out.println("getTimeOfDeathsPerSession = " + String.format("%.8f", timeOfDeathsPerSessionSeconds) + "s");
    }

    @Test
    /**
     * Deaths based on Sublimiter 1780 deaths over 8 hours.
     * */
    public void testSublimiter1780() throws Exception {
        var db = new DeathsDB(dbFile.toPath());

        int sessionId = db.startSession("default");

        // patch start date.   start isnt really important as I dont know when subby got 1780 deaths.
        Statement stmt = db.conn.createStatement();
        stmt.execute("UPDATE SESSION SET START = DateTime('2023-05-08 08:00:00.000', 'utc') WHERE NAME = 'default' AND END IS NULL");

        stmt.close();

        db.debugSessionTable();

        // 13|2023-11-06 09:01:55|New World|overworld|death.attack.mob|entity.minecraft.zombie||16777215
        // 67|2024-04-03 09:41:17|New World/-4891078107714469795|overworld|death.attack.mob||Zoidberg|11141120

        DeathRecord record = new DeathRecord(NOT_SET, "New World", "overworld",
                "death.attack.mob","entity.minecraft.zombie", null, 16777215, System.currentTimeMillis() / 1000 );
        db.newDeath(record);

        long cnt = db.getActiveDeathsSession("default");
        assertEquals(1, cnt, "Should have 1 death");

        stmt = db.conn.createStatement();
        stmt.execute("DELETE FROM DEATH_LOG");
        stmt.close();

        // Assume deaths are caused by 3 raids of 100, 100 subscriptions and 50 bit donors
        WeightedRandomBag<String> bag = createTwitchUserMap(3, 100, 50);

        cnt = db.getActiveDeathsSession("default");
        assertEquals(0, cnt, "Should have 0 death");

        // insert 1780 deaths over a 8:00 hour period
        final int DEATHS = 1780;

        int seconds = 8 * 60 * 60;  // 8 hrs in seconds
        int delta = seconds / DEATHS;

        long startTs = 1683525600;

        long start = System.nanoTime();
        for (int i = 1; i <= DEATHS; i++) {
            long offset = startTs + (delta * i);
            // TODO use random player based on stream names so we can test high score query.
            DeathRecord deathRecord = new DeathRecord(NOT_SET, "New World", "overworld",
                    "death.attack.mob",null, bag.getRandom(), 16777215, offset );
            db.newDeath(deathRecord);
        }
        long end = System.nanoTime();
        float insertTimeSeconds = ((float)end - (float)start) / 1000000000.0f;

        db.debugDeathLogTable();

        start = System.nanoTime();
        cnt = db.getActiveDeathsSession("default");
        end = System.nanoTime();
        assertEquals(DEATHS, cnt, "Should have "+DEATHS+" death");

        float activeSessionTimeSeconds = ((float)end - (float)start) / 1000000000.0f;

        start = System.nanoTime();
        var deaths = db.getTimeOfDeathsPerSession(sessionId);
        end = System.nanoTime();
        assertEquals(DEATHS, deaths.size(), "Should have "+ DEATHS + " deaths");
        float timeOfDeathsPerSessionSeconds = ((float)end - (float)start) / 1000000000.0f;

        System.out.println("Insert = " + String.format("%.8f", insertTimeSeconds) + "s");
        System.out.println("getActiveDeathsSession = " + String.format("%.8f", activeSessionTimeSeconds) + "s");
        System.out.println("getTimeOfDeathsPerSession = " + String.format("%.8f", timeOfDeathsPerSessionSeconds) + "s");
    }

    public static void setSessionStartDate(Connection conn, int sessionId, long unixepochSeconds) throws SQLException {
        String sql = "UPDATE SESSION SET START = datetime(?,'unixepoch') WHERE ID = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, unixepochSeconds);
            pstmt.setInt(2, sessionId);
            pstmt.executeUpdate();
        }
    }

    public static WeightedRandomBag<String> createTwitchUserMap(int raids, int subscribers, int bits_donors) {
        final WeightedRandomBag<String> bag = new WeightedRandomBag<String>();

        // assumes no dups though which is probably not true, e.g raider doesnt give subs or bits when they raid which is not really true.

        // People who raid cause more deaths, assume max 100 mobs spawned.
        for (int i = 0; i< raids; i++) {
            bag.addEntry("raider_" + i, 100.0);
        }

        // Random gift people give 1 or 5 subs but they are twitch user who recieves them so low chance but lots.
        for (int i = 0;i<subscribers;i++) {
            bag.addEntry("subscriber_" + i, 1.0f);
        }

        // Bit donations spawn a specific number of mobs 3 - 5 normally.   Higher level donations means harder mobs so they kill more.
        for (int i = 0;i<bits_donors;i++) {
            bag.addEntry("bit_donor_" + i, 5.0f);
        }

        return bag;
    }

}
