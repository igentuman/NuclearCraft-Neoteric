package igentuman.nc.util.capability;

import igentuman.nc.handler.storage.UuidBackedItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes an {@link net.minecraftforge.items.IItemHandler} for a container item. The handler owns no
 * stacks and stores nothing in item NBT — contents live in the UUID-keyed level store (server) or the
 * client cache. Item NBT keeps only {@code uuid}, {@code dim} (+ {@code magnet}).
 */
public class ItemCapabilityProvider implements ICapabilityProvider {

    private final LazyOptional<IItemHandler> inventoryOptional;

    public ItemCapabilityProvider(ItemStack stack, int inventorySize) {
        UuidBackedItemHandler handler = new UuidBackedItemHandler(stack, inventorySize);
        this.inventoryOptional = LazyOptional.of(() -> handler);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryOptional.cast();
        }
        return LazyOptional.empty();
    }
}
