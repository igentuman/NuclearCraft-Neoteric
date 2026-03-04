package igentuman.nc.block.target_chamber;

import igentuman.api.platform.NCNames;
import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.TRANSPARENT_BLOCKS_PATTERN;
import static igentuman.nc.util.TextUtils.__;

public class TargetChamberBlock extends MultiblockBlock {

    public TargetChamberBlock(Properties pProperties) {
        super(pProperties.strength(8f, 3600000f));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return NCNames.of(asItem()).matches(".*glass.*");
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return NCNames.of(asItem()).matches(".*glass.*") ? 1.0F : 0.2F;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && TRANSPARENT_BLOCKS_PATTERN.matcher(getCode()).matches();
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        //list.add(__("tooltip.kugelblitz.block_" + pStack.getItem()).withStyle(ChatFormatting.AQUA));
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
