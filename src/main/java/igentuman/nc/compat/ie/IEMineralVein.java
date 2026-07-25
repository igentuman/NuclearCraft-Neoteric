package igentuman.nc.compat.ie;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

/** Immersive Engineering mineral vein compat utility; reads core sample NBT and extracts ore from IE's vein system. */
public class IEMineralVein {

    /** Reads the chunk position from an IE core sample item's NBT. */
    public static ChunkPos getChunkPos(ItemStack coreSample) {
        CompoundTag tag = coreSample.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("x") && tag.contains("z")) {
            return new ChunkPos(tag.getInt("x"), tag.getInt("z"));
        }
        if (tag.contains("chunkX") && tag.contains("chunkZ")) {
            return new ChunkPos(tag.getInt("chunkX"), tag.getInt("chunkZ"));
        }
        return null;
    }

    /** Reads the dimension key from an IE core sample item's NBT. */
    public static ResourceKey<Level> getDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("dimension")) {
            String dim = tag.getString("dimension");
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(dim));
        }
        return null;
    }

    /** Gets the next ore item from IE's mineral vein system for the given core sample. */
    public static ItemStack getNextVeinItem(Level level, ItemStack stack, List<ItemStack> allowed) {
        return ItemStack.EMPTY;
    }
}
