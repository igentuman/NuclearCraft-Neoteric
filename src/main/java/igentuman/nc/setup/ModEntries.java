package igentuman.nc.setup;

import igentuman.nc.recipe.bomb.NcBlastRecipes;
import igentuman.nc.registration.FluidDefinition;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.recipe.fusion.FusionRecipes;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.ModEntryBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMetalOreMaterial;
import static igentuman.nc.setup.entries.Accelerator.accelerator;
import static igentuman.nc.setup.entries.Blocks.blocks;
import static igentuman.nc.setup.entries.Bomb.bomb;
import static igentuman.nc.setup.entries.FissionFuel.fissionFuel;
import static igentuman.nc.setup.entries.Energy.energy;
import static igentuman.nc.setup.entries.FissionReactor.fissionReactor;
import static igentuman.nc.setup.entries.Fluids.fluids;
import static igentuman.nc.setup.entries.FusionReactor.fusionReactor;
import static igentuman.nc.setup.entries.Isotopes.isotopes;
import static igentuman.nc.setup.entries.Kugelblitz.kugelblitz;
import static igentuman.nc.setup.entries.Materials.materials;
import static igentuman.nc.setup.entries.ParticleChamber.particleChamber;
import static igentuman.nc.setup.entries.Parts.*;
import static igentuman.nc.setup.entries.Processors.processors;
import static igentuman.nc.setup.entries.Storage.storage;

/** Central registry of all mod content entries with the init entry-point and declaration helpers. */
public class ModEntries {
    public static final HashMap<String, ModEntry> ENTRIES = new HashMap<>();
    public static final HashMap<String, IsotopeEntry> ISOTOPES = new HashMap<>();
    public static final LinkedHashMap<String, FissionFuelEntry> FISSION_FUEL = new LinkedHashMap<>();
    public static final LinkedHashMap<String, HeatSinkEntry> HEAT_SINKS = new LinkedHashMap<>();
    public static List<String> HS_SCHEDULE = new ArrayList<>();
    public static BlockBehaviour.Properties COMMON_BLOCK_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops();
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    public static void init() {
        materials();
        isotopes();
        fluids();
        fissionFuel();
        parts();
        records();
        blocks();
        tools();
        armor();
        processors();
        fissionReactor();
        accelerator();
        FusionRecipes.init();
        fusionReactor();
        particleChamber();
        kugelblitz();
        storage();
        energy();
        bomb();
        FissionRecipes.init();
        NcBlastRecipes.init();
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

    public static void coolant(String name, int color) {
        mat(name, color).fluid(FluidDefinition.liquid().setName(name)).build();
    }

    public static void liquid(String name, int color, int temperature) {
        mat(name, color).fluid(FluidDefinition.liquid().setName(name).setTemperature(temperature)).build();
    }

    public static FluidDefinition molten(int temperature) {
        return FluidDefinition.metal().setTemperature(temperature);
    }

    public static ModEntry get(String name) {
        return ENTRIES.getOrDefault(name, null);
    }

    /** Resolves the source {@link net.minecraft.world.level.material.Fluid} registered for a material
     *  by its entry name ({@code water} maps to vanilla water). Null when the material has no fluid. */
    public static net.minecraft.world.level.material.Fluid fluidOf(String name) {
        if (name.equals("water")) return net.minecraft.world.level.material.Fluids.WATER;
        ModEntry e = get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasFluid())
                ? e.materialEntry().materialFluid().source().get() : null;
    }

    public static boolean isEnabled(String name) {
        ModEntry entry = get(name);
        return entry == null || entry.isEnabled();
    }
}
