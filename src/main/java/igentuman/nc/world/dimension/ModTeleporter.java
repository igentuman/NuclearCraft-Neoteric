package igentuman.nc.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {

    public static BlockPos thisPos = BlockPos.ZERO;

    public ModTeleporter(BlockPos pos) {
        thisPos = pos;
    }
    
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos surfacePos = findSurface(destWorld, new BlockPos(thisPos.getX(), 256, thisPos.getZ()));
        
        return new PortalInfo(
            new Vec3(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5),
            Vec3.ZERO,
            entity.getYRot(),
            entity.getXRot()
        );
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        int y = 256;
        BlockPos destinationPos = new BlockPos(thisPos.getX(), y, thisPos.getZ());
        
        BlockPos surfacePos = findSurface(destinationWorld, destinationPos);
        
        entity.setPos(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5);
        
        entity = repositionEntity.apply(true);
        return entity;
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