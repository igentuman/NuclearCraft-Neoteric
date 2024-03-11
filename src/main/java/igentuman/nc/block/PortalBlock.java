package igentuman.nc.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class PortalBlock extends Block {

    private static final VoxelShape SHAPE = VoxelShapes.box(0, 0, 0, 1, .8, 1);

    public PortalBlock() {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(-1.0F, 3600000.0F)
                );
    }
/*
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            *//*if (level.dimension().equals(Dimensions.WASTELAND)) {
                teleportTo(player, pos.north(), Level.OVERWORLD);
            } else {
                teleportTo(player, pos.north(), Dimensions.WASTELAND);
            }*//*
        }
    }

    private void teleportTo(ServerPlayer player, BlockPos pos, ResourceKey<World> id) {
        ServerWorld world = player.getServer().getLevel(id);
    }*/
}