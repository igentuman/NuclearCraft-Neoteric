package igentuman.nc.handler.fluid;

import igentuman.nc.testing.MinecraftTestBootstrap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidStackHandlerTest {

    private static HolderLookup.Provider registries;
    private static FMLLoader loader;

    @BeforeAll
    static void bootstrap() throws ReflectiveOperationException {
        loader = MinecraftTestBootstrap.open();
        Fluids.WATER.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        Fluids.LAVA.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @AfterAll
    static void closeLoader() {
        if (loader != null) loader.close();
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
    void currentEmptyTanksKeepConfiguredCapacityAndValidators() {
        FluidStackHandler handler = new FluidStackHandler(2, 100_000);
        handler.setTankValidator(0, stack -> stack.getFluid() == Fluids.WATER);
        handler.setFluidInTank(1, new FluidStack(Fluids.WATER, 500));
        CompoundTag saved = new FluidStackHandler(2, 100_000).serializeNBT(registries);

        handler.deserializeNBT(registries, saved);

        assertEquals(100_000, handler.getTankCapacity(0));
        assertEquals(100_000, handler.getTankCapacity(1));
        assertTrue(handler.getFluidInTank(1).isEmpty());
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.LAVA, 1000)));
        try (var transaction = Transaction.openRoot()) {
            assertEquals(50_000, handler.insert(0, FluidResource.of(Fluids.WATER), 50_000, transaction));
            transaction.commit();
        }
    }

    @Test
    void fixedCapacitiesRetainConfiguredValuesWithoutLosingFluid() {
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
    void malformedCurrentFluidIsRejectedWithoutClearingContents() {
        FluidStackHandler handler = new FluidStackHandler(1, 100_000);
        handler.setFluidInTank(0, new FluidStack(Fluids.WATER, 2000));
        CompoundTag invalid = handler.serializeNBT(registries);
        invalid.getListOrEmpty("Tanks").getCompoundOrEmpty(0).putString("Fluid", "broken");

        assertThrows(IllegalArgumentException.class, () -> handler.deserializeNBT(registries, invalid));
        assertEquals(2000, handler.getFluidInTank(0).getAmount());
    }

    @Test
    void resizingPreservesConfiguredCapacitiesAndValidators() {
        FluidStackHandler handler = new FluidStackHandler(1, 100_000);
        handler.setTankValidator(0, stack -> false);
        CompoundTag resized = new FluidStackHandler(2, 100_000).serializeNBT(registries);

        handler.deserializeNBT(registries, resized);

        assertEquals(2, handler.getTanks());
        assertEquals(100_000, handler.getTankCapacity(0));
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.WATER, 1000)));
    }
}
