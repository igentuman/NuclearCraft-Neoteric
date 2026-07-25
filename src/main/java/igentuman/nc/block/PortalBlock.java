package igentuman.nc.block;

import igentuman.nc.setup.level.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.5, 1);

    public PortalBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(8.0F, 3600.0F)
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayer player && !player.isPassenger()) {
            teleport(player, pos);
        }
    }

    private void teleport(ServerPlayer player, BlockPos pos) {
        ServerLevel current = (ServerLevel) player.level();
        MinecraftServer server = current.getServer();
        ResourceKey<Level> target = current.dimension() == ModDimensions.WASTELAND ? Level.OVERWORLD : ModDimensions.WASTELAND;
        ServerLevel destination = server.getLevel(target);
        if (destination == null) return;
        BlockPos ref = pos.north();
        BlockPos surface = findSurface(destination, new BlockPos(ref.getX(), 256, ref.getZ()));
        Vec3 landing = new Vec3(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5);
        player.changeDimension(new DimensionTransition(destination, landing, Vec3.ZERO, player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
    }

    private static BlockPos findSurface(ServerLevel level, BlockPos start) {
        BlockPos pos = start;
        while (pos.getY() > level.getMinBuildHeight()) {
            if (level.isEmptyBlock(pos) && !level.isEmptyBlock(pos.below())) {
                return pos;
            }
            pos = pos.below();
        }
        return new BlockPos(start.getX(), 70, start.getZ());
    }
}
