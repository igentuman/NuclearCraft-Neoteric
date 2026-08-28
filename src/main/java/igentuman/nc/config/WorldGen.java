package igentuman.nc.config;

import igentuman.nc.registration.MaterialEntry;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Builds per-material ore worldgen config (height range, vein size, veins per chunk) from material entries. */
public class WorldGen {
    public static ModConfigSpec SPEC;

    public static final Map<String, OreGenConfig> ORE_CONFIGS = new HashMap<>();

    public static void init(Collection<MaterialEntry> materials) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        for (MaterialEntry mat : materials) {
            if (!mat.hasWorldgenConfig()) continue;
            builder.push(mat.name);
            ModConfigSpec.IntValue minHeight = builder
                .comment("Minimum Y level for ore generation")
                .defineInRange("min_height", mat.worldgenMinHeight, -64, 320);
            ModConfigSpec.IntValue maxHeight = builder
                .comment("Maximum Y level for ore generation")
                .defineInRange("max_height", mat.worldgenMaxHeight, mat.worldgenMinHeight, 320);
            ModConfigSpec.IntValue veinSize = builder
                .comment("Number of blocks per ore vein")
                .defineInRange("vein_size", mat.worldgenQty, 1, 64);
            ModConfigSpec.IntValue veinsPerChunk = builder
                .comment("Number of ore veins attempted per chunk")
                .defineInRange("veins_per_chunk", 4, 0, 32);
            builder.pop();
            ORE_CONFIGS.put(mat.name, new OreGenConfig(minHeight, maxHeight, veinSize, veinsPerChunk));
        }
        SPEC = builder.build();
    }

    public record OreGenConfig(
        ModConfigSpec.IntValue minHeight,
        ModConfigSpec.IntValue maxHeight,
        ModConfigSpec.IntValue veinSize,
        ModConfigSpec.IntValue veinsPerChunk
    ) {}
}
