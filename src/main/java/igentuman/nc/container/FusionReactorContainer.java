package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.handler.fluid.FluidStackHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Container for the fusion reactor core. Adds typed accessors over the controller's fluid tanks
 * and plasma/heat state so the screen can render dedicated bars without reaching into the BE.
 *
 * <p>Tank layout (see {@code FusionReaction}): 0/1 fuel A/B, 2 coolant (in), 3..6 products (out),
 * 7 hot coolant (out).
 */
public class FusionReactorContainer extends MultiblockControllerContainer {

    public static final int TANK_FUEL_A = 0;
    public static final int TANK_FUEL_B = 1;
    public static final int TANK_COOLANT = 2;
    public static final int TANK_PRODUCT_FIRST = 3;
    public static final int PRODUCT_COUNT = 4;
    public static final int TANK_HOT_COOLANT = 7;

    public FusionReactorContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        super(containerId, playerInventory, extraData);
    }

    public FusionReactorContainer(int containerId, Inventory playerInventory,
                                  MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, playerInventory, blockEntity, data);
    }

    public FusionReactorControllerBE fusion() {
        return getBlockEntity() instanceof FusionReactorControllerBE be ? be : null;
    }

    private FluidStackHandler tanks() {
        FusionReactorControllerBE be = fusion();
        return be != null ? be.fluidTanks() : null;
    }

    public FluidStack fluid(int tank) {
        FluidStackHandler t = tanks();
        return (t != null && tank < t.getTanks()) ? t.getFluidInTank(tank) : FluidStack.EMPTY;
    }

    public int capacity(int tank) {
        FluidStackHandler t = tanks();
        return (t != null && tank < t.getTanks()) ? t.getTankCapacity(tank) : 0;
    }

    public double reactorHeat() {
        FusionReactorControllerBE be = fusion();
        return be != null ? be.reactorHeat : 0;
    }

    public double maxHeat() {
        FusionReactorControllerBE be = fusion();
        return be != null ? be.maxHeat : 0;
    }

    @Override
    protected void layoutPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 27 + col * 18, 105 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 27 + col * 18, 163));
        }
    }

    public double plasmaTemperature() {
        FusionReactorControllerBE be = fusion();
        return be != null ? be.plasmaTemperature : 0;
    }

    public double maxPlasmaTemperature() {
        FusionReactorControllerBE be = fusion();
        return be != null ? be.maxPlasmaTemperature : 0;
    }
}
