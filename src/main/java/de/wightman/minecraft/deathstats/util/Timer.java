package de.wightman.minecraft.deathstats.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * A simpler timer closable which logs the time from creation to <pre>close()</pre> being called.
 * @since 2.0.0
 */
public final class Timer implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);

    private final String name;
    private final long start;

    public Timer(final String name) {
        this.name = name;
        this.start = System.nanoTime();
    }

    @Override
    public void close()  {
        if (LOGGER.isDebugEnabled()) {
            long now = System.nanoTime();
            long duration = (now - start) / 1000000;
            LOGGER.debug("Timer {} = {} ms", name, duration);
        }
    }
}
