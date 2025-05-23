package igentuman.nc.block;

import igentuman.nc.world.dimension.Dimensions;
import igentuman.nc.world.dimension.ModTeleporter;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import static igentuman.nc.world.dimension.Dimensions.WASTELAND_KEY;

public class PortalBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, .5, 1);

    public PortalBlock() {
        super(Properties.of()
                .sound(SoundType.METAL)
                .strength(-1.0F, 3600000.0F)
                .noLootTable());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if (entity instanceof ServerPlayer player) {
            teleportTo(player, pos.north());
        }
    }

    private void teleportTo(Entity player, BlockPos pPos) {
        if (player.level() instanceof ServerLevel serverlevel) {
            MinecraftServer minecraftserver = serverlevel.getServer();
            ResourceKey<Level> resourcekey = player.level().dimension() == Dimensions.WASTELAND ?
                    Level.OVERWORLD : Dimensions.WASTELAND;

            ServerLevel portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !player.isPassenger()) {
                if(resourcekey == Dimensions.WASTELAND) {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos));
                } else {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos));
                }
            }
        }
    }
}