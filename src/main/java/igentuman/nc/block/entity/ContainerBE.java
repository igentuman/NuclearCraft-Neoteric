package igentuman.nc.block.entity;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.handler.ItemStorageCapabilityHandler;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BE;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class ContainerBE extends NuclearCraftBE implements ISizeToggable {

    public final ItemStorageCapabilityHandler inventory;

    public ContainerBE(String name) {
        super(STORAGE_BE.get(name).get(), BlockPos.ZERO, null);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), SideMode.DEFAULT);
        }
        inventory = createInventory();
        itemHandler = LazyOptional.of(() -> inventory);

    }

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

/*    @Nonnull
    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(SIDE_CONFIG, sideConfig)
                .build();
    }*/

    public void tickClient() {

    }
    public void tickServer() {
        transferItems();
    }

    private void transferItems() {
        for (Direction direction : Direction.values()) {
            if (sideConfig.get(direction.ordinal()) == SideMode.DISABLED) continue;
            if (level == null) continue;
            TileEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if(be == null) continue;
            if (be.getCapability(ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
                be.getCapability(ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(cap -> {
                    boolean transactionDone = false;
                    switch (sideConfig.get(direction.ordinal())) {
                        case OUT:
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
                        break;
                        case IN:
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
                        break;
                    }
                });
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HANDLER_CAPABILITY && (side != null && sideConfig.get(side.ordinal()) != SideMode.DISABLED)) {
            return getItemHandler().cast();
        }
        return super.getCapability(cap, side);
    }


    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    @Override
    public @NotNull CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundNBT tag) {
        CompoundNBT tank = new CompoundNBT();
        tag.put("Inventory", inventory.serializeNBT());
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public void loadClientData(CompoundNBT tag) {
        if(tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
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
         //   level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void saveAdditional(CompoundNBT tag) {
        tag.put("Inventory", inventory.serializeNBT());
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }


    public ISizeToggable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
     //   level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
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
