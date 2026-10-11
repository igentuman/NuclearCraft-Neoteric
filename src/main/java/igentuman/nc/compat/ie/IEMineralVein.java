package igentuman.nc.compat.ie;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class IEMineralVein {

    public static ChunkPos getChunkPos(ItemStack coreSample) {
        CoresampleItem.ItemData data = coreSample.get(IEDataComponents.CORESAMPLE);
        if (data == null || data.position().dimension() == null) return null;
        return data.position().position().toChunkPos();
    }

    public static ResourceKey<Level> getDimension(ItemStack coreSample) {
        CoresampleItem.ItemData data = coreSample.get(IEDataComponents.CORESAMPLE);
        return data == null ? null : data.position().dimension();
    }

    public static ItemStack getNextVeinItem(Level level, ItemStack coreSample, Predicate<ItemStack> acceptsOre) {
        CoresampleItem.ItemData data = coreSample.get(IEDataComponents.CORESAMPLE);
        if (data == null || data.veins().isEmpty()) return ItemStack.EMPTY;

        List<MineralMix> eligibleMixes = new ArrayList<>();
        List<List<StackWithChance>> eligibleOutputs = new ArrayList<>();
        int totalWeight = 0;
        for (var holder : CoresampleItem.getMineralMixes(level, coreSample)) {
            MineralMix mix = holder.value();
            List<StackWithChance> outputs = mix.outputs.stream()
                    .filter(output -> output.chance() > 0 && acceptsOre.test(output.stack().get()))
                    .toList();
            if (outputs.isEmpty()) continue;
            eligibleMixes.add(mix);
            eligibleOutputs.add(outputs);
            totalWeight += Math.max(0, mix.weight);
        }
        if (eligibleMixes.isEmpty()) return ItemStack.EMPTY;

        int selected = ThreadLocalRandom.current().nextInt(eligibleMixes.size());
        if (totalWeight > 0) {
            int roll = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < eligibleMixes.size(); i++) {
                roll -= Math.max(0, eligibleMixes.get(i).weight);
                if (roll < 0) {
                    selected = i;
                    break;
                }
            }
        }

        List<StackWithChance> outputs = eligibleOutputs.get(selected);
        float totalChance = 0;
        for (StackWithChance output : outputs) totalChance += output.chance();
        float roll = ThreadLocalRandom.current().nextFloat() * totalChance;
        for (StackWithChance output : outputs) {
            roll -= output.chance();
            if (roll < 0) return output.stack().get().copyWithCount(1);
        }
        return outputs.getLast().stack().get().copyWithCount(1);
    }
}
