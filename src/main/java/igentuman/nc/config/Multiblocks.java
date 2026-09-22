package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Multiblock tuning config: fission reactor sizing, heat, fuel and coolant values, mirrored into hot-path fields. */
public class Multiblocks {
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;

    public static final ModConfigSpec.IntValue FISSION_MIN_SIZE;
    public static final ModConfigSpec.IntValue FISSION_MAX_SIZE;
    public static final ModConfigSpec.DoubleValue FISSION_EXPLOSION_RADIUS;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_MULTIPLIER_CAP;
    public static final ModConfigSpec.DoubleValue FISSION_MODERATOR_FE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_MODERATOR_HEAT_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<Boolean> FISSION_SUPPORTS_BOILING;
    public static final ModConfigSpec.DoubleValue FISSION_BOILING_MULT;
    public static final ModConfigSpec.DoubleValue FISSION_FE_GENERATION_MULTIPLIER;
    public static final ModConfigSpec.IntValue FISSION_ACTIVE_COOLANT_PER_TICK;
    public static final ModConfigSpec.DoubleValue FISSION_FUEL_HEAT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_DEPLETION_MULTIPLIER;

    public static final ModConfigSpec.DoubleValue KUGELBLITZ_GENERATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue KUGELBLITZ_EVAPORATION_MULTIPLIER;
    public static final ModConfigSpec.IntValue KUGELBLITZ_LASER_DISTANCE;
    public static final ModConfigSpec.LongValue KUGELBLITZ_EXPL_CHARGE;
    public static final ModConfigSpec.DoubleValue KUGELBLITZ_EXPLOSION_RADIUS;
    public static final ModConfigSpec.ConfigValue<Boolean> KUGELBLITZ_BLACKHOLE_SHADER;

    public static final ModConfigSpec.IntValue TURBINE_MIN_SIZE;
    public static final ModConfigSpec.IntValue TURBINE_MAX_SIZE;
    public static final ModConfigSpec.IntValue TURBINE_BLADE_FLOW;
    public static final ModConfigSpec.DoubleValue TURBINE_ENERGY_GEN;

    public static final ModConfigSpec.IntValue HX_MIN_SIZE;
    public static final ModConfigSpec.IntValue HX_MAX_SIZE;
    public static final ModConfigSpec.IntValue HX_ENERGY_PER_BLOCK;
    public static final ModConfigSpec.DoubleValue HX_THROUGHPUT_PER_BLOCK;
    public static final ModConfigSpec.IntValue HX_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue HX_FLUID_CAPACITY_PER_BLOCK;
    public static final ModConfigSpec.IntValue HX_RADIATOR_COOLING;
    public static final ModConfigSpec.IntValue HX_HEAT_CAPACITY_PER_BLOCK;

    public static final ModConfigSpec.IntValue MSR_MIN_SIZE;
    public static final ModConfigSpec.IntValue MSR_MAX_SIZE;
    public static final ModConfigSpec.IntValue MSR_PEBBLES_PER_FUEL_CELL;
    public static final ModConfigSpec.IntValue MSR_VOLUME_PER_FUEL_CELL;

    public static final ModConfigSpec.IntValue ACCELERATOR_SCALE_PRESET;
    public static final ModConfigSpec.DoubleValue ACCELERATOR_BEAM_ATTENUATION;
    public static final ModConfigSpec.LongValue ACCELERATOR_BEAM_SCALING;
    public static final ModConfigSpec.LongValue ACCELERATOR_HEAT_CAPACITY_PER_BLOCK;
    public static final ModConfigSpec.LongValue ACCELERATOR_BASE_ENERGY_REQUIREMENT;
    public static final ModConfigSpec.DoubleValue ACCELERATOR_THERMAL_CONDUCTIVITY;
    public static final ModConfigSpec.LongValue RING_ACCELERATOR_MINIMUM_INPUT_ENERGY_KEV;
    public static final ModConfigSpec.IntValue ACCELERATOR_OVERHEAT_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue ACCELERATOR_BEAM_CONNECTION_REACH;

