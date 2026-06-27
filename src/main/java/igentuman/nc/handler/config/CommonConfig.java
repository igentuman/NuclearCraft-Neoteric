package igentuman.nc.handler.config;

import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.*;


public class CommonConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final EnergyGenerationConfig ENERGY_GENERATION = new EnergyGenerationConfig(BUILDER);
    public static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(BUILDER);
    public static final MiscConfig MISC_CONFIG = new MiscConfig(BUILDER);
    public static final GTCEUCompatibilityConfig GTCEU_CONFIG = new GTCEUCompatibilityConfig(BUILDER);
    public static final Q36Config Q36_CONFIG = new Q36Config(BUILDER);
    public static final BombConfig BOMB_CONFIG = new BombConfig(BUILDER);
    public static final AnomalyConfig ANOMALY_CONFIG = new AnomalyConfig(BUILDER);

    public static final StorageBlocksConfig STORAGE_BLOCKS = new StorageBlocksConfig(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();
    private static boolean loaded = false;
    private static List<Runnable> loadActions = new ArrayList<>();

    public static void setLoaded() {
        if (!loaded)
            loadActions.forEach(Runnable::run);
        loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void onLoad(Runnable action) {
        if (loaded)
            action.run();
        else
            loadActions.add(action);
    }

    public static class EnergyGenerationConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_SOLAR_PANELS;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> SOLAR_PANELS_GENERATION;
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_RTG;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> RTG_GENERATION;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> RTG_RADIATION;
        public final ForgeConfigSpec.ConfigValue<Integer> STEAM_TURBINE;
        public final ForgeConfigSpec.ConfigValue<Integer> DECAY_GENERATOR;
        public final ForgeConfigSpec.ConfigValue<Double> GENERATION_MULTIPLIER;



        public EnergyGenerationConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Energy");

            GENERATION_MULTIPLIER = builder
                    .comment("Multiplier for all power generation in the mod")
                    .defineInRange("generation_multiplier", 1.0, 0.001, 1000.0);

            REGISTER_SOLAR_PANELS = builder
                    .comment("Allow solar panel registration: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("register_panel", SolarPanels.initialRegistered(), o -> o instanceof ArrayList);

            SOLAR_PANELS_GENERATION = builder
                    .comment("Solar panel power generation: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("panel_power", SolarPanels.initialPower(), o -> o instanceof ArrayList);

            REGISTER_RTG = builder
                    .comment("Allow rtg registration: " + String.join(", ", RTGs.all().keySet()))
                    .define("register_rtg", RTGs.initialRegistered(), o -> o instanceof ArrayList);

            RTG_GENERATION = builder
                    .comment("rtg generation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_power", RTGs.initialPower(), o -> o instanceof ArrayList);

            RTG_RADIATION = builder
                    .comment("rtg radiation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_radiation", RTGs.initialRadiation(), o -> o instanceof ArrayList);

            STEAM_TURBINE = builder
                    .comment("Steam turbine (one block) base power gen")
                    .define("steam_turbine_power_gen", 80);

            DECAY_GENERATOR = builder
                    .comment("Decay Generator base power gen")
                    .define("decay_generator_power_gen", 128);

            builder.pop();
        }
    }

    public static class StorageBlocksConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_BARREL;
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_CONTAINER;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> BARREL_CAPACITY;

        public StorageBlocksConfig(ForgeConfigSpec.Builder builder) {

            builder.push("storage_blocks")
                    .comment("Blocks to store items, fluids, etc...");

            REGISTER_CONTAINER = builder
                    .comment("Allow container registration: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("container_block_registration", ContainerBlocks.initialRegistered(), o -> o instanceof ArrayList);

            REGISTER_BARREL = builder
                    .comment("Allow barrel registration: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("barrel_block_registration", BarrelBlocks.initialRegistered(), o -> o instanceof ArrayList);

            BARREL_CAPACITY = builder
                    .comment("Barrel capacity in Buckets: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("barrel_capacity", BarrelBlocks.initialCapacity(), o -> o instanceof ArrayList);

            builder.pop();
        }

    }

    public static class EnergyStorageConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ENERGY_BLOCK;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> ENERGY_BLOCK_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> LITHIUM_ION_BATTERY_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> LIGHTNING_ROD_CHARGE;
        public final ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_PER_BLOCK;

        public EnergyStorageConfig(ForgeConfigSpec.Builder builder) {
            builder.push("energy_storage");

            LIGHTNING_ROD_CHARGE = builder
                    .define("lightning_rod_charge", 1048576);

            REGISTER_ENERGY_BLOCK = builder
                    .comment("Allow block registration: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_registration", BatteryBlocks.initialRegistered(), o -> o instanceof ArrayList);

            ENERGY_BLOCK_STORAGE = builder
                    .comment("Storage: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_storage", BatteryBlocks.initialPower(), o -> o instanceof ArrayList);

            LITHIUM_ION_BATTERY_STORAGE = builder
                    .define("lithium_ion_battery_storage", 1048576);

            QNP_ENERGY_STORAGE = builder
                    .define("qnp_energy_storage", 2097152);

            QNP_ENERGY_PER_BLOCK = builder
                    .define("qnp_energy_per_block", 200);

            builder.pop();
        }

        public int getCapacityFor(String code) {
            if(code.equals("lithium_ion_cell")) {
                return LITHIUM_ION_BATTERY_STORAGE.get();
            }
            return BatteryBlocks.all().get(code).config().getStorage();
        }
    }


    public static class MiscConfig {
        public final ForgeConfigSpec.ConfigValue<Boolean> DEBUG_LOG;

        public MiscConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Misc");

            DEBUG_LOG = builder
                    .comment("Debug logging. Enable in case of issues to collect more data")
                    .define("debug_logging", false);

            builder.pop();
        }
    }

    public static class Q36Config {
        public final ForgeConfigSpec.IntValue PULSE_COOLDOWN_TICKS;
        public final ForgeConfigSpec.IntValue PULSE_QC_COST;
        public final ForgeConfigSpec.DoubleValue PULSE_DAMAGE;
        public final ForgeConfigSpec.LongValue PULSE_RADIATION;
        public final ForgeConfigSpec.IntValue PULSE_FX_TICKS;

        public final ForgeConfigSpec.IntValue BEAM_COOLDOWN_TICKS;
        public final ForgeConfigSpec.IntValue BEAM_QC_COST;
        public final ForgeConfigSpec.DoubleValue BEAM_DAMAGE;
        public final ForgeConfigSpec.LongValue BEAM_RADIATION;
        public final ForgeConfigSpec.DoubleValue BEAM_RANGE;
        public final ForgeConfigSpec.IntValue BEAM_BLOCK_BREAK_RADIUS;
        public final ForgeConfigSpec.IntValue BEAM_FX_TICKS;

        public Q36Config(ForgeConfigSpec.Builder builder) {
            builder.push("q36_quantite_disruptor");

            PULSE_COOLDOWN_TICKS = builder
                    .comment("Cooldown between pulse shots, in ticks")
                    .defineInRange("pulse_cooldown_ticks", 10, 1, 1200);
            PULSE_QC_COST = builder
                    .comment("Quantite charge cost per pulse shot")
                    .defineInRange("pulse_qc_cost", 200, 0, Integer.MAX_VALUE);
            PULSE_DAMAGE = builder
                    .comment("Pulse projectile damage on direct hit")
                    .defineInRange("pulse_damage", 50.0D, 0.0D, 100000.0D);
            PULSE_RADIATION = builder
                    .comment("Pulse radiation dose applied on direct hit")
                    .defineInRange("pulse_radiation", 200_000L, 0L, Long.MAX_VALUE);
            PULSE_FX_TICKS = builder
                    .comment("Muzzle flash FX duration after a pulse shot, in ticks")
                    .defineInRange("pulse_fx_ticks", 10, 0, 200);

            BEAM_COOLDOWN_TICKS = builder
                    .comment("Cooldown between beam shots, in ticks")
                    .defineInRange("beam_cooldown_ticks", 60, 1, 1200);
            BEAM_QC_COST = builder
                    .comment("Quantite charge cost per beam shot")
                    .defineInRange("beam_qc_cost", 2000, 0, Integer.MAX_VALUE);
            BEAM_DAMAGE = builder
                    .comment("Beam direct hit damage")
                    .defineInRange("beam_damage", 200.0D, 0.0D, 100000.0D);
            BEAM_RADIATION = builder
                    .comment("Beam radiation dose applied on direct hit")
                    .defineInRange("beam_radiation", 2_000_000L, 0L, Long.MAX_VALUE);
            BEAM_RANGE = builder
                    .comment("Beam maximum range in blocks")
                    .defineInRange("beam_range", 64.0D, 1.0D, 512.0D);
            BEAM_BLOCK_BREAK_RADIUS = builder
                    .comment("Beam impact block break radius (0 disables block breaking)")
                    .defineInRange("beam_block_break_radius", 3, 0, 16);
            BEAM_FX_TICKS = builder
                    .comment("Muzzle flash FX duration after a beam shot, in ticks")
                    .defineInRange("beam_fx_ticks", 20, 0, 200);

            builder.pop();
        }
    }

    public static class BombConfig {
        public final ForgeConfigSpec.IntValue RADIUS_HORIZONTAL;
        public final ForgeConfigSpec.IntValue RADIUS_VERTICAL;
        public final ForgeConfigSpec.IntValue FUSE_TICKS;
        public final ForgeConfigSpec.IntValue OPS_PER_TICK;
        public final ForgeConfigSpec.IntValue CHUNK_RESENDS_PER_TICK;
        public final ForgeConfigSpec.BooleanValue FAST_BLOCK_WRITES;

        public BombConfig(ForgeConfigSpec.Builder builder) {
            builder.push("pu_239_bomb");
            RADIUS_HORIZONTAL = builder
                    .comment("Horizontal blast radius in blocks (default 128 = 8 chunks)")
                    .defineInRange("radius_horizontal", 196, 16, 384);
            RADIUS_VERTICAL = builder
                    .comment("Vertical blast radius in blocks (default 64)")
                    .defineInRange("radius_vertical", 64, 8, 256);
            FUSE_TICKS = builder
                    .comment("Ticks between redstone arming and detonation (default 60 = 3 s)")
                    .defineInRange("fuse_ticks", 60, 1, 12000);
            OPS_PER_TICK = builder
                    .comment("Block ops applied per server tick during detonation (default 2048)")
                    .defineInRange("ops_per_tick", 16384, 64, 65536);
            CHUNK_RESENDS_PER_TICK = builder
                    .comment("Whole-chunk packets resent to clients per server tick during detonation. Raise to make blast visible to players faster; bandwidth cost scales with player count.")
                    .defineInRange("chunk_resends_per_tick", 16, 1, 1024);
            FAST_BLOCK_WRITES = builder
                    .comment("If true, bombs mutate chunk sections directly and resend whole chunks instead of using server.setBlock per op. ~50-100x faster but skips neighbor updates, fluid flow, falling-block triggers, and Forge block events inside the blast. Set false to roll back to vanilla setBlock path.")
                    .define("fast_block_writes", true);
            builder.pop();
        }
    }

    public static class AnomalyConfig {
        // Global
        public final ForgeConfigSpec.BooleanValue ENABLED;
        public final ForgeConfigSpec.BooleanValue SHADER;
        public final ForgeConfigSpec.DoubleValue SPAWN_CHANCE_PER_CELL;
        public final ForgeConfigSpec.IntValue CELL_SIZE;
        public final ForgeConfigSpec.IntValue ACTIVATION_CELL_RADIUS;

        // Per-variant enable + weight (indexed by AnomalyType ordinal in AnomalySpawnManager)
        public final ForgeConfigSpec.BooleanValue ENABLE_GRAVITATIONAL;
        public final ForgeConfigSpec.BooleanValue ENABLE_ELECTRIC;
        public final ForgeConfigSpec.BooleanValue ENABLE_RADIOACTIVE;
        public final ForgeConfigSpec.BooleanValue ENABLE_BURNING;
        public final ForgeConfigSpec.BooleanValue ENABLE_PSYCHO;
        public final ForgeConfigSpec.BooleanValue ENABLE_TELEPORTING;
        public final ForgeConfigSpec.IntValue WEIGHT_GRAVITATIONAL;
        public final ForgeConfigSpec.IntValue WEIGHT_ELECTRIC;
        public final ForgeConfigSpec.IntValue WEIGHT_RADIOACTIVE;
        public final ForgeConfigSpec.IntValue WEIGHT_BURNING;
        public final ForgeConfigSpec.IntValue WEIGHT_PSYCHO;
        public final ForgeConfigSpec.IntValue WEIGHT_TELEPORTING;

        // Gravitational
        public final ForgeConfigSpec.DoubleValue GRAV_RADIUS;
        public final ForgeConfigSpec.DoubleValue GRAV_PULL;
        public final ForgeConfigSpec.DoubleValue GRAV_LAUNCH_CHANCE;
        public final ForgeConfigSpec.DoubleValue GRAV_LAUNCH_POWER;
        public final ForgeConfigSpec.IntValue GRAV_ABSORB_THRESHOLD;
        public final ForgeConfigSpec.IntValue GRAV_ABSORB_INTERVAL;
        public final ForgeConfigSpec.BooleanValue GRAV_BLOCK_ABSORB;
        public final ForgeConfigSpec.BooleanValue GRAV_EXPLOSION;
        public final ForgeConfigSpec.DoubleValue GRAV_EXPLOSION_POWER;
        public final ForgeConfigSpec.DoubleValue GRAV_MASS_SCALE;

        // Electric
        public final ForgeConfigSpec.DoubleValue ELECTRIC_RADIUS;
        public final ForgeConfigSpec.DoubleValue ELECTRIC_DAMAGE;
        public final ForgeConfigSpec.IntValue ELECTRIC_INTERVAL;
        public final ForgeConfigSpec.IntValue ELECTRIC_DISCHARGE_FE;

        // Radioactive
        public final ForgeConfigSpec.IntValue RAD_RADIUS;
        public final ForgeConfigSpec.LongValue RAD_DOSE;
        public final ForgeConfigSpec.IntValue RAD_INTERVAL;

        // Burning
        public final ForgeConfigSpec.DoubleValue BURN_RADIUS;
        public final ForgeConfigSpec.DoubleValue BURN_EXPLOSION_RADIUS;
        public final ForgeConfigSpec.IntValue BURN_EXPLOSION_MIN_TICKS;
        public final ForgeConfigSpec.IntValue BURN_EXPLOSION_MAX_TICKS;
        public final ForgeConfigSpec.IntValue BURN_WATER_BLOCKS_TO_NEUTRALIZE;
        public final ForgeConfigSpec.BooleanValue BURN_BLOCK_IGNITE;
        public final ForgeConfigSpec.BooleanValue BURN_EXPLOSION;

        // Psycho
        public final ForgeConfigSpec.DoubleValue PSYCHO_RADIUS;
        public final ForgeConfigSpec.IntValue PSYCHO_INTERVAL;
        public final ForgeConfigSpec.IntValue PSYCHO_AMPLIFIER;
        public final ForgeConfigSpec.IntValue PSYCHO_VEX_INTERVAL;
        public final ForgeConfigSpec.IntValue PSYCHO_VEX_MAX;
        public final ForgeConfigSpec.IntValue PSYCHO_VEX_LIFETIME;

        // Teleporting
        public final ForgeConfigSpec.DoubleValue TP_VICTIM_RADIUS;
        public final ForgeConfigSpec.IntValue TP_MAX_DISTANCE;
        public final ForgeConfigSpec.IntValue TP_SELF_MIN_TICKS;
        public final ForgeConfigSpec.IntValue TP_SELF_MAX_TICKS;
        public final ForgeConfigSpec.DoubleValue TP_SELF_RADIUS;
        public final ForgeConfigSpec.IntValue TP_HOVER_OFFSET;

        // Crystal shards
        public final ForgeConfigSpec.BooleanValue SHARD_DROPS_ENABLED;
        public final ForgeConfigSpec.IntValue SHARD_DROP_MIN;
        public final ForgeConfigSpec.IntValue SHARD_DROP_MAX;
        public final ForgeConfigSpec.DoubleValue SHARD_DROP_CHANCE;
        public final ForgeConfigSpec.IntValue RARITY_WEIGHT_COMMON;
        public final ForgeConfigSpec.IntValue RARITY_WEIGHT_RARE;
        public final ForgeConfigSpec.IntValue RARITY_WEIGHT_EPIC;
        public final ForgeConfigSpec.IntValue RARITY_WEIGHT_LEGENDARY;
        public final ForgeConfigSpec.IntValue BUFF_REFRESH_TICKS;
        public final ForgeConfigSpec.IntValue BUFF_DURATION_TICKS;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> BUFF_EFFECT_BLACKLIST;

        public AnomalyConfig(ForgeConfigSpec.Builder builder) {
            builder.push("anomalies");

            ENABLED = builder
                    .comment("Master switch for the Wasteland anomaly system")
                    .define("enabled", true);
            SHADER = builder
                    .comment("Enable the per-variant anomaly post-process shaders (distortion/glow/lightning). Disable on weak GPUs.")
                    .define("shader", true);
            SPAWN_CHANCE_PER_CELL = builder
                    .comment("Probability (0..1) that a given placement cell contains an anomaly")
                    .defineInRange("spawn_chance_per_cell", 0.45D, 0.0D, 1.0D);
            CELL_SIZE = builder
                    .comment("Side length, in blocks, of each square placement cell")
                    .defineInRange("cell_size", 128, 16, 4096);
            ACTIVATION_CELL_RADIUS = builder
                    .comment("How many cells around each player are scanned for spawning")
                    .defineInRange("activation_cell_radius", 2, 0, 8);

            builder.push("variants");
            ENABLE_GRAVITATIONAL = builder.define("enable_gravitational", true);
            WEIGHT_GRAVITATIONAL = builder.defineInRange("weight_gravitational", 2, 0, 1000);
            ENABLE_ELECTRIC = builder.define("enable_electric", true);
            WEIGHT_ELECTRIC = builder.defineInRange("weight_electric", 3, 0, 1000);
            ENABLE_RADIOACTIVE = builder.define("enable_radioactive", true);
            WEIGHT_RADIOACTIVE = builder.defineInRange("weight_radioactive", 3, 0, 1000);
            ENABLE_BURNING = builder.define("enable_burning", true);
            WEIGHT_BURNING = builder.defineInRange("weight_burning", 3, 0, 1000);
            ENABLE_PSYCHO = builder.define("enable_psycho", true);
            WEIGHT_PSYCHO = builder.defineInRange("weight_psycho", 2, 0, 1000);
            ENABLE_TELEPORTING = builder.define("enable_teleporting", true);
            WEIGHT_TELEPORTING = builder.defineInRange("weight_teleporting", 2, 0, 1000);
            builder.pop();

            builder.push("gravitational");
            GRAV_RADIUS = builder.defineInRange("radius", 12.0D, 1.0D, 32.0D);
            GRAV_PULL = builder
                    .comment("Per-tick velocity added toward the centre at the edge of the radius")
                    .defineInRange("pull", 0.3D, 0.0D, 2.0D);
            GRAV_LAUNCH_CHANCE = builder
                    .comment("Per-tick chance to fling a caught living entity upward")
                    .defineInRange("launch_chance", 0.05D, 0.0D, 1.0D);
            GRAV_LAUNCH_POWER = builder
                    .comment("Upward velocity applied when a living entity is launched. ~2.0 hurls them well into the sky.")
                    .defineInRange("launch_power", 2.0D, 0.0D, 10.0D);
            GRAV_ABSORB_THRESHOLD = builder
                    .comment("Absorbed blocks needed to trigger the collapse explosion (counterplay)")
                    .defineInRange("absorb_threshold", 320, 1, 100000);
            GRAV_ABSORB_INTERVAL = builder.defineInRange("absorb_interval_ticks", 50, 1, 1200);
            GRAV_BLOCK_ABSORB = builder
                    .comment("Allow the anomaly to remove (absorb) nearby blocks. Disable to prevent terrain edits.")
                    .define("block_absorb", true);
            GRAV_EXPLOSION = builder.define("explosion_on_collapse", true);
            GRAV_EXPLOSION_POWER = builder.defineInRange("explosion_power", 18.0D, 0.0D, 64.0D);
            GRAV_MASS_SCALE = builder
                    .comment("Extra radius/pull multiplier gained as the anomaly absorbs mass. 0 = no growth; 1.0 = up to 2x radius and pull at the collapse threshold.")
                    .defineInRange("mass_scale", 1.0D, 0.0D, 8.0D);
            builder.pop();

            builder.push("electric");
            ELECTRIC_RADIUS = builder.defineInRange("radius", 28.0D, 1.0D, 64.0D);
            ELECTRIC_DAMAGE = builder.defineInRange("damage", 16.0D, 0.0D, 1000.0D);
            ELECTRIC_INTERVAL = builder.defineInRange("strike_interval_ticks", 50, 1, 1200);
            ELECTRIC_DISCHARGE_FE = builder
                    .comment("Free energy buffer (FE) an adjacent battery must offer to neutralise it. 10 MFE = 10,000,000 FE.")
                    .defineInRange("discharge_fe_threshold", 100_000_000, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("radioactive");
            RAD_RADIUS = builder.defineInRange("radius", 48, 4, 256);
            RAD_DOSE = builder
                    .comment("Radiation dose applied to each living entity within radius per emit (scaled by distance falloff). Same units as the player radiation capability.")
                    .defineInRange("dose", 100_000L, 0L, Long.MAX_VALUE);
            RAD_INTERVAL = builder.defineInRange("emit_interval_ticks", 20, 1, 1200);
            builder.pop();

            builder.push("burning");
            BURN_RADIUS = builder.defineInRange("radius", 10.0D, 1.0D, 64.0D);
            BURN_EXPLOSION_RADIUS = builder.defineInRange("explosion_radius", 2.0D, 0.0D, 64.0D);
            BURN_EXPLOSION_MIN_TICKS = builder.defineInRange("explosion_min_ticks", 200, 1, 24000);
            BURN_EXPLOSION_MAX_TICKS = builder.defineInRange("explosion_max_ticks", 400, 1, 24000);
            BURN_WATER_BLOCKS_TO_NEUTRALIZE = builder
                    .comment("Water source blocks within 3 blocks needed to extinguish it (counterplay)")
                    .defineInRange("water_blocks_to_neutralize", 8, 1, 1000);
            BURN_BLOCK_IGNITE = builder
                    .comment("Allow the anomaly to set nearby flammable blocks on fire. Disable to prevent fire spread.")
                    .define("block_ignite", true);
            BURN_EXPLOSION = builder.define("periodic_explosion", true);
            builder.pop();

            builder.push("psycho");
            PSYCHO_RADIUS = builder.defineInRange("radius", 48.0D, 1.0D, 128.0D);
            PSYCHO_INTERVAL = builder.defineInRange("interval_ticks", 40, 1, 1200);
            PSYCHO_AMPLIFIER = builder.defineInRange("effect_amplifier", 0, 0, 10);
            PSYCHO_VEX_INTERVAL = builder
                    .comment("Average ticks between vex spawns (actual timing jittered +/-25%)")
                    .defineInRange("vex_spawn_interval_ticks", 40, 1, 1200);
            PSYCHO_VEX_MAX = builder
                    .comment("Max vexes allowed within radius before spawning is suppressed. 0 disables vex spawning.")
                    .defineInRange("vex_max_nearby", 6, 0, 64);
            PSYCHO_VEX_LIFETIME = builder
                    .comment("Ticks a spawned vex lives before vanishing. 0 = no limit (persistent).")
                    .defineInRange("vex_lifetime_ticks", 1200, 0, 24000);
            builder.pop();

            builder.push("teleporting");
            TP_VICTIM_RADIUS = builder.defineInRange("victim_radius", 5.0D, 1.0D, 32.0D);
            TP_MAX_DISTANCE = builder.defineInRange("victim_max_distance", 100, 1, 1000);
            TP_SELF_MIN_TICKS = builder.defineInRange("self_blink_min_ticks", 60, 1, 1200);
            TP_SELF_MAX_TICKS = builder.defineInRange("self_blink_max_ticks", 120, 1, 1200);
            TP_SELF_RADIUS = builder.defineInRange("self_blink_radius", 12.0D, 1.0D, 32.0D);
            TP_HOVER_OFFSET = builder.defineInRange("hover_offset", 3, 0, 32);
            builder.pop();

            builder.push("shards");
            SHARD_DROPS_ENABLED = builder
                    .comment("Drop resonite shards when an anomaly is resolved through its counterplay")
                    .define("shard_drops_enabled", true);
            SHARD_DROP_MIN = builder.defineInRange("shard_drop_min", 1, 0, 64);
            SHARD_DROP_MAX = builder.defineInRange("shard_drop_max", 2, 0, 64);
            SHARD_DROP_CHANCE = builder.defineInRange("shard_drop_chance", 1.0D, 0.0D, 1.0D);
            RARITY_WEIGHT_COMMON = builder
                    .comment("Relative weights for the rarity rolled when a crystal is analyzed")
                    .defineInRange("rarity_weight_common", 85, 0, 1000000);
            RARITY_WEIGHT_RARE = builder.defineInRange("rarity_weight_rare", 8, 0, 1000000);
            RARITY_WEIGHT_EPIC = builder.defineInRange("rarity_weight_epic", 6, 0, 1000000);
            RARITY_WEIGHT_LEGENDARY = builder.defineInRange("rarity_weight_legendary", 1, 0, 1000000);
            BUFF_REFRESH_TICKS = builder
                    .comment("How often (ticks) carried analyzed crystals re-apply their buff")
                    .defineInRange("buff_refresh_ticks", 120, 1, 1200);
            BUFF_DURATION_TICKS = builder
                    .comment("Duration (ticks) of the applied buff; keep above the refresh interval")
                    .defineInRange("buff_duration_ticks", 240, 1, 24000);
            BUFF_EFFECT_BLACKLIST = builder
                    .comment("Effect ids excluded from the crystal analysis buff pool")
                    .defineList("buff_effect_blacklist",
                            Arrays.asList("minecraft:hero_of_the_village", "minecraft:dolphins_grace", "minecraft:conduit_power"),
                            o -> o instanceof String);
            builder.pop();

            builder.pop();
        }
    }

    public static class GTCEUCompatibilityConfig {
        public enum GTCEUCompatibility {
            ONLY_FE, ONLY_GTCEU, GTCEU_AND_FE
        }
        public enum GTCEUTier {
            ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV, MAX;

            public static GTCEUTier byId(int energyTier) {
                if (energyTier < 0 || energyTier >= GTCEUTier.values().length) {
                    return GTCEUTier.MAX;
                }
                return GTCEUTier.values()[energyTier];
            }
        }

        public final ForgeConfigSpec.ConfigValue<GTCEUCompatibility> COMPATIBILITY;
        public final ForgeConfigSpec.ConfigValue<Boolean> OVERCHARGE_EXPLOSIONS;
        public final ForgeConfigSpec.ConfigValue<Boolean> LIMIT_FE_OUTPUT;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> FISSION_REACTOR_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> TURBINE_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> FUSION_REACTOR_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> KUGELBLITZ_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> ACCELERATORS_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> PROCESSOR_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<Integer> ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER;

        public GTCEUCompatibilityConfig(ForgeConfigSpec.Builder builder) {
            builder.push("GregTech Energy Compatibility");
            List<String> tiers = Arrays.stream(GTCEUTier.values())
                    .map(GTCEUTier::name)
                    .toList();
            builder.comment("Tiers: " + String.join(", ", tiers));

            COMPATIBILITY = builder
                    .comment("ONLY_FE - Only FE energy system is used")
                    .comment("ONLY_GTCEU - Only GregTech Energy system is used")
                    .comment("GTCEU_AND_FE - Both systems are used, but GTCEU is preferred")
                    .defineEnum("gregtech_energy_compatibility", GTCEUCompatibility.GTCEU_AND_FE);

            FISSION_REACTOR_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("fission_reactor_energy_tier", EV);
            
            OVERCHARGE_EXPLOSIONS = builder
                    .comment("This only counts if GTCEU is supported")
                    .comment("Explode machines when input energy is more than max input")
                    .comment("This doesn't count FE energy input")
                    .define("gregtech_energy_overcharge_explosions", true);

            LIMIT_FE_OUTPUT = builder
                    .comment("This only counts if GTCEU_AND_FE compatibility used")
                    .comment("Output FE/t will be limited to max GTCEU output")
                    .comment("Formula: FE/t = voltage * amperage * (EU to FE convertion rate, usually 4)")
                    .define("limit_fe_output", false);

            TURBINE_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("turbine_energy_tier", EV);

            FUSION_REACTOR_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("fusion_reactor_energy_tier", IV);

            KUGELBLITZ_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("kugelblitz_energy_tier", ZPM);

            ACCELERATORS_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("accelerators_energy_tier", ZPM);

            PROCESSOR_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .defineEnum("processor_energy_tier", MV);

            ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .comment("How many energy upgrades are needed for processor to reach next energy tier")
                    .defineInRange("energy_upgrades_for_next_tier", 16, 8, 64);

            builder.pop();
        }
    }
}
