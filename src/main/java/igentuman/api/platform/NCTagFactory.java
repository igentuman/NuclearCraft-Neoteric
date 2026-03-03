package igentuman.api.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

/**
 * Platform wrapper for TagKey creation.
 * Encapsulates registry key access (getRegistryKey() → key() in 1.21.1)
 * and provides typed convenience methods per registry.
 *
 * Also wraps NeoForge Tags.Items renames:
 * - Tags.Items.GLASS → Tags.Items.GLASS_BLOCKS (1.21.1)
 * - Tags.Items.GLASS_PANES → Tags.Items.GLASS_PANES (unchanged)
 */
public final class NCTagFactory {
    private NCTagFactory() {}

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id) {
        return TagKey.create(registryKey, id);
    }

    public static TagKey<Fluid> fluidTag(ResourceLocation id) {
        return TagKey.create(BuiltInRegistries.FLUID.key(), id);
    }

    public static TagKey<Block> blockTag(ResourceLocation id) {
        return TagKey.create(BuiltInRegistries.BLOCK.key(), id);
    }

    public static TagKey<Item> itemTag(ResourceLocation id) {
        return TagKey.create(BuiltInRegistries.ITEM.key(), id);
    }

    public static TagKey<Biome> biomeTag(ResourceLocation id) {
        return TagKey.create(net.minecraft.core.registries.Registries.BIOME, id);
    }

    // --- NeoForge Tags.Items renames (Forge → NeoForge 1.21.1) ---

    /**
     * Tags.Items.GLASS → Tags.Items.GLASS_BLOCKS in NeoForge 1.21.1.
     */
    public static TagKey<Item> glass() {
        return Tags.Items.GLASS_BLOCKS;
    }

    /**
     * Tags.Items.GLASS_PANES (unchanged in 1.21.1).
     */
    public static TagKey<Item> glassPanes() {
        return Tags.Items.GLASS_PANES;
    }

    /**
     * Tags.Items.INGOTS_BRICK → Tags.Items.BRICKS_NORMAL in NeoForge 1.21.1.
     */
    public static TagKey<Item> bricksNormal() {
        return Tags.Items.BRICKS_NORMAL;
    }

    /**
     * Tags.Items.INGOTS_NETHER_BRICK → Tags.Items.BRICKS_NETHER in NeoForge 1.21.1.
     */
    public static TagKey<Item> bricksNether() {
        return Tags.Items.BRICKS_NETHER;
    }

    /**
     * Tags.Items.SAND → Tags.Items.SANDS in NeoForge 1.21.1.
     */
    public static TagKey<Item> sands() {
        return Tags.Items.SANDS;
    }
}
