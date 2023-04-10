package igentuman.nc.util;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class InventoryUtils {

    private InventoryUtils() {
    }


    /**
     * Like {@link ItemHandlerHelper#canItemStacksStack(ItemStack, ItemStack)} but empty stacks mean equal (either param). Thiakil: not sure why.
     *
     * @param toInsert stack a
     * @param inSlot   stack b
     *
     * @return true if they are compatible
     */
    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(inSlot, toInsert);
    }

    @Nullable
    public static IItemHandler assertItemHandler(String desc, BlockEntity tile, Direction side) {
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side).resolve();
        if (capability.isPresent()) {
            return capability.get();
        }
        NuclearCraft.LOGGER.warn("'{}' was wrapped around a non-IItemHandler inventory. This should not happen!", desc, new Exception());
        if (tile == null) {
            NuclearCraft.LOGGER.warn(" - null tile");
        } else {
            NuclearCraft.LOGGER.warn(" - details: {} {}", tile, tile.getBlockPos());
        }
        return null;
    }

    public static boolean isItemHandler(BlockEntity tile, Direction side) {
        return CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side).isPresent();
    }

}