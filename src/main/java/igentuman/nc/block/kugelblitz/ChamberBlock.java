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

import java.util.List;

public class ChamberBlock extends MultiblockBlock {

    public ChamberBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
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
        return adjacentBlockState.getBlock().equals(this) && asItem().toString().matches(".*photon.*|.*slope.*");
    }


    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(Component.translatable("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
