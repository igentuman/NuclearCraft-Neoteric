package igentuman.nc.multiblock.fusion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.NuclearCraft.MODID;

public final class FusionTags {

    private FusionTags() {}

    public static final TagKey<Block> CASING = block("fusion_reactor_casing");
    public static final TagKey<Block> ELECTROMAGNETS = block("electromagnets");
    public static final TagKey<Block> RF_AMPLIFIERS = block("rf_amplifiers");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, path));
    }
}
