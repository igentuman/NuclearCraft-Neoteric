package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class HeatExchangerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final HeatExchangerConf HEAT_EXCHANGER_CONFIG = new HeatExchangerConf(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();
    private static boolean loaded = false;
    private static final List<Runnable> loadActions = new ArrayList<>();

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

    public static class HeatExchangerConf {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> ENERGY_PER_BLOCK;
        public final ForgeConfigSpec.ConfigValue<Double> THROUGHPUT_PER_BLOCK;
        public final ForgeConfigSpec.ConfigValue<Integer> ENERGY_CAPACITY;
        public final ForgeConfigSpec.ConfigValue<Integer> FLUID_CAPACITY;
        public final ForgeConfigSpec.ConfigValue<Integer> RADIATOR_COOLING;
        public final ForgeConfigSpec.ConfigValue<Integer> HEAT_CAPACITY_PER_BLOCK;

        public HeatExchangerConf(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Heat Exchanger").push("heat_exchanger");

            MIN_SIZE = builder
                    .comment("Multiblock min size.")
                    .defineInRange("min_size", 3, 3, 25);

            MAX_SIZE = builder
                    .comment("Multiblock max size.")
                    .defineInRange("max_size", 11, 5, 25);

            ENERGY_PER_BLOCK = builder
                    .comment("Standby FE/t consumed per interior heat exchanger block while powered by redstone. Recipes themselves are free; running out of energy halts processing.")
                    .defineInRange("energy_per_block", 200, 0, 1000000);

            THROUGHPUT_PER_BLOCK = builder
                    .comment("Recipe progress units per server tick, per interior heat exchanger block.")
                    .defineInRange("throughput_per_block", 5000.0D, 0.01D, 1000D);

            ENERGY_CAPACITY = builder
                    .comment("Internal energy buffer (FE).")
                    .defineInRange("energy_capacity", 10000000, 0, Integer.MAX_VALUE);

            FLUID_CAPACITY = builder
                    .comment("Fluid tank capacity (mB) per interior heat exchanger block, for each tank.")
                    .defineInRange("fluid_capacity_per_block", 10000, 1000, 1000000);

            RADIATOR_COOLING = builder
                    .comment("Heat removed from the buffer per tick (H/t) per radiator block in the shell. Passive: applies whenever the multiblock is formed.")
                    .defineInRange("radiator_cooling", 500000, 0, Integer.MAX_VALUE);

            HEAT_CAPACITY_PER_BLOCK = builder
                    .comment("Heat buffer capacity per interior heat exchanger block.")
                    .defineInRange("heat_capacity_per_block", 1000000, 1, Integer.MAX_VALUE);

            builder.pop();
        }
    }
}
