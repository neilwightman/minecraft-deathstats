package de.wightman.minecraft.deathstats.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * An event fired when any death occurs or other commands which require the overlay to query the values.
 */
public class OverlayUpdateEvent extends Event {

    public OverlayUpdateEvent() {
    }
}
