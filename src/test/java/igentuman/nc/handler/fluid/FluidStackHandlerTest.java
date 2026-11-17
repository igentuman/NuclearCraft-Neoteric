package igentuman.nc.handler.fluid;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FluidStackHandlerTest {

    private static HolderLookup.Provider registries;

    @BeforeAll
    static void bootstrap() {
        LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void roundTripPreservesEmptyAndFilledTankCapacities() {
        FluidStackHandler original = new FluidStackHandler(2, new int[]{100_000, 200_000});
        original.setFluidInTank(1, new FluidStack(Fluids.WATER, 12_000));

        FluidStackHandler restored = new FluidStackHandler(2, 1000);
        restored.deserializeNBT(registries, original.serializeNBT(registries));

        assertEquals(100_000, restored.getTankCapacity(0));
        assertEquals(200_000, restored.getTankCapacity(1));
        assertTrue(restored.getFluidInTank(0).isEmpty());
        assertEquals(12_000, restored.getFluidInTank(1).getAmount());
    }

    @Test
    void legacyEmptyTanksKeepConfiguredCapacityAndValidators() {
        FluidStackHandler handler = new FluidStackHandler(2, 100_000);
        handler.setTankValidator(0, stack -> stack.getFluid() == Fluids.WATER);
        handler.setFluidInTank(1, new FluidStack(Fluids.WATER, 500));
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("Size", 2);
        legacy.put("Tanks", new ListTag());

        handler.deserializeNBT(registries, legacy);

        assertEquals(100_000, handler.getTankCapacity(0));
        assertEquals(100_000, handler.getTankCapacity(1));
        assertTrue(handler.getFluidInTank(1).isEmpty());
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.LAVA, 1000)));
        assertEquals(50_000, handler.fillTank(0, new FluidStack(Fluids.WATER, 50_000), FluidAction.EXECUTE));
    }

    @Test
    void fixedCapacitiesRepairAlreadyAffectedTanksWithoutLosingFluid() {
        FluidStackHandler affected = new FluidStackHandler(2, 1000);
        affected.setFluidInTank(0, new FluidStack(Fluids.WATER, 1000));
        affected.setFluidInTank(1, new FluidStack(Fluids.LAVA, 500));
        FluidStackHandler restored = new FluidStackHandler(2, new int[]{100_000, 1_000_000});
        restored.setFixedCapacities();

        restored.deserializeNBT(registries, affected.serializeNBT(registries));

        assertEquals(100_000, restored.getTankCapacity(0));
        assertEquals(1_000_000, restored.getTankCapacity(1));
        assertEquals(1000, restored.getFluidInTank(0).getAmount());
        assertEquals(500, restored.getFluidInTank(1).getAmount());
    }

    @Test
    void emptyUpdateClearsOldFluidAndRestoresDynamicCapacity() {
        FluidStackHandler server = new FluidStackHandler(1, 250_000);
        FluidStackHandler client = new FluidStackHandler(1, 100_000);
        client.setFluidInTank(0, new FluidStack(Fluids.WATER, 1000));

        client.deserializeNBT(registries, server.serializeNBT(registries));

        assertTrue(client.getFluidInTank(0).isEmpty());
        assertEquals(250_000, client.getTankCapacity(0));
    }

    @Test
    void legacyInlineFluidStillLoads() {
        CompoundTag tank = (CompoundTag) new FluidStack(Fluids.WATER, 2000).save(registries);
        tank.putInt("Tank", 0);
        ListTag tanks = new ListTag();
        tanks.add(tank);
        CompoundTag legacy = new CompoundTag();
        legacy.put("Tanks", tanks);
        FluidStackHandler handler = new FluidStackHandler(1, 100_000);

        handler.deserializeNBT(registries, legacy);

        assertEquals(100_000, handler.getTankCapacity(0));
        assertEquals(2000, handler.getFluidInTank(0).getAmount());
    }

    @Test
    void resizingPreservesConfiguredCapacitiesAndValidators() {
        FluidStackHandler handler = new FluidStackHandler(1, 100_000);
        handler.setTankValidator(0, stack -> false);
        CompoundTag resized = new CompoundTag();
        resized.putInt("Size", 2);
        resized.put("Tanks", new ListTag());

        handler.deserializeNBT(registries, resized);

        assertEquals(2, handler.getTanks());
        assertEquals(100_000, handler.getTankCapacity(0));
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.WATER, 1000)));
    }
}
