package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class TurbineBearingBlock extends MultiblockBlock {

    public TurbineBearingBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(__("tooltip.nc.bearing.desc"), ChatFormatting.BLUE));
    }
}
