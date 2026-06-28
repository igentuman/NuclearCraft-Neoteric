package igentuman.nc.datagen.recipe;

import igentuman.nc.registration.FuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.datagen.recipe.processors.AlloySmelterRecipes.alloySmelter;
import static igentuman.nc.datagen.recipe.processors.AssemblerRecipes.assembler;
import static igentuman.nc.datagen.recipe.processors.CentrifugeRecipes.centrifuge;
import static igentuman.nc.datagen.recipe.processors.ChemicalReactorRecipes.chemicalReactor;
import static igentuman.nc.datagen.recipe.processors.CrystallizerRecipes.crystallizer;
import static igentuman.nc.datagen.recipe.processors.DecayHastenerRecipes.decayHastener;
import static igentuman.nc.datagen.recipe.processors.ElectrolyzerRecipes.electrolyzer;
import static igentuman.nc.datagen.recipe.processors.FluidEnricherRecipes.fluidEnricher;
import static igentuman.nc.datagen.recipe.processors.FluidInfuserRecipes.fluidInfuser;
import static igentuman.nc.datagen.recipe.processors.FuelReprocessorRecipes.fuelReprocessor;
import static igentuman.nc.datagen.recipe.processors.IngotFormerRecipes.ingotFormer;
import static igentuman.nc.datagen.recipe.processors.IsotopeSeparatorRecipes.isotopeSeparator;
import static igentuman.nc.datagen.recipe.processors.ManufactoryRecipes.manufactory;
import static igentuman.nc.datagen.recipe.processors.MelterRecipes.melter;
import static igentuman.nc.datagen.recipe.processors.PressurizerRecipes.pressurizer;
import static igentuman.nc.datagen.recipe.processors.RockCrusherRecipes.rockCrusher;
import static igentuman.nc.datagen.recipe.processors.SteamTurbineRecipes.steamTurbine;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public static final int TIME = 200;
    public static final int ENERGY = 50;
    public static final int MOLTEN_INGOT = 90;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput recipeOutput) {
        manufactory(recipeOutput);
        alloySmelter(recipeOutput);
        pressurizer(recipeOutput);
        ingotFormer(recipeOutput);
        melter(recipeOutput);
        rockCrusher(recipeOutput);
        isotopeSeparator(recipeOutput);
        decayHastener(recipeOutput);
        fuelReprocessor(recipeOutput);
        assembler(recipeOutput);
        chemicalReactor(recipeOutput);
        crystallizer(recipeOutput);
        fluidEnricher(recipeOutput);
        fluidInfuser(recipeOutput);
        centrifuge(recipeOutput);
        electrolyzer(recipeOutput);
        steamTurbine(recipeOutput);
    }

    // --- record helpers ---

    public record I(Item item, int n) {}
    public record F(Fluid fluid, int n) {}

    public static final I[] NI = {};
    public static final F[] NF = {};

    public static I i(Item it, int n) { return it == null ? null : new I(it, n); }
    public static F f(Fluid fl, int n) { return fl == null ? null : new F(fl, n); }

    public static I iso(String name, int n) { return i(isotope(name), n); }
    public static I dst(String name, int n) { return i(dust(name), n); }
    public static I ing(String name, int n) { return i(ingot(name), n); }
    public static I gm(String name, int n)  { return i(gem(name), n); }
    public static I prt(String name, int n) { return i(part(name), n); }
    public static I plt(String name, int n) { return i(plate(name), n); }
    public static I van(Item it, int n)     { return i(it, n); }
    public static F fl(String name, int n)  { return f(fluidOf(name), n); }

    public static void rec(RecipeOutput out, String proc, String name, I[] ii, F[] fi, I[] io, F[] fo) {
        for (I x : ii) if (x == null) return;
        for (F x : fi) if (x == null) return;
        for (I x : io) if (x == null) return;
        for (F x : fo) if (x == null) return;
        UniversalProcessorRecipeBuilder b = UniversalProcessorRecipeBuilder.processor(proc);
        for (I x : ii) b.itemInput(x.item(), x.n());
        for (F x : fi) b.fluidInput(x.fluid(), x.n());
        for (I x : io) b.itemOutput(x.item(), x.n());
        for (F x : fo) b.fluidOutput(x.fluid(), x.n());
        b.processTime(TIME).energyPerTick(ENERGY).save(out, name);
    }

    public static void i2i(RecipeOutput out, String proc, String name, Item in, Item o, int on) {
        rec(out, proc, name, new I[]{i(in, 1)}, NF, new I[]{i(o, on)}, NF);
    }

    public static void sep(RecipeOutput out, String name, Item in, int inN, Item o1, int n1, Item o2, int n2) {
        if (o1 != null && o1 == o2) {
            rec(out, "isotope_separator", name, new I[]{i(in, inN)}, NF, new I[]{i(o1, n1 + n2)}, NF);
        } else {
            rec(out, "isotope_separator", name, new I[]{i(in, inN)}, NF, new I[]{i(o1, n1), i(o2, n2)}, NF);
        }
    }

    public static void crush(RecipeOutput out, String name, Item in, int inN, I... outs) {
        rec(out, "rock_crusher", name, new I[]{i(in, inN)}, NF, outs, NF);
    }

    public static void reproc(RecipeOutput out, String key, I... outs) {
        for (String t : new String[]{"", "_tr"}) {
            rec(out, "fuel_reprocessor", key.replace('/', '_').replace('-', '_') + t,
                    new I[]{i(depletedFuel(key, t), 1)}, NF, outs, NF);
        }
    }

    public static void assemble(RecipeOutput out, String name, Item o, int on, I... ins) {
        rec(out, "assembler", name, ins, NF, new I[]{i(o, on)}, NF);
    }

    public static void chem(RecipeOutput out, String name, Fluid a, int an, Fluid b, int bn, F... outs) {
        rec(out, "chemical_reactor", name, NI, new F[]{f(a, an), f(b, bn)}, NI, outs);
    }

    public static void crystal(RecipeOutput out, String name, Fluid fluid, int fn, I o) {
        rec(out, "crystallizer", name, NI, new F[]{f(fluid, fn)}, new I[]{o}, NF);
    }

    public static void enrich(RecipeOutput out, String name, Fluid fluid, int fn, I in, Fluid o, int on) {
        rec(out, "fluid_enricher", name, new I[]{in}, new F[]{f(fluid, fn)}, NI, new F[]{f(o, on)});
    }

    public static void infuse(RecipeOutput out, String name, Fluid fluid, int fn, Item in, int inN, Item o, int on) {
        rec(out, "fluid_infuser", name, new I[]{i(in, inN)}, new F[]{f(fluid, fn)}, new I[]{i(o, on)}, NF);
    }

    public static void split(RecipeOutput out, String proc, String name, Fluid in, int inN, F... outs) {
        rec(out, proc, name, NI, new F[]{f(in, inN)}, NI, outs);
    }

    public static void f2f(RecipeOutput out, String proc, String name, Fluid in, int inN, Fluid o, int on) {
        rec(out, proc, name, NI, new F[]{f(in, inN)}, NI, new F[]{f(o, on)});
    }

    public static void alloy(RecipeOutput out, String processor, String recipeName,
                       ItemLike output, int outCount, ItemLike a, int aCount, ItemLike b, int bCount) {
        if (output == null || a == null || b == null) return;
        UniversalProcessorRecipeBuilder.processor(processor)
                .itemInput(a, aCount)
                .itemInput(b, bCount)
                .itemOutput(output, outCount)
                .processTime(TIME).energyPerTick(ENERGY)
                .save(out, recipeName);
    }

    // --- legacy helper kept for ManufactoryRecipes ---

    public static void grind(RecipeOutput out, String processor, String recipeName, ItemLike input, ItemLike output, int outCount) {
        if (input == null || output == null) return;
        UniversalProcessorRecipeBuilder.processor(processor)
                .itemInput(input)
                .itemOutput(output, outCount)
                .processTime(TIME).energyPerTick(ENERGY)
                .save(out, recipeName);
    }

    // --- item/fluid lookup helpers ---

    public static Iterable<MaterialEntry> materials() {
        java.util.List<MaterialEntry> list = new java.util.ArrayList<>();
        for (ModEntry e : ModEntries.ENTRIES.values()) {
            if (e.materialEntry() != null) list.add(e.materialEntry());
        }
        return list;
    }

    public static Item dust(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasDust()) ? e.materialEntry().dust().get() : null;
    }

    public static Item ingot(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasIngot()) ? e.materialEntry().ingot().get() : null;
    }

    public static Item gem(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasGem()) ? e.materialEntry().gem().get() : null;
    }

    public static Item plate(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasPlate()) ? e.materialEntry().plate().get() : null;
    }

    public static Item part(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.hasItem()) ? e.item().get() : null;
    }

    public static Item oreItem(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasOre()) ? e.materialEntry().oreItem().get() : null;
    }

    public static Item blockItem(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasBlock()) ? e.materialEntry().storageItem().get() : null;
    }

    public static Item isotope(String name) {
        IsotopeEntry e = ModEntries.ISOTOPES.get(name);
        return e != null ? e.base().get() : null;
    }

    public static Item isotopeVar(IsotopeEntry e, String suffix) {
        var d = e.variants().get(suffix);
        return d != null ? d.get() : null;
    }

    public static Item fuel(String key, String variant) {
        FuelEntry e = ModEntries.FUELS.get(key);
        if (e == null) return null;
        var d = e.fuelItems().get(variant);
        return d != null ? d.get() : null;
    }

    public static Item depletedFuel(String key, String variant) {
        FuelEntry e = ModEntries.FUELS.get(key);
        if (e == null) return null;
        var d = e.depletedItems().get(variant);
        return d != null ? d.get() : null;
    }

    public static Fluid fluidOf(String name) {
        if (name.equals("water")) return Fluids.WATER;
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasFluid())
                ? e.materialEntry().materialFluid().source().get() : null;
    }
}
