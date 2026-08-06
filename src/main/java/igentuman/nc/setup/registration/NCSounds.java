package igentuman.nc.setup.registration;

import igentuman.nc.entity.anomaly.AnomalyType;
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
    public static final RegistryObject<SoundEvent> BOSS_ANGRY = SOUND_EVENTS.register("boss_angry", () -> new SoundEvent(rl( "boss_angry")));
    public static final RegistryObject<SoundEvent> BOSS_ACTION = SOUND_EVENTS.register("boss_action", () -> new SoundEvent(rl( "boss_action")));
    public static final RegistryObject<SoundEvent> BOSS_HIT = SOUND_EVENTS.register("boss_hit", () -> new SoundEvent(rl( "boss_hit")));
    public static final RegistryObject<SoundEvent> BOSS_IDLE = SOUND_EVENTS.register("boss_idle", () -> new SoundEvent(rl( "boss_idle")));
    public static final RegistryObject<SoundEvent> FERAL_GHOUL_CHARGE = SOUND_EVENTS.register("feral_ghoul_charge", () -> new SoundEvent(rl( "feral_ghoul_charge")));
    public static final RegistryObject<SoundEvent> FERAL_GHOUL_DEATH = SOUND_EVENTS.register("feral_ghoul_death", () -> new SoundEvent(rl( "feral_ghoul_death")));
    public static final RegistryObject<SoundEvent> ITEM_CHARGED = SOUND_EVENTS.register("charge_energy", () -> new SoundEvent(rl( "charge_energy")));
    public static final RegistryObject<SoundEvent> FUSION_CHARGING = SOUND_EVENTS.register("tile.fusion_charging", () -> new SoundEvent(rl( "tile.fusion_charging")));
    public static final RegistryObject<SoundEvent> LASER_SHOOT = SOUND_EVENTS.register("tile.laser_shoot", () -> new SoundEvent(rl( "tile.laser_shoot")));
    public static final RegistryObject<SoundEvent> BLACKHOLE_SPAWN = SOUND_EVENTS.register("tile.blackhole_spawn", () -> new SoundEvent(rl( "tile.blackhole_spawn")));
    public static final RegistryObject<SoundEvent> BLACKHOLE_IDLE = SOUND_EVENTS.register("tile.blackhole_idle", () -> new SoundEvent(rl( "tile.blackhole_idle")));
    public static final RegistryObject<SoundEvent> FUSION_READY = SOUND_EVENTS.register("tile.fusion_ready", () -> new SoundEvent(rl( "tile.fusion_ready")));
    public static final RegistryObject<SoundEvent> FUSION_RUNNING = SOUND_EVENTS.register("tile.fusion_running", () -> new SoundEvent(rl( "tile.fusion_running")));
    public static final RegistryObject<SoundEvent> TURBINE = SOUND_EVENTS.register("tile.turbine", () -> new SoundEvent(rl( "tile.turbine")));
    public static final RegistryObject<SoundEvent> FISSION_REACTOR = SOUND_EVENTS.register("tile.fission_reactor", () -> new SoundEvent(rl( "tile.fission_reactor")));
    public static final RegistryObject<SoundEvent> MSR_RUNNING = SOUND_EVENTS.register("tile.msr_running", () -> new SoundEvent(rl( "tile.msr_running")));
    public static final RegistryObject<SoundEvent> Q36_BEAM_SHOT = SOUND_EVENTS.register("q36.beam_shot", () -> new SoundEvent(rl( "q36.beam_shot")));
    public static final RegistryObject<SoundEvent> Q36_PULSE_SHOT = SOUND_EVENTS.register("q36.pulse_shot", () -> new SoundEvent(rl( "q36.pulse_shot")));
    public static final RegistryObject<SoundEvent> RECORD_WANDERER = SOUND_EVENTS.register("music.wanderer", () -> new SoundEvent(rl( "music.wanderer")));
    public static final RegistryObject<SoundEvent> RECORD_END_OF_THE_WORLD = SOUND_EVENTS.register("music.end_of_the_world", () -> new SoundEvent(rl( "music.end_of_the_world")));
    public static final RegistryObject<SoundEvent> RECORD_MONEY_FOR_NOTHING = SOUND_EVENTS.register("music.money_for_nothing", () -> new SoundEvent(rl( "music.money_for_nothing")));
    public static final RegistryObject<SoundEvent> RECORD_HYPERSPACE = SOUND_EVENTS.register("music.hyperspace", () -> new SoundEvent(rl( "music.hyperspace")));
    public static final RegistryObject<SoundEvent> MUSIC_WASTELAND = SOUND_EVENTS.register("music.wasteland", () -> SoundEvent.createVariableRangeEvent(rl( "music.wasteland")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_FIRST = SOUND_EVENTS.register("bomb.first_distance", () -> new SoundEvent(rl("bomb.first_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_SECOND = SOUND_EVENTS.register("bomb.second_distance", () -> new SoundEvent(rl("bomb.second_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_THIRD = SOUND_EVENTS.register("bomb.third_distance", () -> new SoundEvent(rl("bomb.third_distance")));
    public static final RegistryObject<SoundEvent> BOMB_BLAST_FOURTH = SOUND_EVENTS.register("bomb.fourth_distance", () -> new SoundEvent(rl("bomb.fourth_distance")));

    public static final RegistryObject<SoundEvent> ANOMALY_GRAVITATIONAL = SOUND_EVENTS.register("entity.anomaly.gravitational", () -> new SoundEvent(rl("entity.anomaly.gravitational")));
    public static final RegistryObject<SoundEvent> ANOMALY_ELECTRIC = SOUND_EVENTS.register("entity.anomaly.electric", () -> new SoundEvent(rl("entity.anomaly.electric")));
    public static final RegistryObject<SoundEvent> ANOMALY_RADIOACTIVE = SOUND_EVENTS.register("entity.anomaly.radioactive", () -> new SoundEvent(rl("entity.anomaly.radioactive")));
    public static final RegistryObject<SoundEvent> ANOMALY_BURNING = SOUND_EVENTS.register("entity.anomaly.burning", () -> new SoundEvent(rl("entity.anomaly.burning")));
    public static final RegistryObject<SoundEvent> ANOMALY_PSYCHO = SOUND_EVENTS.register("entity.anomaly.psycho", () -> new SoundEvent(rl("entity.anomaly.psycho")));
    public static final RegistryObject<SoundEvent> ANOMALY_TELEPORTING = SOUND_EVENTS.register("entity.anomaly.teleporting", () -> new SoundEvent(rl("entity.anomaly.teleporting")));

    public static final HashMap<String, RegistryObject<SoundEvent>> ANOMALY_SOUNDS = initAnomalySounds();

    public static final HashMap<String, RegistryObject<SoundEvent>> SOUND_MAP = initSoundMap();

    private static HashMap<String, RegistryObject<SoundEvent>> initAnomalySounds() {
        HashMap<String, RegistryObject<SoundEvent>> map = new HashMap<>();
        map.put("gravitational", ANOMALY_GRAVITATIONAL);
        map.put("electric", ANOMALY_ELECTRIC);
        map.put("radioactive", ANOMALY_RADIOACTIVE);
        map.put("burning", ANOMALY_BURNING);
        map.put("psycho", ANOMALY_PSYCHO);
        map.put("teleporting", ANOMALY_TELEPORTING);
        return map;
    }

    public static SoundEvent getAnomalySound(AnomalyType type) {
        RegistryObject<SoundEvent> ro = ANOMALY_SOUNDS.get(type.id());
        return ro == null ? null : ro.get();
    }

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
                SOUND_EVENTS.register("geiger_5", () -> new SoundEvent(rl( "geiger_5"))),
                SOUND_EVENTS.register("geiger_6", () -> new SoundEvent(rl( "geiger_6")))
        );
    }

    public static void init() {

    }
}