    public static final ModConfigSpec.IntValue TARGET_CHAMBER_MIN_SIZE;
    public static final ModConfigSpec.IntValue TARGET_CHAMBER_MAX_SIZE;
    public static final ModConfigSpec.IntValue DECAY_CHAMBER_MIN_SIZE;
    public static final ModConfigSpec.IntValue DECAY_CHAMBER_MAX_SIZE;
    public static final ModConfigSpec.LongValue DECAY_CHAMBER_BASE_POWER;
    public static final ModConfigSpec.IntValue COLLISION_CHAMBER_MIN_TRANSVERSE_SIZE;
    public static final ModConfigSpec.IntValue COLLISION_CHAMBER_MAX_TRANSVERSE_SIZE;
    public static final ModConfigSpec.IntValue COLLISION_CHAMBER_MIN_LENGTH;
    public static final ModConfigSpec.IntValue COLLISION_CHAMBER_MAX_LENGTH;
    public static final ModConfigSpec.IntValue COLLISION_CHAMBER_PREFERRED_LENGTH;
    public static final ModConfigSpec.LongValue COLLISION_CHAMBER_BASE_POWER;

    public static final ModConfigSpec.LongValue PARTICLE_SCHEDULER_TIME_BUDGET_NANOS;
    public static final ModConfigSpec.IntValue PARTICLE_SCHEDULER_BLOCK_READ_BUDGET;
    public static final ModConfigSpec.IntValue PARTICLE_SCHEDULER_BLOCK_ENTITY_READ_BUDGET;
    public static final ModConfigSpec.IntValue PARTICLE_SCHEDULER_MUTATION_BUDGET;
    public static final ModConfigSpec.IntValue PARTICLE_SCHEDULER_AUDIT_INTERVAL_TICKS;

    public static final ModConfigSpec SPEC;

    public static int fissionMinSize = 3;
    public static int fissionMaxSize = 26;
    public static double fissionExplosionRadius = 4.0;
    public static double fissionHeatCapacity = 1_000_000;
    public static double fissionHeatMultiplier = 1.0;
    public static double fissionHeatMultiplierCap = 3.0;
    public static double fissionModeratorFeMultiplier = 16.666666667;
    public static double fissionModeratorHeatMultiplier = 33.33333333;
    public static boolean fissionSupportsBoiling = true;
    public static double fissionBoilingMult = 100.0;
    public static double fissionFeGenerationMultiplier = 10.0;
    public static int fissionActiveCoolantPerTick = 10;
    public static double fissionFuelHeatMultiplier = 1.0;
    public static double fissionDepletionMultiplier = 1.0;

    public static double kugelblitzGenerationMultiplier = 1.0;
    public static double kugelblitzEvaporationMultiplier = 1.0;
    public static int kugelblitzLaserDistance = 32;
    public static long kugelblitzExplCharge = 10_240_000_000L;
    public static double kugelblitzExplosionRadius = 10.0;
    public static boolean kugelblitzBlackholeShader = true;

    public static int turbineMinSize = 5;
    public static int turbineMaxSize = 17;
    public static int turbineBladeFlow = 2000;
    public static double turbineEnergyGen = 10.0;

    public static int hxMinSize = 5;
    public static int hxMaxSize = 15;
    public static int hxEnergyPerBlock = 200;
    public static double hxThroughputPerBlock = 50000.0;
    public static int hxEnergyCapacity = 1_000_000;
    public static int hxFluidCapacityPerBlock = 100_000;
    public static int hxRadiatorCooling = 1_000_000;
    public static int hxHeatCapacityPerBlock = 10_000_000;

    public static int msrMinSize = 5;
    public static int msrMaxSize = 11;
    public static int msrPebblesPerFuelCell = 10;
    public static int msrVolumePerFuelCell = 10000;

