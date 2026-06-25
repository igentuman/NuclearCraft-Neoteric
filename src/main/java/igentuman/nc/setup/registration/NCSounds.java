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
    public static final RegistryObject<SoundEvent> BOSS_ANGRY = SOUND_EVENTS.register("boss_angry", () -> SoundEvent.createVariableRangeEvent(rl( "boss_angry")));
    public static final RegistryObject<SoundEvent> BOSS_ACTION = SOUND_EVENTS.register("boss_action", () -> SoundEvent.createVariableRangeEvent(rl( "boss_action")));
    public static final RegistryObject<SoundEvent> BOSS_HIT = SOUND_EVENTS.register("boss_hit", () -> SoundEvent.createVariableRangeEvent(rl( "boss_hit")));
    public static final RegistryObject<SoundEvent> BOSS_IDLE = SOUND_EVENTS.register("boss_idle", () -> SoundEvent.createVariableRangeEvent(rl( "boss_idle")));
    public static final RegistryObject<SoundEvent> FERAL_GHOUL_CHARGE = SOUND_EVENTS.register("feral_ghoul_charge", () -> SoundEvent.createVariableRangeEvent(rl( "feral_ghoul_charge")));
    public static final RegistryObject<SoundEvent> FERAL_GHOUL_DEATH = SOUND_EVENTS.register("feral_ghoul_death", () -> SoundEvent.createVariableRangeEvent(rl( "feral_ghoul_death")));
    public static final RegistryObject<SoundEvent> ITEM_CHARGED = SOUND_EVENTS.register("charge_energy", () -> SoundEvent.createVariableRangeEvent(rl( "charge_energy")));
    public static final RegistryObject<SoundEvent> FUSION_CHARGING = SOUND_EVENTS.register("tile.fusion_charging", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_charging")));
    public static final RegistryObject<SoundEvent> LASER_SHOOT = SOUND_EVENTS.register("tile.laser_shoot", () -> SoundEvent.createVariableRangeEvent(rl( "tile.laser_shoot")));
    public static final RegistryObject<SoundEvent> BLACKHOLE_SPAWN = SOUND_EVENTS.register("tile.blackhole_spawn", () -> SoundEvent.createVariableRangeEvent(rl( "tile.blackhole_spawn")));
    public static final RegistryObject<SoundEvent> BLACKHOLE_IDLE = SOUND_EVENTS.register("tile.blackhole_idle", () -> SoundEvent.createVariableRangeEvent(rl( "tile.blackhole_idle")));
    public static final RegistryObject<SoundEvent> FUSION_READY = SOUND_EVENTS.register("tile.fusion_ready", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_ready")));
    public static final RegistryObject<SoundEvent> FUSION_RUNNING = SOUND_EVENTS.register("tile.fusion_running", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fusion_running")));
    public static final RegistryObject<SoundEvent> TURBINE = SOUND_EVENTS.register("tile.turbine", () -> SoundEvent.createVariableRangeEvent(rl( "tile.turbine")));
    public static final RegistryObject<SoundEvent> FISSION_REACTOR = SOUND_EVENTS.register("tile.fission_reactor", () -> SoundEvent.createVariableRangeEvent(rl( "tile.fission_reactor")));
    public static final RegistryObject<SoundEvent> MSR_RUNNING = SOUND_EVENTS.register("tile.msr_running", () -> SoundEvent.createVariableRangeEvent(rl( "tile.msr_running")));
    public static final RegistryObject<SoundEvent> Q36_BEAM_SHOT = SOUND_EVENTS.register("q36.beam_shot", () -> SoundEvent.createVariableRangeEvent(rl( "q36.beam_shot")));
    public static final RegistryObject<SoundEvent> Q36_PULSE_SHOT = SOUND_EVENTS.register("q36.pulse_shot", () -> SoundEvent.createVariableRangeEvent(rl( "q36.pulse_shot")));
    public static final RegistryObject<SoundEvent> RECORD_WANDERER = SOUND_EVENTS.register("music.wanderer", () -> SoundEvent.createVariableRangeEvent(rl( "music.wanderer")));
    public static final RegistryObject<SoundEvent> RECORD_END_OF_THE_WORLD = SOUND_EVENTS.register("music.end_of_the_world", () -> SoundEvent.createVariableRangeEvent(rl( "music.end_of_the_world")));
    public static final RegistryObject<SoundEvent> RECORD_MONEY_FOR_NOTHING = SOUND_EVENTS.register("music.money_for_nothing", () -> SoundEvent.createVariableRangeEvent(rl( "music.money_for_nothing")));
    public static final RegistryObject<SoundEvent> RECORD_HYPERSPACE = SOUND_EVENTS.register("music.hyperspace", () -> SoundEvent.createVariableRangeEvent(rl( "music.hyperspace")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_FIRST = SOUND_EVENTS.register("bomb.first_distance", () -> SoundEvent.createVariableRangeEvent(rl("bomb.first_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_SECOND = SOUND_EVENTS.register("bomb.second_distance", () -> SoundEvent.createVariableRangeEvent(rl("bomb.second_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_THIRD = SOUND_EVENTS.register("bomb.third_distance", () -> SoundEvent.createVariableRangeEvent(rl("bomb.third_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_FOURTH = SOUND_EVENTS.register("bomb.fourth_distance", () -> SoundEvent.createVariableRangeEvent(rl("bomb.fourth_distance")));

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
                SOUND_EVENTS.register("geiger_5", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_5"))),
                SOUND_EVENTS.register("geiger_6", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_6")))
        );
    }

    public static void init() {

    }
}