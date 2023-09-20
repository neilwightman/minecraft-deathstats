package de.wightman.minecraft.deathstats.event;


import de.wightman.minecraft.deathstats.DeathStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DeathSoundEvents {

    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DeathStats.MOD_ID);

    public static final RegistryObject<SoundEvent> HIGH_SCORE = registrySoundEvent("high_score");

    private static RegistryObject<SoundEvent> registrySoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DeathStats.MOD_ID, name)));
    }

    public static void registerSoundEvent(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
