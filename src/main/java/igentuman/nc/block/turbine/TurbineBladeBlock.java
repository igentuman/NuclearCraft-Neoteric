package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.block.entity.turbine.TurbineBladeBE;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.CoilDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;
import static net.minecraft.world.level.block.Blocks.ANVIL;
import static net.minecraft.world.level.block.Blocks.IRON_BARS;

public class TurbineBladeBlock extends DirectionalBlock implements EntityBlock {
    public static final BooleanProperty HIDDEN = BlockStateProperties.POWERED;
    public TurbineBladeBlock(Properties pProperties) {
        super(Properties.copy(IRON_BARS).noCollission().forceSolidOff());
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if(pState.getValue(HIDDEN)) {
            return false;
        }
        return super.isCollisionShapeFullBlock(pState, pLevel, pPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pState.getValue(HIDDEN)) {
            return Block.box(0, 0, 0, 0, 0, 0);
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    public double efficiency = 0;
    public double expansion = 0;
    public String type = "";
    public BladeDef def;

    private void initParams() {
        Item item = Item.byBlock(this);
        if(item.toString().isEmpty()) return;
        type = item.toString().replaceAll("turbine_", "");
        def = TurbineRegistration.blades().get(type);
        efficiency = def.getEfficiency();
        expansion = def.getExpansion();
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
        return this.defaultBlockState().setValue(FACING, dir).setValue(HIDDEN, false);
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
        if(def == null) initParams();
        TurbineBladeBE be = (TurbineBladeBE) TURBINE_BE.get("turbine_blade").get().create(pPos, pState);
        be.setBladeDef(def);
        return be;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING).add(HIDDEN);
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


    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        initParams();

        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(
                    Component.translatable("tooltip.nc.description.efficiency", TextUtils.numberFormat(def.getEfficiency())),
                    ChatFormatting.AQUA));
            list.add(TextUtils.applyFormat(
                    Component.translatable("tooltip.nc.description.expansion", TextUtils.numberFormat(def.getExpansion())),
                    ChatFormatting.GOLD));
        } else {
            list.add(TextUtils.applyFormat(Component.translatable("tooltip.nc.blade.desc", TURBINE_CONFIG.BLADE_FLOW.get()), ChatFormatting.BLUE));
        }
        list.add(TextUtils.applyFormat(Component.translatable("tooltip.toggle_description_keys"), ChatFormatting.GRAY));
    }
}
