package igentuman.nc.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import igentuman.nc.setup.registration.WorldGeneration;
import igentuman.nc.world.dimension.Dimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static igentuman.nc.handler.config.OreGenConfig.ORE_CONFIG;
import static igentuman.nc.world.dimension.Dimensions.WASTELAND_DIM_TYPE;
import static net.minecraft.core.registries.Registries.DIMENSION;

public class OrePlacementModifier extends PlacementModifier {

    public static final MapCodec<OrePlacementModifier> CODEC = Codec.STRING.fieldOf("name")
            .xmap(OrePlacementModifier::new, (modifier) -> modifier.name);

    private final String name;
    private boolean register;
    private int amount;
    private int minHeight;
    private int maxHeight;
    private List<String> dims;
    private HashMap<ResourceKey<Level>, Boolean> dimsCache = new HashMap<>();

    public OrePlacementModifier(String name) {
        this.name = name;
        try {
            register = ORE_CONFIG.ORES.get(name).register.get();
            amount = ORE_CONFIG.ORES.get(name).veinSize.get();
            minHeight = ORE_CONFIG.ORES.get(name).min_height.get();
            maxHeight = ORE_CONFIG.ORES.get(name).max_height.get();
            dims = ORE_CONFIG.ORES.get(name).dimensions.get();
        } catch (Exception e) {
            register = ORE_CONFIG.ORES.get(name).register.getDefault();
            amount = ORE_CONFIG.ORES.get(name).veinSize.getDefault();
            minHeight = ORE_CONFIG.ORES.get(name).min_height.getDefault();
            maxHeight = ORE_CONFIG.ORES.get(name).max_height.getDefault();
            dims = ORE_CONFIG.ORES.get(name).dimensions.getDefault();
        }
        if (!register) {
            amount = 0;
        }
    }


    @Override
    public @NotNull PlacementModifierType<?> type() {
        return WorldGeneration.NC_ORE_MODIFIER.get();
    }

    @Override
    public @NotNull Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int actualCount = determinePlacementCount(context, random);
        int x = (pos.getX() >> 4 << 4) + random.nextInt(16);
        int z = (pos.getZ() >> 4 << 4) + random.nextInt(16);
        if(actualCount == 0) {
            return Stream.empty();
        }
        return Stream.generate(() -> new BlockPos(
                x,
                minHeight + random.nextInt(maxHeight - Math.max(minHeight, context.getMinGenY()) + 1),
                z
        )).limit(actualCount);
    }

    private int determinePlacementCount(PlacementContext context, RandomSource random) {
        try {
            WorldGenLevel level = context.getLevel();
            int veinSize = amount;
            if(!canPlace(level)) {
                return 0;
            }
            return random.nextInt(veinSize);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean canPlace(WorldGenLevel level) {
        if (dimsCache.containsKey(level.getLevel().dimension())) {
            return dimsCache.get(level.getLevel().dimension());
        }
        for (String dim : dims) {
            if (level.getLevel().dimension().location().toString().equals(dim)) {
                dimsCache.put(level.getLevel().dimension(), true);
                return true;
            }
            dimsCache.put(level.getLevel().dimension(), false);
        }
        return false;
    }
}