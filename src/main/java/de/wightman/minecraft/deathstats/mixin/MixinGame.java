package de.wightman.minecraft.deathstats.mixin;

import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.client.Game;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Game.class)
public class MixinGame {

    @Inject(at = @At("TAIL"),
            method = "onStartGameSession",
            cancellable = true)
    public void onStartGameSession(CallbackInfo callback) {
        DeathStats.getInstance().startSession();
    }

    @Inject(at = @At("TAIL"),
            method = "onLeaveGameSession",
            cancellable = true)
    public void onLeaveGameSession(CallbackInfo callback) {
        DeathStats.getInstance().endSession();
    }

}
