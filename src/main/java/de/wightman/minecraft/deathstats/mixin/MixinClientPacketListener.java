package de.wightman.minecraft.deathstats.mixin;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin the client packet listener for death events and passes the packet to <pre>DeathStats.handlePlayerCombatKill()</pre>.
 * THe onSpawn Forge client event occurs too late and has been broken by Physics Mod.
 * @version 1.0.0
 * @see DeathStats#handlePlayerCombatKill(ClientboundPlayerCombatKillPacket)
 * @see <a href="https://github.com/haubna/PhysicsMod/issues/726">PhysicsMod Issue 726</a>
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"),
            method = "handlePlayerCombatKill")
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientPlayerCombatKillPacket, CallbackInfo callback) {
        // Run on client thread only
        if ( minecraft.isSameThread() ) {
            DeathStats.getInstance().handlePlayerCombatKill(clientPlayerCombatKillPacket);
        }
    }
}