package igentuman.nc.item;

import igentuman.nc.block.TooltipBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** Retains block-owned tooltip lines at the normal item-tooltip position. */
public class NCBlockItem extends BlockItem {
    public NCBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (getBlock() instanceof TooltipBlock tooltipBlock) {
            tooltipBlock.addTooltip(stack, context, display, builder, flag);
        }
    }
}
