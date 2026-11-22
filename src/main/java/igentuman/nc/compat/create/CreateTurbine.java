package igentuman.nc.compat.create;

import igentuman.nc.registration.ModEntryBuilder;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.turbine.TurbineCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class CreateTurbine {

    private CreateTurbine() {
    }

    public static void registerBearing(String name) {
        ModEntryBuilder.add(name)
                .block(KineticTurbineBearingBlock::new, ModEntryBuilder::defaultMultiblockProps)
                .blockEntity(TurbineBearingKineticBE::new)
                .build();
    }

    /** Drives both bearings; reconnect from TurbineLogic when the integration is restored. */
    public static void driveBearings(ServerLevel level, TurbineCache tc, float rotationSpeed, double maxFlow, double realFlow) {
        Direction.Axis axis = tc.axis;
        if (axis == null) return;
        double coilsDrag = tc.coilsEfficiency > 0
                ? Math.max(1, 100.0 / tc.coilsEfficiency * Math.log(Math.log10(tc.activeCoils + 4) + 2))
                : 1;
        double divisor = Math.max(1, Math.log(Math.max(1, tc.activeCoils) / coilsDrag) + 1);
        float speed = (float) Math.min(256, rotationSpeed * 5000 / divisor * Multiblocks.turbineKineticConversion);
        float capacity = (float) ((Math.log1p(maxFlow) + Math.log1p(realFlow)) * 5);
        for (BlockPos bearing : new BlockPos[]{tc.bearingPos1, tc.bearingPos2}) {
            if (bearing != null) driveBearing(level, bearing, axis, speed, capacity);
        }
    }

    public static void driveBearing(ServerLevel level, BlockPos pos, Direction.Axis axis, float speed, float capacity) {
        if (!level.isLoaded(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.AXIS) && state.getValue(BlockStateProperties.AXIS) != axis) {
            level.setBlock(pos, state.setValue(BlockStateProperties.AXIS, axis), Block.UPDATE_CLIENTS);
        }
        if (level.getBlockEntity(pos) instanceof TurbineBearingKineticBE bearing) {
            bearing.drive(speed, capacity);
        }
    }
}
