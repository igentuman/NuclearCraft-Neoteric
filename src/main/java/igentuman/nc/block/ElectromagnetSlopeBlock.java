package igentuman.nc.block;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;


public class ElectromagnetSlopeBlock extends ElectromagnetBlock {
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    public ElectromagnetSlopeBlock(Properties pProperties) {
        super(Properties.of(Material.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(ORIENTATION, pRotation.rotation().rotate(pState.getValue(ORIENTATION)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.setValue(ORIENTATION, pMirror.rotation().rotate(pState.getValue(ORIENTATION)));
    }

    public static Direction getFrontFacing(BlockState pState) {
        return pState.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState pState) {
        return pState.getValue(ORIENTATION).top();
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace();
        Direction direction1;
        if (direction.getAxis() == Direction.Axis.Y) {
            if(direction.equals(Direction.DOWN))
                direction1 = pContext.getHorizontalDirection();
            else
                direction1 = pContext.getHorizontalDirection().getOpposite();
        } else {
            direction = pContext.getNearestLookingVerticalDirection().getOpposite();

            if(direction.equals(Direction.DOWN))
                direction1 = pContext.getHorizontalDirection();
            else
                direction1 = pContext.getHorizontalDirection()
                        .getOpposite();
        }

            return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction1));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ORIENTATION);
    }
}
