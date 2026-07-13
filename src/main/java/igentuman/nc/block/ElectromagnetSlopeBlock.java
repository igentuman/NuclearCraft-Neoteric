package igentuman.nc.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.feature.jigsaw.JigsawOrientation;

import javax.annotation.Nullable;


public class ElectromagnetSlopeBlock extends ElectromagnetBlock {

    public static final EnumProperty<JigsawOrientation> ORIENTATION = BlockStateProperties.ORIENTATION;

    public ElectromagnetSlopeBlock(Properties pProperties) {
        super(Properties.of(Material.METAL)
                .noOcclusion()
                .strength(3f)
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, JigsawOrientation.NORTH_UP));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext pContext) {
        Direction face = pContext.getClickedFace();
        Direction front = face.getAxis() == Direction.Axis.Y ? face : verticalLookDirection(pContext);
        Direction top = front == Direction.DOWN
                ? pContext.getHorizontalDirection()
                : pContext.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(ORIENTATION, JigsawOrientation.fromFrontAndTop(front, top));
    }

    private static Direction verticalLookDirection(BlockItemUseContext pContext) {
        for (Direction dir : pContext.getNearestLookingDirections()) {
            if (dir.getAxis() == Direction.Axis.Y) {
                return dir.getOpposite();
            }
        }
        return Direction.UP;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        JigsawOrientation orientation = pState.getValue(ORIENTATION);
        return pState.setValue(ORIENTATION, JigsawOrientation.fromFrontAndTop(pRotation.rotate(orientation.front()), pRotation.rotate(orientation.top())));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        JigsawOrientation orientation = pState.getValue(ORIENTATION);
        return pState.setValue(ORIENTATION, JigsawOrientation.fromFrontAndTop(pMirror.mirror(orientation.front()), pMirror.mirror(orientation.top())));
    }

    public static Direction getFrontFacing(BlockState pState) {
        return pState.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState pState) {
        return pState.getValue(ORIENTATION).top();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ORIENTATION);
    }
}
