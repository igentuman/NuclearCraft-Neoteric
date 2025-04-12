package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.block.entity.turbine.TurbineBladeBE;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.antlr.v4.runtime.misc.NotNull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineBladeBlock extends DirectionalBlock {

    public static final DirectionProperty BLOCK_FACING = FACING;

    public TurbineBladeBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(BLOCK_FACING, Direction.NORTH)
        );
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World level = context.getLevel();
        BlockState neighbor = level.getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Direction dir = context.getNearestLookingDirection();
        if(neighbor.getBlock() instanceof TurbineRotorBlock) {
            dir = context.getClickedFace().getOpposite();
        } else
        if(neighbor.getBlock() instanceof TurbineBladeBlock) {
            dir = neighbor.getValue(BLOCK_FACING);
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
                    dir = state.getValue(BLOCK_FACING);
                    break;
                }
            }
        }
        return this.defaultBlockState().setValue(BLOCK_FACING, dir);
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState block, LivingEntity entity, ItemStack stack)
    {
        /*if(attachment.getBlock() instanceof TurbineRotorBlock) {
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
        return false;*/
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if(def == null) initParams();
        TurbineBladeBE be = (TurbineBladeBE) TURBINE_BE.get("turbine_blade").get().create();
        be.setBladeDef(def);
        return be;
    }

    private String blockEntityCode()
    {
        return asItem().toString();
    }


    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }


    @Override
    public void appendHoverText(ItemStack pStack, IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        initParams();

        if(DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(
                    new TranslationTextComponent("tooltip.nc.description.efficiency", TextUtils.numberFormat(def.getEfficiency())),
                    TextFormatting.AQUA));
            list.add(TextUtils.applyFormat(
                    new TranslationTextComponent("tooltip.nc.description.expansion", TextUtils.numberFormat(def.getExpansion())),
                    TextFormatting.GOLD));
        } else {
            list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.nc.blade.desc", TURBINE_CONFIG.BLADE_FLOW.get()), TextFormatting.BLUE));
        }
        list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.toggle_description_keys"), TextFormatting.GRAY));
    }
}
