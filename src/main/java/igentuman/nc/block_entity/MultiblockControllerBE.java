package igentuman.nc.block_entity;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

/**
 * Controller block entity for a multiblock. Bridges block lifecycle to {@link MultiblockHandler}
 * and persists the cache (which owns the structure position set) so re-validation is skipped on world reload.
 */
public class MultiblockControllerBE extends GlobalBlockEntity implements MenuProvider {

    /** Buffered cache NBT read in {@link #loadAdditional} before the instance is built in {@link #onLoad}. */
    private CompoundTag pendingCacheNbt;
    private HolderLookup.Provider pendingRegistries;

    @NBTField(syncToClient = true)
    public boolean formed = false;

    public MultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String multiblockName) {
        super(type, pos, state, multiblockName);
    }

    public String getMultiblockName() {
        return name;
    }

    /** Called from the controller block on first placement (server side). */
    public void onControllerPlaced(ServerLevel level) {
        MultiblockEntry entry = MultiblockRegistry.getByController(name);
        if (entry == null) return;
        MultiblockHandler.initMultiblock(level, worldPosition, facing(), entry);
        setChanged();
    }

    /** Called from the controller block on removal. */
    public void onControllerRemoved(ServerLevel level) {
        MultiblockHandler.destroyMultiblock(level, worldPosition);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        MultiblockHandler.submitTick(serverLevel, worldPosition);
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, worldPosition);
        boolean newFormed = instance != null && instance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (formed) {
            recipeInfo.tick();
        }
        if (recipeInfo.changed || wasChanged) {
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();

    }

    private Direction facing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (level instanceof ServerLevel serverLevel) {
            MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, worldPosition);
            if (instance != null && instance.formed) {
                CompoundTag cacheTag = new CompoundTag();
                instance.cache.saveNbt(cacheTag, registries);
                tag.put("cache", cacheTag);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("cache")) {
            pendingCacheNbt = tag.getCompound("cache");
            pendingRegistries = registries;
        } else {
            pendingCacheNbt = null;
            pendingRegistries = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && name != null) {
            MultiblockEntry entry = MultiblockRegistry.getByController(name);
            if (entry != null) {
                MultiblockHandler.restoreMultiblock(serverLevel, worldPosition, facing(), entry, pendingCacheNbt, pendingRegistries);
            }
            pendingCacheNbt = null;
            pendingRegistries = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MultiblockControllerContainer(containerId, playerInventory, this, containerData);
    }
}
