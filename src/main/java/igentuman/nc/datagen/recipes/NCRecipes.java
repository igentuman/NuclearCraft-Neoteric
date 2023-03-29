package igentuman.nc.datagen.recipes;

import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.registration.*;
import igentuman.nc.setup.registration.fuel.FuelManager;
import igentuman.nc.setup.registration.fuel.NCFuel;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.world.item.Items.*;
import static igentuman.nc.util.DataGenUtil.*;
public class NCRecipes extends RecipeProvider {

    public NCRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }



    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        materials(consumer);
        processors(consumer);
        solarPanels(consumer);
        fissionBlocks(consumer);
        FuelRecipes.generate(consumer);
    }

    private void fissionBlocks(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(NCBlocks.MULTI_BLOCKS.get("fission_reactor_casing").get(), 4)
                .pattern("LPL")
                .pattern("PTP")
                .pattern("LPL")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('L', forgePlate("lead"))
                .group(MODID+"_fission")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCBlocks.MULTI_BLOCKS.get("fission_reactor_glass").get())
                .pattern(" P ")
                .pattern("PTP")
                .pattern(" P ")
                .define('P', Tags.Items.GLASS)
                .define('T', NCBlocks.MULTI_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCBlocks.MULTI_BLOCKS.get("fission_reactor_solid_fuel_cell").get())
                .pattern("TGT")
                .pattern("G G")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCBlocks.MULTI_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCBlocks.MULTI_BLOCKS.get("empty_active_heat_sink").get())
                .pattern("TIT")
                .pattern("IBI")
                .pattern("TIT")
                .define('I', forgePlate("thermoconducting"))
                .define('B', BUCKET)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCBlocks.MULTI_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        for(String name: FissionBlocks.heatsinks.keySet()) {
            if(!name.contains("active")) {
                if(name.contains("empty")) continue;
                TagKey<Item> i = forgeDust(name);
                if(name.contains("slime")) {
                    i = Tags.Items.SLIMEBALLS;
                }
                ShapedRecipeBuilder.shaped(NCBlocks.MULTI_BLOCKS.get(name+"_heat_sink").get())
                        .pattern(" I ")
                        .pattern("IBI")
                        .pattern(" I ")
                        .define('I', Ingredient.of(i))
                        .define('B', NCBlocks.MULTI_BLOCKS.get("empty_heat_sink").get())
                        .group(MODID+"_fission")
                        .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCBlocks.MULTI_BLOCKS.get("empty_heat_sink").get()))
                        .save(consumer);
            }
        }
    }



    private void solarPanels(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/basic").get())
                .pattern("GQG")
                .pattern("PLP")
                .pattern("CSC")
                .define('G', forgeDust("graphite"))
                .define('Q', forgeDust("quartz"))
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('L', Tags.Items.GEMS_LAPIS)
                .define('S', DAYLIGHT_DETECTOR)
                .define('C', NCItems.NC_PARTS.get("coil_copper").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("coil_copper").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/basic").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCEnergyBlocks.ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(consumer);

    }

    private void materials(Consumer<FinishedRecipe> consumer) {
        for(String name: Materials.ingots().keySet()) {
            if(Materials.ingots().get(name).block) {
                ShapelessRecipeBuilder.shapeless(NCBlocks.NC_BLOCKS.get(name).get())
                        .requires(Ingredient.of(forgeIngot(name)), 9)
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
                ShapelessRecipeBuilder.shapeless(NCItems.NC_INGOTS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeBlock(name)))
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer, name+"_from_block");
            }
            if(Materials.ingots().get(name).nugget) {
                ShapelessRecipeBuilder.shapeless(NCItems.NC_INGOTS.get(name).get())
                        .requires(Ingredient.of(forgeNugget(name)), 9)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer,name+"_from_nugget");
                ShapelessRecipeBuilder.shapeless(NCItems.NC_NUGGETS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeIngot(name)), 1)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
            }
            if(Materials.ingots().get(name).hasOre()) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeOre(name)),
                        NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(name)).build()))
                        .save(consumer, MODID+"_"+name+"_ore");
            }

            if(Materials.ingots().get(name).chunk) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeChunk(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeChunk(name)).build()))
                        .save(consumer, MODID+"_"+name+"_raw");
            }
            if(Materials.ingots().get(name).dust) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeDust(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeDust(name)).build()))
                        .save(consumer, MODID+"_"+name+"_dust");
            }
            if(Materials.ingots().get(name).plate) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgePlate(name)),
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PLATES.get("basic").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(FLINT))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("plate_advanced").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("actuator").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("chassis").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BUCKET))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("plate_advanced").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("motor").get()))
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
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("actuator").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(NCProcessors.PROCESSORS.get("super_cooler").get())
                .pattern("PDP")
                .pattern("HCH")
                .pattern("PSP")
                .define('P', NCItems.NC_PARTS.get("plate_advanced").get())
                .define('D', forgeIngot("magnesium_diboride"))
                .define('H', forgeIngot("hard_carbon"))
                .define('S', NCItems.NC_PARTS.get("servo").get())
                .define('C', NCItems.NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(NCItems.NC_PARTS.get("servo").get()))
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