package igentuman.nc.block.entity;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.handler.ItemStorageCapabilityHandler;
import igentuman.nc.setup.storage.ContainerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BE;

public class ContainerBE extends NuclearCraftBE implements ISizeToggable {

    public final ItemStorageCapabilityHandler inventory;

    private ItemStorageCapabilityHandler createInventory() {
        return new ItemStorageCapabilityHandler(ContainerBlocks.all().get(getName()).getCapacity(), 64);
    }

    public LazyOptional<ItemStorageCapabilityHandler> getItemHandler() {
        return itemHandler;
    }

    protected final LazyOptional<ItemStorageCapabilityHandler> itemHandler;

    public static final ModelProperty<HashMap<Integer, SideMode>> SIDE_CONFIG = new ModelProperty<>();

    public ContainerBE(BlockPos pPos, BlockState pBlockState) {
        super(STORAGE_BE.get(getName(pBlockState)).get(), pPos, pBlockState);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), SideMode.DEFAULT);
        }
        inventory = createInventory();
        itemHandler = LazyOptional.of(() -> inventory);
    }


    public HashMap<Integer, SideMode> sideConfig = new HashMap<>();

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
        transferItems();
    }

    private void transferItems() {
        for (Direction direction : Direction.values()) {
            if (sideConfig.get(direction.ordinal()) == SideMode.DISABLED) continue;
            if (level == null) continue;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
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
                                ItemStack stack = cap.getStackInSlot(i);
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundTag tag) {
        CompoundTag tank = new CompoundTag();
        tag.put("Inventory", inventory.serializeNBT());
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public void loadClientData(CompoundTag tag) {
        if(tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        if(!tag.contains("sideConfig")) return;
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
            requestModelDataUpdate();
            if(level == null) return;
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public ISizeToggable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
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
}