    public static int acceleratorScalePreset = 1;
    public static double acceleratorBeamAttenuation = 0.02;
    public static long acceleratorBeamScaling = 10_000L;
    public static long acceleratorHeatCapacityPerBlock = 25_000L;
    public static long acceleratorBaseEnergyRequirement = 10_000L;
    public static double acceleratorThermalConductivity = 0.0025;
    public static long ringAcceleratorMinimumInputEnergyKeV = 5_000L;
    public static int acceleratorOverheatCooldownTicks = 1_200;
    public static int acceleratorBeamConnectionReach = 16;

    public static int targetChamberMinSize = 5;
    public static int targetChamberMaxSize = 11;
    public static int decayChamberMinSize = 5;
    public static int decayChamberMaxSize = 11;
    public static long decayChamberBasePower = 2_000L;
    public static int collisionChamberMinTransverseSize = 5;
    public static int collisionChamberMaxTransverseSize = 11;
    public static int collisionChamberMinLength = 13;
    public static int collisionChamberMaxLength = 21;
    public static int collisionChamberPreferredLength = 17;
    public static long collisionChamberBasePower = 2_000L;

    public static long particleSchedulerTimeBudgetNanos = 2_000_000L;
    public static int particleSchedulerBlockReadBudget = 4_096;
    public static int particleSchedulerBlockEntityReadBudget = 128;
    public static int particleSchedulerMutationBudget = 16;
    public static int particleSchedulerAuditIntervalTicks = 200;
    private static long particleMachinesRevision;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        DEBUG_LOGGING = builder.comment("Enable debug logging for multiblocks.")
                .define("debug_logging", false);
        builder.pop();

        builder.push("fission_reactor");
        FISSION_MIN_SIZE = builder.comment("Minimum reactor edge length (cuboid casing).")
                .defineInRange("min_size", 3, 3, 24);
        FISSION_MAX_SIZE = builder.comment("Maximum reactor edge length (cuboid casing).")
                .defineInRange("max_size", 26, 5, 32);
        FISSION_EXPLOSION_RADIUS = builder.comment("Explosion radius on meltdown. 4 ~ TNT. 0 disables the explosion.")
                .defineInRange("explosion_radius", 4.0, 0.0, 20.0);
        FISSION_HEAT_CAPACITY = builder.comment("Base heat capacity before the size scaling factor; meltdown when exceeded.")
                .defineInRange("heat_capacity", 1_000_000.0, 1000.0, 100_000_000.0);
        FISSION_HEAT_MULTIPLIER = builder.comment("Heat/cooling ratio coefficient feeding the heat multiplier curve.")
                .defineInRange("heat_multiplier", 1.0, 0.01, 20.0);
        FISSION_HEAT_MULTIPLIER_CAP = builder.comment("Upper bound of the collected-heat multiplier.")
                .defineInRange("heat_multiplier_cap", 3.0, 0.01, 3.0);
        FISSION_MODERATOR_FE_MULTIPLIER = builder.comment("Percent FE bonus per moderator attached to a fuel cell.")
                .defineInRange("moderator_fe_multiplier", 16.666666667, 0.0, 1000.0);
        FISSION_MODERATOR_HEAT_MULTIPLIER = builder.comment("Percent heat bonus per moderator attached to a fuel cell.")
                .defineInRange("moderator_heat_multiplier", 33.33333333, 0.0, 1000.0);
        FISSION_SUPPORTS_BOILING = builder.comment("Allow toggling the reactor into boiling (steam) mode.")
                .define("supports_boiling_mode", true);
        FISSION_BOILING_MULT = builder.comment("Steam production rate multiplier (percent).")
                .defineInRange("boiling_mult", 100.0, 0.01, 1_000_000.0);
        FISSION_FE_GENERATION_MULTIPLIER = builder.comment("Global FE generation multiplier.")
                .defineInRange("fe_generation_multiplier", 10.0, 0.01, 1_000_000.0);
        FISSION_ACTIVE_COOLANT_PER_TICK = builder.comment("mB of coolant a single active heat sink consumes per tick.")
                .defineInRange("active_heatsink_coolant_per_tick", 10, 1, 10000);
        builder.pop();

