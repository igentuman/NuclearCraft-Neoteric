package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.TRANSPARENT_BLOCKS_PATTERN;
import static igentuman.nc.util.TextUtils.__;

public class TargetChamberCameraBlock extends MultiblockBlock {

    public TargetChamberCameraBlock(Properties pProperties) {
        super(pProperties.strength(8f, 3600000f));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        //list.add(__("tooltip.kugelblitz.block_" + pStack.getItem()).withStyle(ChatFormatting.AQUA));
        list.add(__("tooltip.target_chamber.camera").withStyle(ChatFormatting.GREEN));
    }
}
