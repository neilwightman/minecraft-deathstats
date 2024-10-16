package de.wightman.minecraft.deathstats.db;

import de.wightman.minecraft.deathstats.record.DeathRecord;
import de.wightman.minecraft.deathstats.record.SessionRecord;
import de.wightman.minecraft.deathstats.util.Timer;
import de.wightman.minecraft.deathstats.gui.TopDeathStatsScreen;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static de.wightman.minecraft.deathstats.record.DeathRecord.NOT_SET;

/**
 * @since 2.0.0
 */
public class DeathsDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathsDB.class);

    protected @Nullable Connection conn;
    protected final Path path;

    public DeathsDB(final Path dbFile) throws SQLException {
        Objects.requireNonNull(dbFile, "dbFile cannot be null");

        // Need to force load the class as jarjar doesnt load the services from the jar.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Couldnt load sqlite driver", e);
        }

        this.path = dbFile;

        String url = "jdbc:sqlite:" + dbFile;

        conn = DriverManager.getConnection(url);
        DatabaseMetaData meta = conn.getMetaData();

        LOGGER.info("Connected to {}. {} {}", url, meta.getDriverName(), meta.getDriverVersion());

        createDeathLogTable(conn);

        createSessionTable(conn);

        createConfigTable(conn);
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.warn("Cannot close connection", e);
            }
            conn = null;
        }
    }

    public Path getPath() {
        return path;
    }

    // NOTE: The date and time functions use UTC or "zulu" time internally, and so the "Z" suffix is a no-op

    private static void createSessionTable(final Connection conn) throws SQLException {
        String createSessionSql = """
                CREATE TABLE IF NOT EXISTS SESSION (
                    ID INTEGER PRIMARY KEY,
                    START DATETIME DEFAULT CURRENT_TIMESTAMP,
                    END DATETIME,
                    NAME TEXT NOT NULL
                );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSessionSql);
        }
    }

    public int startSession(final String sessionName) throws SQLException {
        if (conn == null) return -1;

        endSession(sessionName);
        final String insertSql = "INSERT INTO SESSION(name) VALUES(?) RETURNING ID";
        try (final PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();
            int id = rs.getInt("ID");
            return id;
        }
    }

    public void endSession(final String sessionName) throws SQLException {
        if (conn == null) return;

        final String setAllEndDates = "UPDATE SESSION SET END = DateTime('now') WHERE NAME = ? AND END IS NULL";
        try (final PreparedStatement pstmt = conn.prepareStatement(setAllEndDates)) {
            pstmt.setString(1, sessionName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Returns the sessions from most current going backwards, offset should default to current session.
     * @param sessionName the session name e.g. DeathStats.DEFAULT_SESSION
     * @param pageSize the number of sessions to return
     * @param offset the ID of the session to start from, this session will be included in the result.
     */
    public List<SessionRecord> getSessions(final String sessionName, final int pageSize, final int offset) throws SQLException {
        if (conn == null) return Collections.EMPTY_LIST;

        final List<SessionRecord> entries = new ArrayList<>();

        // TODO include max rows maybe pagination
        final String sql = """
                    SELECT ID, datetime(START, 'localtime') AS START, datetime(END, 'localtime') AS END, NAME FROM SESSION
                    WHERE NAME = ?
                    AND ID <= ?
                    ORDER BY START DESC LIMIT ?
                """;
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, pageSize);
            final ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ID");
                Timestamp start = rs.getTimestamp("START");
                Timestamp end = rs.getTimestamp("END");
                String name = rs.getString("NAME");
                SessionRecord session = new SessionRecord(id,start.getTime(), end == null ? NOT_SET : end.getTime(), name);
                entries.add(session);
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session id", e);
        }

        return entries;
    }

    public void debugSessionTable() {
        if (conn == null) return;
        if (LOGGER.isDebugEnabled()) {
            final String selectSql = "SELECT * FROM SESSION";
            try (final Statement stmt = conn.createStatement()) {
                final ResultSet rs = stmt.executeQuery(selectSql);
                dumpResultSet(rs);
            } catch (SQLException e) {
                LOGGER.warn("Failed to query death log", e);
            }
        }
    }

    private static void createDeathLogTable(final Connection conn) throws SQLException {
        String createDeathLogSql = """
                CREATE TABLE IF NOT EXISTS DEATH_LOG (
                    ID INTEGER PRIMARY KEY,
                    TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
                    WORLD TEXT NOT NULL,
                    DIMENSION TEXT NOT NUll,
                    MESSAGE TEXT NOT NULL,
                    KILLED_BY_KEY TEXT,
                    KILLED_BY_STR TEXT,
                    ARGB INTEGER
                );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createDeathLogSql);
        }
    }

    public void newDeath(final DeathRecord record) throws SQLException {
        if (conn == null) return;

        final String insertSql = "INSERT INTO DEATH_LOG(WORLD,DIMENSION,MESSAGE,KILLED_BY_KEY,KILLED_BY_STR,ARGB,TIME) VALUES(?,?,?,?,?,?,datetime(?,'unixepoch'))";
        try (final PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, record.world());
            pstmt.setString(2, record.dimension());
            pstmt.setString(3, record.deathMessage());
            pstmt.setString(4, record.killedByKey());
            pstmt.setString(5, record.killedByStr());
            pstmt.setInt(6, record.argb());
            pstmt.setLong(7, record.ts());
            pstmt.executeUpdate();
        }
    }

    public void debugDeathLogTable() {
        if (conn == null) return;

        if (LOGGER.isDebugEnabled()) {
            final String selectSql = "SELECT * FROM DEATH_LOG";
            try (final Statement stmt = conn.createStatement()) {
                final ResultSet rs = stmt.executeQuery(selectSql);
                dumpResultSet(rs);
            } catch (SQLException e) {
                LOGGER.warn("Failed to query death log", e);
            }
        }
    }

    private void dumpResultSet(final @NotNull ResultSet rs) {
        Objects.requireNonNull(rs, "rs cannot be null");

        try {
            final int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                final StringBuilder str = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i != 1) str.append(',');

                    str.append(rs.getObject(i));
                }
                LOGGER.debug(str.toString());
            }
        } catch (final SQLException sql) {
            LOGGER.warn("Cannot dump results set", sql);
        }
    }

    public long getActiveDeathsSession(final String sessionName) {
        if (conn == null) return -1;

        final String sql = """
                SELECT COUNT(*) AS DEATHS FROM DEATH_LOG
                 WHERE TIME > (SELECT START FROM SESSION WHERE NAME = ? ORDER BY START DESC LIMIT 1)
                """;
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("deaths");
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session for '{}'", sessionName, e);
        }
        return 0;
    }

    public long getMaxDeathsPerSession(final String sessionName) {
        if (conn == null) return -1;

        final String sql = """
                SELECT SESSION.ID,SESSION.START,COUNT(SESSION.ID) AS DEATHS FROM 'SESSION' 
                JOIN 'DEATH_LOG' ON DEATH_LOG.TIME > SESSION.START  
                AND DEATH_LOG.TIME < SESSION.END 
                AND SESSION.NAME = ? 
                GROUP BY SESSION.ID ORDER BY DEATHS DESC LIMIT 1
                """;
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("deaths");
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session for '{}'", sessionName, e);
        }
        return 0;
    }

    /**
     * This method returns just the timestamp, linux expoch (in milliseconds), of each death in this session.
     */
    public List<Long> getTimeOfDeathsPerSession(final int sessionId) {
        if (conn == null) return Collections.EMPTY_LIST;

        final String sql = """
                SELECT SESSION.ID,SESSION.START,SESSION.END,datetime(DEATH_LOG.TIME,'localtime') AS TIME FROM 'SESSION' 
                JOIN 'DEATH_LOG' ON DEATH_LOG.TIME > SESSION.START 
                AND (DEATH_LOG.TIME < SESSION.END OR SESSION.END IS NULL)
                AND SESSION.ID = ?
                """;
        final List<Long> dates = new ArrayList<>();

        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            final ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp d = rs.getTimestamp("TIME");
                dates.add(d.getTime());
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session for '{}'", sessionId, e);
        }
        return dates;
    }

    public int getActiveSessionId(final String sessionName) {
        if (conn == null) return -1;

        final String sql = "SELECT ID FROM SESSION WHERE NAME = ? AND END IS NULL";
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session id", e);
        }
        return -1;
    }

    public List<TopDeathStatsScreen.DeathLeaderBoardEntry> getLeaderBoardForSession(final int sessionId) {
        if (conn == null) return Collections.EMPTY_LIST;

        final List<TopDeathStatsScreen.DeathLeaderBoardEntry> entries = new ArrayList<>();

        // TODO include max rows maybe pagination
        final String sql = """
                    SELECT COUNT(*) AS CNT, MESSAGE, KILLED_BY_KEY, KILLED_BY_STR, MIN(ARGB) as ARGB
                    FROM DEATH_LOG
                    JOIN SESSION
                        ON DEATH_LOG.TIME > SESSION.START
                        AND (DEATH_LOG.TIME < SESSION.END OR SESSION.END IS NULL)
                        AND SESSION.ID = ?
                    GROUP BY MESSAGE, KILLED_BY_KEY, KILLED_BY_STR
                    ORDER BY CNT DESC
                            
                """;
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            final ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                long count = rs.getLong("CNT");
                String message = rs.getString("MESSAGE");
                String killedbykey = rs.getString("KILLED_BY_KEY");
                String killedbystr = rs.getString("KILLED_BY_STR");
                int argb = rs.getInt("ARGB");
                entries.add(new TopDeathStatsScreen.DeathLeaderBoardEntry(count, message, killedbykey, killedbystr, argb));
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to query active session id", e);
        }

        return entries;
    }

    /**
     * Deletes the current session and sets the last closed session as opened again.
     */
    public void resumeLastSession( /* TODO Add session name */) {
        if (conn == null) return;

        // delete active session
        final String CLOSE_ACTIVE = "DELETE FROM SESSION WHERE ID = (SELECT MAX(ID) FROM SESSION);";
        // reopen last session
        final String REOPEN_OLD = "UPDATE SESSION SET END = NULL WHERE ID = ( SELECT ID FROM SESSION WHERE END IS NOT NULL ORDER BY START DESC LIMIT 1 )";

        try {
            final boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Statement statement1 = conn.createStatement();
            Statement statement2 = conn.createStatement();
            try {
                statement1.executeUpdate(CLOSE_ACTIVE);
                statement2.executeUpdate(REOPEN_OLD);
            } catch (Exception e) {
                conn.rollback();
            } finally {
                conn.commit();
                conn.setAutoCommit(oldAutoCommit);
                statement1.close();
                statement2.close();
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Allow a max to be set
    // store configs in the db

    private static void createConfigTable(final Connection conn) throws SQLException {
        try ( var timer = new Timer("createConfigTable")) {
            String createSessionSql = """
                    CREATE TABLE IF NOT EXISTS CONFIG (
                        KEY TEXT NOT NULL PRIMARY KEY,
                        VALUE TEXT
                    );
                    """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createSessionSql);
            }
        }
    }

    public void debugConfigTable() {
        if (conn == null) return;

        if (LOGGER.isDebugEnabled()) {
            try ( var timer = new Timer("debugConfigTable")) {
                final String selectSql = "SELECT * FROM CONFIG";
                try (final Statement stmt = conn.createStatement()) {
                    final ResultSet rs = stmt.executeQuery(selectSql);
                    dumpResultSet(rs);
                } catch (SQLException e) {
                    LOGGER.warn("Failed to query death log", e);
                }
            }
        }
    }

    public void setConfig(final @NotNull String key, final @NotNull String value) throws SQLException {
        if (conn == null) return;

        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        try ( var timer = new Timer("setConfig")) {
            final String insertSql = "INSERT INTO CONFIG(KEY,VALUE) VALUES(?,?) ON CONFLICT(KEY) DO UPDATE SET VALUE = ?";
            try (final PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, key);
                pstmt.setString(2, value);
                pstmt.setString(3, value);
                pstmt.executeUpdate();
            }
        }
    }

    public @Nullable String getConfig(@NotNull final String key) {
        if (conn == null) return null;

        try ( var timer = new Timer("getConfig")) {
            Objects.requireNonNull(key, "key must not be null");

            final String sql = "SELECT VALUE FROM CONFIG WHERE KEY = ?";
            try (final PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, key);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("VALUE");
                }
            } catch (SQLException e) {
                LOGGER.warn("Failed to query active session id", e);
            }
        }
        return null;
    }
}

