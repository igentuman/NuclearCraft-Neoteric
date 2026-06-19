package igentuman.nc.block.turbine;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class TurbineBearingKineticBlock extends TurbineBearingBlock implements IRotate {

    public TurbineBearingKineticBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(BlockStateProperties.AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(BlockStateProperties.AXIS);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
        if(!level.isClientSide()) {
            MultiblockHandler.get(level.dimension()).trackBlockChange(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void onPlace(BlockState pState, Level level, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, level, pPos, pOldState, pMovedByPiston);
        if(!level.isClientSide()) {
            MultiblockHandler.get(level.dimension()).trackBlockChange(pPos);
        }
    }
}
