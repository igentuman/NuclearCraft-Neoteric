package igentuman.nc.entity.bomb.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public sealed interface BlastOp {

    record SetBlock(BlockPos pos, BlockState state) implements BlastOp {}

    record VoidSection(int sx, int sy, int sz) implements BlastOp {}

    record DamageEntity(UUID entityId, float amount) implements BlastOp {}

    record RadiationDeposit(ChunkPos chunk, double distanceFactor) implements BlastOp {}
}
