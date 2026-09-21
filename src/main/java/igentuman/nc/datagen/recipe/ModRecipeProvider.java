package igentuman.nc.datagen.recipe;

import igentuman.nc.datagen.recipe.processors.KugelblitzChamberRecipes;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.datagen.recipe.VanillaRecipes.craftingRecipes;
import static igentuman.nc.datagen.recipe.processors.AlloySmelterRecipes.alloySmelter;
import static igentuman.nc.datagen.recipe.processors.AnalyzerRecipes.analyzer;
import static igentuman.nc.datagen.recipe.processors.AssemblerRecipes.assembler;
import static igentuman.nc.datagen.recipe.processors.CentrifugeRecipes.centrifuge;
import static igentuman.nc.datagen.recipe.processors.ChemicalReactorRecipes.chemicalReactor;
import static igentuman.nc.datagen.recipe.processors.CrystallizerRecipes.crystallizer;
import static igentuman.nc.datagen.recipe.processors.DecayHastenerRecipes.decayHastener;
import static igentuman.nc.datagen.recipe.processors.ElectrolyzerRecipes.electrolyzer;
import static igentuman.nc.datagen.recipe.processors.ExtractorRecipes.extractor;
import static igentuman.nc.datagen.recipe.processors.FissionBoilingRecipes.fissionBoiling;
import static igentuman.nc.datagen.recipe.processors.FissionFuelRecipes.fissionFuel;
import static igentuman.nc.datagen.recipe.processors.FusionReactorRecipes.fusion;
import static igentuman.nc.datagen.recipe.processors.FluidEnricherRecipes.fluidEnricher;
import static igentuman.nc.datagen.recipe.processors.FluidInfuserRecipes.fluidInfuser;
import static igentuman.nc.datagen.recipe.processors.FuelReprocessorRecipes.fuelReprocessor;
import static igentuman.nc.datagen.recipe.processors.GasScrubberRecipes.gasScrubber;
import static igentuman.nc.datagen.recipe.processors.HeatExchangerRecipes.heatExchanger;
import static igentuman.nc.datagen.recipe.processors.IngotFormerRecipes.ingotFormer;
import static igentuman.nc.datagen.recipe.processors.IrradiatorRecipes.irradiator;
import static igentuman.nc.datagen.recipe.processors.IsotopeSeparatorRecipes.isotopeSeparator;
import static igentuman.nc.datagen.recipe.processors.LeacherRecipes.leacher;
import static igentuman.nc.datagen.recipe.processors.ManufactoryRecipes.manufactory;
import static igentuman.nc.datagen.recipe.processors.MSRRecipes.msr;
import static igentuman.nc.datagen.recipe.processors.MelterRecipes.melter;
import static igentuman.nc.datagen.recipe.processors.PressurizerRecipes.pressurizer;
import static igentuman.nc.datagen.recipe.processors.PumpRecipes.pump;
import static igentuman.nc.datagen.recipe.processors.RockCrusherRecipes.rockCrusher;
import static igentuman.nc.datagen.recipe.processors.SteamTurbineRecipes.steamTurbine;
import static igentuman.nc.datagen.recipe.processors.SubatomicLiquifierRecipes.subatomicLiquifier;
import static igentuman.nc.datagen.recipe.processors.OreVeinRecipes.oreVeins;
import static igentuman.nc.datagen.recipe.processors.TurbineRecipes.turbine;
import static igentuman.nc.datagen.recipe.processors.SupercoolerRecipes.supercooler;
import static igentuman.nc.util.TagUtil.*;
import igentuman.nc.datagen.recipe.particle.AcceleratorCoolantRecipes;
import igentuman.nc.datagen.recipe.particle.CollisionChamberRecipes;
import igentuman.nc.datagen.recipe.particle.DecayChamberRecipes;
import igentuman.nc.datagen.recipe.particle.TargetChamberRecipes;

