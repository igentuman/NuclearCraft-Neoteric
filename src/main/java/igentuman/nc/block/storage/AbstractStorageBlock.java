package igentuman.nc.block.storage;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.SideMode;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.WrenchUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractStorageBlock extends BaseEntityBlock {

    protected final String name;
    private final MapCodec<? extends BaseEntityBlock> codec = simpleCodec(props -> create(props));

    protected AbstractStorageBlock(Properties properties, String name) {
        super(properties);
        this.name = name;
    }

    protected abstract AbstractStorageBlock create(Properties properties);

    public String storageName() {
        return name;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof AbstractStorageBE sbe ? sbe.getComparatorSignal() : 0;
    }

    protected boolean tryWrench(ItemStack stack, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!WrenchUtil.isWrench(stack)) return false;
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractStorageBE sbe) {
                Direction dir = hit.getDirection();
                if (player.isShiftKeyDown()) dir = dir.getOpposite();
                SideMode mode = sbe.toggleSideConfig(dir);
                player.sendSystemMessage(Component.translatable("message.nuclearcraft.switch_side.mode", mode.name()));
            }
        }
        return true;
    }

    protected boolean persistOnDrop(AbstractStorageBE be) {
        return false;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            ItemStack drop = new ItemStack(this);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractStorageBE sbe && persistOnDrop(sbe)) {
                CompoundTag data = be.saveWithoutMetadata(level.registryAccess());
                BlockItem.setBlockEntityData(drop, be.getType(), data);
            }
            popResource(level, pos, drop);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof GlobalBlockEntity gbe) gbe.clientTick();
            };
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof GlobalBlockEntity gbe) gbe.serverTick();
        };
    }
}
