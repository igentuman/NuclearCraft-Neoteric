package igentuman.nc.block.entity;

import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NCProcessor extends BlockEntity {
    public NCProcessor(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public NCProcessor(BlockPos pPos, BlockState pBlockState) {
        super(NCProcessors.PROCESSORS_BE.get("test").get(), pPos, pBlockState);
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

}
