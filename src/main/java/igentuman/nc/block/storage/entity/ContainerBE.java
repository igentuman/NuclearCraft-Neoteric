package igentuman.nc.block.storage.entity;

import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.content.storage.ContainerBlocks;
import igentuman.nc.handler.storage.ContainerInventoryStore;
import igentuman.nc.handler.storage.StoredInventory;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BE;

public class ContainerBE extends NuclearCraftBE implements SideModeToggleable {

    public final UuidBackedItemHandler inventory;
    protected final LazyOptional<IItemHandlerModifiable> itemHandler;

    private double loadRate = 0;

    private UUID uuid;
    private CompoundTag legacyInventory;
    private boolean prepared = false;

    private UuidBackedItemHandler createInventory() {
        return new UuidBackedItemHandler(getCapacity(), () -> uuid);
    }

    public LazyOptional<IItemHandlerModifiable> getItemHandler() {
        return itemHandler;
    }

    public static final ModelProperty<HashMap<Integer, SideMode>> SIDE_CONFIG = new ModelProperty<>();

    public ContainerBE(BlockPos pPos, BlockState pBlockState) {
        super(STORAGE_BE.get(getName(pBlockState)).get(), pPos, pBlockState);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), SideMode.DEFAULT);
        }
        inventory = createInventory();
        itemHandler = LazyOptional.of(() -> inventory);
    }

    public int getCapacity() {
        return ContainerBlocks.all().get(getName()).getCapacity();
    }

    @Nonnull
    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(SIDE_CONFIG, sideConfig)
                .build();
    }

    public void tickClient() {

    }

    public void tickServer() {
        prepareIfNeeded();
        transferItems();
        updateLoadRate();
    }

    private void prepareIfNeeded() {
        if (prepared) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (uuid == null) uuid = UUID.randomUUID();
        if (legacyInventory != null) {
            ContainerInventoryStore store = ContainerInventoryStore.get(serverLevel.getServer());
            if (!store.has(uuid)) {
                StoredInventory inv = store.getOrCreate(uuid, getCapacity());
                inv.read(legacyInventory);
                store.markDirtyOnly();
            }
            legacyInventory = null;
        }
        prepared = true;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void updateLoadRate() {
        double wasRate = loadRate;
        loadRate = 0.05;
        for(int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;
            loadRate += (stack.getCount()/(double)stack.getMaxStackSize())/inventory.getSlots();
        }
        if(wasRate != loadRate) {
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    private void transferItems() {
        for (Direction direction : Direction.values()) {
            if (sideConfig.get(direction.ordinal()) == SideMode.DISABLED) continue;
            if (level == null) continue;
            BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
            if(be == null) continue;
            if (be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).isPresent()) {
                be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(cap -> {
                    boolean transactionDone = false;
                    switch (sideConfig.get(direction.ordinal())) {
                        case OUT -> {
                            for(int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if(stack.isEmpty()) continue;
                                ItemStack copy = stack.copy();
                                for(int j = 0; j < cap.getSlots(); j++) {
                                    ItemStack left = cap.insertItem(j, copy, true);
                                    if(left.getCount() < copy.getCount()) {
                                        cap.insertItem(j, copy, false);
                                        inventory.extractItem(i, copy.getCount()-left.getCount(), false);
                                        transactionDone = true;
                                        break;
                                    }
                                }
                                if(transactionDone) break;
                            }
                        }
                        case IN -> {
                            for(int i = 0; i < cap.getSlots(); i++) {
                                ItemStack stack = cap.extractItem(i, 64, true);
                                if(stack.isEmpty()) continue;
                                ItemStack copy = stack.copy();
                                for(int j = 0; j < inventory.getSlots(); j++) {
                                    ItemStack left = inventory.insertItem(j, copy, true);
                                    if(left.getCount() < copy.getCount()) {
                                        inventory.insertItem(j, copy, false);
                                        cap.extractItem(i, copy.getCount()-left.getCount(), false);
                                        transactionDone = true;
                                        break;
                                    }
                                }
                                if(transactionDone) break;
                            }
                        }
                    }
                });
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && (side != null && sideConfig.get(side.ordinal()) != SideMode.DISABLED)) {
            return getItemHandler().cast();
        }
        return super.getCapability(cap, side);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID assignUuidIfAbsent() {
        if (uuid == null) uuid = UUID.randomUUID();
        return uuid;
    }

    /** Imports a legacy {@code "Inventory"} tag (from a placed item) into the store under this block's UUID. */
    public void migrateLegacyInventory(CompoundTag invTag) {
        if (!(level instanceof ServerLevel serverLevel)) {
            legacyInventory = invTag;
            return;
        }
        if (uuid == null) uuid = UUID.randomUUID();
        ContainerInventoryStore store = ContainerInventoryStore.get(serverLevel.getServer());
        if (!store.has(uuid)) {
            StoredInventory inv = store.getOrCreate(uuid, getCapacity());
            inv.read(invTag);
            store.markDirtyOnly();
        }
    }

    protected void saveClientData(CompoundTag tag) {
        if (uuid != null) tag.putUUID("uuid", uuid);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public void loadClientData(CompoundTag tag) {
        if (tag.hasUUID("uuid")) uuid = tag.getUUID("uuid");
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("uuid")) uuid = tag.getUUID("uuid");
        if (tag.contains("Inventory")) legacyInventory = tag.getCompound("Inventory");
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    private void loadSideConfig(int[] tagData) {
        boolean changed = false;
        for (int i = 0; i < sideConfig.size(); i++) {
            SideMode newMode = SideMode.values()[tagData[i]];
            if(sideConfig.get(i) != newMode) {
                changed = true;
                sideConfig.remove(i);
                sideConfig.put(i, newMode);
            }
        }
        if(changed) {
            setChanged();
            //requestModelDataUpdate();
            if(level == null) return;
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (uuid != null) tag.putUUID("uuid", uuid);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public SideModeToggleable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        return sideConfig.get(direction);
    }

    public String getTier() {
        return getName();
    }

    public int getRows() {
        return ContainerBlocks.all().get(getName()).getRows();
    }

    public int getColls() {
        return ContainerBlocks.all().get(getName()).getColls();
    }

    public double getLoadRate() {
        return loadRate;
    }
}
