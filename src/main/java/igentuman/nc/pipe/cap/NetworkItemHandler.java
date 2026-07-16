package igentuman.nc.pipe.cap;

import igentuman.nc.block.pipe.entity.PipeConnectorBE;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class NetworkItemHandler extends NetworkHandler implements IItemHandler {

    public NetworkItemHandler(PipeConnectorBE connector) {
        super(connector);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || blocked()) {
            return stack;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return stack;
        }
        ItemStack remaining = stack;
        for (long packed : net.getDestinations(PipeCapabilityType.ITEM, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE dest = manager.getConnectorBE(packed);
            if (dest == null) {
                continue;
            }
            BlockPos dPos = dest.getBlockPos();
            for (Direction face : Direction.values()) {
                BlockEntity nbe = dest.getExternalNeighbor(face);
                if (nbe == null) {
                    continue;
                }
                IItemHandler h = nbe.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) {
                    continue;
                }
                remaining = ItemHandlerHelper.insertItem(h, remaining, simulate);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remaining;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || blocked()) {
            return ItemStack.EMPTY;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return ItemStack.EMPTY;
        }
        for (long packed : net.getSources(PipeCapabilityType.ITEM, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE src = manager.getConnectorBE(packed);
            if (src == null) {
                continue;
            }
            BlockPos sPos = src.getBlockPos();
            for (Direction face : Direction.values()) {
                BlockEntity nbe = src.getExternalNeighbor(face);
                if (nbe == null) {
                    continue;
                }
                IItemHandler h = nbe.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) {
                    continue;
                }
                for (int s = 0; s < h.getSlots(); s++) {
                    ItemStack got = h.extractItem(s, amount, simulate);
                    if (!got.isEmpty()) {
                        return got;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