        builder.push("fission_fuel");
        FISSION_FUEL_HEAT_MULTIPLIER = builder.comment("Heat multiplier applied to all fission fuels.")
                .defineInRange("fuel_heat_multiplier", 1.0, 0.01, 100.0);
        FISSION_DEPLETION_MULTIPLIER = builder.comment("Fuel longevity multiplier; higher = fuel lasts longer.")
                .defineInRange("depletion_multiplier", 1.0, 0.01, 1000.0);
        builder.pop();

        builder.push("kugelblitz_chamber");
        KUGELBLITZ_GENERATION_MULTIPLIER = builder.comment("Multiplier for kugelblitz chamber FE generation.")
                .defineInRange("fe_generation_multiplier", 1.0, 0.001, 1000.0);
        KUGELBLITZ_EVAPORATION_MULTIPLIER = builder.comment("Adjust rate of blackhole evaporation.")
                .defineInRange("blackhole_evaporation_rate", 1.0, 0.001, 1000.0);
        KUGELBLITZ_LASER_DISTANCE = builder.comment("Laser burst distance.")
                .defineInRange("laser_distance", 32, 8, 64);
        KUGELBLITZ_EXPL_CHARGE = builder.comment("EXPL FE requirement per pulse.")
                .defineInRange("expl_fe", 10_240_000_000L, 2_048_000_000L, 20_480_000_000L);
        KUGELBLITZ_EXPLOSION_RADIUS = builder.comment("Explosion radius on meltdown. 0 disables the explosion.")
                .defineInRange("explosion_radius", 10.0, 0.0, 20.0);
        KUGELBLITZ_BLACKHOLE_SHADER = builder.comment("Enable blackhole distortion shader.")
                .define("blackhole_shader", true);
        builder.pop();

        builder.push("turbine");
        TURBINE_MIN_SIZE = builder.comment("Minimum turbine edge length.")
                .defineInRange("min_size", 5, 5, 25);
        TURBINE_MAX_SIZE = builder.comment("Maximum turbine edge length.")
                .defineInRange("max_size", 17, 5, 25);
        TURBINE_BLADE_FLOW = builder.comment("Steam flow per blade in mB/t.")
                .defineInRange("blade_flow", 2000, 100, 1_000_000);
        TURBINE_ENERGY_GEN = builder.comment("Energy generation multiplier.")
                .defineInRange("energy_gen", 10.0, 1.0, 1_000_000.0);
        builder.pop();

