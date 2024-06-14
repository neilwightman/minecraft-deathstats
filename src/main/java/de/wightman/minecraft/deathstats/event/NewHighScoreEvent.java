package de.wightman.minecraft.deathstats.event;

import net.neoforged.bus.api.Event;

/**
 * An event fired when a new high score is hit.
 */
public class NewHighScoreEvent extends Event {

    public static final NewHighScoreEvent HIGH_SCORE_EVENT = new NewHighScoreEvent();

    private NewHighScoreEvent() {
    }
}
