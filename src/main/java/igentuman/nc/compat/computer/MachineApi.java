package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.setup.Registers;
import li.cil.oc2.api.bus.device.object.Callback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class MachineApi<T extends GlobalBlockEntity> extends ComputerApi<T> {

    protected MachineApi(T be, ComputerErrors errors) {
        super(be, errors);
    }

    @Override
    public String typeName() {
        return be.name;
    }

    @LuaFunction
    @Callback
    public final String getName() {
        return be.name;
    }

    @LuaFunction
    @Callback
    public final int getProgress() {
        return be.progress;
    }

    @LuaFunction
    @Callback
    public final int getMaxProgress() {
        return be.maxProgress;
    }

    @LuaFunction
    @Callback
    public final boolean isProcessing() {
        return be.recipeInfo.recipe != null;
    }

    @LuaFunction
    @Callback
    public final int getEnergyPerTick() {
        return energyPerTick();
    }

    protected int energyPerTick() {
        return be.recipeInfo.energyPerTick;
    }

    @LuaFunction
    @Callback
    public final @Nullable Map<String, Object> getEnergy() {
        if (!be.hasEnergyStorage()) return null;
        Map<String, Object> result = new HashMap<>();
        result.put("stored", be.energyStorage.getEnergyStored());
        result.put("capacity", be.energyStorage.getMaxEnergyStored());
        return result;
    }

    @LuaFunction
    @Callback
    public final Map<String, Object> list() {
        Map<Integer, Map<String, Object>> items = new HashMap<>();
        if (be.hasInventory()) {
            ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) items.put(i + 1, itemInfo(stack));
            }
        }
        Map<Integer, Map<String, Object>> fluids = new HashMap<>();
        if (be.hasFluidTanks()) {
            FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < handler.getTanks(); i++) {
                if (!handler.getFluidInTank(i).isEmpty()) fluids.put(i + 1, tankInfo(handler, i));
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("fluids", fluids);
        return result;
    }

    @LuaFunction
    @Callback
    public final @Nullable Map<String, Object> getItem(int slot) {
        if (!be.hasInventory()) return null;
        ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
        if (slot < 1 || slot > handler.getSlots()) return null;
        ItemStack stack = handler.getStackInSlot(slot - 1);
        if (stack.isEmpty()) return null;
        Map<String, Object> item = itemInfo(stack);
        item.put("maxCount", stack.getMaxStackSize());
        return item;
    }

    @LuaFunction
    @Callback
    public final Map<Integer, Map<String, Object>> tanks() {
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        if (!be.hasFluidTanks()) return result;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        for (int i = 0; i < handler.getTanks(); i++) {
            result.put(i + 1, tankInfo(handler, i));
        }
        return result;
    }

    @LuaFunction
    @Callback
    public final @Nullable Map<String, Object> getTank(int index) {
        if (!be.hasFluidTanks()) return null;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        if (index < 1 || index > handler.getTanks()) return null;
        return tankInfo(handler, index - 1);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final boolean voidSlot(int slot) {
        if (!be.hasInventory()) return false;
        ItemCapabilityHandler handler = be.contentHandler.getItemHandler();
        if (slot < 1 || slot > handler.getSlots()) return false;
        handler.setStackInSlot(slot - 1, ItemStack.EMPTY);
        be.setChanged();
        return true;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final boolean voidTank(int index) {
        if (!be.hasFluidTanks()) return false;
        FluidCapabilityHandler handler = be.contentHandler.getFluidHandler();
        if (index < 1 || index > handler.getTanks()) return false;
        handler.getInternalHandler().voidTank(index - 1);
        be.setChanged();
        return true;
    }

    private static Map<String, Object> itemInfo(ItemStack stack) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        item.put("count", stack.getCount());
        return item;
    }

    private static Map<String, Object> tankInfo(FluidCapabilityHandler handler, int tank) {
        FluidStack fluid = handler.getFluidInTank(tank);
        Map<String, Object> info = new HashMap<>();
        info.put("capacity", handler.getTankCapacity(tank));
        info.put("name", fluid.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
        info.put("amount", fluid.getAmount());
        return info;
    }

    @Nullable
    protected static Map<String, Object> particleInfo(int registryId, long amount, long energyKeV, double focus) {
        ParticleDefinition definition = registryId < 0 ? null : Registers.PARTICLE_DEFINITION_REGISTRY.byId(registryId);
        ResourceLocation id = definition == null ? null : Registers.PARTICLE_DEFINITION_REGISTRY.getKey(definition);
        if (id == null || amount <= 0) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("particle", id.toString());
        info.put("amount", amount);
        info.put("energy", energyKeV);
        info.put("focus", focus);
        return info;
    }
}
