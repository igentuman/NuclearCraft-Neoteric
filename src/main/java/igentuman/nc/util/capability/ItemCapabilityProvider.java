package igentuman.nc.util.capability;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCSerialization;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class ItemCapabilityProvider {

    private final ItemStack stack;
    private final ItemInventoryHandler inventoryHandler;

    public ItemCapabilityProvider(ItemStack stack, int inventorySize, int stackSize) {
        this.stack = stack;
        this.inventoryHandler = new ItemInventoryHandler(inventorySize, stackSize) {
            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                super.setStackInSlot(slot, stack);
                saveToNBT();
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                ItemStack result = super.insertItem(slot, stack, simulate);
                if (!simulate) {
                    saveToNBT();
                }
                return result;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack result = super.extractItem(slot, amount, simulate);
                if (!simulate && !result.isEmpty()) {
                    saveToNBT();
                }
                return result;
            }
        };
        loadFromNBT();
    }

    public IItemHandler getItemHandler() {
        return inventoryHandler;
    }

    private HolderLookup.Provider registries() {
        return ServerLifecycleHooks.getCurrentServer().registryAccess();
    }

    private void loadFromNBT() {
        CompoundTag tag = NCItemStacks.getTagCopy(stack);
        if (tag.contains("Inventory")) {
            NCSerialization.deserialize(inventoryHandler, registries(), tag.getCompound("Inventory"));
        }
    }

    public void saveToNBT() {
        CompoundTag inventoryNBT = NCSerialization.serialize(inventoryHandler, registries());
        NCItemStacks.modifyTag(stack, tag -> tag.put("Inventory", inventoryNBT));
    }

    public ItemInventoryHandler getInventoryHandler() {
        return inventoryHandler;
    }

    public void forceSave() {
        saveToNBT();
    }
}
