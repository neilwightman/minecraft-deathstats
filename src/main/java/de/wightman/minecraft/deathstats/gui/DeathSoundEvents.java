package de.wightman.minecraft.deathstats.gui;


import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DeathSoundEvents {

    public static DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, DeathStats.MOD_ID);

    public static final Supplier<SoundEvent> HIGH_SCORE = registrySoundEvent("high_score");

    private static Supplier<SoundEvent> registrySoundEvent(String name) {
        return SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DeathStats.MOD_ID, name)));
    }

    public static void registerSoundEvent(IEventBus eventBus) {
        SOUND_EVENT.register(eventBus);
    }

}