        builder.push("heat_exchanger");
        HX_MIN_SIZE = builder.comment("Minimum heat exchanger edge length.")
                .defineInRange("min_size", 5, 3, 25);
        HX_MAX_SIZE = builder.comment("Maximum heat exchanger edge length.")
                .defineInRange("max_size", 15, 5, 25);
        HX_ENERGY_PER_BLOCK = builder.comment("Standby FE/t consumed per interior heat exchanger block while powered by redstone.")
                .defineInRange("energy_per_block", 200, 0, 1_000_000);
        HX_THROUGHPUT_PER_BLOCK = builder.comment("Recipe operation units per server tick, per interior heat exchanger block.")
                .defineInRange("throughput_per_block", 50000.0, 0.01, 100000.0);
        HX_ENERGY_CAPACITY = builder.comment("Internal energy buffer (FE).")
                .defineInRange("energy_capacity", 1_000_000, 0, Integer.MAX_VALUE);
        HX_FLUID_CAPACITY_PER_BLOCK = builder.comment("Fluid tank capacity (mB) per interior heat exchanger block, for each tank.")
                .defineInRange("fluid_capacity_per_block", 100_000, 1000, 1_000_000);
        HX_RADIATOR_COOLING = builder.comment("Heat removed from the buffer per tick (H/t) per radiator block. Passive: applies whenever formed.")
                .defineInRange("radiator_cooling", 1_000_000, 0, Integer.MAX_VALUE);
        HX_HEAT_CAPACITY_PER_BLOCK = builder.comment("Heat buffer capacity per interior heat exchanger block.")
                .defineInRange("heat_capacity_per_block", 10_000_000, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("msr_reactor");
        MSR_MIN_SIZE = builder.comment("Minimum MSR edge length (cuboid casing).")
                .defineInRange("min_size", 5, 3, 24);
        MSR_MAX_SIZE = builder.comment("Maximum MSR edge length (cuboid casing).")
                .defineInRange("max_size", 11, 5, 32);
        MSR_PEBBLES_PER_FUEL_CELL = builder.comment("TRISO pebbles held per fuel cell block.")
                .defineInRange("pebbles_per_fuel_cell", 10, 2, 100);
        MSR_VOLUME_PER_FUEL_CELL = builder.comment("Molten salt volume (mB) per fuel cell block.")
                .defineInRange("volume_per_fuel_cell", 10000, 1, 1000000);
        builder.pop();

        builder.push("accelerators");
        ACCELERATOR_SCALE_PRESET = builder.comment("Accelerator size preset: 1 = 6..100, 2 = 60..1000, 3 = 600..10000.")
                .defineInRange("scale_preset", 1, 1, 3);
        ACCELERATOR_BEAM_ATTENUATION = builder.defineInRange("beam_attenuation", 0.02, 0.0, 1.0);
        ACCELERATOR_BEAM_SCALING = builder.defineInRange("beam_scaling", 10_000L, 1L, Long.MAX_VALUE);
        ACCELERATOR_HEAT_CAPACITY_PER_BLOCK = builder.defineInRange("heat_capacity_per_block", 25_000L, 1L, 1_000_000_000L);
        ACCELERATOR_BASE_ENERGY_REQUIREMENT = builder.defineInRange("base_energy_requirement", 10_000L, 0L, Long.MAX_VALUE);
        ACCELERATOR_THERMAL_CONDUCTIVITY = builder.defineInRange("thermal_conductivity", 0.0025, 0.0, 1.0);
        RING_ACCELERATOR_MINIMUM_INPUT_ENERGY_KEV = builder.defineInRange("ring_minimum_input_energy_kev", 5_000L, 0L, Long.MAX_VALUE);
        ACCELERATOR_OVERHEAT_COOLDOWN_TICKS = builder.defineInRange("overheat_cooldown_ticks", 1_200, 0, 72_000);
        ACCELERATOR_BEAM_CONNECTION_REACH = builder.defineInRange("beam_connection_reach", 16, 1, 256);
        builder.pop();

        builder.push("particle_chambers");
        TARGET_CHAMBER_MIN_SIZE = builder.defineInRange("target_min_size", 5, 5, 11);
        TARGET_CHAMBER_MAX_SIZE = builder.defineInRange("target_max_size", 11, 5, 11);
        DECAY_CHAMBER_MIN_SIZE = builder.defineInRange("decay_min_size", 5, 5, 11);
        DECAY_CHAMBER_MAX_SIZE = builder.defineInRange("decay_max_size", 11, 5, 11);
        DECAY_CHAMBER_BASE_POWER = builder.defineInRange("decay_base_power", 2_000L, 0L, Long.MAX_VALUE);
        COLLISION_CHAMBER_MIN_TRANSVERSE_SIZE = builder.defineInRange("collision_min_transverse_size", 5, 5, 11);
        COLLISION_CHAMBER_MAX_TRANSVERSE_SIZE = builder.defineInRange("collision_max_transverse_size", 11, 5, 11);
        COLLISION_CHAMBER_MIN_LENGTH = builder.defineInRange("collision_min_length", 13, 13, 21);
        COLLISION_CHAMBER_MAX_LENGTH = builder.defineInRange("collision_max_length", 21, 13, 21);
        COLLISION_CHAMBER_PREFERRED_LENGTH = builder.defineInRange("collision_preferred_length", 17, 13, 21);
        COLLISION_CHAMBER_BASE_POWER = builder.defineInRange("collision_base_power", 2_000L, 0L, Long.MAX_VALUE);
        builder.pop();

        builder.push("particle_multiblock_scheduler");
        PARTICLE_SCHEDULER_TIME_BUDGET_NANOS = builder.defineInRange("time_budget_nanos", 2_000_000L, 50_000L, 50_000_000L);
        PARTICLE_SCHEDULER_BLOCK_READ_BUDGET = builder.defineInRange("block_read_budget", 4_096, 1, 1_000_000);
        PARTICLE_SCHEDULER_BLOCK_ENTITY_READ_BUDGET = builder.defineInRange("block_entity_read_budget", 128, 0, 65_536);
        PARTICLE_SCHEDULER_MUTATION_BUDGET = builder.defineInRange("mutation_budget", 16, 1, 65_536);
        PARTICLE_SCHEDULER_AUDIT_INTERVAL_TICKS = builder.defineInRange("audit_interval_ticks", 200, 20, 72_000);
        builder.pop();

        SPEC = builder.build();
    }

