package igentuman.nc.compat.ie;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IEMineralVein {

    private static final Random RANDOM = new Random();

    public static ChunkPos getChunkPos(ItemStack oreSample) {
        ColumnPos pos = CoresampleItem.getCoords(oreSample);
        if (pos != null) {
            return pos.toChunkPos();
        }
        return null;
    }

    public static ResourceKey<Level> getDimension(ItemStack stack) {
        return CoresampleItem.getDimension(stack);
    }

    public static ItemStack getNextVeinItem(Level level, ItemStack stack, List<ItemStack> allowed) {
        MineralMix[] mixes = CoresampleItem.getMineralMixes(level, stack);
        if (mixes.length == 0) {
            return ItemStack.EMPTY;
        }
        MineralMix selected = pickWeighted(mixes);
        if (selected == null) {
            return ItemStack.EMPTY;
        }
        return pickAllowedOre(selected, allowed);
    }

    private static ItemStack pickAllowedOre(MineralMix mix, List<ItemStack> allowed) {
        if (mix.outputs == null || mix.outputs.length == 0) {
            return ItemStack.EMPTY;
        }
        if (allowed == null || allowed.isEmpty()) {
            return mix.getRandomOre(RANDOM);
        }
        List<StackWithChance> filtered = new ArrayList<>();
        float total = 0f;
        for (StackWithChance entry : mix.outputs) {
            if (entry.chance() < 0) continue;
            ItemStack candidate = entry.stack().get();
            if (candidate.isEmpty()) continue;
            if (!isAllowed(candidate, allowed)) continue;
            filtered.add(entry);
            total += entry.chance();
        }
        if (filtered.isEmpty() || total <= 0f) {
            return ItemStack.EMPTY;
        }
        float roll = RANDOM.nextFloat() * total;
        for (StackWithChance entry : filtered) {
            roll -= entry.chance();
            if (roll < 0) {
                return entry.stack().get().copy();
            }
        }
        return filtered.get(filtered.size() - 1).stack().get().copy();
    }

    private static boolean isAllowed(ItemStack candidate, List<ItemStack> allowed) {
        for (ItemStack a : allowed) {
            if (a.isEmpty()) continue;
            if (a.is(candidate.getItem())) return true;
        }
        return false;
    }

    private static MineralMix pickWeighted(MineralMix[] mixes) {
        int total = 0;
        for (MineralMix mix : mixes) {
            total += Math.max(0, mix.weight);
        }
        if (total <= 0) {
            return mixes[RANDOM.nextInt(mixes.length)];
        }
        int roll = RANDOM.nextInt(total);
        for (MineralMix mix : mixes) {
            roll -= Math.max(0, mix.weight);
            if (roll < 0) {
                return mix;
            }
        }
        return mixes[mixes.length - 1];
    }
}
