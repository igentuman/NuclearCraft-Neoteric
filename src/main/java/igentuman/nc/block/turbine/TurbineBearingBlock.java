package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.compat.create.CreateTurbine;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static igentuman.nc.util.ModUtil.isCreateLoaded;
import static igentuman.nc.util.TextUtils.__;

public class TurbineBearingBlock extends MultiblockBlock implements EntityBlock {

    public TurbineBearingBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getNearestLookingDirection().getAxis();
        for (Direction direction : Direction.values()) {
            if (context.getLevel().getBlockState(context.getClickedPos().relative(direction)).getBlock() instanceof TurbineRotorBlock) {
                axis = direction.getAxis();
                break;
            }
        }
        return defaultBlockState().setValue(BlockStateProperties.AXIS, axis);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(__("tooltip.nc.bearing.desc"), ChatFormatting.BLUE));
        list.add(TextUtils.applyFormat(__("tooltip.nc.bearing.create"), ChatFormatting.GOLD));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return TURBINE_BE.get("turbine_bearing").get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!isCreateLoaded()) {
            return null;
        }
        return (lvl, pos, blockState, t) -> CreateTurbine.tickBearing(t);
    }
}
