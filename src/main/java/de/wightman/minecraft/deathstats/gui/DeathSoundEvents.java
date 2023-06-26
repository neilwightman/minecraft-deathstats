package de.wightman.minecraft.deathstats.gui;


import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class DeathSoundEvents {

    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DeathStats.MOD_ID);

    public static final RegistryObject<SoundEvent> HIGH_SCORE = registrySoundEvent("high_score");

    private static RegistryObject<SoundEvent> registrySoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(DeathStats.MOD_ID, name)));
    }

    public static void registerSoundEvent(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
