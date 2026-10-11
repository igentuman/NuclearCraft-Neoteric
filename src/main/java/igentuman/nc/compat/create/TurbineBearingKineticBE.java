package igentuman.nc.compat.create;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TurbineBearingKineticBE extends GeneratingKineticBlockEntity {

    private static final int STALE_TICKS = 5;

    private float targetSpeed;
    private float stressCapacity;
    private int ticksSinceUpdate;

    public TurbineBearingKineticBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state);
    }

    public void drive(float speed, float capacity) {
        ticksSinceUpdate = 0;
        if (speed == targetSpeed && capacity == stressCapacity) return;
        targetSpeed = speed;
        stressCapacity = capacity;
        updateGeneratedRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (++ticksSinceUpdate > STALE_TICKS && (targetSpeed != 0 || stressCapacity != 0)) {
            targetSpeed = 0;
            stressCapacity = 0;
            updateGeneratedRotation();
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        lastCapacityProvided = stressCapacity;
        return stressCapacity;
    }

    @Override
    public float getGeneratedSpeed() {
        BlockState state = getBlockState();
        if (!state.hasProperty(BlockStateProperties.AXIS)) return 0;
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        return convertToDirection(targetSpeed, Direction.get(Direction.AxisDirection.NEGATIVE, axis));
    }
}
