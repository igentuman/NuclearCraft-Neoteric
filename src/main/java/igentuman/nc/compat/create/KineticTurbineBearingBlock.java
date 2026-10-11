package igentuman.nc.compat.create;

import com.simibubi.create.content.kinetics.base.IRotate;
import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.setup.ModEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KineticTurbineBearingBlock extends MultiblockBlock implements IRotate, EntityBlock {

    public KineticTurbineBearingBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
            if (neighbor.getBlock() instanceof TurbineRotorBlock) {
                return defaultBlockState().setValue(BlockStateProperties.AXIS, direction.getAxis());
            }
        }
        return defaultBlockState().setValue(BlockStateProperties.AXIS, context.getNearestLookingDirection().getAxis());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(BlockStateProperties.AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(BlockStateProperties.AXIS);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get("turbine_bearing").blockEntity().get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, blockState, be) -> {
            if (be instanceof TurbineBearingKineticBE bearing) bearing.tick();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nc.bearing.create").withStyle(ChatFormatting.GOLD));
    }
}
