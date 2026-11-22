package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.api.multiblock.part.TurbineCoilDef;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class TurbineCoilBlock extends MultiblockBlock implements TooltipBlock {

    @Nullable
    private final TurbineCoilDef def;

    public TurbineCoilBlock(BlockBehaviour.Properties props, @Nullable TurbineCoilDef def) {
        super(props);
        this.def = def;
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (def == null) return;
        tooltip.accept(__("tooltip.nuclearcraft.turbine_coil.efficiency", TextUtils.numberFormat(def.getEfficiency()))
                .withStyle(ChatFormatting.GOLD));
        if (flag.hasShiftDown()) {
            if (def.rules.length > 0) {
                tooltip.accept(TextUtils.applyFormat(def.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
    }
}
