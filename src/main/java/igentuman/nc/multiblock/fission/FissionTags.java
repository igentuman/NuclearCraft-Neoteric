package igentuman.nc.multiblock.fission;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.NuclearCraft.MODID;

public final class FissionTags {

    private FissionTags() {}

    public static final TagKey<Block> CASING = block("fission_reactor_casing");
    public static final TagKey<Block> REACTOR_INNER = block("reactor_inner");
    public static final TagKey<Block> MODERATORS = block("moderators");

    public static final TagKey<Item> CASING_ITEM = item("fission_reactor_casing");
    public static final TagKey<Item> MODERATORS_ITEM = item("moderators");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, path));
    }
}
