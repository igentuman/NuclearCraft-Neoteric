package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** CC:Tweaked peripheral exposing a multiblock controller's state, inventory and tanks to Lua. */
public class ControllerPerihperal implements IPeripheral {

    private final MultiblockControllerBE be;

    public ControllerPerihperal(MultiblockControllerBE be) {
        this.be = be;
    }

    @Override
    public String getType() {
        return be.name;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ControllerPerihperal p && p.be == be;
    }

    @Override
    public void attach(IComputerAccess computer) {}

    @Override
    public void detach(IComputerAccess computer) {}

    @LuaFunction
    public final String getName() {
        return be.name;
    }

    @LuaFunction
    public final String getMultiblockName() {
        return be.getMultiblockName();
    }

    @LuaFunction
    public final boolean isFormed() {
        return be.formed;
    }

    @LuaFunction
    public final int getStructureSize() {
        if (!(be.getLevel() instanceof ServerLevel serverLevel)) return 0;
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, be.getBlockPos());
        if (instance == null || !instance.formed) return 0;
        return instance.cache.getStructurePositions().size();
    }

    @LuaFunction
    public final int getProgress() {
        return be.progress;
    }

    @LuaFunction
    public final int getMaxProgress() {
        return be.maxProgress;
    }

    @LuaFunction
    public final boolean isProcessing() {
        return be.recipeInfo.recipe != null;
    }

    @LuaFunction
    public final int getEnergyPerTick() {
        return be.recipeInfo.energyPerTick;
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getEnergy() {
        if (!be.hasEnergyStorage()) return null;
        Map<String, Object> result = new HashMap<>();
        result.put("stored", be.energyStorage.getEnergyStored());
        result.put("capacity", be.energyStorage.getMaxEnergyStored());
        return result;
    }

    /**
     * Returns all item slots as a 1-indexed table. Empty slots are omitted.
     * Each entry: {name, count}.
     */
    @LuaFunction
    public final Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Map<String, Object>> items = new HashMap<>();
        Map<Integer, Map<String, Object>> fluids = new HashMap<>();

        if (be.hasInventory()) {
            ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                    item.put("count", stack.getCount());
                    items.put(i + 1, item);
                }
            }
        }

        if (be.hasFluidTanks()) {
            FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (!fluid.isEmpty()) {
                    Map<String, Object> tank = new HashMap<>();
                    tank.put("name", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
                    tank.put("amount", fluid.getAmount());
                    tank.put("capacity", handler.getTankCapacity(i));
                    fluids.put(i + 1, tank);
                }
            }
        }

        result.put("items", items);
        result.put("fluids", fluids);
        return result;
    }

    /**
     * Returns item detail for a specific slot (1-indexed). Returns null if slot empty or out of range.
     */
    @LuaFunction
    public final @Nullable Map<String, Object> getItem(int slot) {
        if (!be.hasInventory()) return null;
        ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
        if (slot < 1 || slot > handler.getSlots()) return null;
        ItemStack stack = handler.getStackInSlot(slot - 1);
        if (stack.isEmpty()) return null;
        Map<String, Object> item = new HashMap<>();
        item.put("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        item.put("count", stack.getCount());
        item.put("maxCount", stack.getMaxStackSize());
        return item;
    }

    /**
     * Returns all fluid tanks as a 1-indexed table.
     * Each entry: {name, amount, capacity}. name is empty string if tank is empty.
     */
    @LuaFunction
    public final Map<Integer, Map<String, Object>> tanks() {
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        if (!be.hasFluidTanks()) return result;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            Map<String, Object> tank = new HashMap<>();
            tank.put("capacity", handler.getTankCapacity(i));
            if (!fluid.isEmpty()) {
                tank.put("name", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
                tank.put("amount", fluid.getAmount());
            } else {
                tank.put("name", "");
                tank.put("amount", 0);
            }
            result.put(i + 1, tank);
        }
        return result;
    }

    @LuaFunction
    public final boolean voidSlot(int slot) {
        if (!be.hasInventory()) return false;
        ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
        if (slot < 1 || slot > handler.getSlots()) return false;
        handler.setStackInSlot(slot - 1, ItemStack.EMPTY);
        return true;
    }

    @LuaFunction
    public final boolean voidTank(int index) {
        if (!be.hasFluidTanks()) return false;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        if (index < 1 || index > handler.getTanks()) return false;
        handler.getInternalHandler().voidTank(index - 1);
        return true;
    }

    /**
     * Returns fluid detail for a specific tank (1-indexed). Returns null if out of range.
     */
    @LuaFunction
    public final @Nullable Map<String, Object> getTank(int index) {
        if (!be.hasFluidTanks()) return null;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        if (index < 1 || index > handler.getTanks()) return null;
        FluidStack fluid = handler.getFluidInTank(index - 1);
        Map<String, Object> tank = new HashMap<>();
        tank.put("capacity", handler.getTankCapacity(index - 1));
        if (!fluid.isEmpty()) {
            tank.put("name", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            tank.put("amount", fluid.getAmount());
        } else {
            tank.put("name", "");
            tank.put("amount", 0);
        }
        return tank;
    }
}
