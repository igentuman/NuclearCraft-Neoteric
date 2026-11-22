package igentuman.nc.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public interface TooltipBlock {
    void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                    Consumer<Component> builder, TooltipFlag flag);
}
