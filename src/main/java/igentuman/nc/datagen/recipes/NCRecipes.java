package igentuman.nc.datagen.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.registration.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCTools.GEIGER_COUNTER;
import static net.minecraft.world.item.Items.*;
import static igentuman.nc.util.DataGenUtil.*;
public class NCRecipes extends RecipeProvider {

    public NCRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }



    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        materials(consumer);
        parts(consumer);
        items(consumer);
        processors(consumer);
        solarPanels(consumer);
        fissionBlocks(consumer);
        turbineBlocks(consumer);
        FuelRecipes.generate(consumer);
        CustomRecipes.generate(consumer);
    }

    private void items(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(ALL_NC_ITEMS.get("dosimeter").get())
                .pattern(" G ")
                .pattern("SBS")
                .pattern(" L ")
                .define('S', STRING)
                .define('L', forgePlate(Materials.lead))
                .define('G', ALL_NC_ITEMS.get("gelatin").get())
                .define('B', NC_PARTS.get("bioplastic").get())
                .unlockedBy("item", has(ALL_NC_ITEMS.get("gelatin").get()))
                .save(consumer, new ResourceLocation(MODID, "dosimeter"));

        ShapedRecipeBuilder.shaped(GEIGER_COUNTER.get())
                .pattern("SFF")
                .pattern("CDR")
                .pattern("BFF")
                .define('C', forgeIngot(Materials.copper))
                .define('D', ALL_NC_ITEMS.get("dosimeter").get())
                .define('S', forgeIngot(Materials.steel))
                .define('F', forgeIngot(Materials.ferroboron))
                .define('R', forgeDust("redstone"))
                .define('B', NC_PARTS.get("bioplastic").get())
                .unlockedBy("item", has(forgeIngot(Materials.ferroboron)))
                .save(consumer, new ResourceLocation(MODID, "geiger_counter"));

        ShapelessRecipeBuilder.shapeless(ALL_NC_ITEMS.get("smore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("graham_cracker").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("graham_cracker").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("graham_cracker").get()))
                .save(consumer, new ResourceLocation(MODID, "smore"));

        ShapelessRecipeBuilder.shapeless(ALL_NC_ITEMS.get("moresmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("smore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("smore").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("smore").get()))
                .save(consumer, new ResourceLocation(MODID, "moresmore"));

        ShapelessRecipeBuilder.shapeless(ALL_NC_ITEMS.get("foursmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("moresmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("moresmore").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("moresmore").get()))
                .save(consumer, new ResourceLocation(MODID, "foursmore"));
    }

    private void parts(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(ALL_NC_ITEMS.get("upgrade_speed").get())
                .pattern("LRL")
                .pattern("RPR")
                .pattern("LRL")
                .define('L', forgeDust(Materials.lapis))
                .define('R', forgeDust("redstone"))
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer, new ResourceLocation(MODID, "upgrade_speed"));

        ShapedRecipeBuilder.shaped(ALL_NC_ITEMS.get("upgrade_energy").get())
                .pattern("ORO")
                .pattern("RPR")
                .pattern("ORO")
                .define('O', forgeDust(Materials.obsidian))
                .define('R', forgeDust(Materials.quartz))
                .define('P', LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer, new ResourceLocation(MODID, "upgrade_energy"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_basic").get())
                .pattern("LG")
                .pattern("GL")
                .define('L', forgeIngot(Materials.lead))
                .define('G', forgeDust(Materials.graphite))
                .unlockedBy("item", has(forgeIngot(Materials.lead)))
                .save(consumer, new ResourceLocation(MODID, "plate_basic"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_basic").get())
                .pattern("GL")
                .pattern("LG")
                .define('L', forgeIngot(Materials.lead))
                .define('G', forgeDust(Materials.graphite))
                .unlockedBy("item", has(forgeIngot(Materials.lead)))
                .save(consumer, new ResourceLocation(MODID, "plate_basic2"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_advanced").get())
                .pattern(" R ")
                .pattern("TPT")
                .pattern(" R ")
                .define('R', REDSTONE)
                .define('P', NCItems.NC_PARTS.get("plate_basic").get())
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_basic").get()))
                .save(consumer, new ResourceLocation(MODID, "plate_advanced"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_du").get())
                .pattern("SUS")
                .pattern("UPU")
                .pattern("SUS")
                .define('U', NC_ISOTOPES.get("uranium/238").get())
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', forgeDust(Materials.sulfur))
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer, new ResourceLocation(MODID, "plate_du"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_elite").get())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', forgeDust(Materials.crystal_binder))
                .define('P', NCItems.NC_PARTS.get("plate_du").get())
                .define('B', forgeIngot(Materials.boron))
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_du").get()))
                .save(consumer, new ResourceLocation(MODID, "plate_elite"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("plate_extreme").get())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', forgeDust(Materials.hsla_steel))
                .define('P', NCItems.NC_PARTS.get("plate_elite").get())
                .define('B', forgeIngot(Materials.extreme))
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_elite").get()))
                .save(consumer, new ResourceLocation(MODID, "plate_extreme"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("coil_copper").get())
                .pattern("CC ")
                .pattern("II ")
                .pattern("CC ")
                .define('C', forgeIngot(Materials.copper))
                .define('I', forgeIngot(Materials.iron))
                .unlockedBy("item", has(forgeIngot(Materials.copper)))
                .save(consumer, new ResourceLocation(MODID, "coil_copper"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("coil_magnesium_diboride").get())
                .pattern("MM ")
                .pattern("TT ")
                .pattern("MM ")
                .define('M', forgeIngot(Materials.magnesium_diboride))
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.magnesium_diboride)))
                .save(consumer, new ResourceLocation(MODID, "coil_magnesium_diboride"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("servo").get())
                .pattern("F F")
                .pattern("RSR")
                .pattern("SCS")
                .define('F', forgeIngot(Materials.ferroboron))
                .define('S', forgeIngot(Materials.steel))
                .define('R', REDSTONE)
                .define('C', forgeIngot(Materials.copper))
                .unlockedBy("item", has(forgeIngot(Materials.ferroboron)))
                .save(consumer, new ResourceLocation(MODID, "servo"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("motor").get())
                .pattern("SSG")
                .pattern("CCI")
                .pattern("SSG")
                .define('G', forgeNugget(Materials.gold))
                .define('S', forgeIngot(Materials.steel))
                .define('I', forgeIngot(Materials.iron))
                .define('C', NCItems.NC_PARTS.get("coil_copper").get())
                .unlockedBy("item", has(NCItems.NC_PARTS.get("coil_copper").get()))
                .save(consumer, new ResourceLocation(MODID, "motor"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("actuator").get())
                .pattern("  S")
                .pattern("FP ")
                .pattern("CF ")
                .define('F', forgeIngot(Materials.ferroboron))
                .define('S', forgeIngot(Materials.steel))
                .define('C', forgeIngot(Materials.copper))
                .define('P', PISTON)
                .unlockedBy("item", has(PISTON))
                .save(consumer, new ResourceLocation(MODID, "actuator"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("chassis").get())
                .pattern("LSL")
                .pattern("STS")
                .pattern("LSL")
                .define('S', forgeIngot(Materials.steel))
                .define('L', forgeIngot(Materials.lead))
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, new ResourceLocation(MODID, "chassis"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("empty_frame").get())
                .pattern("PTP")
                .pattern("I I")
                .pattern("PTP")
                .define('I', forgeIngot(Materials.iron))
                .define('P', NCItems.NC_PARTS.get("plate_basic").get())
                .define('T', forgeIngot(Materials.tin))
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_basic").get()))
                .save(consumer, new ResourceLocation(MODID, "empty_frame"));

        ShapedRecipeBuilder.shaped(NCItems.NC_PARTS.get("steel_frame").get())
                .pattern("STS")
                .pattern("TBT")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tough_alloy))
                .define('B', forgeIngot(Materials.bronze))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, new ResourceLocation(MODID, "steel_frame"));

    }

    private void fissionBlocks(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get(), 4)
                .pattern("LPL")
                .pattern("PTP")
                .pattern("LPL")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('L', forgePlate("lead"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get("fission_reactor_controller").get())
                .pattern("LPL")
                .pattern("PTP")
                .pattern("LPL")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('T', COMPARATOR)
                .define('L', FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get("fission_reactor_glass").get())
                .pattern(" P ")
                .pattern("PTP")
                .pattern(" P ")
                .define('P', Tags.Items.GLASS)
                .define('T', FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get("fission_reactor_solid_fuel_cell").get())
                .pattern("TGT")
                .pattern("G G")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get("empty_active_heat_sink").get())
                .pattern("TIT")
                .pattern("IBI")
                .pattern("TIT")
                .define('I', forgePlate("thermoconducting"))
                .define('B', BUCKET)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FissionReactor.MULTI_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        for(String name: FissionBlocks.heatsinks.keySet()) {
            if(!name.matches(".*active.*|.*water.*")) {
                if(name.contains("empty")) continue;
                TagKey<Item> i = forgeDust(name);
                if(name.contains("slime")) {
                    i = Tags.Items.SLIMEBALLS;
                }
                ShapedRecipeBuilder.shaped(FissionReactor.MULTI_BLOCKS.get(name+"_heat_sink").get())
                        .pattern(" I ")
                        .pattern("IBI")
                        .pattern(" I ")
                        .define('I', Ingredient.of(i))
                        .define('B', FissionReactor.MULTI_BLOCKS.get("empty_heat_sink").get())
                        .group(MODID+"_fission")
                        .unlockedBy("item", has(FissionReactor.MULTI_BLOCKS.get("empty_heat_sink").get()))
                        .save(consumer);
            }
        }
    }

    private void turbineBlocks(Consumer<FinishedRecipe> consumer) {


    }

    private void solarPanels(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/basic").get())
                .pattern("LQL")
                .pattern("PLP")
                .pattern("CSC")
                .define('Q', GLASS_PANE)
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('L', Tags.Items.GEMS_LAPIS)
                .define('S', DAYLIGHT_DETECTOR)
                .define('C', NCItems.NC_PARTS.get("coil_copper").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("coil_copper").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PCP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/basic").get())
                .define('G', forgeDust("graphite"))
                .define('C', NCItems.NC_PARTS.get("coil_copper").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/basic").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/du").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_du").get())
                .define('S', NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .define('G', forgeDust("graphite"))
                .define('M', NCItems.NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/elite").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_elite").get())
                .define('S', NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/du").get())
                .define('G', forgeDust("graphite"))
                .define('M', NCItems.NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(consumer);

    }

    private void materials(Consumer<FinishedRecipe> consumer) {
        for(String name: Materials.ingots().keySet()) {
            if(Materials.ingots().get(name).block) {
                ShapelessRecipeBuilder.shapeless(NCBlocks.NC_BLOCKS.get(name).get())
                        .requires(Ingredient.of(forgeIngot(name)), 9)
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
                ShapelessRecipeBuilder.shapeless(NCItems.NC_INGOTS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeBlock(name)))
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer, name+"_from_block");
            }
            if(Materials.ingots().get(name).nugget) {
                ShapelessRecipeBuilder.shapeless(NCItems.NC_INGOTS.get(name).get())
                        .requires(Ingredient.of(forgeNugget(name)), 9)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer,name+"_from_nugget");
                ShapelessRecipeBuilder.shapeless(NCItems.NC_NUGGETS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeIngot(name)), 1)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
            }
            if(Materials.ingots().get(name).hasOre()) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeOre(name)),
                        NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(name)).build()))
                        .save(consumer, MODID+"_"+name+"_ore");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeOre(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_ore");
            }

            if(Materials.ingots().get(name).chunk) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeChunk(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeChunk(name)).build()))
                        .save(consumer, MODID+"_"+name+"_raw");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeChunk(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeChunk(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_raw");
            }
            if(Materials.ingots().get(name).dust) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeDust(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeDust(name)).build()))
                        .save(consumer, MODID+"_"+name+"_dust");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeDust(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeDust(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_dust");
            }
            if(Materials.ingots().get(name).plate) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgePlate(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgePlate(name)).build()))
                        .save(consumer, MODID+"_"+name+"_plate");
            }
        }

    }

    private void processors(Consumer<FinishedRecipe> consumer)
    {
/*        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("nuclear_furnace").get())
                .pattern("PTP")
                .pattern("TFT")
                .pattern("PTP")
                .define('T', Tags.Items.INGOTS_IRON)
                .define('P', NCItems.NC_PLATES.get("basic").get())
                .define('F', Blocks.FURNACE)
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PLATES.get("basic").get()))
                .save(consumer);*/

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("manufactory").get())
                .pattern("LRL")
                .pattern("FPF")
                .pattern("LSL")
                .define('P', PISTON)
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('F', FLINT)
                .define('L', forgeIngot("lead"))
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(FLINT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("centrifuge").get())
                .pattern("LSL")
                .pattern("FPF")
                .pattern("LRL")
                .define('P', NCItems.NC_PARTS.get("chassis").get())
                .define('S', forgeIngot("ferroboron"))
                .define('F', NCItems.NC_PARTS.get("motor").get())
                .define('L', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('R', NCItems.NC_PARTS.get("servo").get())
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("chemical_reactor").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NCItems.NC_PARTS.get("chassis").get())
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .define('F', NCItems.NC_PARTS.get("motor").get())
                .define('L', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('X', Tags.Items.DUSTS_GLOWSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("alloy_smelter").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', BLAST_FURNACE)
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('F', Tags.Items.DUSTS_REDSTONE)
                .define('L', NCItems.NC_PARTS.get("plate_basic").get())
                .define('X', Tags.Items.INGOTS_BRICK)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("melter").get())
                .pattern("LXL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NCItems.NC_PARTS.get("chassis").get())
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .define('L', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('X', Tags.Items.INGOTS_NETHER_BRICK)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("ingot_former").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NCItems.NC_PARTS.get("chassis").get())
                .define('S', forgeIngot("tough_alloy"))
                .define('F', HOPPER)
                .define('L', NCItems.NC_PARTS.get("plate_basic").get())
                .define('X', forgeIngot("ferroboron"))
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("fuel_reprocessor").get())
                .pattern("PGP")
                .pattern("ECE")
                .pattern("PSP")
                .define('P', forgeIngot("tin_silver"))
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('G', Tags.Items.DUSTS_GLOWSTONE)
                .define('E', ENDER_PEARL)
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("decay_hastener").get())
                .pattern("PBP")
                .pattern("TCT")
                .pattern("PAP")
                .define('C', forgeIngot("tin_silver")) //todo fission_controller
                .define('P', forgeIngot("boron"))
                .define('A', NCItems.NC_PARTS.get("actuator").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('B', forgeBlock("boron"))
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("actuator").get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("isotope_separator").get())
                .pattern("PMP")
                .pattern("RCR")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_basic").get())
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("pressurizer").get())
                .pattern("PTP")
                .pattern("ACA")
                .pattern("PNP")
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('T', TERRACOTTA)
                .define('N', ANVIL)
                .define('A', NCItems.NC_PARTS.get("actuator").get())
                .group("nuclearcraft")
                .unlockedBy("has_chassis", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

       /* ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("salt_mixer").get())
                .pattern("PSP")
                .pattern("BCB")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_basic").get())
                .define('S', NCItems.INGOT_STEEL.get())
                .define('B', Items.BUCKET)
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(Items.BUCKET))
                .save(consumer);*/

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("fluid_enricher").get())
                .pattern("PHP")
                .pattern("LCL")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('L', LAPIS_LAZULI)
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .define('H', HOPPER)
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("electrolyzer").get())
                .pattern("PGP")
                .pattern("SCS")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('G', forgeIngot("graphite"))
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("motor").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("assembler").get())
                .pattern("PHP")
                .pattern("ACA")
                .pattern("PMP")
                .define('P', NCItems.NC_PARTS.get("plate_basic").get())
                .define('H', forgeIngot("hard_carbon"))
                .define('A', NCItems.NC_PARTS.get("actuator").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("actuator").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("supercooler").get())
                .pattern("PDP")
                .pattern("HCH")
                .pattern("PSP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('D', forgeIngot("magnesium_diboride"))
                .define('H', forgeIngot("hard_carbon"))
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NCItems.NC_PARTS.get("servo").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("extractor").get())
                .pattern("PMP")
                .pattern("BCB")
                .pattern("PSP")
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('M', forgeIngot("magnesium"))
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .define('B', BUCKET)
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("crystallizer").get())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PUP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('U', CAULDRON)
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("steam_turbine").get())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', NCItems.NC_PARTS.get("coil_copper").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('U', CAULDRON)
                .define('F', FURNACE)
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("irradiator").get())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('S', NCItems.NC_PARTS.get("coil_magnesium_diboride").get())
                .define('C', NCItems.NC_PARTS.get("chassis2").get())
                .define('U', CHEST)
                .define('F', FURNACE)
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("fluid_infuser").get())
                .pattern("PBP")
                .pattern("GCG")
                .pattern("PSP")
                .define('B', BUCKET)
                .define('G', forgeIngot("gold"))
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("rock_crusher").get())
                .pattern("PMP")
                .pattern("ACA")
                .pattern("PTP")
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('A', NCItems.NC_PARTS.get("actuator").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('M', NCItems.NC_PARTS.get("motor").get())
                .group("nuclearcraft")
                .unlockedBy("has_item", has(NCItems.NC_PARTS.get("motor").get()))
                .save(consumer);
    }
}