    /** Pushes config values into the plain mirror fields. Call on config load/reload. */
    public static void refresh() {
        fissionMinSize = FISSION_MIN_SIZE.get();
        fissionMaxSize = FISSION_MAX_SIZE.get();
        fissionExplosionRadius = FISSION_EXPLOSION_RADIUS.get();
        fissionHeatCapacity = FISSION_HEAT_CAPACITY.get();
        fissionHeatMultiplier = FISSION_HEAT_MULTIPLIER.get();
        fissionHeatMultiplierCap = FISSION_HEAT_MULTIPLIER_CAP.get();
        fissionModeratorFeMultiplier = FISSION_MODERATOR_FE_MULTIPLIER.get();
        fissionModeratorHeatMultiplier = FISSION_MODERATOR_HEAT_MULTIPLIER.get();
        fissionSupportsBoiling = FISSION_SUPPORTS_BOILING.get();
        fissionBoilingMult = FISSION_BOILING_MULT.get();
        fissionFeGenerationMultiplier = FISSION_FE_GENERATION_MULTIPLIER.get();
        fissionActiveCoolantPerTick = FISSION_ACTIVE_COOLANT_PER_TICK.get();
        fissionFuelHeatMultiplier = FISSION_FUEL_HEAT_MULTIPLIER.get();
        fissionDepletionMultiplier = FISSION_DEPLETION_MULTIPLIER.get();
        kugelblitzGenerationMultiplier = KUGELBLITZ_GENERATION_MULTIPLIER.get();
        kugelblitzEvaporationMultiplier = KUGELBLITZ_EVAPORATION_MULTIPLIER.get();
        kugelblitzLaserDistance = KUGELBLITZ_LASER_DISTANCE.get();
        kugelblitzExplCharge = KUGELBLITZ_EXPL_CHARGE.get();
        kugelblitzExplosionRadius = KUGELBLITZ_EXPLOSION_RADIUS.get();
        kugelblitzBlackholeShader = KUGELBLITZ_BLACKHOLE_SHADER.get();
        turbineMinSize = TURBINE_MIN_SIZE.get();
        turbineMaxSize = TURBINE_MAX_SIZE.get();
        turbineBladeFlow = TURBINE_BLADE_FLOW.get();
        turbineEnergyGen = TURBINE_ENERGY_GEN.get();
        hxMinSize = HX_MIN_SIZE.get();
        hxMaxSize = HX_MAX_SIZE.get();
        hxEnergyPerBlock = HX_ENERGY_PER_BLOCK.get();
        hxThroughputPerBlock = HX_THROUGHPUT_PER_BLOCK.get();
        hxEnergyCapacity = HX_ENERGY_CAPACITY.get();
        hxFluidCapacityPerBlock = HX_FLUID_CAPACITY_PER_BLOCK.get();
        hxRadiatorCooling = HX_RADIATOR_COOLING.get();
        hxHeatCapacityPerBlock = HX_HEAT_CAPACITY_PER_BLOCK.get();
        msrMinSize = MSR_MIN_SIZE.get();
        msrMaxSize = MSR_MAX_SIZE.get();
        msrPebblesPerFuelCell = MSR_PEBBLES_PER_FUEL_CELL.get();
        msrVolumePerFuelCell = MSR_VOLUME_PER_FUEL_CELL.get();
        acceleratorScalePreset = ACCELERATOR_SCALE_PRESET.get();
        acceleratorBeamAttenuation = ACCELERATOR_BEAM_ATTENUATION.get();
        acceleratorBeamScaling = ACCELERATOR_BEAM_SCALING.get();
        acceleratorHeatCapacityPerBlock = ACCELERATOR_HEAT_CAPACITY_PER_BLOCK.get();
        acceleratorBaseEnergyRequirement = ACCELERATOR_BASE_ENERGY_REQUIREMENT.get();
        acceleratorThermalConductivity = ACCELERATOR_THERMAL_CONDUCTIVITY.get();
        ringAcceleratorMinimumInputEnergyKeV = RING_ACCELERATOR_MINIMUM_INPUT_ENERGY_KEV.get();
        acceleratorOverheatCooldownTicks = ACCELERATOR_OVERHEAT_COOLDOWN_TICKS.get();
        acceleratorBeamConnectionReach = ACCELERATOR_BEAM_CONNECTION_REACH.get();
        targetChamberMinSize = TARGET_CHAMBER_MIN_SIZE.get();
        targetChamberMaxSize = TARGET_CHAMBER_MAX_SIZE.get();
        decayChamberMinSize = DECAY_CHAMBER_MIN_SIZE.get();
        decayChamberMaxSize = DECAY_CHAMBER_MAX_SIZE.get();
        decayChamberBasePower = DECAY_CHAMBER_BASE_POWER.get();
        collisionChamberMinTransverseSize = COLLISION_CHAMBER_MIN_TRANSVERSE_SIZE.get();
        collisionChamberMaxTransverseSize = COLLISION_CHAMBER_MAX_TRANSVERSE_SIZE.get();
        collisionChamberMinLength = COLLISION_CHAMBER_MIN_LENGTH.get();
        collisionChamberMaxLength = COLLISION_CHAMBER_MAX_LENGTH.get();
        collisionChamberPreferredLength = COLLISION_CHAMBER_PREFERRED_LENGTH.get();
        collisionChamberBasePower = COLLISION_CHAMBER_BASE_POWER.get();
        particleSchedulerTimeBudgetNanos = PARTICLE_SCHEDULER_TIME_BUDGET_NANOS.get();
        particleSchedulerBlockReadBudget = PARTICLE_SCHEDULER_BLOCK_READ_BUDGET.get();
        particleSchedulerBlockEntityReadBudget = PARTICLE_SCHEDULER_BLOCK_ENTITY_READ_BUDGET.get();
        particleSchedulerMutationBudget = PARTICLE_SCHEDULER_MUTATION_BUDGET.get();
        particleSchedulerAuditIntervalTicks = PARTICLE_SCHEDULER_AUDIT_INTERVAL_TICKS.get();
        particleMachinesRevision = Math.incrementExact(particleMachinesRevision);
    }

    public static ParticleMachinesConfig particleMachines() {
        return new ParticleMachinesConfig(
                new ParticleMachinesConfig.Accelerator(acceleratorScalePreset, acceleratorBeamAttenuation,
                        acceleratorBeamScaling, acceleratorHeatCapacityPerBlock, acceleratorBaseEnergyRequirement,
                        acceleratorThermalConductivity, ringAcceleratorMinimumInputEnergyKeV,
                        acceleratorOverheatCooldownTicks, acceleratorBeamConnectionReach),
                new ParticleMachinesConfig.Chambers(targetChamberMinSize, targetChamberMaxSize,
                        decayChamberMinSize, decayChamberMaxSize, decayChamberBasePower,
                        collisionChamberMinTransverseSize, collisionChamberMaxTransverseSize,
                        collisionChamberMinLength, collisionChamberMaxLength, collisionChamberPreferredLength,
                        collisionChamberBasePower),
                new ParticleMachinesConfig.Scheduler(particleSchedulerTimeBudgetNanos,
                        particleSchedulerBlockReadBudget, particleSchedulerBlockEntityReadBudget,
                        particleSchedulerMutationBudget, particleSchedulerAuditIntervalTicks),
                particleMachinesRevision);
    }
}
