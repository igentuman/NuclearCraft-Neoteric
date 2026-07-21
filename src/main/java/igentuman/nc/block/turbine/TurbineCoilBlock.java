package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.turbine.TurbineCoilDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class TurbineCoilBlock extends MultiblockBlock {

    @Nullable
    private final TurbineCoilDef def;

    public TurbineCoilBlock(BlockBehaviour.Properties props, @Nullable TurbineCoilDef def) {
        super(props);
        this.def = def;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (def == null) return;
        tooltip.add(__("tooltip.nuclearcraft.turbine_coil.efficiency", TextUtils.numberFormat(def.getEfficiency()))
                .withStyle(ChatFormatting.GOLD));
        if (flag.hasShiftDown()) {
            if (def.rules.length > 0) {
                tooltip.add(TextUtils.applyFormat(def.getPlacementRule(), ChatFormatting.AQUA));
            }
        } else {
            tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.shift"), ChatFormatting.GRAY));
        }
    }
}
