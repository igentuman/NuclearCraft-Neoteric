package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class CoolerBlock extends MultiblockBlock implements TooltipBlock {

    private final CoolerDef definition;

    public CoolerBlock(Properties properties, CoolerDef definition) {
        super(properties);
        this.definition = definition;
    }

    public CoolerDef definition() {
        return definition;
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(__("tooltip.nuclearcraft.cooler.description").withStyle(ChatFormatting.GRAY));
        tooltip.accept(__("tooltip.nuclearcraft.heat_sink.heat", TextUtils.numberFormat(definition.coolingPerTick()))
                .withStyle(ChatFormatting.GOLD));
        if (flag.hasShiftDown()) {
            if (!definition.placementRules().isEmpty()) {
                tooltip.accept(TextUtils.applyFormat(definition.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
    }
}
