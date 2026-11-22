package igentuman.nc.item;

import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Consumer;

public class CraftingPatternItem extends Item {

    public CraftingPatternItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!CraftingPattern.isEncoded(stack)) {
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.crafting_pattern.blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        CraftingPattern p = CraftingPattern.from(stack);
        if (p == null) return;
        if (p.isProcessing()) {
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.crafting_pattern.processing",
                    Component.translatable("block.nuclearcraft." + p.processor())).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.crafting_pattern.crafting").withStyle(ChatFormatting.AQUA));
        }
        for (ItemStack out : p.itemOutputs()) {
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.crafting_pattern.output",
                    Component.literal(out.getCount() + "x ").append(out.getHoverName())).withStyle(ChatFormatting.GRAY));
        }
        for (FluidStack out : p.fluidOutputs()) {
            tooltip.accept(Component.translatable("tooltip.nuclearcraft.crafting_pattern.output",
                    Component.literal(TextUtils.formatLiquid(out.getAmount()) + " ").append(out.getHoverName())).withStyle(ChatFormatting.GRAY));
        }
    }
}
