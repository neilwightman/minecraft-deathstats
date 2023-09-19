package de.wightman.minecraft.deathstats.mixin;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
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

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(at = @At("HEAD"),
            method = "handlePlayerCombatKill")
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientPlayerCombatKillPacket, CallbackInfo callback) {
        // Run on client thread only
        if ( minecraft.isSameThread() ) {
            DeathStats.getInstance().handlePlayerCombatKill(clientPlayerCombatKillPacket);
        }
    }

}