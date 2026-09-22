package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.IBeamPort;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.WrenchUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.__;

/** Block entity block for a multiblock port; opens the port menu, exposes a comparator signal and cycles redstone mode. */
public class MultiblockPartBlock extends BaseEntityBlock {

    public static MapCodec<MultiblockPartBlock> CODEC;
    protected final String name;
    protected final Supplier<BlockEntityType<? extends MultiblockPortBE>> beTypeSupplier;

    public MultiblockPartBlock(BlockBehaviour.Properties props, String name,
                                Supplier<BlockEntityType<? extends MultiblockPortBE>> beTypeSupplier) {
        super(props);
        this.name = name;
        this.beTypeSupplier = beTypeSupplier;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        MultiblockHandler.trackBlockChange(level, pos, oldState, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        MultiblockHandler.trackBlockChange(level, pos, state, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (CODEC == null) {
            CODEC = simpleCodec(props -> new MultiblockPartBlock(props, name, beTypeSupplier));
        }
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beTypeSupplier.get().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof MultiblockPortBE partBE) partBE.serverTick();
        };
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return 0;
        return level.getBlockEntity(pos) instanceof MultiblockPortBE port ? port.getComparatorOutput() : 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (WrenchUtil.isWrench(stack) && level.getBlockEntity(pos) instanceof IBeamPort port) {
            if (!level.isClientSide) {
                BeamPortMode mode = player.isShiftKeyDown()
                        ? port.cycleBeamPortMode(line -> player.sendSystemMessage(Component.literal(line)))
                        : port.cycleBeamPortMode();
                player.sendSystemMessage(__("message.nuclearcraft.beam_port_mode",
                        __("message.nuclearcraft.beam_port_mode." + mode.getSerializedName())));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
                ModEntry entry = ModEntries.get(partBE.name);
                if (entry != null && entry.menu() != null) {
                    serverPlayer.openMenu(partBE, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
