package igentuman.nc.setup.entries;

import igentuman.nc.block.PortalBlock;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static igentuman.nc.registration.ModEntryBuilder.add;

/** Declares decorative world blocks such as wasteland earth and glowing mushroom. */
public class Blocks extends ModEntries {
    public static void blocks() {
        deco("wasteland_earth", BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .sound(SoundType.GRAVEL)
                .strength(1.5f)
                .requiresCorrectToolForDrops());
        add("wasteland_portal").block(() -> new PortalBlock()).build();
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
