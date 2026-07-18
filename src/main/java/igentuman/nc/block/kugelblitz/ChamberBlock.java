package igentuman.nc.block.kugelblitz;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.material.Material;

import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.util.TextUtils.__;

public class ChamberBlock extends MultiblockBlock {

    public ChamberBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties pProperties) {
        super(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(8f, 3600000f));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return asItem().toString().matches(".*photon.*");
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return asItem().toString().matches(".*photon.*") ? 1.0F : 0.2F;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && TRANSPARENT_BLOCKS_PATTERN.matcher(getCode()).matches();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(__("tooltip.kugelblitz.block_" + pStack.getItem()).withStyle(ChatFormatting.AQUA));
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
