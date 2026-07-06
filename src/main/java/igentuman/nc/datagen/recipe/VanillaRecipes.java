package igentuman.nc.datagen.recipe;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Processors;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.*;
import static igentuman.nc.util.TagUtil.*;
import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.item.Items.BUCKET;

public class VanillaRecipes {

    private static RecipeOutput recipeOutput;

    public static void craftingRecipes(RecipeOutput out) {
        recipeOutput = out;
        for(ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() == null) {
                continue;
            }
            String name = entry.materialEntry().name;
            if (entry.materialEntry().hasBlock() && entry.materialEntry().hasIngot()) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().storageBlock().get())
                        .requires(Ingredient.of(ingotTag(name)), 9)
                        .group(MODID+"_blocks")
                        .unlockedBy("has_ingot", has(entry.materialEntry().ingot()))
                        .save(out);

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().ingot().get(), 9)
                        .requires(Ingredient.of(blockTag(name)))
                        .group(MODID+"_ingots")
                        .unlockedBy("has_block", has(entry.materialEntry().storageBlock()))
                        .save(out, MODID + ":" + name + "_ingot_from_block");
            }
            if (entry.materialEntry().hasBlock() && entry.materialEntry().hasGem()) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().storageBlock().get())
                        .requires(Ingredient.of(gemTag(name)), 9)
                        .group(MODID+"_blocks")
                        .unlockedBy("has_gem", has(entry.materialEntry().gem()))
                        .save(out, MODID + ":" + name + "_block_from_gem");

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().gem().get(), 9)
                        .requires(Ingredient.of(blockTag(name)))
                        .group(MODID+"_gems")
                        .unlockedBy("has_block", has(entry.materialEntry().storageBlock()))
                        .save(out, MODID + ":" + name + "_gem_from_block");
            }
            if (entry.materialEntry().hasIngot() && entry.materialEntry().hasNugget()) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().ingot().get())
                        .requires(Ingredient.of(nuggetTag(name)), 9)
                        .group(MODID+"_ingots")
                        .unlockedBy("has_nugget", has(entry.materialEntry().nugget()))
                        .save(out, MODID + ":" + name + "_ingot_from_nugget");

                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, entry.materialEntry().nugget().get(), 9)
                        .requires(Ingredient.of(ingotTag(name)))
                        .group(MODID+"_nuggets")
                        .unlockedBy("has_ingot", has(entry.materialEntry().ingot()))
                        .save(out, MODID + ":" + name + "_nugget_from_ingot");
            }
            if (entry.materialEntry().hasIngot()) {
                if (entry.materialEntry().hasRawOre()) {
                    smelting(entry.materialEntry().rawOre(), Ingredient.of(rawTag(name)), entry.materialEntry().ingot());
                }
                if (entry.materialEntry().hasDust()) {
                    smelting(entry.materialEntry().dust(), Ingredient.of(dustTag(name)), entry.materialEntry().ingot());
                }
                if (entry.materialEntry().hasPlate()) {
                    smelting(entry.materialEntry().plate(), Ingredient.of(plateTag(name)), entry.materialEntry().ingot());
                }
            }
        }
        processors();
        parts();
        fissionBlocks();
        fuelPellets();
        
