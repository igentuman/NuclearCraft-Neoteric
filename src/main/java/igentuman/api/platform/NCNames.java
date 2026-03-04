package igentuman.api.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Platform wrapper for getting registry names without namespace prefix.
 * In NeoForge 1.21.1, Item.toString() returns "namespace:path" instead of just "path".
 * This utility provides the path-only form needed for HashMap key lookups.
 */
public final class NCNames {
    private NCNames() {}

    /** Get the registry path of an item (without namespace prefix). */
    public static String of(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    /** Get the registry path of a block's item form (without namespace prefix). */
    public static String ofBlock(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
