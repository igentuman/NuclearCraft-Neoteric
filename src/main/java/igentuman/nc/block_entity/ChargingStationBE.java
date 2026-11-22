package igentuman.nc.block_entity;

import igentuman.nc.handler.energy.ItemEnergyTransfer;
import igentuman.nc.handler.fluid.ContainerFluidHandler;
import igentuman.nc.setup.Registers;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.item.HEVItem;
import igentuman.nc.item.Q36Item;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class ChargingStationBE extends GlobalBlockEntity implements MenuProvider {

    public static final int CHARGE_PER_TICK = 500_000;
    public static final int FLUID_PER_TICK = 100;
    public static final int Q36_QC_PER_FLUID = 50;
    public static final int Q36_FLUID_PER_TICK = 20;

    @NBTField(syncToClient = true)
    public int redstoneMode = 0;

    private boolean validatorsInstalled = false;

    public ChargingStationBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drops();
    }

    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % 2;
        setChanged();
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        installSlotValidators();
        if (redstoneMode == 1 && level != null && !level.hasNeighborSignal(worldPosition)) return;
        charge();
        contentHandler.tick();
        if (wasChanged) {
            setChanged();
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            wasChanged = false;
        }
    }

    @Override
    public void clientTick() {
        installSlotValidators();
    }

    private void installSlotValidators() {
        if (validatorsInstalled || level == null) return;
        if (contentHandler.hasItemCapability()) {
            ItemCapabilityHandler handler = contentHandler.getItemHandler();
            handler.setSlotValidator(0, this::isValidInput);
            handler.setSideInsertLocked(false);
        }
        validatorsInstalled = true;
    }

    private boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof Q36Item) return true;
        if (stack.getItem() instanceof HEVItem) return true;
        if (ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM) != null) return true;
        var fluidCap = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        var quantite = ModEntries.fluidOf("quantite_energy");
        if (fluidCap != null && quantite != null) {
            var resource = FluidResource.of(quantite);
            for (int tank = 0; tank < fluidCap.size(); tank++) {
                var stored = fluidCap.getResource(tank);
                if ((stored.isEmpty() || stored.getFluid() == quantite)
                        && fluidCap.isValid(tank, resource) && fluidCap.getCapacityAsLong(tank, resource) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void charge() {
        ItemCapabilityHandler items = contentHandler.getItemHandler();
        if (items == null) return;
        ItemStack stack = items.getStackInSlot(0);
        if (stack.isEmpty()) return;

        FluidCapabilityHandler tanks = contentHandler.getFluidHandler();
        boolean hasFluid = tanks != null && tanks.getFluidInTank(0).getAmount() > 0;
        boolean hasEnergy = energyStorage != null && energyStorage.getEnergyStored() > 0;
        if (!hasFluid && !hasEnergy) return;

        if (stack.getItem() instanceof Q36Item) {
            chargeQuantite(tanks, false);
            return;
        }

        if (stack.getItem() instanceof HEVItem) {
            chargeQuantite(tanks, true);
            chargeEnergy();
            return;
        }

        var access = ItemAccess.forHandlerIndexStrict(items, 0);
        var itemFluid = access.getCapability(Capabilities.Fluid.ITEM);
        if (itemFluid != null && tanks != null) {
            try (var transaction = Transaction.openRoot()) {
                if (ResourceHandlerUtil.moveFirst(new ContainerFluidHandler(tanks, 0), itemFluid,
                        resource -> true, FLUID_PER_TICK, transaction) != null) {
                    transaction.commit();
                }
            }
            return;
        }

        chargeEnergy();
    }

    private void chargeEnergy() {
        if (energyStorage == null || energyStorage.getAmountAsLong() <= 0) return;
        ItemCapabilityHandler items = contentHandler.getItemHandler();
        var access = ItemAccess.forHandlerIndexStrict(items, 0);
        ItemEnergyTransfer.charge(energyStorage, access, CHARGE_PER_TICK, null);
    }

    private void chargeQuantite(@Nullable FluidCapabilityHandler tanks, boolean hev) {
        if (tanks == null) return;
        var quantite = ModEntries.fluidOf("quantite_energy");
        var fluid = tanks.getInternalHandler().getResource(0);
        if (quantite == null || fluid.isEmpty() || fluid.getFluid() != quantite) return;
        var access = ItemAccess.forHandlerIndexStrict(contentHandler.getItemHandler(), 0);
        var resource = access.getResource();
        int count = access.getAmount();
        if (count <= 0) return;
        int current = hev ? HEVItem.getQeCharge(resource.toStack()) : Q36Item.getCharge(resource.toStack());
        int capacity = hev ? HEVItem.MAX_QE_CHARGE : Q36Item.MAX_CHARGE;
        int missing = capacity - current;
        if (missing <= 0) return;
        long totalMissing = (long) missing * count;
        int needed = (int) Math.min(Q36_FLUID_PER_TICK,
                (totalMissing + Q36_QC_PER_FLUID - 1) / Q36_QC_PER_FLUID);
        int fluidToUse = Math.min(needed, tanks.getInternalHandler().getAmountAsInt(0));
        int chargePerItem = (int) Math.min(missing, (long) fluidToUse * Q36_QC_PER_FLUID / count);
        if (chargePerItem <= 0) return;
        // Round up the fluid cost for the actual per-item charge, including a final partial mB.
        fluidToUse = (int) (((long) chargePerItem * count + Q36_QC_PER_FLUID - 1) / Q36_QC_PER_FLUID);
        var charged = resource.toStack();
        if (hev) {
            HEVItem.setQeCharge(charged, current + chargePerItem);
        } else {
            Q36Item.setCharge(charged, current + chargePerItem);
        }
        try (var transaction = Transaction.openRoot()) {
            if (tanks.extract(0, fluid, fluidToUse, transaction) != fluidToUse) return;
            if (access.exchange(ItemResource.of(charged), count, transaction) != count) return;
            transaction.commit();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new igentuman.nc.container.UniversalProcessorContainer(containerId, playerInventory, this, containerData);
    }
}
