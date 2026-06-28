package igentuman.nc.setup.entries;

import igentuman.nc.setup.ModEntries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class Blocks extends ModEntries {
    public static void blocks() {
        deco("wasteland_earth", BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .sound(SoundType.GRAVEL)
                .strength(1.5f)
                .requiresCorrectToolForDrops());
        deco("glowing_mushroom", BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .sound(SoundType.GRASS)
                .noCollission()
                .noOcclusion()
                .instabreak()
                .randomTicks()
                .lightLevel(state -> 5));
    }
}
