package igentuman.nc.block.heat_exchanger;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class RadiatorBlock extends MultiblockBlock {

    public RadiatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
        list.add(__("heat_exchanger.radiator.descr").withStyle(ChatFormatting.GOLD));
    }
}
