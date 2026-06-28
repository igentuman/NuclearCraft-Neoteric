package igentuman.nc.setup;

import igentuman.nc.registration.FluidDefinition;
import igentuman.nc.registration.FuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.ModEntryBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMetalOreMaterial;
import static igentuman.nc.setup.entries.Blocks.blocks;
import static igentuman.nc.setup.entries.FissionFuel.fissionFuel;
import static igentuman.nc.setup.entries.Fluids.fluids;
import static igentuman.nc.setup.entries.Isotopes.isotopes;
import static igentuman.nc.setup.entries.Materials.materials;
import static igentuman.nc.setup.entries.Parts.*;

public class ModEntries {
    public static final HashMap<String, ModEntry> ENTRIES = new HashMap<>();
    public static final HashMap<String, IsotopeEntry> ISOTOPES = new HashMap<>();
    public static final LinkedHashMap<String, FuelEntry> FUELS = new LinkedHashMap<>();
    public static BlockBehaviour.Properties COMMON_BLOCK_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops();
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    public static void init() {
        materials();
        isotopes();
        fluids();
        fissionFuel();
        parts();
        blocks();
        tools();
        armor();
    }

    public static void deco(String name, BlockBehaviour.Properties props) {
        add(name).block(() -> new Block(props)).build();
    }

    public static ModEntryBuilder mat(String name, int color) {
        return add(name).material(color);
    }

    public static void oreMetal(String name, int color) {
        addMetalOreMaterial(name, color).build();
    }

    public static void blockOnly(String name) {
        mat(name, DEFAULT_COLOR).storageBlock().build();
    }

    public static void alloy(String name, int color) {
        mat(name, color).ingot().dust().nugget().plate().storageBlock().fluid().build();
    }

    public static void ingotPlateDustFluid(String name, int color) {
        mat(name, color).ingot().plate().dust().fluid().build();
    }

    public static void ingotDustFluid(String name, int color) {
        mat(name, color).ingot().dust().fluid().build();
    }

    public static void ingotOnly(String name, int color) {
        mat(name, color).ingot().build();
    }

    public static void dustIngot(String name) {
        mat(name, DEFAULT_COLOR).dust().ingot().build();
    }

    public static void dustOnly(String name) {
        mat(name, DEFAULT_COLOR).dust().build();
    }

    public static void dustGem(String name) {
        mat(name, DEFAULT_COLOR).dust().gem().build();
    }

    public static void dustFluid(String name, int color, int temperature) {
        mat(name, color).dust().fluid(molten(temperature)).build();
    }

    public static void acid(String name, int color) {
        mat(name, color).fluid(FluidDefinition.acid().setName(name)).build();
    }

    public static void gas(String name, int color, int temperature) {
        mat(name, color).fluid(FluidDefinition.gas(temperature).setName(name)).build();
    }

    public static FluidDefinition molten(int temperature) {
        return FluidDefinition.metal().setTemperature(temperature);
    }

    public static ModEntry get(String name) {
        return ENTRIES.getOrDefault(name, null);
    }

    public static boolean isEnabled(String name) {
        ModEntry entry = get(name);
        return entry == null || entry.isEnabled();
    }
}
