package igentuman.nc.datagen.recipe;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Crafter;
import igentuman.nc.setup.entries.Processors;
import igentuman.nc.setup.entries.Turbine;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
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

/** Generates vanilla crafting and smelting recipes for materials, parts, processors, and structure blocks. */
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
        collectors();
        fissionBlocks();
        fuelPellets();
        storageBlocks();
        energyBlocks();
        turbineBlocks();
        hxBlocks();
        msrBlocks();
        crafterBlocks();
        bomb();
        pipes();
        designerBlocks();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("charging_station"), 1)
                .pattern("SBS")
                .pattern("S S")
                .pattern("STS")
                .define('B', item("basic_barrel"))
                .define('T', item("advanced_voltaic_pile"))
                .define('S', IRON_BARS)
                .group(MODID)
                .unlockedBy("item", has(item("advanced_voltaic_pile")))
                .save(recipeOutput, rl("charging_station"));
/*        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RESONITE_CRYSTAL.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', RESONITE_SHARD.get())
                .group(MODID)
                .unlockedBy("item", has(RESONITE_SHARD.get()))
                .save(recipeOutput);*/
    }

    private static void designerBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("fission_reactor_designer"), 1)
                .pattern("PGP")
                .pattern("GCG")
                .pattern("PGP")
                .define('P', item("plate_basic"))
                .define('G', item("fission_reactor_glass"))
                .define('C', item("advanced_processor"))
                .group(MODID)
                .unlockedBy("item", has(item("advanced_processor")))
                .save(recipeOutput, rl("fission_reactor_designer"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("multiblock_builder"), 1)
                .pattern("PCP")
                .pattern("CFC")
                .pattern("PCP")
                .define('P', item("plate_basic"))
                .define('C', item("basic_processor"))
                .define('F', item("steel_frame"))
                .group(MODID)
                .unlockedBy("item", has(item("steel_frame")))
                .save(recipeOutput, rl("multiblock_builder"));
    }

    private static void hevArmor() {
        var tough = ModEntries.get("tough_armor").armorSetEntry();
        var hev = ModEntries.get("hev_armor").armorSetEntry();
        ItemLike plate = item("plate_extreme");
        ItemLike cell = item("lithium_ion_cell");

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, hev.helmet().get())
                .pattern(" T ").pattern("THT").pattern(" B ")
                .define('T', plate).define('H', tough.helmet().get()).define('B', cell)
                .unlockedBy("item", has(plate))
                .save(recipeOutput, rl("hev_helmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, hev.chestplate().get())
                .pattern("THT").pattern("TBT").pattern("TTT")
                .define('T', plate).define('H', tough.chestplate().get()).define('B', cell)
                .unlockedBy("item", has(plate))
                .save(recipeOutput, rl("hev_chestplate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, hev.leggings().get())
                .pattern("THT").pattern("T T").pattern("B B")
                .define('T', plate).define('H', tough.leggings().get()).define('B', cell)
                .unlockedBy("item", has(plate))
                .save(recipeOutput, rl("hev_leggings"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, hev.boots().get())
                .pattern("THT").pattern("B B")
                .define('T', plate).define('H', tough.boots().get()).define('B', cell)
                .unlockedBy("item", has(plate))
                .save(recipeOutput, rl("hev_boots"));
    }

    private static ItemLike item(String name) {
        return ModEntries.get(name).item();
    }

    private static void pipes() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("pipe"), 8)
                .pattern("BSB")
                .pattern("S S")
                .pattern("BSB")
                .define('B', item("bioplastic"))
                .define('S', plateTag("steel"))
                .unlockedBy("item", has(item("bioplastic")))
                .save(recipeOutput, rl("pipe_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("pipe_connector"), 2)
                .pattern("BSB")
                .pattern("SAS")
                .pattern("BSB")
                .define('A', item("servo"))
                .define('B', item("bioplastic"))
                .define('S', plateTag("steel"))
                .unlockedBy("item", has(item("bioplastic")))
                .save(recipeOutput, rl("pipe_connector_block"));
    }

    private static void tierBarrel(String result, String prev, String metal, String plate) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(result))
                .pattern("GPG").pattern("GBG").pattern("GPG")
                .define('G', plateTag(metal)).define('P', item(plate)).define('B', item(prev))
                .unlockedBy("item", has(item(prev)))
                .save(recipeOutput, rl(result));
    }

    private static void tierContainer(String result, String prev, String metal, String plate) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(result))
                .pattern("DPD").pattern("PCP").pattern("DPD")
                .define('D', plateTag(metal)).define('P', item(plate)).define('C', item(prev))
                .unlockedBy("item", has(item(prev)))
                .save(recipeOutput, rl(result));
    }

    private static void storageBlocks() {
        // Barrels
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("basic_barrel"))
                .pattern("GPG").pattern("G G").pattern("GPG")
                .define('G', plateTag("steel")).define('P', item("plate_basic"))
                .unlockedBy("item", has(item("plate_basic")))
                .save(recipeOutput, rl("basic_barrel"));
        tierBarrel("advanced_barrel", "basic_barrel", "tough_alloy", "plate_advanced");
        tierBarrel("du_barrel", "advanced_barrel", "hsla_steel", "plate_du");
        tierBarrel("elite_barrel", "du_barrel", "platinum", "plate_elite");

        // Containers
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("basic_storage_container"))
                .pattern(" P ").pattern("PCP").pattern(" P ")
                .define('C', CHEST).define('P', item("plate_basic"))
                .unlockedBy("item", has(CHEST))
                .save(recipeOutput, rl("basic_storage_container"));
        tierContainer("advanced_storage_container", "basic_storage_container", "bronze", "plate_advanced");
        tierContainer("du_storage_container", "advanced_storage_container", "platinum", "plate_du");
        tierContainer("elite_storage_container", "du_storage_container", "hsla_steel", "plate_elite");

        // Voltaic piles
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("basic_voltaic_pile"))
                .pattern("PSP").pattern("SMS").pattern("PSP")
                .define('P', item("plate_basic")).define('S', item("coil_copper")).define('M', blockTag("magnesium"))
                .unlockedBy("item", has(item("coil_copper")))
                .save(recipeOutput, rl("basic_voltaic_pile"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("advanced_voltaic_pile"))
                .pattern("PMP").pattern("VVV").pattern("PCP")
                .define('P', item("plate_advanced")).define('M', ingotTag("magnesium"))
                .define('V', item("basic_voltaic_pile")).define('C', ingotTag("zinc"))
                .unlockedBy("item", has(item("basic_voltaic_pile")))
                .save(recipeOutput, rl("advanced_voltaic_pile"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("du_voltaic_pile"))
                .pattern("PMP").pattern("VVV").pattern("PCP")
                .define('P', item("plate_du")).define('M', ingotTag("magnesium"))
                .define('V', item("advanced_voltaic_pile")).define('C', ingotTag("silver"))
                .unlockedBy("item", has(item("advanced_voltaic_pile")))
                .save(recipeOutput, rl("du_voltaic_pile"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("elite_voltaic_pile"))
                .pattern("PMP").pattern("VVV").pattern("PCP")
                .define('P', item("plate_elite")).define('M', plateTag("magnesium"))
                .define('V', item("du_voltaic_pile")).define('C', plateTag("cobalt"))
                .unlockedBy("item", has(item("du_voltaic_pile")))
                .save(recipeOutput, rl("elite_voltaic_pile"));

        // Lithium-ion cell + batteries
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("lithium_ion_cell"))
                .pattern("CCC").pattern("FLF").pattern("DDD")
                .define('C', plateTag("hard_carbon")).define('D', plateTag("lithium_manganese_dioxide"))
                .define('F', plateTag("ferroboron")).define('L', plateTag("lithium"))
                .unlockedBy("item", has(item("plate_basic")))
                .save(recipeOutput, rl("lithium_ion_cell"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("basic_lithium_ion_battery"))
                .pattern("PCP").pattern("CSC").pattern("PCP")
                .define('P', item("plate_basic")).define('C', item("lithium_ion_cell")).define('S', item("coil_copper"))
                .unlockedBy("item", has(item("lithium_ion_cell")))
                .save(recipeOutput, rl("basic_lithium_ion_battery"));
        tierLithiumBattery("advanced_lithium_ion_battery", "basic_lithium_ion_battery", "plate_advanced", ingotTag("lithium_manganese_dioxide"));
        tierLithiumBattery("du_lithium_ion_battery", "advanced_lithium_ion_battery", "plate_du", ingotTag("lithium_manganese_dioxide"));
        tierLithiumBattery("elite_lithium_ion_battery", "du_lithium_ion_battery", "plate_elite", plateTag("lithium_manganese_dioxide"));
    }

    private static void tierLithiumBattery(String result, String prev, String plate, TagKey<Item> dopant) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(result))
                .pattern("PDP").pattern("LLL").pattern("PSP")
                .define('P', item(plate)).define('D', dopant)
                .define('L', item(prev)).define('S', item("coil_magnesium_diboride"))
                .unlockedBy("item", has(item(prev)))
                .save(recipeOutput, rl(result));
    }

    private static void parts() {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("speed_upgrade").item())
                .pattern("LRL")
                .pattern("RPR")
                .pattern("LRL")
                .define('L', dustTag("lapis"))
                .define('R', dustTag("redstone"))
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(recipeOutput, rl("upgrade_speed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("energy_upgrade").item())
                .pattern("ORO")
                .pattern("RPR")
                .pattern("ORO")
                .define('O', dustTag("obsidian"))
                .define('R', dustTag("quartz"))
                .define('P', LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(recipeOutput, rl("upgrade_energy"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("spaxelhoe_thorium").item())
                .pattern("TTT")
                .pattern("TIT")
                .pattern(" I ")
                .define('T', plateTag("thorium"))
                .define('I', ingotTag("iron"))
                .unlockedBy("item", has(ModEntries.get("thorium").materialEntry().plate()))
                .save(recipeOutput, rl("spaxelhoe_thorium"));

        hevArmor();

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

    private static void collectors() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("water_collector"))
                .pattern("PIP")
                .pattern("GBG")
                .pattern("PIP")
                .define('G', plateTag("thermoconducting"))
                .define('P', item("plate_basic"))
                .define('B', NAUTILUS_SHELL)
                .define('I', WATER_BUCKET)
                .group(MODID)
                .unlockedBy("item", has(item("plate_basic")))
                .save(recipeOutput, rl("water_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("compact_water_collector"))
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', item("water_collector"))
                .define('I', plateTag("platinum"))
                .group(MODID)
                .unlockedBy("item", has(item("water_collector")))
                .save(recipeOutput, rl("compact_water_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("lava_collector"))
                .pattern("PIP")
                .pattern("B B")
                .pattern("PIP")
                .define('P', item("plate_advanced"))
                .define('B', plateTag("thermoconducting"))
                .define('I', LAVA_BUCKET)
                .group(MODID)
                .unlockedBy("item", has(item("plate_advanced")))
                .save(recipeOutput, rl("lava_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("nitrogen_collector"))
                .pattern("PIP")
                .pattern("BMB")
                .pattern("PIP")
                .define('M', item("motor"))
                .define('P', item("plate_advanced"))
                .define('B', dustTag("pyrolitic_carbon"))
                .define('I', plateTag("beryllium"))
                .group(MODID)
                .unlockedBy("item", has(item("plate_advanced")))
                .save(recipeOutput, rl("nitrogen_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("compact_nitrogen_collector"))
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', item("nitrogen_collector"))
                .define('I', plateTag("beryllium"))
                .group(MODID)
                .unlockedBy("item", has(item("nitrogen_collector")))
                .save(recipeOutput, rl("compact_nitrogen_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("helium_collector"))
                .pattern("PIP")
                .pattern("BMB")
                .pattern("PIP")
                .define('M', item("motor"))
                .define('P', item("plate_advanced"))
                .define('B', plateTag("thorium"))
                .define('I', ingotTag("thorium"))
                .group(MODID)
                .unlockedBy("item", has(item("plate_advanced")))
                .save(recipeOutput, rl("helium_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("compact_helium_collector"))
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', item("helium_collector"))
                .define('I', plateTag("cobalt"))
                .group(MODID)
                .unlockedBy("item", has(item("helium_collector")))
                .save(recipeOutput, rl("compact_helium_collector"));
    }

    private static void crafterBlocks() {
        // Engineer's Crafting Table
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Crafter.ENGINEERS_CRAFTING_TABLE_BLOCK.get(), 1)
                .pattern("SCS")
                .pattern("PHP")
                .pattern("STS")
                .define('S', item("plate_basic"))
                .define('H', CHEST)
                .define('P', item("basic_electric_circuit"))
                .define('C', CRAFTING_TABLE)
                .define('T', item("basic_voltaic_pile"))
                .group(MODID)
                .unlockedBy("item", has(item("plate_basic")))
                .save(recipeOutput, rl("engineers_crafting_table"));

        // Crafting Pattern
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Crafter.CRAFTING_PATTERN.get())
                .requires(PAPER)
                .requires(Ingredient.of(dustTag("coal")))
                .unlockedBy("item", has(dustTag("coal")))
                .save(recipeOutput, rl("crafting_pattern"));
    }

    private static void bomb() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, part("pu_239_pit"))
                .pattern("FFF").pattern("FNF").pattern("FFF")
                .define('F', isotope("plutonium/239"))
                .define('N', part("neutron_initiator"))
                .unlockedBy("item", has(part("neutron_initiator")))
                .save(recipeOutput, rl("pu_239_pit"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, part("pu_239_core"))
                .pattern("FFF").pattern("FNF").pattern("FFF")
                .define('F', isotope("uranium/238"))
                .define('N', part("pu_239_pit"))
                .unlockedBy("item", has(part("pu_239_pit")))
                .save(recipeOutput, rl("pu_239_core"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("pu_239_bomb"))
                .pattern("CCC").pattern("CNC").pattern("CCC")
                .define('C', part("compression_charge"))
                .define('N', part("pu_239_core"))
                .unlockedBy("item", has(part("pu_239_core")))
                .save(recipeOutput, rl("pu_239_bomb"));
    }

    private static void msrBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_controller").block())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LZL")
                .define('Z', CAULDRON)
                .define('P', ModEntries.get("plate_advanced").item())
                .define('D', ModEntries.get(Processors.DECAY_HASTENER).block())
                .define('T', ModEntries.get(Processors.CHEMICAL_REACTOR).block())
                .define('L', ModEntries.get("fission_reactor_casing").block())
                .group(MODID+"_msr")
                .unlockedBy("item", has(ModEntries.get("plate_advanced").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_fuel_cell").block())
                .pattern("TGT")
                .pattern("GBG")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', BUCKET)
                .define('T', ingotTag("tough_alloy"))
                .group(MODID+"_msr")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").block()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("msr_port").block())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', ModEntries.get("servo").item())
                .define('P', BUCKET)
                .define('T', ModEntries.get("fission_reactor_casing").block())
                .define('L', plateTag("tough_alloy"))
                .group(MODID+"_msr")
                .unlockedBy("item", has(ModEntries.get("fission_reactor_casing").block()))
                .save(recipeOutput);
    }

    private static void turbineBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_casing").item(), 4)
                .pattern("SSS")
                .pattern("SLS")
                .pattern("SSS")
                .define('S', plateTag("hsla_steel"))
                .define('L', ModEntries.get("chassis").item())
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_bearing").item(), 2)
                .pattern("GGG")
                .pattern("GSG")
                .pattern("GGG")
                .define('S', ingotTag("hsla_steel"))
                .define('G', ingotTag("gold"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag("hsla_steel")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_controller").item(), 1)
                .pattern("GCG")
                .pattern("CBC")
                .pattern("GCG")
                .define('C', ModEntries.get("turbine_casing").item())
                .define('G', ModEntries.get("basic_electric_circuit").item())
                .define('B', BUCKET)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag("hsla_steel")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_rotor_shaft").item(), 4)
                .pattern("STS")
                .pattern("STS")
                .pattern("STS")
                .define('S', ingotTag("hsla_steel"))
                .define('T', ingotTag("zinc"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag("hsla_steel")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_port").item(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag("hsla_steel"))
                .define('T', ingotTag("zinc"))
                .define('B', CAULDRON)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ingotTag("hsla_steel")))
                .save(recipeOutput);

        for(String type: Turbine.COILS) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_"+type+"_coil").item(), 1)
                    .pattern("SBS")
                    .pattern("SBS")
                    .pattern("SBS")
                    .define('S', ingotTag("stainless_steel"))
                    .define('B', ingotTag(type))
                    .group(MODID+"_turbine")
                    .unlockedBy("item", has(ingotTag("stainless_steel")))
                    .save(recipeOutput);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_extreme_rotor_blade").item(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag("extreme"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag("extreme")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_steel_rotor_blade").item(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag("steel"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag("steel")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_basic_rotor_blade").item(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag("iron"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag("iron")))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("turbine_sic_sic_cmc_rotor_blade").item(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', plateTag("sic_sic_cmc"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(plateTag("sic_sic_cmc")))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModEntries.get("turbine_glass").item(), 1)
                .requires(ModEntries.get("turbine_casing").item())
                .requires(Tags.Items.GLASS_BLOCKS)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(ModEntries.get("turbine_casing").item()))
                .save(recipeOutput);

    }

    private static void hxBlocks() {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger_casing").item(), 4)
                .pattern("SSS")
                .pattern("SLS")
                .pattern("SSS")
                .define('S', plateTag("hsla_steel"))
                .define('L', ModEntries.get("chassis").item())
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger_controller").item(), 1)
                .pattern("GCG")
                .pattern("CBC")
                .pattern("GCG")
                .define('C', ModEntries.get("heat_exchanger_casing").item())
                .define('G', ModEntries.get("basic_electric_circuit").item())
                .define('B', BUCKET)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("heat_exchanger_casing").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger").item(), 1)
                .pattern("LPL")
                .pattern("PMP")
                .pattern("LPL")
                .define('L', plateTag("thermoconducting"))
                .define('P', plateTag("copper"))
                .define('M', ModEntries.get("chassis").item())
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("chassis").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger_radiator").item(), 2)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("TTT")
                .define('T', ingotTag("copper"))
                .define('A', plateTag("aluminum"))
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("heat_exchanger_casing").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger_hot_coolant_port").item(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag("hsla_steel"))
                .define('T', ingotTag("gold"))
                .define('B', CAULDRON)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("heat_exchanger_casing").item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("heat_exchanger_cold_coolant_port").item(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', ingotTag("hsla_steel"))
                .define('T', ingotTag("zinc"))
                .define('B', CAULDRON)
                .group(MODID+"_heat_exchanger")
                .unlockedBy("item", has(ModEntries.get("heat_exchanger_casing").item()))
                .save(recipeOutput);
    }

/*    private static void solarPanels() {
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

    private static void energyBlocks() {
        rtg("uranium_rtg", "plate_basic", "uranium238");
        rtg("americium_rtg", "plate_advanced", "americium241");
        rtg("plutonium_rtg", "plate_advanced", "plutonium238");
        rtg("californium_rtg", "plate_advanced", "californium250");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("solar_panel_basic"))
                .pattern("LQL").pattern("PLP").pattern("CSC")
                .define('L', Tags.Items.GEMS_LAPIS)
                .define('Q', GLASS_PANE)
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('S', DAYLIGHT_DETECTOR)
                .define('C', item("coil_copper"))
                .group(MODID + "_solar_panels")
                .unlockedBy("item", has(item("coil_copper")))
                .save(recipeOutput);

        solarPanel("solar_panel_advanced", "solar_panel_basic", "plate_advanced", "quartz", "coil_copper");
        solarPanel("solar_panel_du", "solar_panel_advanced", "plate_du", "energetic_blend", "coil_magnesium_diboride");
        solarPanel("solar_panel_elite", "solar_panel_du", "plate_elite", "energetic_blend", "coil_magnesium_diboride");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item("decay_generator"))
                .pattern("PGP").pattern("GUG").pattern("PGP")
                .define('P', item("coil_copper"))
                .define('G', REDSTONE)
                .define('U', IRON_BLOCK)
                .group(MODID + "_energy")
                .unlockedBy("item", has(item("coil_copper")))
                .save(recipeOutput);
    }

    private static void rtg(String result, String plate, String fuelMaterial) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(result))
                .pattern("PGP").pattern("GUG").pattern("PGP")
                .define('P', item(plate))
                .define('G', plateTag("graphite"))
                .define('U', blockTag(fuelMaterial))
                .group(MODID + "_rtg")
                .unlockedBy("item", has(item(plate)))
                .save(recipeOutput);
    }

    private static void solarPanel(String result, String prev, String plate, String dust, String coil) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item(result))
                .pattern("PGP").pattern("SSS").pattern("PCP")
                .define('P', item(plate))
                .define('S', item(prev))
                .define('G', dustTag(dust))
                .define('C', item(coil))
                .group(MODID + "_solar_panels")
                .unlockedBy("item", has(item(prev)))
                .save(recipeOutput);
    }

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
        kugelblitzCraftingRecipes(recipeOutput);

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

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModEntries.get(tier + "_electromagnet").block())
                    .requires(ModEntries.get(tier + "_electromagnet_slope").block())
                    .group(MODID + "_fusion")
                    .unlockedBy("item", has(ModEntries.get(tier + "_electromagnet_slope").block()))
                    .save(out, rl(tier + "_electromagnet_from_slope"));
        }

        fusionMagnetRecipes(out);
    }

    private record MagnetTier(String name, Ingredient coil) {}

    private static void fusionMagnetRecipes(RecipeOutput out) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("basic_rf_amplifier").block())
                .pattern("CCC")
                .pattern("SWS")
                .pattern("CCC")
                .define('C', plateTag("copper"))
                .define('W', ModEntries.get("coil_copper").item())
                .define('S', ingotTag("stainless_steel"))
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(out, rl("basic_rf_amplifier"));

        Item rfBase = ModEntries.get("basic_rf_amplifier").item().get();
        for (MagnetTier t : new MagnetTier[]{
                new MagnetTier("magnesium_diboride", Ingredient.of(ingotTag("magnesium_diboride"))),
                new MagnetTier("niobium_tin", Ingredient.of(ingotTag("niobium_tin"))),
                new MagnetTier("niobium_titanium", Ingredient.of(ingotTag("niobium_titanium"))),
                new MagnetTier("bscco", Ingredient.of(ModEntries.get("coil_bscco").item())),
        }) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(t.name() + "_rf_amplifier").block())
                    .pattern("CCC")
                    .pattern("SBS")
                    .pattern("CCC")
                    .define('C', t.coil())
                    .define('S', ingotTag("stainless_steel"))
                    .define('B', rfBase)
                    .group(MODID + "_fusion")
                    .unlockedBy("item", has(rfBase))
                    .save(out, rl(t.name() + "_rf_amplifier"));
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("basic_electromagnet").block())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', ModEntries.get("coil_copper").item())
                .define('S', ingotTag("stainless_steel"))
                .define('B', ingotTag("tough_alloy"))
                .group(MODID + "_fusion")
                .unlockedBy("item", has(ModEntries.get("coil_copper").item()))
                .save(out, rl("basic_electromagnet"));

        Item emBase = ModEntries.get("basic_electromagnet").item().get();
        for (MagnetTier t : new MagnetTier[]{
                new MagnetTier("magnesium_diboride", Ingredient.of(ModEntries.get("coil_magnesium_diboride").item())),
                new MagnetTier("niobium_tin", Ingredient.of(ingotTag("niobium_tin"))),
                new MagnetTier("niobium_titanium", Ingredient.of(ingotTag("niobium_titanium"))),
                new MagnetTier("bscco", Ingredient.of(ModEntries.get("coil_bscco").item())),
        }) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get(t.name() + "_electromagnet").block())
                    .pattern("CCC")
                    .pattern("SBS")
                    .pattern("CCC")
                    .define('C', t.coil())
                    .define('S', ingotTag("stainless_steel"))
                    .define('B', emBase)
                    .group(MODID + "_fusion")
                    .unlockedBy("item", has(emBase))
                    .save(out, rl(t.name() + "_electromagnet"));
        }
    }

    private static void kugelblitzCraftingRecipes(RecipeOutput out) {
        Item neutronium = ingot("neutronium");
        Item toughAlloy = ingot("tough_alloy");
        if (neutronium == null || toughAlloy == null) return;
        var circuit = ModEntries.get("basic_electric_circuit").item();
        String g = MODID + "_kugelblitz";

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("neutronium_frame").block(), 2)
                .pattern("NTN").pattern("T T").pattern("NTN")
                .define('N', neutronium).define('T', toughAlloy)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("quantum_transformer").block())
                .pattern("NCN").pattern("CEC").pattern("NCN")
                .define('N', ModEntries.get("neutronium_frame").block()).define('C', circuit).define('E', ENDER_EYE)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("quantum_flux_regulator").block())
                .pattern("NGN").pattern("GRG").pattern("NGN")
                .define('N', ModEntries.get("neutronium_frame").block()).define('G', GLASS).define('R', REDSTONE_BLOCK)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("event_horizon_stabilizer").block())
                .pattern("NGN").pattern("GEG").pattern("NGN")
                .define('N', ModEntries.get("neutronium_frame").block()).define('G', GLASS).define('E', ENDER_PEARL)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("photon_concentrator").block())
                .pattern("GQG").pattern("QCQ").pattern("GQG")
                .define('G', GLASS).define('Q', QUARTZ).define('C', circuit)
                .group(g).unlockedBy("item", has(toughAlloy)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("chamber_port").block())
                .pattern("NCN").pattern("N N").pattern("NCN")
                .define('N', ModEntries.get("neutronium_frame").block()).define('C', circuit)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("chamber_terminal").block())
                .pattern("NCN").pattern("CSC").pattern("NCN")
                .define('N', ModEntries.get("neutronium_frame").block()).define('C', circuit).define('S', NETHER_STAR)
                .group(g).unlockedBy("item", has(neutronium)).save(out);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModEntries.get("expl").block())
                .pattern("TCT").pattern("TDT").pattern("TCT")
                .define('T', toughAlloy).define('C', circuit).define('D', DIAMOND)
                .group(g).unlockedBy("item", has(toughAlloy)).save(out);
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

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }
}
