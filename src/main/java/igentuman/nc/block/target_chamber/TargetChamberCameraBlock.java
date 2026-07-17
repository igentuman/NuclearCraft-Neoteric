package igentuman.nc.block.target_chamber;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class TargetChamberCameraBlock extends MultiblockBlock {

    public TargetChamberCameraBlock(Properties pProperties) {
        super(pProperties.strength(8f, 3600000f));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        //list.add(__("tooltip.kugelblitz.block_" + pStack.getItem()).withStyle(ChatFormatting.AQUA));
        list.add(__("tooltip.nc.target_chamber.camera").withStyle(ChatFormatting.GREEN));
    }
}
