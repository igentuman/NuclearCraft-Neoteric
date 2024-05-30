package igentuman.nc.setup.registration;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.List;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.SOUND_EVENTS;

public final class NCSounds {

    private NCSounds() {
    }

    public static final List<RegistryObject<SoundEvent>> GEIGER_SOUNDS = initGeigerSounds();
    public static final RegistryObject<SoundEvent> ITEM_CHARGED = SOUND_EVENTS.register("charge_energy", () -> SoundEvent.createVariableRangeEvent(rl( "charge_energy")));
    public static final RegistryObject<SoundEvent> FUSION_CHARGING = SOUND_EVENTS.register("tile.fusion_charging", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_charging")));
    public static final RegistryObject<SoundEvent> FUSION_READY = SOUND_EVENTS.register("tile.fusion_ready", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_ready")));
    public static final RegistryObject<SoundEvent> FUSION_RUNNING = SOUND_EVENTS.register("tile.fusion_running", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_running")));
    public static final RegistryObject<SoundEvent> FUSION_SWITCH = SOUND_EVENTS.register("tile.fusion_switch", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_switch")));
    public static final RegistryObject<SoundEvent> FISSION_REACTOR = SOUND_EVENTS.register("tile.fission_reactor", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fission_reactor")));
    public static final RegistryObject<SoundEvent> RECORD_WANDERER = SOUND_EVENTS.register("music.wanderer", () -> SoundEvent.createVariableRangeEvent(rl( "music.wanderer")));
    public static final RegistryObject<SoundEvent> RECORD_END_OF_THE_WORLD = SOUND_EVENTS.register("music.end_of_the_world", () -> SoundEvent.createVariableRangeEvent(rl( "music.end_of_the_world")));
    public static final RegistryObject<SoundEvent> RECORD_MONEY_FOR_NOTHING = SOUND_EVENTS.register("music.money_for_nothing", () -> SoundEvent.createVariableRangeEvent(rl( "music.money_for_nothing")));
    public static final RegistryObject<SoundEvent> RECORD_HYPERSPACE = SOUND_EVENTS.register("music.hyperspace", () -> SoundEvent.createVariableRangeEvent(rl( "music.hyperspace")));

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
                SOUND_EVENTS.register("geiger_1", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_1"))),
                SOUND_EVENTS.register("geiger_2", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_2"))),
                SOUND_EVENTS.register("geiger_3", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_3"))),
                SOUND_EVENTS.register("geiger_4", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_4"))),
                SOUND_EVENTS.register("geiger_5", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_5")))
        );
    }

    public static void init() {

    }
}