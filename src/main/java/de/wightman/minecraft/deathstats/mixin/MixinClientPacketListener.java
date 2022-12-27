package de.wightman.minecraft.deathstats.mixin;

import static de.wightman.minecraft.deathstats.DeathStats.DEATH_COUNTER;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin the client packet listener for death events.
 * This code only updates a AtomicLong so its processed in the network thread.
 * @version 1.0.0
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow @Final
    private Minecraft minecraft;
    
    @Shadow
    private ClientLevel level;

    @Inject(at = @At("HEAD"),
            method = "handlePlayerCombatKill",
            cancellable = true)
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket p_171775_, CallbackInfo callback) {
        // Run on network thread only
        if ( !minecraft.isSameThread() ) {
            Entity entity = this.level.getEntity(p_171775_.getPlayerId());
            // Only process LocalPlayer
            if (entity == this.minecraft.player) {
                DEATH_COUNTER.incrementAndGet();
            }
        }
    }

}
