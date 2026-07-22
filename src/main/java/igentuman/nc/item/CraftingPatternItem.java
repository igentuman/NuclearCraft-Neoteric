package igentuman.nc.item;

import igentuman.nc.handler.crafter.CraftingPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CraftingPatternItem extends Item {

    public CraftingPatternItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!CraftingPattern.isEncoded(stack)) {
            tooltip.add(Component.translatable("tooltip.nuclearcraft.crafting_pattern.blank").withStyle(ChatFormatting.GRAY));
        }
    }
}
