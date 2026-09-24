package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.api.multiblock.part.CoolerDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class CoolerBlock extends MultiblockBlock {

    private final CoolerDef definition;

    public CoolerBlock(Properties properties, CoolerDef definition) {
        super(properties);
        this.definition = definition;
    }

    public CoolerDef definition() {
        return definition;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(__("tooltip.nuclearcraft.cooler.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(__("tooltip.nuclearcraft.heat_sink.heat", TextUtils.numberFormat(definition.coolingPerTick()))
                .withStyle(ChatFormatting.GOLD));
        if (flag.hasShiftDown()) {
            if (!definition.placementRules().isEmpty()) {
                tooltip.add(TextUtils.applyFormat(definition.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
    }
}
