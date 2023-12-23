package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.block.entity.turbine.TurbineBladeBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineBladeBlock extends DirectionalBlock implements EntityBlock {

    public TurbineBladeBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockState neighbor = level.getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Direction dir = context.getNearestLookingDirection();
        if(neighbor.getBlock() instanceof TurbineRotorBlock) {
            dir = context.getClickedFace().getOpposite();
        } else
        if(neighbor.getBlock() instanceof TurbineBladeBlock) {
            dir = neighbor.getValue(FACING);
        } else {
            for (Direction direction : Direction.values()) {
                BlockState state = level.getBlockState(context.getClickedPos().relative(direction));
                if (state.getBlock() instanceof TurbineRotorBlock) {
                    dir = direction;
                    break;
                }
            }
            for (Direction direction : Direction.values()) {
                BlockState state = level.getBlockState(context.getClickedPos().relative(direction));
                if (state.getBlock() instanceof TurbineBladeBlock) {
                    dir = state.getValue(FACING);
                    break;
                }
            }
        }
        return this.defaultBlockState().setValue(FACING, dir);
    }

    public static boolean processBlockPlace(LevelAccessor level, BlockPos pos, BlockState block, BlockState blockState, BlockState attachment)
    {
        if(attachment.getBlock() instanceof TurbineRotorBlock) {
            return true;
        }
        if(attachment.getBlock() instanceof TurbineBladeBlock) {
            block.setValue(FACING, attachment.getValue(FACING));
            return true;
        }
        for(Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if(state.getBlock() instanceof TurbineRotorBlock) {
                block.setValue(FACING, direction);
                return true;
            }
        }
        for(Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if(state.getBlock() instanceof TurbineBladeBlock) {
                block.setValue(FACING, state.getValue(FACING));
                return true;
            }
        }
        return false;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TURBINE_BE.get("turbine_blade").get().create(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    private String blockEntityCode()
    {
        return asItem().toString();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof TurbineBladeBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof TurbineBladeBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }
}
