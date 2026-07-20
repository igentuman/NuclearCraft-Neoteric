package igentuman.nc.block.storage;

import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.ContainerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ContainerBlock extends AbstractStorageBlock {

    public ContainerBlock(Properties properties, String name) {
        super(properties, name);
    }

    @Override
    protected AbstractStorageBlock create(Properties properties) {
        return new ContainerBlock(properties, name);
    }

    @Override
    protected boolean persistOnDrop(AbstractStorageBE be) {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (tryWrench(stack, level, pos, player, hit)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        open(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;
        if (level.getBlockEntity(pos) instanceof ContainerBE be) {
            be.assignUuidIfAbsent();
            serverPlayer.openMenu(be, buf -> {
                buf.writeBlockPos(pos);
                buf.writeUUID(be.getUuid());
            });
        }
    }
}
