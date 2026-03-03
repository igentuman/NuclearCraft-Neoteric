package igentuman.nc.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class ModTeleporter {

    private final BlockPos targetPos;

    public ModTeleporter(BlockPos pos) {
        this.targetPos = pos;
    }

    /**
     * Creates a DimensionTransition for the given entity to teleport to the destination level.
     * Finds a suitable surface position and places the entity there.
     */
    public DimensionTransition createTransition(Entity entity, ServerLevel destination) {
        BlockPos surfacePos = findSurface(destination, new BlockPos(targetPos.getX(), 256, targetPos.getZ()));

        Vec3 pos = new Vec3(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5);

        return new DimensionTransition(
                destination,
                pos,
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                DimensionTransition.DO_NOTHING
        );
    }

    /**
     * Finds a suitable surface position by moving down from the starting position
     * until a solid block is found.
     */
    private BlockPos findSurface(ServerLevel world, BlockPos startPos) {
        BlockPos pos = startPos;

        while (pos.getY() > 0) {
            boolean currentIsAir = world.isEmptyBlock(pos);
            boolean belowIsSolid = !world.isEmptyBlock(pos.below());

            if (currentIsAir && belowIsSolid) {
                return pos;
            }
            pos = pos.below();
        }

        return new BlockPos(startPos.getX(), 70, startPos.getZ());
    }
}
