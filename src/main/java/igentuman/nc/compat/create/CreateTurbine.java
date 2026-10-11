package igentuman.nc.compat.create;

import igentuman.nc.registration.ModEntryBuilder;
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
                .block(n -> new KineticTurbineBearingBlock(ModEntryBuilder.defaultMultiblockProps()))
                .blockEntity(TurbineBearingKineticBE::new)
                .build();
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