/** Root recipe provider that dispatches to every processor generator and exposes shared recipe helpers. */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public static final int TIME = 200;
    public static final int ENERGY = 50;
    public static final int MOLTEN_INGOT = 90;
    public static final int MOLTEN_NUGGET = 10;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(@NonNull RecipeOutput recipeOutput) {
        craftingRecipes(recipeOutput);
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
        gasScrubber(recipeOutput);
        pump(recipeOutput);
        extractor(recipeOutput);
        supercooler(recipeOutput);
        subatomicLiquifier(recipeOutput);
        fissionFuel(recipeOutput);
        fissionBoiling(recipeOutput);
        irradiator(recipeOutput);
        fusion(recipeOutput);
        turbine(recipeOutput);
        heatExchanger(recipeOutput);
        msr(recipeOutput);
        KugelblitzChamberRecipes.generate(recipeOutput);
        AcceleratorCoolantRecipes.generate(recipeOutput);
        TargetChamberRecipes.generate(recipeOutput);
        DecayChamberRecipes.generate(recipeOutput);
        CollisionChamberRecipes.generate(recipeOutput);
        NuclearBlastRecipes.nuclearBlast(recipeOutput);
        leacher(recipeOutput);
        analyzer(recipeOutput);
        oreVeins(recipeOutput);
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

    public static void rec(RecipeOutput out, String proc, String name, I[] ii, F[] fi, I[] io, F[] fo, int... modifiers) {
        for (I x : ii) if (x == null) return;
        for (F x : fi) if (x == null) return;
        for (I x : io) if (x == null) return;
        for (F x : fo) if (x == null) return;
        int time = modifiers.length > 0 ? modifiers[0] : TIME;
        int power = modifiers.length > 1 ? modifiers[1] : ENERGY;
        UniversalProcessorRecipeBuilder b = UniversalProcessorRecipeBuilder.processor(proc);
        for (I x : ii) b.itemInput(x.item(), x.n());
        for (F x : fi) b.fluidInput(x.fluid(), x.n());
        for (I x : io) b.itemOutput(x.item(), x.n());
        for (F x : fo) b.fluidOutput(x.fluid(), x.n());
        b.processTime(time).energyPerTick(power).save(out, name);
    }



    public static void i2i(RecipeOutput out, String proc, String name, Item in, Item o, int on, int... modifiers) {
        rec(out, proc, name, new I[]{i(in, 1)}, NF, new I[]{i(o, on)}, NF, modifiers);
    }

    public static void sep(RecipeOutput out, String name, Item in, int inN, Item o1, int n1, Item o2, int n2, int... modifiers) {
        if (o1 != null && o1 == o2) {
            rec(out, "isotope_separator", name, new I[]{i(in, inN)}, NF, new I[]{i(o1, n1 + n2)}, NF, modifiers);
        } else {
            rec(out, "isotope_separator", name, new I[]{i(in, inN)}, NF, new I[]{i(o1, n1), i(o2, n2)}, NF, modifiers);
        }
    }

    public static void crush(RecipeOutput out, String name, Item in, int inN, I[] outs, int... modifiers) {
        rec(out, "rock_crusher", name, new I[]{i(in, inN)}, NF, outs, NF, modifiers);
    }

    public static void reproc(RecipeOutput out, String key, I[] outs, int... modifiers) {
        for (String t : new String[]{"", "_tr"}) {
            rec(out, "fuel_reprocessor", key.replace('/', '_').replace('-', '_') + t,
                    new I[]{i(depletedFuel(key, t), 1)}, NF, outs, NF, modifiers);
        }
    }

    public static void assemble(RecipeOutput out, String name, Item o, int on, I[] ins, int... modifiers) {
        rec(out, "assembler", name, ins, NF, new I[]{i(o, on)}, NF, modifiers);
    }

    public static void chem(RecipeOutput out, String name, Fluid a, int an, Fluid b, int bn, F[] outs, int... modifiers) {
        rec(out, "chemical_reactor", name, NI, new F[]{f(a, an), f(b, bn)}, NI, outs, modifiers);
    }

    public static void crystal(RecipeOutput out, String name, Fluid fluid, int fn, I o, int... modifiers) {
        rec(out, "crystallizer", name, NI, new F[]{f(fluid, fn)}, new I[]{o}, NF, modifiers);
    }

    public static void enrich(RecipeOutput out, String name, Fluid fluid, int fn, I in, Fluid o, int on, int... modifiers) {
        rec(out, "fluid_enricher", name, new I[]{in}, new F[]{f(fluid, fn)}, NI, new F[]{f(o, on)}, modifiers);
    }

    public static void infuse(RecipeOutput out, String name, Fluid fluid, int fn, Item in, int inN, Item o, int on, int... modifiers) {
        rec(out, "fluid_infuser", name, new I[]{i(in, inN)}, new F[]{f(fluid, fn)}, new I[]{i(o, on)}, NF, modifiers);
    }

    public static void split(RecipeOutput out, String proc, String name, Fluid in, int inN, F[] outs, int... modifiers) {
        rec(out, proc, name, NI, new F[]{f(in, inN)}, NI, outs, modifiers);
    }

    public static void f2f(RecipeOutput out, String proc, String name, Fluid in, int inN, Fluid o, int on, int... modifiers) {
        rec(out, proc, name, NI, new F[]{f(in, inN)}, NI, new F[]{f(o, on)}, modifiers);
    }

    public static void melt(RecipeOutput out, String proc, String name, Item in, int inN, Fluid o, int on, int... modifiers) {
        rec(out, proc, name, new I[]{i(in, inN)}, NF, NI, new F[]{f(o, on)}, modifiers);
    }

    public static void f2i(RecipeOutput out, String proc, String name, Fluid in, int inN, Item o, int on, int... modifiers) {
        rec(out, proc, name, NI, new F[]{f(in, inN)}, new I[]{i(o, on)}, NF, modifiers);
    }

    public static void alloy(RecipeOutput out, String processor, String recipeName,
                       ItemLike output, int outCount, ItemLike a, int aCount, ItemLike b, int bCount, int...modifiers) {
        if (output == null || a == null || b == null) return;
        int time = modifiers.length>0 ? modifiers[0] : TIME;
        int power = modifiers.length>1 ? modifiers[1] : ENERGY;
        UniversalProcessorRecipeBuilder.processor(processor)
                .itemInput(a, aCount)
                .itemInput(b, bCount)
                .itemOutput(output, outCount)
                .processTime(time).energyPerTick(power)
                .save(out, recipeName);
    }

    // --- legacy helper kept for ManufactoryRecipes ---

    public static void grind(RecipeOutput out, String processor, String recipeName, ItemLike input, ItemLike output, int outCount, int... modifiers) {
        if (input == null || output == null) return;
        int time = modifiers.length > 0 ? modifiers[0] : TIME;
        int power = modifiers.length > 1 ? modifiers[1] : ENERGY;
        UniversalProcessorRecipeBuilder.processor(processor)
                .itemInput(input)
                .itemOutput(output, outCount)
                .processTime(time).energyPerTick(power)
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
        if (e != null && e.hasItem()) return e.item().get();
        if (name.endsWith("_heat_sink")) {
            HeatSinkEntry hs = ModEntries.HEAT_SINKS.get(name.substring(0, name.length() - "_heat_sink".length()));
            return hs != null ? hs.block().get().asItem() : null;
        }
        return null;
    }

    public static Item waste(String name) {
        return part(name + "_spallation_waste");
    }

    public static Item modItem(String rl) {
        ResourceLocation id = ResourceLocation.parse(rl);
        return BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.get(id) : null;
    }

    public static Item oreItem(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasOre()) ? e.materialEntry().oreItem().get() : null;
    }

    public static Item blockItem(String name) {
        ModEntry e = ModEntries.get(name);
        return (e != null && e.materialEntry() != null && e.materialEntry().hasBlock()) ? e.materialEntry().storageItem().get() : null;
    }

    /** Returns the common material tag for a concrete material form used in a recipe. */
    public static TagKey<Item> materialTag(ItemLike itemLike) {
        if (itemLike == null) return null;
        Item item = itemLike.asItem();
        for (MaterialEntry material : materials()) {
            String name = material.name;
            if (material.hasIngot() && material.ingot().get() == item) return ingotTag(name);
            if (material.hasDust() && material.dust().get() == item) return dustTag(name);
            if (material.hasPlate() && material.plate().get() == item) return plateTag(name);
            if (material.hasBlock() && material.storageItem().get() == item) return blockTag(name);
            if (material.hasGem() && material.gem().get() == item) return gemTag(name);
            if (material.hasNugget() && material.nugget().get() == item) return nuggetTag(name);
            if (material.hasRawOre() && material.rawOre().get() == item) return rawTag(name);
            if (material.hasOre() && material.oreItem().get() == item) return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + name));
            if (material.hasDeepslateOre() && material.deepslateOreItem().get() == item) return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + name));
        }
        if (item == Items.IRON_INGOT) return ingotTag("iron");
        if (item == Items.GOLD_INGOT) return ingotTag("gold");
        if (item == Items.COPPER_INGOT) return ingotTag("copper");
        if (item == Items.NETHERITE_INGOT) return ingotTag("netherite");
        if (item == Items.DIAMOND) return gemTag("diamond");
        if (item == Items.EMERALD) return gemTag("emerald");
        if (item == Items.LAPIS_LAZULI) return gemTag("lapis");
        if (item == Items.QUARTZ) return gemTag("quartz");
        if (item == Items.AMETHYST_SHARD) return gemTag("amethyst");
        if (item == Items.REDSTONE) return dustTag("redstone");
        if (item == Items.GLOWSTONE_DUST) return dustTag("glowstone");
        if (item == Items.RAW_IRON) return rawTag("iron");
        if (item == Items.RAW_GOLD) return rawTag("gold");
        if (item == Items.RAW_COPPER) return rawTag("copper");
        if (item == Items.IRON_BLOCK) return blockTag("iron");
        if (item == Items.GOLD_BLOCK) return blockTag("gold");
        if (item == Items.COPPER_BLOCK) return blockTag("copper");
        if (item == Items.NETHERITE_BLOCK) return blockTag("netherite");
        if (item == Items.DIAMOND_BLOCK) return blockTag("diamond");
        if (item == Items.EMERALD_BLOCK) return blockTag("emerald");
        if (item == Items.LAPIS_BLOCK) return blockTag("lapis");
        if (item == Items.REDSTONE_BLOCK) return blockTag("redstone");
        if (item == Items.COAL_BLOCK) return blockTag("coal");
        if (item == Items.QUARTZ_BLOCK) return blockTag("quartz");
        return null;
    }

    /** Returns the common tag generated for a concrete NuclearCraft material fluid. */
    public static TagKey<Fluid> fluidTag(Fluid fluid) {
        if (fluid == null) return null;
        for (MaterialEntry material : materials()) {
            TagKey<Fluid> tag = fluidTag(material, fluid);
            if (tag != null) return tag;
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry material : fuel.fluids()) {
                TagKey<Fluid> tag = fluidTag(material, fluid);
                if (tag != null) return tag;
            }
        }
        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry material : isotope.fluids()) {
                TagKey<Fluid> tag = fluidTag(material, fluid);
                if (tag != null) return tag;
            }
        }
        return null;
    }

    private static TagKey<Fluid> fluidTag(MaterialEntry material, Fluid fluid) {
        if (!material.hasFluid()) return null;
        var materialFluid = material.materialFluid();
        if (materialFluid.source().get() != fluid && materialFluid.flowing().get() != fluid) return null;
        String fluidName = material.fluidDefinition.resolveName(material.name);
        return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", fluidName));
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
        FissionFuelEntry e = ModEntries.FISSION_FUEL.get(key);
        if (e == null) return null;
        var d = e.fuelItems().get(variant);
        return d != null ? d.get() : null;
    }

    public static Item depletedFuel(String key, String variant) {
        FissionFuelEntry e = ModEntries.FISSION_FUEL.get(key);
        if (e == null) return null;
        var d = e.depletedItems().get(variant);
        return d != null ? d.get() : null;
    }

    public static Fluid fluidOf(String name) {
        return ModEntries.fluidOf(name);
    }

    public static Fluid isotopeFluid(String name, String variant) {
        IsotopeEntry e = ModEntries.ISOTOPES.get(name);
        return e == null ? null : e.fluid(variant);
    }

    public static Fluid fuelFluid(String key, String variant) {
        FissionFuelEntry e = ModEntries.FISSION_FUEL.get(key);
        return e == null ? null : e.fuelFluid(variant);
    }

    public static Fluid depletedFuelFluid(String key, String variant) {
        FissionFuelEntry e = ModEntries.FISSION_FUEL.get(key);
        return e == null ? null : e.depletedFluid(variant);
    }
}
