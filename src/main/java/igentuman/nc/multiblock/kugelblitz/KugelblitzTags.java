package igentuman.nc.multiblock.kugelblitz;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.NuclearCraft.MODID;

public final class KugelblitzTags {

    private KugelblitzTags() {}

    public static final TagKey<Block> CASING = block("kugelblitz_casing");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, path));
    }
}
