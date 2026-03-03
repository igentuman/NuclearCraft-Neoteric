package igentuman.nc.block.turbine;

import igentuman.nc.block.turbine.entity.TurbineBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static igentuman.nc.util.TextUtils.__;

public class TurbineRotorBlock extends DirectionalBlock implements EntityBlock {

    public TurbineRotorBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).noOcclusion());
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(TurbineRotorBlock::new);
    }

    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();

        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(context.getClickedPos().relative(direction));
            if (neighbor.getBlock() instanceof TurbineRotorBlock) {
                return this.defaultBlockState().setValue(FACING, neighbor.getValue(FACING)).setValue(ACTIVE, false);
            }
        }
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(context.getClickedPos().relative(direction));
            if(neighbor.is(TurbineRegistration.TURBINE_BLOCKS.get("turbine_bearing").get())) {
                return this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(ACTIVE, false);
            }
        }
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(!pState.getValue(ACTIVE)) {
            return null;
        }
        return super.getVisualShape(pState, pLevel, pPos, pContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING).add(ACTIVE);
    }
    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this);
    }

    private String codeID()
    {
        return BuiltInRegistries.BLOCK.getKey(this).getPath();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TURBINE_BE.get("turbine_rotor_shaft").get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof TurbineBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof TurbineBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        if(level.isClientSide()) return;
        MultiblockHandler.get(((Level)level).dimension()).trackBlockChange(neighbor);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
        if(pLevel.isClientSide()) return;
        MultiblockHandler.get(pLevel.dimension()).trackBlockChange(pPos, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if(level.isClientSide()) return;
        MultiblockHandler.get(level.dimension()).trackBlockChange(pos, true);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(__("tooltip.nc.rotor_shaft.desc"), ChatFormatting.BLUE));
    }
}
