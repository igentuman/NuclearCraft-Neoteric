package igentuman.nc.setup;

import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.addSoundEvent;

public final class NCSounds {

    private NCSounds() {
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_ANGRY = addSoundEvent("boss_angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_ACTION = addSoundEvent("boss_action");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_HIT = addSoundEvent("boss_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_IDLE = addSoundEvent("boss_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> FERAL_GHOUL_CHARGE = addSoundEvent("feral_ghoul_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> FERAL_GHOUL_DEATH = addSoundEvent("feral_ghoul_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_CHARGED = addSoundEvent("charge_energy");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_CHARGING = addSoundEvent("tile.fusion_charging");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_SHOOT = addSoundEvent("tile.laser_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKHOLE_SPAWN = addSoundEvent("tile.blackhole_spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACKHOLE_IDLE = addSoundEvent("tile.blackhole_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_READY = addSoundEvent("tile.fusion_ready");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_RUNNING = addSoundEvent("tile.fusion_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE = addSoundEvent("tile.turbine");
    public static final DeferredHolder<SoundEvent, SoundEvent> FISSION_REACTOR = addSoundEvent("tile.fission_reactor");
    public static final DeferredHolder<SoundEvent, SoundEvent> MSR_RUNNING = addSoundEvent("tile.msr_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> Q36_BEAM_SHOT = addSoundEvent("q36.beam_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> Q36_PULSE_SHOT = addSoundEvent("q36.pulse_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_WANDERER = addSoundEvent("music.wanderer");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_END_OF_THE_WORLD = addSoundEvent("music.end_of_the_world");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_MONEY_FOR_NOTHING = addSoundEvent("music.money_for_nothing");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_HYPERSPACE = addSoundEvent("music.hyperspace");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_BLAST_FIRST = addSoundEvent("bomb.first_distance");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_BLAST_SECOND = addSoundEvent("bomb.second_distance");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_BLAST_THIRD = addSoundEvent("bomb.third_distance");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOMB_BLAST_FOURTH = addSoundEvent("bomb.fourth_distance");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_GRAVITATIONAL = addSoundEvent("entity.anomaly.gravitational");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_ELECTRIC = addSoundEvent("entity.anomaly.electric");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_RADIOACTIVE = addSoundEvent("entity.anomaly.radioactive");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_BURNING = addSoundEvent("entity.anomaly.burning");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_PSYCHO = addSoundEvent("entity.anomaly.psycho");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANOMALY_TELEPORTING = addSoundEvent("entity.anomaly.teleporting");

    public static void init() {
    }
}
