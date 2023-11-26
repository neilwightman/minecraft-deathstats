/**
 * This package wraps up the sqllite dbs to store and query the data without exposing JDBC outside of this package.
 * <p>
 * The database stores each death with death information.
 * Named events, like
 *  <ul>
 *      <li>Session started,</li>
 *      <li>Day started (local time).</li>
 *      <li>Custom named event, like Twitch Raid / Twitch Hype Train.</li>
 *  </ul>
 * </p>
 * <pre>
 * CREATE TABLE DEATH_LOG (
 *     ID INTEGER PRIMARY KEY,
 *     TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
 *     WORLD TEXT NOT NULL,
 *     DIMENSION TEXT NOT NUll,
 *     MESSAGE TEXT NOT NULL,
 *     KILLED_BY_KEY TEXT,
 *     KILLED_BY_STR TEXT,
 *     ARGB INTEGER
 * );
 * CREATE TABLE SESSION (
 *     ID INTEGER PRIMARY KEY,
 *     START DATETIME DEFAULT CURRENT_TIMESTAMP,
 *     END DATETIME,
 *     NAME TEXT NOT NULL
 * );
 * </pre>
 * @since 2.0.0
 */
package de.wightman.minecraft.deathstats.db;