package igentuman.nc.item;

import igentuman.nc.block_entity.catalyst.EnergyCatalyst;
import igentuman.nc.block_entity.catalyst.QuantumCatalyst;
import igentuman.nc.block_entity.catalyst.SpeedCatalyst;
import igentuman.nc.block_entity.catalyst.StackCatalyst;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Processor upgrade item whose tooltip reports the bonuses supplied by the hovered stack. */
public class ProcessorUpgradeItem extends Item {

    public enum Type {
        ENERGY("energy"),
        SPEED("speed"),
        STACK("stack"),
        QUANTUM("quantum");

        private final String key;

        Type(String key) {
            this.key = key;
        }
    }

    private final Type type;

    public ProcessorUpgradeItem(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int count = stack.getCount();
        tooltip.add(Component.translatable("tooltip.nuclearcraft.upgrade." + type.key + ".description")
                .withStyle(ChatFormatting.GRAY));
        switch (type) {
            case ENERGY -> tooltip.add(Component.translatable("tooltip.nuclearcraft.upgrade.energy_capacity",
                            count * EnergyCatalyst.CAPACITY_PERCENT_PER_POWER)
                    .withStyle(ChatFormatting.GREEN));
            case SPEED -> addSpeed(tooltip, SpeedCatalyst.speedMultiplier(count));
            case STACK -> {
                addSpeed(tooltip, SpeedCatalyst.speedMultiplier(count));
                addParallel(tooltip, StackCatalyst.parallelLimit(count));
            }
            case QUANTUM -> {
                addSpeed(tooltip, QuantumCatalyst.speedMultiplier(count));
                addParallel(tooltip, QuantumCatalyst.parallelLimit(count));
            }
        }
    }

    private static void addSpeed(List<Component> tooltip, int speed) {
        tooltip.add(Component.translatable("tooltip.nuclearcraft.upgrade.speed_bonus", speed)
                .withStyle(ChatFormatting.AQUA));
    }

    private static void addParallel(List<Component> tooltip, int parallel) {
        tooltip.add(Component.translatable("tooltip.nuclearcraft.upgrade.parallel_bonus", parallel)
                .withStyle(ChatFormatting.GOLD));
    }
}
