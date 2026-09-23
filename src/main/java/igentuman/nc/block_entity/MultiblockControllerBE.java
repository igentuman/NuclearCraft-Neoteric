package igentuman.nc.block_entity;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.multiblock.StructureRole;
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

import java.util.List;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

/** Base controller block entity for a multiblock; bridges block lifecycle to {@link MultiblockHandler} and persists its cache. */
public class MultiblockControllerBE extends GlobalBlockEntity implements MenuProvider {

    private CompoundTag pendingCacheNbt;
    private CompoundTag pendingRuntimeNbt;
    private HolderLookup.Provider pendingRegistries;
    protected MultiblockHandler.MultiblockInstance mbInstance;
    @NBTField(syncToClient = true)
    public boolean formed = false;

    public MultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String multiblockName) {
        super(type, pos, state, multiblockName);
    }

    public String getMultiblockName() {
        return name;
    }

    @Nullable
    protected MultiblockHandler.MultiblockInstance instance() {
        if (mbInstance == null && level instanceof ServerLevel serverLevel) {
            mbInstance = MultiblockHandler.getInstance(serverLevel, worldPosition);
        }
        return mbInstance;
    }

    /** Whether this controller currently owns an accepted structure; ports read capabilities through it. */
    public boolean structureFormed() {
        return formed;
    }

    /** Positions this controller assigned to a structural role, in channel order. */
    public List<BlockPos> rolePositions(StructureRole role) {
        return List.of();
    }

    @Nullable
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        return null;
    }

    /** Called from the controller block on first placement (server side). */
    public void onControllerPlaced(ServerLevel level) {
        MultiblockEntry entry = MultiblockRegistry.getByController(name);
        if (entry == null) return;
        mbInstance = MultiblockHandler.initMultiblock(level, worldPosition, facing(), entry);
        setChanged();
    }

    /** Called from the controller block on removal. */
    public void onControllerRemoved(ServerLevel level) {
        MultiblockHandler.destroyMultiblock(level, worldPosition);
    }

    public void tickMultiblock(ServerLevel level) {
        MultiblockHandler.submitTick(level, mbInstance, worldPosition);
        if (mbInstance == null) {
            mbInstance = MultiblockHandler.getInstance(level, worldPosition);
        }
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed && !mbInstance.dirty;
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
            if (instance != null) {
                CompoundTag cacheTag = new CompoundTag();
                instance.cache.saveNbt(cacheTag, registries);
                tag.put("cache", cacheTag);
                CompoundTag runtimeTag = new CompoundTag();
                instance.logic.saveRuntime(runtimeTag, registries);
                if (!runtimeTag.isEmpty()) tag.put("runtime", runtimeTag);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pendingRuntimeNbt = tag.contains("runtime") ? tag.getCompound("runtime")
                : tag.contains("particleRuntime") ? tag.getCompound("particleRuntime") : null;
        if (tag.contains("cache")) {
            pendingCacheNbt = tag.getCompound("cache");
            pendingRegistries = registries;
        } else {
            pendingCacheNbt = null;
            pendingRegistries = pendingRuntimeNbt == null ? null : registries;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && name != null) {
            MultiblockEntry entry = MultiblockRegistry.getByController(name);
            if (entry != null) {
                if (pendingCacheNbt != null) {
                    mbInstance = MultiblockHandler.restoreMultiblock(serverLevel, worldPosition, facing(), entry, pendingCacheNbt, pendingRegistries);
                } else {
                    mbInstance = MultiblockHandler.initMultiblock(serverLevel, worldPosition, facing(), entry);
                }
                if (mbInstance != null && pendingRuntimeNbt != null) {
                    mbInstance.logic.loadRuntime(pendingRuntimeNbt, pendingRegistries);
                }
            }
            pendingCacheNbt = null;
            pendingRuntimeNbt = null;
            pendingRegistries = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveClientTagData(tag);
        tag.put("ContentHandler", contentHandler.serializeNBT(registries));
        if (energyStorage != null) {
            tag.put("Energy", energyStorage.serializeNBT(registries));
        }
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
