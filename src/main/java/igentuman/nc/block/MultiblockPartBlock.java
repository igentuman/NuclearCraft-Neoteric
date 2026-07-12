package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

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
                serverPlayer.openMenu(partBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