/*        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RESONITE_CRYSTAL.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', RESONITE_SHARD.get())
                .group(MODID)
                .unlockedBy("item", has(RESONITE_SHARD.get()))
                .save(recipeOutput);*/
    }

    private static void parts() {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("spaxelhoe_thorium").item())
                .pattern("TTT")
                .pattern("TIT")
                .pattern(" I ")
                .define('T', plateTag("thorium"))
                .define('I', ingotTag("iron"))
                .unlockedBy("item", has(ModEntries.get("thorium").materialEntry().plate()))
                .save(recipeOutput, rl("spaxelhoe_thorium"));

        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ALL_NC_ITEMS.get("upgrade_speed").get())
                .pattern("LRL")
                .pattern("RPR")
                .pattern("LRL")
                .define('L', dustTag("lapis"))
                .define('R', dustTag("redstone"))
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(recipeOutput, rl("upgrade_speed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ALL_NC_ITEMS.get("upgrade_energy").get())
                .pattern("ORO")
                .pattern("RPR")
                .pattern("ORO")
                .define('O', dustTag("obsidian"))
                .define('R', dustTag("quartz"))
                .define('P', LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(recipeOutput, rl("upgrade_energy"));*/

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_basic").item(), 2)
                .pattern("LG")
                .pattern("GL")
                .define('L', ingotTag("lead"))
                .define('G', dustTag("graphite"))
                .unlockedBy("item", has(ingot("lead")))
                .save(recipeOutput, rl("plate_basic"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_basic").item(), 2)
                .pattern("GL")
                .pattern("LG")
                .define('L', ingotTag("lead"))
                .define('G', dustTag("graphite"))
                .unlockedBy("item", has(ingot("lead")))
                .save(recipeOutput, rl("plate_basic2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_advanced").item(), 2)
                .pattern(" P ")
                .pattern("TRT")
                .pattern(" P ")
                .define('R', REDSTONE)
                .define('P', ModEntries.get("plate_basic").item())
                .define('T', ingotTag("tough_alloy"))
                .unlockedBy("item", has(ModEntries.get("plate_basic").item()))
                .save(recipeOutput, rl("plate_advanced"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_du").item())
                .pattern("SUS")
                .pattern("UPU")
                .pattern("SUS")
                .define('U', isotope("uranium/238"))
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', dustTag("sulfur"))
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput, rl("plate_du"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_elite").item())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', dustTag("crystal_binder"))
                .define('P', ModEntries.get("plate_du").item())
                .define('B', ingotTag("boron"))
                .unlockedBy("item", has(ModEntries.get("plate_du").item()))
                .save(recipeOutput, rl("plate_elite"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("plate_extreme").item())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', dustTag("hsla_steel"))
                .define('P', ModEntries.get("plate_elite").item())
                .define('B', ingotTag("extreme"))
                .unlockedBy("item", has(ModEntries.get("plate_elite").item()))
                .save(recipeOutput, rl("plate_extreme"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("coil_copper").item())
                .pattern("CC ")
                .pattern("II ")
                .pattern("CC ")
                .define('C', ingotTag("copper"))
                .define('I', ingotTag("iron"))
                .unlockedBy("item", has(COPPER_INGOT))
                .save(recipeOutput, rl("coil_copper"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("coil_magnesium_diboride").item())
                .pattern("MM ")
                .pattern("TT ")
                .pattern("MM ")
                .define('M', ingotTag("magnesium_diboride"))
                .define('T', ingotTag("tough_alloy"))
                .unlockedBy("item", has(ingot("magnesium_diboride")))
                .save(recipeOutput, rl("coil_magnesium_diboride"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("servo").item())
                .pattern("F F")
                .pattern("RSR")
                .pattern("SCS")
                .define('F', ingotTag("ferroboron"))
                .define('S', ingotTag("steel"))
                .define('R', REDSTONE)
                .define('C', ingotTag("copper"))
                .unlockedBy("item", has(ingot("ferroboron")))
                .save(recipeOutput, rl("servo"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("motor").item())
                .pattern("SSG")
                .pattern("CCI")
                .pattern("SSG")
                .define('G', nuggetTag("gold"))
                .define('S', ingotTag("steel"))
                .define('I', ingotTag("iron"))
                .define('C', ModEntries.get("coil_copper").item())
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(recipeOutput, rl("motor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("actuator").item())
                .pattern("  S")
                .pattern("FP ")
                .pattern("CF ")
                .define('F', ingotTag("ferroboron"))
                .define('S', ingotTag("steel"))
                .define('C', ingotTag("copper"))
                .define('P', PISTON)
                .unlockedBy("item", has(PISTON))
                .save(recipeOutput, rl("actuator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("chassis").item())
                .pattern("LSL")
                .pattern("STS")
                .pattern("LSL")
                .define('S', ingotTag("steel"))
                .define('L', ingotTag("lead"))
                .define('T', ingotTag("tough_alloy"))
                .unlockedBy("item", has(ingot("tough_alloy")))
                .save(recipeOutput, rl("chassis"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("empty_frame").item())
                .pattern("PTP")
                .pattern("I I")
                .pattern("PTP")
                .define('I', ingotTag("iron"))
                .define('P', ModEntries.get("plate_basic").item())
                .define('T', ingotTag("tin"))
                .unlockedBy("item", has(ModEntries.get("plate_basic").item()))
                .save(recipeOutput, rl("empty_frame"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("steel_frame").item())
                .pattern("STS")
                .pattern("TBT")
                .pattern("STS")
                .define('S', ingotTag("steel"))
                .define('T', ingotTag("tough_alloy"))
                .define('B', ingotTag("bronze"))
                .unlockedBy("item", has(ingot("tough_alloy")))
                .save(recipeOutput, rl("steel_frame"));

    }

/*    private static void msrBlocks() {
        // MSR Controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_controller").get())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LZL")
                .define('Z', CAULDRON)
                .define('P', ModEntries.get("plate_advanced").item())
                .define('D', NCProcessors.PROCESSORS.get(Processors.DECAY_HASTENER).get())
                .define('T', NCProcessors.PROCESSORS.get(Processors.CHEMICAL_REACTOR).get())
                .define('L', ModEntries.get("fission_reactor_casing").get())
                .group(MODID+"_msr")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(consumer, rl("msr_controller"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_fuel_cell").get())
                .pattern("TGT")
                .pattern("GBG")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS)
                .define('B', BUCKET)
                .define('T', ingotTag("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_port").get())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', ModEntries.get("servo").item())
                .define('P', BUCKET)
                .define('T', ModEntries.get("fission_reactor_casing").get())
                .define('L', plateTag("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").get()))
                .save(recipeOutput);
    }

    private static void turbineBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_casing").get(), 4)
                .pattern("SSS")
                .pattern("SLS")
                .pattern("SSS")
                .define('S', plateTag(Materials.hsla_steel))
                .define('L', ModEntries.get("chassis").item())
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_bearing").get(), 2)
                .pattern("GGG")
                .pattern("GSG")
                .pattern("GGG")
                .define('S', ingotTag(Materials.hsla_steel))
                .define('G', ingotTag("gold"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag(Materials.hsla_steel)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_controller").get(), 1)
                .pattern("GCG")
                .pattern("CBC")
                .pattern("GCG")
                .define('C', TURBINE_BLOCKS.get("turbine_casing").get())
                .define('G', NC_PARTS.get("basic_electric_circuit").get())
                .define('B', BUCKET)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag(Materials.hsla_steel)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_rotor_shaft").get(), 4)
                .pattern("STS")
                .pattern("STS")
                .pattern("STS")
                .define('S', ingotTag(Materials.hsla_steel))
                .define('T', ingotTag(Materials.zinc))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag(Materials.hsla_steel)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_port").get(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag(Materials.hsla_steel))
                .define('T', ingotTag(Materials.zinc))
                .define('B', CAULDRON)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag(Materials.hsla_steel)))
                .save(recipeOutput);

        for(String type: coils.keySet()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_"+type+"_coil").get(), 1)
                    .pattern("SBS")
                    .pattern("SBS")
                    .pattern("SBS")
                    .define('S', ingotTag(Materials.stainless_steel))
                    .define('B', ingotTag(type))
                    .group(MODID+"_turbine")
                    .unlockedBy("item", has(ingotTag(Materials.stainless_steel)))
                    .save(recipeOutput);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_extreme_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag(Materials.extreme))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag(Materials.extreme)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_steel_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag(Materials.steel))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag(Materials.steel)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_basic_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag(Materials.iron))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag(Materials.iron)))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_sic_sic_cmc_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag(Materials.sic_sic_cmc))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag(Materials.sic_sic_cmc)))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_glass").get(), 1)
                .requires(TURBINE_BLOCKS.get("turbine_casing").get())
                .requires(Tags.Items.GLASS)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(TURBINE_BLOCKS.get("turbine_casing").get()))
                .save(recipeOutput);

    }

    private static void heatExchangerBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger_casing").get(), 4)
                .pattern("SSS")
                .pattern("SLS")
                .pattern("SSS")
                .define('S', plateTag(Materials.hsla_steel))
                .define('L', ModEntries.get("chassis").item())
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger_controller").get(), 1)
                .pattern("GCG")
                .pattern("CBC")
                .pattern("GCG")
                .define('C', HX_BLOCKS.get("heat_exchanger_casing").get())
                .define('G', NC_PARTS.get("basic_electric_circuit").get())
                .define('B', BUCKET)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(HX_BLOCKS.get("heat_exchanger_casing").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger_radiator").get(), 2)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("TTT")
                .define('T', ingotTag(Materials.copper))
                .define('A', plateTag(Materials.aluminum))
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(HX_BLOCKS.get("heat_exchanger_casing").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger_hot_coolant_port").get(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag(Materials.hsla_steel))
                .define('T', ingotTag(Materials.gold))
                .define('B', CAULDRON)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(HX_BLOCKS.get("heat_exchanger_casing").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger_cold_coolant_port").get(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag(Materials.hsla_steel))
                .define('T', ingotTag(Materials.zinc))
                .define('B', CAULDRON)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(HX_BLOCKS.get("heat_exchanger_casing").get()))
                .save(recipeOutput);
    }

    private static void solarPanels() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/basic").get())
                .pattern("LQL")
                .pattern("PLP")
                .pattern("CSC")
                .define('Q', GLASS_PANE)
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('L', Tags.Items.GEMS_LAPIS)
                .define('S', DAYLIGHT_DETECTOR)
                .define('C', ModEntries.get("coil_copper").item())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PCP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', ENERGY_BLOCKS.get("solar_panel/basic").get())
                .define('G', dustIngredient(Materials.quartz))
                .define('C', ModEntries.get("coil_copper").item())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/basic").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/du").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_du").item())
                .define('S', ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .define('G', dustIngredient(Materials.energetic_blend))
                .define('M', NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/elite").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_elite").item())
                .define('S', ENERGY_BLOCKS.get("solar_panel/du").get())
                .define('G', dustIngredient(Materials.energetic_blend))
                .define('M', NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(recipeOutput);

    }*/

    private static void fissionBlocks() {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_irradiation_chamber").block())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', ModEntries.get("servo").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('T', CHEST)
                .define('L', plateTag("boron"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("servo").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_port").block())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', ModEntries.get("servo").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('T', ModEntries.get("fission_reactor_casing").block())
                .define('L', plateTag("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").block()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_casing").block(), 4)
                .pattern("LPL")
                .pattern("P P")
                .pattern("LPL")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('L', plateTag("lead"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_controller").block())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LPL")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('D', ModEntries.get(Processors.DECAY_HASTENER).block())
                .define('T', ModEntries.get("basic_electric_circuit").item())
                .define('L', ModEntries.get("fission_reactor_casing").item())
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_glass").block())
                .pattern(" P ")
                .pattern("PTP")
                .pattern(" P ")
                .define('P', Tags.Items.GLASS_BLOCKS)
                .define('T', ModEntries.get("fission_reactor_casing").block())
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fission_reactor_solid_fuel_cell").block())
                .pattern("TGT")
                .pattern("G G")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('T', ingotTag("zirconium"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").block()))
                .save(recipeOutput);

        fusionCraftingRecipes(recipeOutput);

/*        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HX_BLOCKS.get("heat_exchanger").get())
                .pattern("LPL")
                .pattern("PMP")
                .pattern("LPL")
                .define('M', ModEntries.get("chassis").item())
                .define('P', plateTag("copper"))
                .define('L',  plateTag(Materials.thermoconducting))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);*/

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.HEAT_SINKS.get("empty").block())
                .pattern("TIT")
                .pattern("ABA")
                .pattern("TIT")
                .define('I', plateTag("thermoconducting"))
                .define('B', BUCKET)
                .define('A', IRON_BARS)
                .define('T', ingotTag("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").block()))
                .save(recipeOutput);

/*        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.HEAT_SINKS.get("empty").block())
                .pattern("TIT")
                .pattern("IBI")
                .pattern("TIT")
                .define('I', ModEntries.get("motor").item())
                .define('B', ModEntries.HEAT_SINKS.get("empty").block())
                .define('T', plateTag("thermoconducting"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(ModEntries.HEAT_SINKS.get("empty").block()))
                .save(recipeOutput);*/

        for(String name: ModEntries.HEAT_SINKS.keySet()) {
            if(name.matches(".*water.*|.*liquid.*|.*empty.*|.*cryotheum.*")) {
                continue;
            }
            TagKey<Item> i = dustTag(name.replace("active_", ""));
            if(name.contains("slime")) {
                i = Tags.Items.SLIMEBALLS;
            }
            if(name.contains("nether_brick")) {
                i = Tags.Items.BRICKS_NETHER;
            }
            Block empty = ModEntries.HEAT_SINKS.get("empty").block().get();
            if (name.contains("active")) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.HEAT_SINKS.get(name).block())
                        .pattern("SIS")
                        .pattern("IBI")
                        .pattern("SIS")
                        .define('I', Ingredient.of(i))
                        .define('S', ModEntries.get("servo").item())
                        .define('B', empty)
                        .group(MODID + "_fission")
                        .unlockedBy("item", has(empty))
                        .save(recipeOutput);
            } else {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.HEAT_SINKS.get(name).block())
                        .pattern(" I ")
                        .pattern("IBI")
                        .pattern(" I ")
                        .define('I', Ingredient.of(i))
                        .define('B', empty)
                        .group(MODID + "_fission")
                        .unlockedBy("item", has(empty))
                        .save(recipeOutput);
            }

        }
    }

    private static void fusionCraftingRecipes(RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fusion_reactor_casing").block(), 4)
                .pattern("LPL")
                .pattern("P P")
                .pattern("LPL")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('L', plateTag("lead"))
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fusion_reactor_glass").block())
                .pattern(" P ")
                .pattern("PTP")
                .pattern(" P ")
                .define('P', Tags.Items.GLASS_BLOCKS)
                .define('T', ModEntries.get("fusion_reactor_casing").block())
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("fusion_reactor_casing").block()))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fusion_reactor_connector").block(), 2)
                .pattern("LTL")
                .pattern("TMT")
                .pattern("LTL")
                .define('M', ModEntries.get("basic_electric_circuit").item())
                .define('T', ModEntries.get("fusion_reactor_casing").block())
                .define('L', plateTag("lead"))
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("fusion_reactor_casing").block()))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fusion_reactor_port").block())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', ModEntries.get("servo").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('T', ModEntries.get("fusion_reactor_casing").block())
                .define('L', plateTag("tough_alloy"))
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("fusion_reactor_casing").block()))
                .save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("fusion_reactor_core").block())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LPL")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('D', ModEntries.get(Processors.DECAY_HASTENER).block())
                .define('T', ModEntries.get("basic_electric_circuit").item())
                .define('L', ModEntries.get("fusion_reactor_casing").item())
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("fusion_reactor_casing").block()))
                .save(out);

        for (String tier : new String[]{"basic", "magnesium_diboride", "niobium_tin", "niobium_titanium", "bscco"}) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModEntries.get(tier + "_electromagnet_slope").block())
                    .requires(ModEntries.get(tier + "_electromagnet").block())
                    .group(MODID + "_fusion")
                    .unlockedBy("item", has(ModEntries.get(tier + "_electromagnet").block()))
                    .save(out);
        }
    }

    private static void fuelPellets() {
        for (IsotopeEntry iso : ModEntries.ISOTOPES.values()) {
            for (String variant : new String[]{"_ox", "_ni", "_za"}) {
                Item in = isotopeVar(iso, variant);
                if (in == null) continue;
                fuelSmelt(in, iso.base().get(), iso.itemId + variant + "_sml");
            }
        }

        for (FissionFuelEntry fe : ModEntries.FISSION_FUEL.values()) {
            String key = fe.key;
            String idStem = fe.group + "_" + fe.name.replace('-', '_');
            int iso1 = fe.base().isotopes[0];
            int iso2 = fe.base().isotopes[1];

            if (!fe.base().isSpecial()) {
                for (String variant : new String[]{"_ox", "_ni", "_za"}) {
                    fuelSmelt(fuel(key, variant), fuel(key, ""), "fuel_" + idStem + variant + "_sml");
                    fuelSmelt(depletedFuel(key, variant), depletedFuel(key, ""), "depleted_" + idStem + variant + "_sml");
                }
                if (fe.group.equals("mixed")) {
                    for (String variant : new String[]{"", "_ox", "_ni", "_za"}) {
                        moxRecipe(key, idStem, variant, iso1);
                    }
                    continue;
                }
                for (String variant : new String[]{"_ox", "_ni", "_za"}) {
                    fuelPelletRecipe(fe, key, idStem, variant, iso1, iso2);
                }
            }
            fuelPelletRecipe(fe, key, idStem, "", iso1, iso2);
        }
    }

    private static void fuelPelletRecipe(FissionFuelEntry fe, String key, String idStem, String variant, int iso1, int iso2) {
        Item pellet = fuel(key, variant);
        Item i1 = isotopeItem(fe.group, iso1, variant);
        Item i2 = isotopeItem(fe.group, iso2, variant);
        if (pellet == null || i1 == null || i2 == null) return;
        int count1 = 1;
        int count2 = 8;
        if (fe.name.startsWith("h")) {
            count1 = 3;
            count2 = 6;
        }
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, pellet, 3)
                .group(MODID + "_ingots")
                .requires(i1, count1)
                .requires(i2, count2)
                .unlockedBy("item", has(pellet))
                .save(recipeOutput, rl("fuel_" + idStem + variant + "_cr"));
    }

    private static void moxRecipe(String key, String idStem, String variant, int plutoniumIsotope) {
        Item pellet = fuel(key, variant);
        Item plutonium = isotopeItem("plutonium", plutoniumIsotope, "");
        Item uranium = isotopeItem("uranium", 238, variant);
        if (pellet == null || plutonium == null || uranium == null) return;
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, pellet, 3)
                .group(MODID + "_ingots")
                .requires(plutonium, 1)
                .requires(uranium, 8)
                .unlockedBy("item", has(pellet))
                .save(recipeOutput, rl("fuel_" + idStem + variant + "_cr"));
    }

    private static void fuelSmelt(Item in, Item out, String id) {
        if (in == null || out == null) return;
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(in), RecipeCategory.MISC, out, 1.0f, 100)
                .unlockedBy("item", has(in))
                .save(recipeOutput, rl(id));
    }

    private static Item isotopeItem(String group, int number, String variant) {
        IsotopeEntry e = ModEntries.ISOTOPES.get(group + "/" + number);
        return e == null ? null : isotopeVar(e, variant);
    }

    private static void processors()
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(SUBATOMIC_LIQUIFIER).block())
                .pattern("PYP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', ModEntries.get(CHEMICAL_REACTOR).block())
                .define('Y', ModEntries.get(CENTRIFUGE).block())
                .define('P', ModEntries.get("plate_extreme").item())
                .define('M', ModEntries.get(CENTRIFUGE).block())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get(CENTRIFUGE).block()))
                .save(recipeOutput);

/*        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ANALYZER).block())
                .pattern("PYP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', CARTOGRAPHY_TABLE)
                .define('Y', ENDER_EYE)
                .define('P', ModEntries.get("plate_basic").item())
                .define('M', ModEntries.get("motor").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(CAULDRON))
                .save(recipeOutput);*/



        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(PUMP).block())
                .pattern("PMP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', CAULDRON)
                .define('P', ModEntries.get("plate_basic").item())
                .define('M', ModEntries.get("motor").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(CAULDRON))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(GAS_SCRUBBER).block())
                .pattern("PGP")
                .pattern("CEC")
                .pattern("PMP")
                .define('C', dustTag("borax"))
                .define('P', ModEntries.get("plate_elite").item())
                .define('E', ingotTag("extreme"))
                .define('M', ModEntries.get("motor").item())
                .define('G', IRON_BARS)
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("borax").materialEntry().dust()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(NUCLEAR_FURNACE).block())
                .pattern("PTP")
                .pattern("TFT")
                .pattern("PTP")
                .define('T', Tags.Items.INGOTS_IRON)
                .define('P', ModEntries.get("plate_basic").item())
                .define('F', FURNACE)
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("plate_basic").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(MANUFACTORY).block())
                .pattern("LRL")
                .pattern("FPF")
                .pattern("LSL")
                .define('P', PISTON)
                .define('S', ModEntries.get("coil_copper").item())
                .define('F', FLINT)
                .define('L', ingotTag("lead"))
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(FLINT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(CENTRIFUGE).block())
                .pattern("LSL")
                .pattern("FPF")
                .pattern("LRL")
                .define('P', ModEntries.get("chassis").item())
                .define('S', ingotTag("ferroboron"))
                .define('F', ModEntries.get("motor").item())
                .define('L', ModEntries.get("plate_advanced").item())
                .define('R', ModEntries.get("servo").item())
                .group(MODID+"_processors")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(CHEMICAL_REACTOR).block())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', ModEntries.get("chassis").item())
                .define('S', ModEntries.get("servo").item())
                .define('F', ModEntries.get("motor").item())
                .define('L', ModEntries.get("plate_advanced").item())
                .define('X', Tags.Items.DUSTS_GLOWSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ALLOY_SMELTER).block())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', BLAST_FURNACE)
                .define('S', ModEntries.get("coil_copper").item())
                .define('F', Tags.Items.DUSTS_REDSTONE)
                .define('L', ModEntries.get("plate_basic").item())
                .define('X', Tags.Items.BRICKS)
                .group(MODID+"_processors")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(MELTER).block())
                .pattern("LXL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', ModEntries.get("chassis").item())
                .define('S', ModEntries.get("servo").item())
                .define('L', ModEntries.get("plate_advanced").item())
                .define('X', Tags.Items.BRICKS_NETHER)
                .group(MODID+"_processors")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(INGOT_FORMER).block())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', ModEntries.get("chassis").item())
                .define('S', ingotTag("tough_alloy"))
                .define('F', HOPPER)
                .define('L', ModEntries.get("plate_basic").item())
                .define('X', ingotTag("ferroboron"))
                .group(MODID+"_processors")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(FUEL_REPROCESSOR).block())
                .pattern("PGP")
                .pattern("ECE")
                .pattern("PSP")
                .define('P', ingotTag("tin_silver"))
                .define('S', ModEntries.get("coil_copper").item())
                .define('G', Tags.Items.DUSTS_GLOWSTONE)
                .define('E', ENDER_PEARL)
                .define('C', ModEntries.get("chassis").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(DECAY_HASTENER).block())
                .pattern("PBP")
                .pattern("TCT")
                .pattern("PAP")
                .define('C', ingotTag("tin_silver"))
                .define('P', ingotTag("boron"))
                .define('A', ModEntries.get("actuator").item())
                .define('T', ingotTag("tough_alloy"))
                .define('B', blockTag("boron"))
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("actuator").item()))
                .save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ISOTOPE_SEPARATOR).block())
                .pattern("PMP")
                .pattern("RCR")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_basic").item())
                .define('M', ModEntries.get("motor").item())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', ModEntries.get("chassis").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(PRESSURIZER).block())
                .pattern("PTP")
                .pattern("ACA")
                .pattern("PNP")
                .define('C', ModEntries.get("chassis").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('T', TERRACOTTA)
                .define('N', ANVIL)
                .define('A', ModEntries.get("actuator").item())
                .group(MODID)
                .unlockedBy("has_chassis", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);
        
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(FLUID_ENRICHER).block())
                .pattern("PHP")
                .pattern("LCL")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('C', ModEntries.get("chassis").item())
                .define('L', LAPIS_LAZULI)
                .define('M', ModEntries.get("motor").item())
                .define('H', HOPPER)
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ELECTROLYZER).block())
                .pattern("PGP")
                .pattern("SCS")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', ModEntries.get("coil_copper").item())
                .define('G', ingotTag("graphite"))
                .define('M', ModEntries.get("motor").item())
                .define('C', ModEntries.get("chassis").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("motor").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ASSEMBLER).block())
                .pattern("PHP")
                .pattern("ACA")
                .pattern("PMP")
                .define('P', ModEntries.get("plate_basic").item())
                .define('H', ingotTag("hard_carbon"))
                .define('A', ModEntries.get("actuator").item())
                .define('C', ModEntries.get("chassis").item())
                .define('M', ModEntries.get("motor").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("actuator").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(SUPERCOOLER).block())
                .pattern("PDP")
                .pattern("HCH")
                .pattern("PSP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('D', ingotTag("magnesium_diboride"))
                .define('H', ingotTag("hard_carbon"))
                .define('S', ModEntries.get("servo").item())
                .define('C', ModEntries.get("chassis").item())
                .group(MODID+"_machines")
                .unlockedBy("item", has(ModEntries.get("servo").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(EXTRACTOR).block())
                .pattern("PMP")
                .pattern("BCB")
                .pattern("PSP")
                .define('C', ModEntries.get("chassis").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('M', ingotTag("magnesium"))
                .define('S', ModEntries.get("servo").item())
                .define('B', BUCKET)
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(CRYSTALLIZER).block())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PUP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', ModEntries.get("coil_copper").item())
                .define('C', ModEntries.get("chassis").item())
                .define('U', CAULDRON)
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(STEAM_TURBINE).block())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', ModEntries.get("coil_copper").item())
                .define('C', ModEntries.get("chassis").item())
                .define('U', CAULDRON)
                .define('F', FURNACE)
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(IRRADIATOR).block())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', ModEntries.get("plate_advanced").item())
                .define('S', ModEntries.get("coil_magnesium_diboride").item())
                .define('C', ModEntries.get("chassis").item())
                .define('U', CHEST)
                .define('F', FURNACE)
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(FLUID_INFUSER).block())
                .pattern("PBP")
                .pattern("GCG")
                .pattern("PSP")
                .define('B', BUCKET)
                .define('G', ingotTag("gold"))
                .define('P', ModEntries.get("plate_advanced").item())
                .define('C', ModEntries.get("chassis").item())
                .define('S', ModEntries.get("servo").item())
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(ROCK_CRUSHER).block())
                .pattern("PMP")
                .pattern("ACA")
                .pattern("PTP")
                .define('C', ModEntries.get("chassis").item())
                .define('P', ModEntries.get("plate_advanced").item())
                .define('A', ModEntries.get("actuator").item())
                .define('T', ingotTag("tough_alloy"))
                .define('M', ModEntries.get("motor").item())
                .group(MODID)
                .unlockedBy("has_item", has(ModEntries.get("motor").item()))
                .save(recipeOutput);
    }

    public static void smelting(DeferredItem<Item> inputItem, Ingredient input, ItemLike output) {
        SimpleCookingRecipeBuilder.smelting(input,
                        RecipeCategory.MISC,
                        output, 1.0f, 200)
                .unlockedBy("has_ore", has(inputItem))
                .save(recipeOutput, MODID+":smelting/"+inputItem.getId().getPath());
        SimpleCookingRecipeBuilder.blasting(input,
                        RecipeCategory.MISC,
                        output, 1.0f, 100)
                .unlockedBy("has_ore", has(inputItem))
                .save(recipeOutput, MODID+":blasting/"+inputItem.getId().getPath());
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }
}
