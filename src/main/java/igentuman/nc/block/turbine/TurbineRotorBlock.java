package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineRotorBlock extends DirectionalBlock {

    public static final DirectionProperty BLOCK_FACING = FACING;

    public TurbineRotorBlock(AbstractBlock.Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(BLOCK_FACING, Direction.NORTH)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(BLOCK_FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TURBINE_BE.get("turbine_rotor_shaft").get().create();
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this);
    }

    private String codeID()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }


    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void appendHoverText(ItemStack pStack, IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.nc.rotor_shaft.desc"), TextFormatting.BLUE));
    }
}
