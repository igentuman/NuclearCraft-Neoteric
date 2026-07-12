package igentuman.nc.block.fusion;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

/** A single cell of the fusion core's 3x3x3 cage. Renders invisible - the controller's BER draws
 *  the whole animated core - while keeping the port BE wiring (caps proxy, redstone, comparator). */
public class FusionCoreProxyBlock extends MultiblockPartBlock {

    public static MapCodec<FusionCoreProxyBlock> CODEC;

    public FusionCoreProxyBlock(BlockBehaviour.Properties props, String name,
                                Supplier<BlockEntityType<? extends MultiblockPortBE>> beTypeSupplier) {
        super(props, name, beTypeSupplier);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (CODEC == null) {
            CODEC = simpleCodec(props -> new FusionCoreProxyBlock(props, name, beTypeSupplier));
        }
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultiblockPortBE port && player.isShiftKeyDown() && port.supportsRedstone()) {
                int mode = port.cycleRedstoneMode();
                String[] keys = port.redstoneModes();
                String key = mode >= 0 && mode < keys.length ? keys[mode] : "none";
                serverPlayer.displayClientMessage(Component.translatable("message.nuclearcraft.redstone_mode",
                        Component.translatable("message.nuclearcraft.redstone_mode." + key)), true);
            } else if (be instanceof MultiblockPortBE partBE) {
                assert partBE.controllerPos != null;
                BlockState bs = level.getBlockState(partBE.controllerPos);
                BlockHitResult hs = new BlockHitResult(partBE.controllerPos.getCenter(), hitResult.getDirection(), partBE.controllerPos, true);
                return bs.useWithoutItem(level, player, hs);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Destroying any cage cell (by any means) tears down the whole core: remove the controller, which
     *  cascades into {@code removeProxyCage} to clear the remaining cells. The controller drops so the
     *  player recovers the core block. Reentrancy is self-limiting: once the controller is gone, the
     *  other cells' removal finds no controller and does nothing. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MultiblockPortBE port && port.controllerPos != null
                    && level.getBlockEntity(port.controllerPos) instanceof MultiblockControllerBE) {
                level.destroyBlock(port.controllerPos, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
