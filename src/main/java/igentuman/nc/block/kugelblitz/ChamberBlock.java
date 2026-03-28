package igentuman.nc.block.kugelblitz;

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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Properties;

import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.util.TextUtils.__;

public class ChamberBlock extends MultiblockBlock {

    public ChamberBlock(Properties pProperties) {
        super(Properties.of().sound(SoundType.METAL).strength(8f, 3600000f));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return NCNames.of(asItem()).matches(".*photon.*");
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return NCNames.of(asItem()).matches(".*photon.*") ? 1.0F : 0.2F;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && TRANSPARENT_BLOCKS_PATTERN.matcher(getCode()).matches();
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(pStack.getItem()).getPath();
        list.add(__("tooltip.kugelblitz.block_" + name).withStyle(ChatFormatting.AQUA));
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
