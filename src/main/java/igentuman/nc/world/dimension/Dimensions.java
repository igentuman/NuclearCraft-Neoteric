package igentuman.nc.world.dimension;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import static igentuman.nc.NuclearCraft.rl;

public class Dimensions {

    public static final int WASTELAND_ID = -4848;
    public static final ResourceKey<Level> WASTELAND = ResourceKey.create(Registry.DIMENSION_REGISTRY, rl("wasteland"));
    public static final ResourceKey<LevelStem> WASTELAND_KEY = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, rl("wasteland"));
    public static final ResourceKey<DimensionType> WASTELAND_DIM_TYPE = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, rl("wasteland_type"));
}

