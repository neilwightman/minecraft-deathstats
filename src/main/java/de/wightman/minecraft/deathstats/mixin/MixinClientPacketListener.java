package de.wightman.minecraft.deathstats.mixin;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin the client packet listener for death events.
 * @version 1.0.0
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"),
            method = "handlePlayerCombatKill",
            cancellable = true)
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket combatKillPacket, CallbackInfo callback) {
        // Run on network thread only, client cannot cancel this event, so it will always run on the minecraft thread.
        // If we ran at tail then the respawn UI is shown which the user can quit and we wouldn't track the death.
        if ( !minecraft.isSameThread() ) {
            DeathStats.getInstance().handlePlayerCombatKill(combatKillPacket);
        }
    }
}
