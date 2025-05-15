package igentuman.nc.block.accelerator;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import javax.annotation.Nullable;
import java.util.List;

public class AcceleratorOrientedBlock extends AcceleratorBlock {
    public static final DirectionProperty HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public AcceleratorOrientedBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(POWERED, false)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING).add(BlockStateProperties.POWERED);
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        //list.add(__("tooltip.kugelblitz.block_" + pStack.getItem()).withStyle(ChatFormatting.AQUA));
        //list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
