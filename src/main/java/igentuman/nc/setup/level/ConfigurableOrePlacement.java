package igentuman.nc.setup.level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import igentuman.nc.config.WorldGen;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/** Placement modifier that positions ore veins at a config-driven random height per material. */
public class ConfigurableOrePlacement extends PlacementModifier {

    public static final MapCodec<ConfigurableOrePlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("material").forGetter(p -> p.materialName)
        ).apply(instance, ConfigurableOrePlacement::new)
    );

    private final String materialName;

    public ConfigurableOrePlacement(String materialName) {
        this.materialName = materialName;
    }

    public static ConfigurableOrePlacement forMaterial(String materialName) {
        return new ConfigurableOrePlacement(materialName);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource rand, BlockPos pos) {
        WorldGen.OreGenConfig config = WorldGen.ORE_CONFIGS.get(materialName);
        if (config == null) return Stream.empty();
        int minH = config.minHeight().get();
        int maxH = Math.max(minH, config.maxHeight().get());
        int y = Mth.randomBetweenInclusive(rand, minH, maxH);
        return Stream.of(new BlockPos(pos.getX(), y, pos.getZ()));
    }

    @Override
    public PlacementModifierType<?> type() {
        return Registers.CONFIGURABLE_ORE_PLACEMENT.get();
    }
}
