package igentuman.nc.block.turbine.entity;

import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineBearingKineticBE extends GeneratingKineticBlockEntity {

    private float targetRPM = 0;
    private float addedStressCapacity = 0;

    private static final int STALE_TICKS = 5;
    private int ticksSinceUpdate = 0;

    public TurbineBearingKineticBE(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public TurbineBearingKineticBE(BlockPos pPos, BlockState pBlockState) {
        super(TURBINE_BE.get("turbine_bearing").get(), pPos, pBlockState);
    }

    public void setRPM(float v) {
        ticksSinceUpdate = 0;
        if (v == targetRPM) return;
        targetRPM = v;
        updateGeneratedRotation();
    }

    public void setStressCapacity(float v) {
        ticksSinceUpdate = 0;
        if (v == addedStressCapacity) return;
        addedStressCapacity = v;
        updateGeneratedRotation();
    }

    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        super.updateFromNetwork(maxStress, currentStress, networkSize);
    }

    public void onSpeedChanged(float previousSpeed) {
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        super.onSpeedChanged(previousSpeed);
    }

    public void setNetwork(@Nullable Long networkIn) {
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        super.setNetwork(networkIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (++ticksSinceUpdate > STALE_TICKS && (targetRPM != 0 || addedStressCapacity != 0)) {
            targetRPM = 0;
            addedStressCapacity = 0;
            updateGeneratedRotation();
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        lastCapacityProvided = addedStressCapacity;
        return addedStressCapacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (level == null || !getBlockState().hasProperty(BlockStateProperties.AXIS)) {
            return 0;
        }
        Direction.Axis axis = getBlockState().getValue(BlockStateProperties.AXIS);
        return convertToDirection(targetRPM, Direction.get(Direction.AxisDirection.NEGATIVE, axis));
    }
}
