package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.handler.fluid.FluidStackHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.neoforged.neoforge.fluids.FluidStack;

public class MsrControllerContainer extends MultiblockControllerContainer {

    public static final int TANK_COLD = 0;
    public static final int TANK_HOT = 1;

    public MsrControllerContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        super(containerId, playerInventory, extraData);
    }

    public MsrControllerContainer(int containerId, Inventory playerInventory,
                                  MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, playerInventory, blockEntity, data);
    }

    public MsrControllerBE msr() {
        return getBlockEntity() instanceof MsrControllerBE be ? be : null;
    }

    private FluidStackHandler tanks() {
        MsrControllerBE be = msr();
        return be != null ? be.fluidTanks() : null;
    }

    private int synced(String field) {
        int idx = getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? getSyncedValue(idx) : 0;
    }

    public FluidStack fluid(int tank) {
        FluidStackHandler t = tanks();
        return (t != null && tank < t.getTanks()) ? t.getFluidInTank(tank) : FluidStack.EMPTY;
    }

    public int capacity(int tank) {
        FluidStackHandler t = tanks();
        return (t != null && tank < t.getTanks()) ? t.getTankCapacity(tank) : 0;
    }

    public double getReactivity() {
        return synced("reactivitySync") / 100.0;
    }

    public double getTemperature() {
        return synced("temperatureSync");
    }

    public double getDepletion() {
        return synced("depletionSync") / 100.0;
    }

    public int getFuelCellsCount() {
        return synced("fuelCellsCountSync");
    }

    public int getPebblesQty() {
        return synced("pebbleCountSync");
    }

    public int getInputRate() {
        return synced("saltInputRate");
    }

    public int getOutputRate() {
        return synced("saltOutputRate");
    }

    public int getOverheatTimer() {
        return synced("overheatTimerSync");
    }

    public boolean isCritical() {
        return synced("isCritical") != 0;
    }

    public boolean isPowered() {
        return synced("powered") != 0;
    }

    public int getSalt() {
        return fluid(TANK_COLD).getAmount();
    }

    public int getHotSalt() {
        return fluid(TANK_HOT).getAmount();
    }
}
