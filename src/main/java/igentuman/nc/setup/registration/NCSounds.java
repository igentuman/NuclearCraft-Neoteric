package igentuman.nc.setup.registration;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public final class NCSounds {

    private NCSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final List<RegistryObject<SoundEvent>> GEIGER_SOUNDS = initGeigerSounds();
    public static final RegistryObject<SoundEvent> ITEM_CHARGED = SOUND_EVENTS.register("charge_energy", () -> new SoundEvent(rl( "charge_energy")));
    public static final RegistryObject<SoundEvent> FUSION_CHARGING = SOUND_EVENTS.register("tile.fusion_charging", () -> new SoundEvent(rl( "tile.fusion_charging")));
    public static final RegistryObject<SoundEvent> FUSION_READY = SOUND_EVENTS.register("tile.fusion_ready", () -> new SoundEvent(rl( "tile.fusion_ready")));
    public static final RegistryObject<SoundEvent> FUSION_RUNNING = SOUND_EVENTS.register("tile.fusion_running", () -> new SoundEvent(rl( "tile.fusion_running")));
    public static final RegistryObject<SoundEvent> FUSION_SWITCH = SOUND_EVENTS.register("tile.fusion_switch", () -> new SoundEvent(rl( "tile.fusion_switch")));
    public static final RegistryObject<SoundEvent> FISSION_REACTOR = SOUND_EVENTS.register("tile.fission_reactor", () -> new SoundEvent(rl( "tile.fission_reactor")));
    public static final RegistryObject<SoundEvent> RECORD_WANDERER = SOUND_EVENTS.register("music.wanderer", () -> new SoundEvent(rl( "music.wanderer")));
    public static final RegistryObject<SoundEvent> RECORD_END_OF_THE_WORLD = SOUND_EVENTS.register("music.end_of_the_world", () -> new SoundEvent(rl( "music.end_of_the_world")));
    public static final RegistryObject<SoundEvent> RECORD_MONEY_FOR_NOTHING = SOUND_EVENTS.register("music.money_for_nothing", () -> new SoundEvent(rl( "music.money_for_nothing")));
    public static final RegistryObject<SoundEvent> RECORD_HYPERSPACE = SOUND_EVENTS.register("music.hyperspace", () -> new SoundEvent(rl( "music.hyperspace")));

    public static final HashMap<String, RegistryObject<SoundEvent>> SOUND_MAP = initSoundMap();

    private static HashMap<String, RegistryObject<SoundEvent>> initSoundMap() {
        HashMap<String, RegistryObject<SoundEvent>> soundMap = new HashMap<>();
        soundMap.put("wanderer", RECORD_WANDERER);
        soundMap.put("end_of_the_world", RECORD_END_OF_THE_WORLD);
        soundMap.put("money_for_nothing", RECORD_MONEY_FOR_NOTHING);
        soundMap.put("hyperspace", RECORD_HYPERSPACE);
        return soundMap;
    }

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