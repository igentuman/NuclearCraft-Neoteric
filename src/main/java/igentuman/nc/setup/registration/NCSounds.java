package igentuman.nc.setup.registration;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public final class NCSounds {

    private NCSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final List<RegistryObject<SoundEvent>> GEIGER_SOUNDS = initGeigerSounds();
    public static final RegistryObject<SoundEvent> ITEM_CHARGED = SOUND_EVENTS.register("charge_energy", () -> new SoundEvent(rl( "charge_energy")));

    private static List<RegistryObject<SoundEvent>> initGeigerSounds() {
        return List.of(
                SOUND_EVENTS.register("geiger_1", () -> new SoundEvent(rl( "geiger_1"))),
                SOUND_EVENTS.register("geiger_2", () -> new SoundEvent(rl( "geiger_2"))),
                SOUND_EVENTS.register("geiger_3", () -> new SoundEvent(rl( "geiger_3"))),
                SOUND_EVENTS.register("geiger_4", () -> new SoundEvent(rl( "geiger_4"))),
                SOUND_EVENTS.register("geiger_5", () -> new SoundEvent(rl( "geiger_5")))
        );
    }
}