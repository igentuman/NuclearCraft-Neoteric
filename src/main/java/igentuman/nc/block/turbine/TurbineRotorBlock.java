package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineBE;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineRotorBlock extends DirectionalBlock {

    public TurbineRotorBlock(AbstractBlock.Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).noOcclusion());
    }

/*    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        World level = context.getLevel();
        BlockState neighbor = level.getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if(!neighbor.isAir() && neighbor.getBlock() instanceof TurbineRotorBlock) {
            return this.defaultBlockState().setValue(FACING, neighbor.getValue(FACING));
        }
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }*/
    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this);
    }

    private String codeID()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }

/*    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TURBINE_BE.get("turbine_rotor_shaft").get().create(pPos, pState);
    }*/

/*    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        ((TurbineBE) Objects.requireNonNull(level.getBlockEntity(pos))).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<TextComponent> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.nc.rotor_shaft.desc"), TextFormatting.BLUE));
    }*/
}
