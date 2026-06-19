package igentuman.nc.datagen.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.datagen.recipes.builder.SpecialRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.datagen.recipes.recipes.AbstractRecipeProvider.dustIngredient;
import static igentuman.nc.datagen.recipes.recipes.AbstractRecipeProvider.isotopeIngredient;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.coolers;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.heatsinks;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BLOCK;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.coils;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;
import static igentuman.nc.setup.registration.Tags.*;
import static net.minecraft.world.item.Items.*;

public class NCRecipes extends RecipeProvider {

    public final static int MOLTEN_NUGGET = 10;
    public final static int MOLTEN_INGOT = 90;
    public final static int MOLTEN_BLOCK = 810;
    public NCRecipes(DataGenerator generatorIn) {
        super(generatorIn.getPackOutput());
    }
    public Consumer<FinishedRecipe> consumer;

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;
        materials(consumer);
        parts(consumer);
        items(consumer);
        processors(consumer);
        solarPanels(consumer);
        energyBlocks(consumer);
        storageBlocks(consumer);
        fissionBlocks(consumer);
        msrBlocks(consumer);
        fusionBlocks(consumer);
        turbineBlocks(consumer);
        kugelBlitzBlocks(consumer);
        acceleratorBlocks(consumer);
        targetChamberBlocks(consumer);
        acceleratorItems(consumer);
        FuelRecipes.generate(consumer);
        CustomRecipes.generate(consumer);
        bombs(consumer);
        SpecialRecipeBuilder.build(consumer, NcRecipeSerializers.SHIELDING);
        SpecialRecipeBuilder.build(consumer, NcRecipeSerializers.RESET_NBT);
    }

    private void bombs(Consumer<FinishedRecipe> consumer) {
        // Barium nitrate dust (simplified): dust_barium + gunpowder -> dust_barium_nitrate
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_DUSTS.get(Materials.barium_nitrate).get())
                .requires(forgeDust(Materials.barium))
                .requires(GUNPOWDER)
                .unlockedBy("item", has(forgeDust(Materials.barium)))
                .save(consumer, rl("barium_nitrate_from_barium"));

        // Neutron initiator: ' B '/'BPB'/' B '
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("neutron_initiator").get())
                .pattern(" B ")
                .pattern("BPB")
                .pattern(" B ")
                .define('B', forgeIngot(Materials.beryllium))
                .define('P', forgeDust(Materials.polonium))
                .unlockedBy("item", has(forgeDust(Materials.polonium)))
                .save(consumer, rl("neutron_initiator"));

        // Compression charge: 'STB'/'TB '/'STB'
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("compression_charge").get())
                .pattern("STB")
                .pattern("TB ")
                .pattern("STB")
                .define('S', forgeIngot(Materials.lead))
                .define('T', TNT)
                .define('B', forgeIngot(Materials.baratol))
                .unlockedBy("item", has(forgeIngot(Materials.baratol)))
                .save(consumer, rl("compression_charge"));

        // PU-239 pit: 'FFF'/'FNF'/'FFF'
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("pu_239_pit").get())
                .pattern("FFF")
                .pattern("FNF")
                .pattern("FFF")
                .define('F', isotopeIngredient(Materials.plutonium239))
                .define('N', NC_PARTS.get("neutron_initiator").get())
                .unlockedBy("item", has(NC_PARTS.get("neutron_initiator").get()))
                .save(consumer, rl("pu_239_pit"));

        // PU-239 core: 'FFF'/'FNF'/'FFF' (U-238 tamper around pit)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("pu_239_core").get())
                .pattern("FFF")
                .pattern("FNF")
                .pattern("FFF")
                .define('F', isotopeIngredient(Materials.uranium238))
                .define('N', NC_PARTS.get("pu_239_pit").get())
                .unlockedBy("item", has(NC_PARTS.get("pu_239_pit").get()))
                .save(consumer, rl("pu_239_core"));

        // Final PU-239 bomb block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PU_239_BOMB_ITEM.get())
                .pattern("CCC")
                .pattern("CNC")
                .pattern("CCC")
                .define('C', NC_PARTS.get("compression_charge").get())
                .define('N', NC_PARTS.get("pu_239_core").get())
                .unlockedBy("item", has(NC_PARTS.get("pu_239_core").get()))
                .save(consumer, rl("pu_239_bomb"));
    }

    private void acceleratorItems(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CHARGING_STATION_BLOCK.get(), 1)
                .pattern("SBS")
                .pattern("S S")
                .pattern("STS")
                .define('B', STORAGE_BLOCKS.get("basic_barrel").get())
                .define('T', ENERGY_BLOCKS.get("advanced_voltaic_pile").get())
                .define('S', IRON_BARS)
                .group(MODID)
                .unlockedBy("item", has(ENERGY_BLOCKS.get("advanced_voltaic_pile").get()))
                .save(consumer, rl("charging_station"));
        // Empty cooler (using steel instead of stainless steel as per current mod)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("empty_cooler").get(), 8)
                .pattern("STS")
                .pattern("SHS")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tough_alloy))
                .define('H', forgeIngot(Materials.thermoconducting))
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(forgeIngot(Materials.steel)))
                .save(consumer, rl("empty_cooler"));

        // Detector casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("empty_detector").get(), 4)
                .pattern("STS")
                .pattern("SBS")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tungsten))
                .define('B', NC_PARTS.get("basic_processor").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("basic_processor").get()))
                .save(consumer, rl("empty_detector"));

        // Wire chamber casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("wire_chamber_casing").get())
                .pattern("WWW")
                .pattern("ACA")
                .pattern("WWW")
                .define('W', NC_PARTS.get("wire_gold_tungsten").get())
                .define('A', NC_PARTS.get("advanced_processor").get())
                .define('C', NC_PARTS.get("empty_detector").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("empty_detector").get()))
                .save(consumer, rl("wire_chamber_casing"));

        // Scintillator PWO
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("scintillator_pwo").get())
                .pattern("PPP")
                .pattern("PWP")
                .pattern("PPP")
                .define('P', forgeDust(Materials.lead))
                .define('W', forgeDust(Materials.tungsten))
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(forgeDust(Materials.tungsten)))
                .save(consumer, rl("scintillator_pwo"));

        // Scintillator plastic
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("scintillator_plastic").get())
                .pattern("PPP")
                .pattern("PBP")
                .pattern("PPP")
                .define('P', NC_PARTS.get("bioplastic").get())
                .define('B', forgeDust(Materials.boron))
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("bioplastic").get()))
                .save(consumer, rl("scintillator_plastic"));

        // Laser assembly
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("laser_assembly").get())
                .pattern("GRG")
                .pattern("RDR")
                .pattern("GRG")
                .define('G', GLASS)
                .define('R', AMETHYST_SHARD)
                .define('D', DIAMOND)
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(DIAMOND))
                .save(consumer, rl("laser_assembly"));

        // Tungsten filament source
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ION_SOURCES.get("tungsten_filament").get())
                .pattern("TTT")
                .pattern("TWT")
                .pattern("TTT")
                .define('T', forgeIngot(Materials.tungsten))
                .define('W', NC_PARTS.get("wire_gold_tungsten").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(forgeIngot(Materials.tungsten)))
                .save(consumer, rl("tungsten_filament_source"));

        // Wire gold tungsten
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("wire_gold_tungsten").get(), 4)
                .pattern("GWG")
                .define('G', forgeIngot(Materials.gold))
                .define('W', forgeIngot(Materials.tungsten))
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(forgeIngot(Materials.tungsten)))
                .save(consumer, rl("wire_gold_tungsten"));

    }

    private void targetChamberBlocks(Consumer<FinishedRecipe> consumer) {
        // Target chamber controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_controller").get())
                .pattern("PTP")
                .pattern("BFB")
                .pattern("PTP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('T', forgeIngot(Materials.tough_alloy))
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('F', PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("target_chamber_controller"));

        // Decay chamber controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("decay_chamber_controller").get())
                .pattern("PTP")
                .pattern("BFB")
                .pattern("PTP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('T', forgePlate(Materials.platinum))
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('F', PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get())
                .group(MODID+"_decay_chamber")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("decay_chamber_controller"));

        // Collision chamber controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get())
                .pattern("PTP")
                .pattern("BFB")
                .pattern("PTP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('T', forgePlate(Materials.tough_alloy))
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('F', PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get())
                .group(MODID+"_collision_chamber")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("collision_chamber_controller"));

        // Target chamber casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get(), 4)
                .pattern("STS")
                .pattern("T T")
                .pattern("STS")
                .define('S', forgePlate(Materials.netherite))
                .define('T', forgeIngot(Materials.tungsten))
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("basic_processor").get()))
                .save(consumer, rl("target_chamber_casing"));

        // Target chamber casing glass (shapeless conversion)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing_glass").get())
                .requires(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get())
                .requires(Tags.Items.GLASS)
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get()))
                .save(consumer, rl("target_chamber_casing_glass"));

        // Target chamber casing from glass (reverse conversion)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing").get())
                .requires(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing_glass").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_casing_glass").get()))
                .save(consumer, rl("target_chamber_casing_from_glass"));

        // Target chamber port
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_port").get(), 4)
                .pattern("THT")
                .pattern("VFV")
                .pattern("THT")
                .define('T', forgeIngot(Materials.tungsten))
                .define('H', HOPPER)
                .define('V', NC_PARTS.get("servo").get())
                .define('F', NC_PARTS.get("steel_frame").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("servo").get()))
                .save(consumer, rl("target_chamber_port"));

        // Target chamber beam port
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get(), 4)
                .pattern("STS")
                .pattern("BFB")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tungsten))
                .define('B', ACCELERATOR_BLOCKS.get("particle_beam").get())
                .define('F', NC_PARTS.get("steel_frame").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(ACCELERATOR_BLOCKS.get("particle_beam").get()))
                .save(consumer, rl("target_chamber_beam_port"));

        // Target chamber camera
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get())
                .pattern("NSN")
                .pattern("NCN")
                .pattern("NSN")
                .define('S', forgeIngot(Materials.steel))
                .define('C', NC_PARTS.get("chassis").get())
                .define('N', forgeIngot(Materials.tin))
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer, rl("target_chamber_camera"));

        // Detectors
        // EM Calorimeter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("em_calorimeter").get())
                .pattern("SSS")
                .pattern("SCS")
                .pattern("SSS")
                .define('S', NC_PARTS.get("scintillator_pwo").get())
                .define('C', NC_PARTS.get("empty_detector").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("empty_detector").get()))
                .save(consumer, rl("em_calorimeter"));

        // Hadron Calorimeter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("hadron_calorimeter").get())
                .pattern("SSS")
                .pattern("SCS")
                .pattern("SSS")
                .define('S', NC_PARTS.get("scintillator_plastic").get())
                .define('C', NC_PARTS.get("empty_detector").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("empty_detector").get()))
                .save(consumer, rl("hadron_calorimeter"));

        // Silicon Tracker
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("silicon_tracker").get())
                .pattern("BAB")
                .pattern("ACA")
                .pattern("BAB")
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('A', NC_PARTS.get("advanced_processor").get())
                .define('C', NC_PARTS.get("empty_detector").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("empty_detector").get()))
                .save(consumer, rl("silicon_tracker"));

        // Wire Chamber
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("wire_chamber").get())
                .pattern("WWW")
                .pattern("WCW")
                .pattern("WWW")
                .define('W', NC_PARTS.get("wire_gold_tungsten").get())
                .define('C', NC_PARTS.get("wire_chamber_casing").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("wire_chamber_casing").get()))
                .save(consumer, rl("wire_chamber"));

        // Bubble Chamber
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PARTICLE_CHAMBER_BLOCKS.get("bubble_chamber").get())
                .pattern("GGG")
                .pattern("GCG")
                .pattern("GGG")
                .define('G', GLASS)
                .define('C', NC_PARTS.get("empty_detector").get())
                .group(MODID+"_target_chamber")
                .unlockedBy("item", has(NC_PARTS.get("empty_detector").get()))
                .save(consumer, rl("bubble_chamber"));
    }

    private void acceleratorBlocks(Consumer<FinishedRecipe> consumer) {

        for(String name: coolers().keySet()) {
            if(name.matches(".*water.*|.*liquid.*|.*empty.*|.*cryotheum.*")) {
                continue;
            }
            TagKey<Item> i = forgeDust(name.replace("active_", ""));
            if(name.contains("slime")) {
                i = Tags.Items.SLIMEBALLS;
            }
            if(name.contains("nether_brick")) {
                i = Tags.Items.INGOTS_NETHER_BRICK;
            }
            Block empty = ACCELERATOR_BLOCKS.get("empty_cooler").get();

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get(name+"_cooler").get())
                    .pattern(" I ")
                    .pattern("IBI")
                    .pattern(" I ")
                    .define('I', Ingredient.of(i))
                    .define('B', empty)
                    .group(MODID+"_accelerator")
                    .unlockedBy("item", has(empty))
                    .save(consumer);

        }
        // Accelerator casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_casing").get(), 4)
                .pattern("STS")
                .pattern("T T")
                .pattern("STS")
                .define('S', forgePlate(Materials.cobalt))
                .define('T', forgeIngot(Materials.tough_alloy))
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("basic_processor").get()))
                .save(consumer, rl("accelerator_casing"));

        // Accelerator casing glass (shapeless conversion)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_casing_glass").get())
                .requires(ACCELERATOR_BLOCKS.get("accelerator_casing").get())
                .requires(Tags.Items.GLASS)
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(ACCELERATOR_BLOCKS.get("accelerator_casing").get()))
                .save(consumer, rl("accelerator_casing_glass"));

        // Accelerator casing from glass (reverse conversion)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_casing").get())
                .requires(ACCELERATOR_BLOCKS.get("accelerator_casing_glass").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(ACCELERATOR_BLOCKS.get("accelerator_casing_glass").get()))
                .save(consumer, rl("accelerator_casing_from_glass"));

        // Beam diverter controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("beam_diverter_controller").get())
                .pattern("PEP")
                .pattern("BFB")
                .pattern("PEP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('E', COMPASS)
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('F', ACCELERATOR_BLOCKS.get("accelerator_casing").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("beam_diverter_controller"));

        // Linear accelerator controller
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get())
                .pattern("PEP")
                .pattern("BFB")
                .pattern("PEP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('B', NC_PARTS.get("basic_processor").get())
                .define('F', ACCELERATOR_BLOCKS.get("accelerator_casing").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("linear_accelerator_controller"));

        // Thoroidal accelerator controller (ring accelerator)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("ring_accelerator_controller").get())
                .pattern("PEP")
                .pattern("AFA")
                .pattern("PEP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('A', NC_PARTS.get("advanced_processor").get())
                .define('F', ACCELERATOR_BLOCKS.get("accelerator_casing").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("ring_accelerator_controller"));

        // Accelerator port
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_port").get(), 4)
                .pattern("SHS")
                .pattern("VFV")
                .pattern("SHS")
                .define('S', forgeIngot(Materials.steel))
                .define('H', HOPPER)
                .define('V', NC_PARTS.get("servo").get())
                .define('F', NC_PARTS.get("steel_frame").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("servo").get()))
                .save(consumer, rl("accelerator_port"));

        // Accelerator beam port
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_beam_port").get(), 4)
                .pattern("STS")
                .pattern("BFB")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tough_alloy))
                .define('B', ACCELERATOR_BLOCKS.get("particle_beam").get())
                .define('F', NC_PARTS.get("steel_frame").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(ACCELERATOR_BLOCKS.get("particle_beam").get()))
                .save(consumer, rl("accelerator_beam_port"));

        // Particle beam
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("particle_beam").get(), 3)
                .pattern("SSS")
                .pattern("BBB")
                .pattern("SSS")
                .define('S', forgeIngot(Materials.steel))
                .define('B', GLASS)
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(forgeIngot(Materials.steel)))
                .save(consumer, rl("particle_beam"));

        // Accelerator ion source port
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())
                .pattern("LPL")
                .pattern("EIE")
                .pattern("LPL")
                .define('L', NC_PARTS.get("laser_assembly").get())
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('E', NC_PARTS.get("basic_processor").get())
                .define('I', ION_SOURCES.get("tungsten_filament").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("laser_assembly").get()))
                .save(consumer, rl("accelerator_ion_source_port"));

        // Electromagnet yoke
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACCELERATOR_BLOCKS.get("electromagnet_yoke").get(), 4)
                .pattern("III")
                .pattern("IFI")
                .pattern("III")
                .define('I', forgeIngot(Materials.iron))
                .define('F', NC_PARTS.get("steel_frame").get())
                .group(MODID+"_accelerator")
                .unlockedBy("item", has(NC_PARTS.get("steel_frame").get()))
                .save(consumer, rl("electromagnet_yoke"));
    }

    private void kugelBlitzBlocks(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EXPL_BLOCK.get())
                .pattern(" P ")
                .pattern("CMC")
                .pattern("LBL")
                .define('C', NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet").get())
                .define('M', NC_RF_AMPLIFIERS.get("niobium_titanium_rf_amplifier").get())
                .define('P', NC_PARTS.get("laser_assembly").get())
                .define('B', ENERGY_BLOCKS.get("elite_lithium_ion_battery").get())
                .define('L',  forgePlate("cobalt"))
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("elite_lithium_ion_battery").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .pattern("ETE")
                .pattern("TPT")
                .pattern("ETE")
                .define('E', forgeIngot(Materials.extreme))
                .define('T', forgeIngot(Materials.platinum))
                .define('P', forgeIngot(Materials.neutronium))
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("event_horizon_stabilizer").get())
                .pattern("BQB")
                .pattern("ENE")
                .pattern("BQB")
                .define('B', forgePlate(Materials.platinum))
                .define('Q', forgeDust(Materials.cobalt))
                .define('E', NC_PARTS.get("plate_elite").get())
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("quantum_flux_regulator").get())
                .pattern("CEC")
                .pattern("ENE")
                .pattern("CEC")
                .define('C', NC_PARTS.get("coil_magnesium_diboride").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("quantum_transformer").get())
                .pattern("DCD")
                .pattern("ENE")
                .pattern("DCD")
                .define('D', forgeDust(Materials.dimensional_blend))
                .define('C', NC_PARTS.get("basic_electric_circuit").get())
                .define('E', NC_PARTS.get("plate_extreme").get())
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("photon_concentrator").get())
                .pattern("GQG")
                .pattern("ENE")
                .pattern("GQG")
                .define('G', GLASS)
                .define('Q', forgeDust(Materials.quartz))
                .define('E', NC_PARTS.get("plate_elite").get())
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("chamber_port").get())
                .pattern("SPS")
                .pattern("ENE")
                .pattern("SPS")
                .define('S', NC_PARTS.get("servo").get())
                .define('P', NC_PARTS.get("plate_extreme").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KUGELBLITZ_BLOCKS.get("chamber_terminal").get())
                .pattern("CPC")
                .pattern("ENE")
                .pattern("CPC")
                .define('C', NC_PARTS.get("basic_electric_circuit").get())
                .define('P', NC_PARTS.get("plate_extreme").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('N', KUGELBLITZ_BLOCKS.get("neutronium_frame").get())
                .group(MODID+"_kugelblitz")
                .unlockedBy("item", has(KUGELBLITZ_BLOCKS.get("neutronium_frame").get()))
                .save(consumer);
    }

    private void fusionBlocks(Consumer<FinishedRecipe> consumer) {
        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MULTIBLOCK_BUILDER_BLOCK.get())
                .pattern("LPL")
                .pattern("CMC")
                .pattern("LPL")
                .define('C', BEACON)
                .define('M', NC_PARTS.get("chassis").get())
                .define('P', REPEATER)
                .define('L',  NC_PARTS.get("plate_basic").get())
                .group(MODID+"_builder")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);*/

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, REDSTONE_DIMMER_BLOCK.get())
                .pattern("LPL")
                .pattern("CMC")
                .pattern("LPL")
                .define('C', COMPARATOR)
                .define('M', NC_PARTS.get("chassis").get())
                .define('P', REPEATER)
                .define('L',  NC_PARTS.get("plate_basic").get())
                .group(MODID+"_dimmer")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FUSION_BLOCKS.get("fusion_core").get())
                .pattern("LPL")
                .pattern("CMC")
                .pattern("LPL")
                .define('C', NCProcessors.PROCESSORS.get(Processors.CHEMICAL_REACTOR).get())
                .define('M', NC_PARTS.get("chassis").get())
                .define('P', NC_PARTS.get("silicon_n_doped").get())
                .define('L',  NC_PARTS.get("plate_elite").get())
                .group(MODID+"_fusion")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FUSION_BLOCKS.get("fusion_reactor_connector").get())
                .pattern("LPL")
                .pattern("PMP")
                .pattern("LPL")
                .define('M', NC_PARTS.get("basic_electric_circuit").get())
                .define('P', forgePlate(Materials.platinum))
                .define('L',  NC_PARTS.get("plate_advanced").get())
                .group(MODID+"_fusion")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FUSION_BLOCKS.get("fusion_reactor_casing").get())
                .pattern("LPL")
                .pattern("PMP")
                .pattern("LPL")
                .define('M', NC_PARTS.get("coil_copper").get())
                .define('P', forgePlate(Materials.cobalt))
                .define('L',  NC_PARTS.get("plate_advanced").get())
                .group(MODID+"_fusion")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FUSION_BLOCKS.get("fusion_reactor_casing_glass").get())
                .pattern(" G ")
                .pattern("GMG")
                .pattern(" G ")
                .define('G', Tags.Items.GLASS)
                .define('M', FUSION_BLOCKS.get("fusion_reactor_casing").get())
                .group(MODID+"_fusion")
                .unlockedBy("item", has(FUSION_BLOCKS.get("fusion_reactor_casing").get()))
                .save(consumer);
    }

    private void storageBlocks(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("basic_storage_container").get())
                .pattern(" P ")
                .pattern("PCP")
                .pattern(" P ")
                .define('C', CHEST)
                .define('P', NC_PARTS.get("plate_basic").get())
                .unlockedBy("item", has(CHEST))
                .save(consumer, rl("basic_storage_container"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("advanced_storage_container").get())
                .pattern("DPD")
                .pattern("PCP")
                .pattern("DPD")
                .define('C', STORAGE_BLOCKS.get("basic_storage_container").get())
                .define('D', forgePlate(Materials.bronze))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .unlockedBy("item", has(STORAGE_BLOCKS.get("basic_storage_container").get()))
                .save(consumer, rl("advanced_storage_container"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("du_storage_container").get())
                .pattern("DPD")
                .pattern("PCP")
                .pattern("DPD")
                .define('C', STORAGE_BLOCKS.get("advanced_storage_container").get())
                .define('D', forgePlate(Materials.platinum))
                .define('P', NC_PARTS.get("plate_du").get())
                .unlockedBy("item", has(STORAGE_BLOCKS.get("advanced_storage_container").get()))
                .save(consumer, rl("du_storage_container"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("elite_storage_container").get())
                .pattern("DPD")
                .pattern("PCP")
                .pattern("DPD")
                .define('C', STORAGE_BLOCKS.get("du_storage_container").get())
                .define('D', forgePlate(Materials.hsla_steel))
                .define('P', NC_PARTS.get("plate_elite").get())
                .unlockedBy("item", has(STORAGE_BLOCKS.get("du_storage_container").get()))
                .save(consumer, rl("elite_storage_container"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("basic_barrel").get())
                .pattern("GPG")
                .pattern("G G")
                .pattern("GPG")
                .define('G', forgePlate(Materials.steel))
                .define('P', NC_PARTS.get("plate_basic").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer, rl("basic_barrel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("advanced_barrel").get())
                .pattern("GPG")
                .pattern("GBG")
                .pattern("GPG")
                .define('B', STORAGE_BLOCKS.get("basic_barrel").get())
                .define('G', forgePlate(Materials.tough_alloy))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("advanced_barrel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("du_barrel").get())
                .pattern("GPG")
                .pattern("GBG")
                .pattern("GPG")
                .define('B', STORAGE_BLOCKS.get("advanced_barrel").get())
                .define('G', forgePlate(Materials.hsla_steel))
                .define('P', NC_PARTS.get("plate_du").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_du").get()))
                .save(consumer, rl("du_barrel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STORAGE_BLOCKS.get("elite_barrel").get())
                .pattern("GPG")
                .pattern("GBG")
                .pattern("GPG")
                .define('B', STORAGE_BLOCKS.get("du_barrel").get())
                .define('G', forgePlate(Materials.platinum))
                .define('P', NC_PARTS.get("plate_elite").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("elite_barrel"));

    }

    private void energyBlocks(Consumer<FinishedRecipe> consumer) {

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PAPER)
                        .requires(ALL_NC_ITEMS.get("research_paper").get(), 2)
                        .unlockedBy("item", has(ALL_NC_ITEMS.get("research_paper").get()))
                        .save(consumer, rl("paper"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("decay_generator").get())
                .pattern("PGP")
                .pattern("GUG")
                .pattern("PGP")
                .define('G', REDSTONE)
                .define('P', NC_PARTS.get("coil_copper").get())
                .define('U', IRON_BLOCK)
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer, rl("decay_generator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("uranium_rtg").get())
                .pattern("PGP")
                .pattern("GUG")
                .pattern("PGP")
                .define('G', forgePlate(Materials.graphite))
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('U', forgeBlock("uranium238"))
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer, rl("uranium_rtg"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("plutonium_rtg").get())
                .pattern("PGP")
                .pattern("GUG")
                .pattern("PGP")
                .define('G', forgePlate(Materials.graphite))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('U', forgeBlock("plutonium238"))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("plutonium_rtg"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("americium_rtg").get())
                .pattern("PGP")
                .pattern("GUG")
                .pattern("PGP")
                .define('G', forgePlate(Materials.graphite))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('U', forgeBlock("americium241"))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("americium_rtg"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("californium_rtg").get())
                .pattern("PGP")
                .pattern("GUG")
                .pattern("PGP")
                .define('G', forgePlate(Materials.graphite))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('U', forgeBlock("californium250"))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("californium_rtg"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("basic_voltaic_pile").get())
                .pattern("PSP")
                .pattern("SMS")
                .pattern("PSP")
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('M', forgeBlock(Materials.magnesium))
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer, rl("basic_voltaic_pile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("advanced_voltaic_pile").get())
                .pattern("PMP")
                .pattern("VVV")
                .pattern("PCP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('V', ENERGY_BLOCKS.get("basic_voltaic_pile").get())
                .define('M', forgeIngot(Materials.magnesium))
                .define('C', forgeIngot(Materials.zinc))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("advanced_voltaic_pile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("du_voltaic_pile").get())
                .pattern("PMP")
                .pattern("VVV")
                .pattern("PCP")
                .define('P', NC_PARTS.get("plate_du").get())
                .define('V', ENERGY_BLOCKS.get("advanced_voltaic_pile").get())
                .define('M', forgeIngot(Materials.magnesium))
                .define('C', forgeIngot(Materials.silver))
                .unlockedBy("item", has(NC_PARTS.get("plate_du").get()))
                .save(consumer, rl("du_voltaic_pile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("elite_voltaic_pile").get())
                .pattern("PMP")
                .pattern("VVV")
                .pattern("PCP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('V', ENERGY_BLOCKS.get("du_voltaic_pile").get())
                .define('M', forgePlate(Materials.magnesium))
                .define('C', forgePlate(Materials.cobalt))
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("elite_voltaic_pile"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("basic_lithium_ion_battery").get())
                .pattern("PCP")
                .pattern("CSC")
                .pattern("PCP")
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('C', LITHIUM_ION_CELL.get())
                .define('S', NC_PARTS.get("coil_copper").get())
                .unlockedBy("item", has(LITHIUM_ION_CELL.get()))
                .save(consumer, rl("basic_lithium_ion_battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("advanced_lithium_ion_battery").get())
                .pattern("PDP")
                .pattern("LLL")
                .pattern("PSP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('D', forgeIngot(Materials.lithium_manganese_dioxide))
                .define('L', ENERGY_BLOCKS.get("basic_lithium_ion_battery").get())
                .define('S', NC_PARTS.get("coil_magnesium_diboride").get())
                .unlockedBy("item", has(NC_PARTS.get("coil_magnesium_diboride").get()))
                .save(consumer, rl("advanced_lithium_ion_battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("du_lithium_ion_battery").get())
                .pattern("PDP")
                .pattern("LLL")
                .pattern("PSP")
                .define('P', NC_PARTS.get("plate_du").get())
                .define('D', forgeIngot(Materials.lithium_manganese_dioxide))
                .define('L', ENERGY_BLOCKS.get("advanced_lithium_ion_battery").get())
                .define('S', NC_PARTS.get("coil_magnesium_diboride").get())
                .unlockedBy("item", has(NC_PARTS.get("coil_magnesium_diboride").get()))
                .save(consumer, rl("du_lithium_ion_battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("elite_lithium_ion_battery").get())
                .pattern("PDP")
                .pattern("LLL")
                .pattern("PSP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('D', forgePlate(Materials.lithium_manganese_dioxide))
                .define('L', ENERGY_BLOCKS.get("du_lithium_ion_battery").get())
                .define('S', NC_PARTS.get("coil_magnesium_diboride").get())
                .unlockedBy("item", has(NC_PARTS.get("coil_magnesium_diboride").get()))
                .save(consumer, rl("elite_lithium_ion_battery"));

    }

    private void items(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get())
                .pattern("CCC")
                .pattern("SWS")
                .pattern("CCC")
                .define('C', forgePlate(Materials.copper))
                .define('W', NC_PARTS.get("coil_copper").get())
                .define('S', forgeIngot(Materials.stainless_steel))
                .unlockedBy("item", has(forgePlate(Materials.copper)))
                .save(consumer, rl("basic_rf_amplifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_RF_AMPLIFIERS.get("magnesium_diboride_rf_amplifier").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', forgeIngot(Materials.magnesium_diboride))
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get())
                .unlockedBy("item", has(NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get()))
                .save(consumer, rl("magnesium_diboride_rf_amplifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_RF_AMPLIFIERS.get("niobium_tin_rf_amplifier").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', forgeIngot(Materials.niobium_tin))
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get())
                .unlockedBy("item", has(NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get()))
                .save(consumer, rl("niobium_tin_rf_amplifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_RF_AMPLIFIERS.get("niobium_titanium_rf_amplifier").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', forgeIngot(Materials.niobium_titanium))
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get())
                .unlockedBy("item", has(NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get()))
                .save(consumer, rl("niobium_titanium_rf_amplifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_RF_AMPLIFIERS.get("bscco_rf_amplifier").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', NC_PARTS.get("coil_bscco").get())
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get())
                .unlockedBy("item", has(NC_RF_AMPLIFIERS.get("basic_rf_amplifier").get()))
                .save(consumer, rl("bscco_rf_amplifier"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', NC_PARTS.get("coil_copper").get())
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer, rl("basic_electromagnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', NC_PARTS.get("coil_magnesium_diboride").get())
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet").get()))
                .save(consumer, rl("magnesium_diboride_electromagnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_tin_electromagnet").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', forgeIngot(Materials.niobium_tin))
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet").get()))
                .save(consumer, rl("niobium_tin_electromagnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', forgeIngot(Materials.niobium_titanium))
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet").get()))
                .save(consumer, rl("niobium_titanium_electromagnet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("bscco_electromagnet").get())
                .pattern("CCC")
                .pattern("SBS")
                .pattern("CCC")
                .define('C', NC_PARTS.get("coil_bscco").get())
                .define('S', forgeIngot(Materials.stainless_steel))
                .define('B', NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet").get()))
                .save(consumer, rl("bscco_electromagnet"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .requires(NC_ELECTROMAGNETS.get("basic_electromagnet_slope").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet_slope").get()))
                .save(consumer, rl("basic_electromagnet_s_n"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("basic_electromagnet_slope").get())
                .requires(NC_ELECTROMAGNETS.get("basic_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("basic_electromagnet").get()))
                .save(consumer, rl("basic_electromagnet_n_s"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet").get())
                .requires(NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet_slope").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet_slope").get()))
                .save(consumer, rl("magnesium_diboride_electromagnet_s_n"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet_slope").get())
                .requires(NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("magnesium_diboride_electromagnet").get()))
                .save(consumer, rl("magnesium_diboride_electromagnet_n_s"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_tin_electromagnet").get())
                .requires(NC_ELECTROMAGNETS.get("niobium_tin_electromagnet_slope").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("niobium_tin_electromagnet_slope").get()))
                .save(consumer, rl("niobium_tin_electromagnet_s_n"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_tin_electromagnet_slope").get())
                .requires(NC_ELECTROMAGNETS.get("niobium_tin_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("niobium_tin_electromagnet").get()))
                .save(consumer, rl("niobium_tin_electromagnet_n_s"));


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet").get())
                .requires(NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet_slope").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet_slope").get()))
                .save(consumer, rl("niobium_titanium_electromagnet_s_n"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet_slope").get())
                .requires(NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("niobium_titanium_electromagnet").get()))
                .save(consumer, rl("niobium_titanium_electromagnet_n_s"));


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("bscco_electromagnet").get())
                .requires(NC_ELECTROMAGNETS.get("bscco_electromagnet_slope").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("bscco_electromagnet_slope").get()))
                .save(consumer, rl("bscco_electromagnet_s_n"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_ELECTROMAGNETS.get("bscco_electromagnet_slope").get())
                .requires(NC_ELECTROMAGNETS.get("bscco_electromagnet").get())
                .unlockedBy("item", has(NC_ELECTROMAGNETS.get("bscco_electromagnet").get()))
                .save(consumer, rl("bscco_electromagnet_n_s"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, LITHIUM_ION_CELL.get())
                .pattern("CCC")
                .pattern("FLF")
                .pattern("DDD")
                .define('C', forgePlate(Materials.hard_carbon))
                .define('F', forgePlate(Materials.ferroboron))
                .define('L', forgePlate(Materials.lithium))
                .define('D', forgePlate(Materials.lithium_manganese_dioxide))
                .unlockedBy("item", has(forgeIngot(Materials.lithium_manganese_dioxide)))
                .save(consumer, rl("lithium_ion_cell"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("lava_collector").get())
                .pattern("PIP")
                .pattern("B B")
                .pattern("PIP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('B', forgePlate(Materials.thermoconducting))
                .define('I', LAVA_BUCKET)
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("lava_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("water_collector").get())
                .pattern("PIP")
                .pattern("GBG")
                .pattern("PIP")
                .define('G', forgePlate(Materials.thermoconducting))
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('B', NAUTILUS_SHELL)
                .define('I', WATER_BUCKET)
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer, rl("water_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("compact_water_collector").get())
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', NC_ITEMS.get("water_collector").get())
                .define('I', forgePlate(Materials.platinum))
                .unlockedBy("item", has(NC_ITEMS.get("water_collector").get()))
                .save(consumer, rl("compact_water_collector"));


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("nitrogen_collector").get())
                .pattern("PIP")
                .pattern("BMB")
                .pattern("PIP")
                .define('M', NC_PARTS.get("motor").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('B', forgeDust(Materials.pyrolitic_carbon))
                .define('I', forgePlate(Materials.beryllium))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("nitrogen_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("compact_nitrogen_collector").get())
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', NC_ITEMS.get("nitrogen_collector").get())
                .define('I', forgePlate(Materials.beryllium))
                .unlockedBy("item", has(NC_ITEMS.get("nitrogen_collector").get()))
                .save(consumer, rl("compact_nitrogen_collector"));


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("helium_collector").get())
                .pattern("PIP")
                .pattern("BMB")
                .pattern("PIP")
                .define('M', NC_PARTS.get("motor").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('B', forgePlate(Materials.thorium))
                .define('I', forgeIngot(Materials.thorium))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("helium_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_ITEMS.get("compact_helium_collector").get())
                .pattern("CCC")
                .pattern("CIC")
                .pattern("CCC")
                .define('C', NC_ITEMS.get("helium_collector").get())
                .define('I', forgePlate(Materials.cobalt))
                .unlockedBy("item", has(NC_ITEMS.get("helium_collector").get()))
                .save(consumer, rl("compact_helium_collector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HEV_HELMET.get())
                .pattern(" T ")
                .pattern("THT")
                .pattern(" B ")
                .define('H', HAZMAT_MASK.get())
                .define('B', LITHIUM_ION_CELL.get())
                .define('T', NC_PARTS.get("plate_extreme").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer, rl("hev_helmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HEV_BOOTS.get())
                .pattern("   ")
                .pattern("THT")
                .pattern("TBT")
                .define('H', HAZMAT_BOOTS.get())
                .define('B', LITHIUM_ION_CELL.get())
                .define('T', NC_PARTS.get("plate_extreme").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer, rl("hev_boots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HEV_PANTS.get())
                .pattern("TTT")
                .pattern("TBT")
                .pattern("THT")
                .define('H', HAZMAT_PANTS.get())
                .define('B', LITHIUM_ION_CELL.get())
                .define('T', NC_PARTS.get("plate_extreme").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer, rl("hev_pants"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HEV_CHEST.get())
                .pattern("THT")
                .pattern("TBT")
                .pattern("TTT")
                .define('H', HAZMAT_CHEST.get())
                .define('B', LITHIUM_ION_CELL.get())
                .define('T', NC_PARTS.get("plate_extreme").get())
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer, rl("hev_chest"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TOUGH_HELMET.get())
                .pattern("TTT")
                .pattern("T T")
                .pattern("   ")
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("tough_helmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TOUGH_BOOTS.get())
                .pattern("   ")
                .pattern("T T")
                .pattern("T T")
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("tough_boots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TOUGH_PANTS.get())
                .pattern("TTT")
                .pattern("T T")
                .pattern("T T")
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("tough_pants"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TOUGH_CHEST.get())
                .pattern("T T")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("tough_chest"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAZMAT_MASK.get())
                .pattern("BIB")
                .pattern("BLB")
                .pattern("YWY")
                .define('Y', NC_SHIELDING.get("light").get())
                .define('W', YELLOW_WOOL)
                .define('I', forgePlate(Materials.steel))
                .define('L', LEATHER_HELMET)
                .define('B', ALL_NC_ITEMS.get("bioplastic").get())
                .unlockedBy("item", has(NC_SHIELDING.get("light").get()))
                .save(consumer, rl("hazmat_head"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAZMAT_CHEST.get())
                .pattern("BWB")
                .pattern("YLY")
                .pattern("YYY")
                .define('Y', NC_SHIELDING.get("light").get())
                .define('W', YELLOW_WOOL)
                .define('L', LEATHER_CHESTPLATE)
                .define('B', ALL_NC_ITEMS.get("bioplastic").get())
                .unlockedBy("item", has(NC_SHIELDING.get("light").get()))
                .save(consumer, rl("hazmat_chest"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAZMAT_PANTS.get())
                .pattern("YYY")
                .pattern("YLY")
                .pattern("YWY")
                .define('Y', NC_SHIELDING.get("light").get())
                .define('W', YELLOW_WOOL)
                .define('L', LEATHER_LEGGINGS)
                .unlockedBy("item", has(NC_SHIELDING.get("light").get()))
                .save(consumer, rl("hazmat_pants"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAZMAT_BOOTS.get())
                .pattern("BIB")
                .pattern("YLY")
                .pattern("YWY")
                .define('Y', NC_SHIELDING.get("light").get())
                .define('W', BLACK_WOOL)
                .define('L', LEATHER_BOOTS)
                .define('B', ALL_NC_ITEMS.get("bioplastic").get())
                .define('I', forgeIngot(Materials.steel))
                .unlockedBy("item", has(NC_SHIELDING.get("light").get()))
                .save(consumer, rl("hazmat_boots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_SHIELDING.get("light").get())
                .pattern("III")
                .pattern("CCC")
                .pattern("LLL")
                .define('I', forgePlate(Materials.iron))
                .define('C', forgePlate(Materials.graphite))
                .define('L', forgePlate(Materials.lead))
                .unlockedBy("item", has(forgePlate(Materials.lead)))
                .save(consumer, rl("light_shielding"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_SHIELDING.get("medium").get())
                .pattern("BBB")
                .pattern("RFR")
                .pattern("PPP")
                .define('B', NC_PARTS.get("bioplastic").get())
                .define('F', forgeIngot(Materials.ferroboron))
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('R', NC_SHIELDING.get("light").get())
                .unlockedBy("item", has(NC_SHIELDING.get("light").get()))
                .save(consumer, rl("medium_shielding"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_SHIELDING.get("heavy").get())
                .pattern("BBB")
                .pattern("RHR")
                .pattern("PPP")
                .define('B', forgePlate(Materials.beryllium))
                .define('H', forgePlate(Materials.hard_carbon))
                .define('P', NC_PARTS.get("plate_du").get())
                .define('R', NC_SHIELDING.get("medium").get())
                .unlockedBy("item", has(NC_SHIELDING.get("medium").get()))
                .save(consumer, rl("heavy_shielding"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_SHIELDING.get("dps").get())
                .pattern("BBB")
                .pattern("RHR")
                .pattern("PPP")
                .define('B', forgeIngot(Materials.lead_platinum))
                .define('H', forgePlate(Materials.hsla_steel))
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('R', NC_SHIELDING.get("heavy").get())
                .unlockedBy("item", has(NC_SHIELDING.get("heavy").get()))
                .save(consumer, rl("dps_shielding"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_FOOD.get("rad_x").get())
                .pattern("BIB")
                .pattern("IRI")
                .pattern("BIB")
                .define('B', ALL_NC_ITEMS.get("bioplastic").get())
                .define('R', ALL_NC_ITEMS.get("radaway").get())
                .define('I', forgeDust(Materials.potassium_iodide))
                .unlockedBy("item", has(forgeDust(Materials.potassium_iodide)))
                .save(consumer, rl("rad_x"));

        SimpleCookingRecipeBuilder.smelting(NcIngredient.of(COCOA_BEANS),
                        RecipeCategory.MISC,
                        ALL_NC_ITEMS.get("roasted_cocoa_beans").get(), 1.0f, 200)
                .unlockedBy("has_ore", has(COCOA_BEANS))
                .save(consumer, MODID+"_roasted_cocoa_beans");

        SimpleCookingRecipeBuilder.smoking(NcIngredient.of(COCOA_BEANS),
                        RecipeCategory.MISC,
                        ALL_NC_ITEMS.get("roasted_cocoa_beans").get(), 1.0f, 100)
                .unlockedBy("has_ore", has(COCOA_BEANS))
                .save(consumer, MODID+"_roasted_cocoa_beans_smoked");

        SimpleCookingRecipeBuilder.smelting(NcIngredient.of(MILK_BUCKET),
                        RecipeCategory.MISC,
                        NCFluids.ALL_FLUID_ENTRIES.get("pasteurized_milk").bucket().get(), 1.0f, 200)
                .unlockedBy("has_ore", has(MILK_BUCKET))
                .save(consumer, MODID+"_pasteurized_milk");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ALL_NC_ITEMS.get("dosimeter").get())
                .pattern(" G ")
                .pattern("SBS")
                .pattern(" L ")
                .define('S', STRING)
                .define('L', forgePlate(Materials.lead))
                .define('G', ALL_NC_ITEMS.get("gelatin").get())
                .define('B', NC_PARTS.get("bioplastic").get())
                .unlockedBy("item", has(ALL_NC_ITEMS.get("gelatin").get()))
                .save(consumer, rl("dosimeter"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GEIGER_COUNTER.get())
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
                .save(consumer, rl("geiger_counter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ALL_NC_ITEMS.get("smore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("graham_cracker").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("graham_cracker").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("graham_cracker").get()))
                .save(consumer, rl("smore"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ALL_NC_ITEMS.get("moresmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("smore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("smore").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("smore").get()))
                .save(consumer, rl("moresmore"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ALL_NC_ITEMS.get("foursmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("moresmore").get())
                .requires(NCItems.ALL_NC_ITEMS.get("milk_chocolate").get())
                .requires(NCItems.ALL_NC_ITEMS.get("marshmallow").get())
                .requires(NCItems.ALL_NC_ITEMS.get("moresmore").get())
                .unlockedBy("item", has(NCItems.ALL_NC_ITEMS.get("moresmore").get()))
                .save(consumer, rl("foursmore"));
    }

    private void parts(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SPAXELHOE_THORIUM.get())
                .pattern("TTT")
                .pattern("TIT")
                .pattern(" I ")
                .define('T', forgePlate(Materials.thorium))
                .define('I', forgeIngot("iron"))
                .unlockedBy("item", has(forgePlate(Materials.thorium)))
                .save(consumer, rl("spaxelhoe_thorium"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ALL_NC_ITEMS.get("upgrade_speed").get())
                .pattern("LRL")
                .pattern("RPR")
                .pattern("LRL")
                .define('L', forgeDust(Materials.lapis))
                .define('R', forgeDust("redstone"))
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer, rl("upgrade_speed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ALL_NC_ITEMS.get("upgrade_energy").get())
                .pattern("ORO")
                .pattern("RPR")
                .pattern("ORO")
                .define('O', forgeDust(Materials.obsidian))
                .define('R', forgeDust(Materials.quartz))
                .define('P', LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("item", has(HEAVY_WEIGHTED_PRESSURE_PLATE))
                .save(consumer, rl("upgrade_energy"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_basic").get(), 2)
                .pattern("LG")
                .pattern("GL")
                .define('L', forgeIngot(Materials.lead))
                .define('G', forgeDust(Materials.graphite))
                .unlockedBy("item", has(forgeIngot(Materials.lead)))
                .save(consumer, rl("plate_basic"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_basic").get(), 2)
                .pattern("GL")
                .pattern("LG")
                .define('L', forgeIngot(Materials.lead))
                .define('G', forgeDust(Materials.graphite))
                .unlockedBy("item", has(forgeIngot(Materials.lead)))
                .save(consumer, rl("plate_basic2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_advanced").get(), 2)
                .pattern(" P ")
                .pattern("TRT")
                .pattern(" P ")
                .define('R', REDSTONE)
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer, rl("plate_advanced"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_du").get())
                .pattern("SUS")
                .pattern("UPU")
                .pattern("SUS")
                .define('U', NC_ISOTOPES.get("uranium/238").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', forgeDust(Materials.sulfur))
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("plate_du"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_elite").get())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', forgeDust(Materials.crystal_binder))
                .define('P', NC_PARTS.get("plate_du").get())
                .define('B', forgeIngot(Materials.boron))
                .unlockedBy("item", has(NC_PARTS.get("plate_du").get()))
                .save(consumer, rl("plate_elite"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("plate_extreme").get())
                .pattern("RBR")
                .pattern("BPB")
                .pattern("RBR")
                .define('R', forgeDust(Materials.hsla_steel))
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('B', forgeIngot(Materials.extreme))
                .unlockedBy("item", has(NC_PARTS.get("plate_elite").get()))
                .save(consumer, rl("plate_extreme"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("coil_copper").get())
                .pattern("CC ")
                .pattern("II ")
                .pattern("CC ")
                .define('C', forgeIngot(Materials.copper))
                .define('I', forgeIngot(Materials.iron))
                .unlockedBy("item", has(forgeIngot(Materials.copper)))
                .save(consumer, rl("coil_copper"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("coil_magnesium_diboride").get())
                .pattern("MM ")
                .pattern("TT ")
                .pattern("MM ")
                .define('M', forgeIngot(Materials.magnesium_diboride))
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.magnesium_diboride)))
                .save(consumer, rl("coil_magnesium_diboride"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("servo").get())
                .pattern("F F")
                .pattern("RSR")
                .pattern("SCS")
                .define('F', forgeIngot(Materials.ferroboron))
                .define('S', forgeIngot(Materials.steel))
                .define('R', REDSTONE)
                .define('C', forgeIngot(Materials.copper))
                .unlockedBy("item", has(forgeIngot(Materials.ferroboron)))
                .save(consumer, rl("servo"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("motor").get())
                .pattern("SSG")
                .pattern("CCI")
                .pattern("SSG")
                .define('G', forgeNugget(Materials.gold))
                .define('S', forgeIngot(Materials.steel))
                .define('I', forgeIngot(Materials.iron))
                .define('C', NC_PARTS.get("coil_copper").get())
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer, rl("motor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("actuator").get())
                .pattern("  S")
                .pattern("FP ")
                .pattern("CF ")
                .define('F', forgeIngot(Materials.ferroboron))
                .define('S', forgeIngot(Materials.steel))
                .define('C', forgeIngot(Materials.copper))
                .define('P', PISTON)
                .unlockedBy("item", has(PISTON))
                .save(consumer, rl("actuator"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("chassis").get())
                .pattern("LSL")
                .pattern("STS")
                .pattern("LSL")
                .define('S', forgeIngot(Materials.steel))
                .define('L', forgeIngot(Materials.lead))
                .define('T', forgeIngot(Materials.tough_alloy))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("chassis"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("empty_frame").get())
                .pattern("PTP")
                .pattern("I I")
                .pattern("PTP")
                .define('I', forgeIngot(Materials.iron))
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('T', forgeIngot(Materials.tin))
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer, rl("empty_frame"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NC_PARTS.get("steel_frame").get())
                .pattern("STS")
                .pattern("TBT")
                .pattern("STS")
                .define('S', forgeIngot(Materials.steel))
                .define('T', forgeIngot(Materials.tough_alloy))
                .define('B', forgeIngot(Materials.bronze))
                .unlockedBy("item", has(forgeIngot(Materials.tough_alloy)))
                .save(consumer, rl("steel_frame"));

    }

    private void fissionBlocks(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_irradiation_chamber").get())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', NC_PARTS.get("servo").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('T', STORAGE_BLOCKS.get("basic_storage_container").get())
                .define('L', forgePlate(Materials.boron))
                .group(MODID+"_fission")
                .unlockedBy("item", has(STORAGE_BLOCKS.get("basic_storage_container").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_port").get())
                .pattern("LPL")
                .pattern("MTM")
                .pattern("LPL")
                .define('M', NC_PARTS.get("servo").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('T', FISSION_BLOCKS.get("fission_reactor_casing").get())
                .define('L', forgePlate("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FISSION_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_casing").get(), 4)
                .pattern("LPL")
                .pattern("P P")
                .pattern("LPL")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('L', forgePlate("lead"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_controller").get())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LPL")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('D', NCProcessors.PROCESSORS.get(Processors.DECAY_HASTENER).get())
                .define('T', NC_PARTS.get("basic_electric_circuit").get())
                .define('L', FISSION_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_glass").get())
                .pattern(" P ")
                .pattern("PTP")
                .pattern(" P ")
                .define('P', Tags.Items.GLASS)
                .define('T', FISSION_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get())
                .pattern("TGT")
                .pattern("G G")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS)
                .define('T', forgeIngot(Materials.zirconium))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FISSION_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("msr_fuel_cell").get())
                .pattern("TGT")
                .pattern("G G")
                .pattern("TGT")
                .define('G', Tags.Items.GLASS)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FISSION_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("msr_controller").get())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LPL")
                .define('P', NC_PARTS.get("plate_extreme").get())
                .define('D', NCProcessors.PROCESSORS.get(Processors.FUEL_REPROCESSOR).get())
                .define('T', NC_PARTS.get("advanced_electric_circuit").get())
                .define('L', FISSION_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("heat_exchanger").get())
                .pattern("LPL")
                .pattern("PMP")
                .pattern("LPL")
                .define('M', NC_PARTS.get("chassis").get())
                .define('P', forgePlate("boron"))
                .define('L',  NC_PARTS.get("plate_advanced").get())
                .group(MODID+"_fission")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);*/

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("empty_heat_sink").get())
                .pattern("TIT")
                .pattern("ABA")
                .pattern("TIT")
                .define('I', forgePlate("thermoconducting"))
                .define('B', BUCKET)
                .define('A', IRON_BARS)
                .define('T', forgeIngot("tough_alloy"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FISSION_BLOCKS.get("fission_reactor_casing").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("empty_active_heat_sink").get())
                .pattern("TIT")
                .pattern("IBI")
                .pattern("TIT")
                .define('I', NC_PARTS.get("motor").get())
                .define('B', FISSION_BLOCKS.get("empty_heat_sink").get())
                .define('T', forgePlate("thermoconducting"))
                .group(MODID+"_fission")
                .unlockedBy("item", has(FISSION_BLOCKS.get("empty_heat_sink").get()))
                .save(consumer);

        for(String name: heatsinks.keySet()) {
            if(name.matches(".*water.*|.*liquid.*|.*empty.*|.*cryotheum.*")) {
                continue;
            }
            TagKey<Item> i = forgeDust(name.replace("active_", ""));
            if(name.contains("slime")) {
                i = Tags.Items.SLIMEBALLS;
            }
            if(name.contains("nether_brick")) {
                i = Tags.Items.INGOTS_NETHER_BRICK;
            }
            Block empty = FISSION_BLOCKS.get("empty_heat_sink").get();
            if(name.contains("active")) {
                empty = FISSION_BLOCKS.get("empty_active_heat_sink").get();
            }
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get(name+"_heat_sink").get())
                    .pattern(" I ")
                    .pattern("IBI")
                    .pattern(" I ")
                    .define('I', Ingredient.of(i))
                    .define('B', empty)
                    .group(MODID+"_fission")
                    .unlockedBy("item", has(empty))
                    .save(consumer);

        }
    }

    private void msrBlocks(Consumer<FinishedRecipe> consumer) {
        // MSR Controller
        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FISSION_BLOCKS.get("msr_controller").get())
                .pattern("LPL")
                .pattern("TDT")
                .pattern("LPL")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('D', NCProcessors.PROCESSORS.get(Processors.DECAY_HASTENER).get())
                .define('T', NCProcessors.PROCESSORS.get(Processors.CHEMICAL_REACTOR).get())
                .define('L', FISSION_BLOCKS.get("fission_reactor_casing").get())
                .group(MODID+"_msr")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer, rl("msr_controller"));*/
    }

    private void turbineBlocks(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_casing").get(), 4)
                .pattern("SSS")
                .pattern("SLS")
                .pattern("SSS")
                .define('S', forgePlate(Materials.hsla_steel))
                .define('L', NC_PARTS.get("chassis").get())
                .group(MODID+"_turbine")
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_bearing").get(), 2)
                .pattern("GGG")
                .pattern("GSG")
                .pattern("GGG")
                .define('S', forgeIngot(Materials.hsla_steel))
                .define('G', forgeIngot("gold"))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgeIngot(Materials.hsla_steel)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_controller").get(), 1)
                .pattern("GCG")
                .pattern("CBC")
                .pattern("GCG")
                .define('C', TURBINE_BLOCKS.get("turbine_casing").get())
                .define('G', NC_PARTS.get("basic_electric_circuit").get())
                .define('B', BUCKET)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgeIngot(Materials.hsla_steel)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_rotor_shaft").get(), 4)
                .pattern("STS")
                .pattern("STS")
                .pattern("STS")
                .define('S', forgeIngot(Materials.hsla_steel))
                .define('T', forgeIngot(Materials.zinc))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgeIngot(Materials.hsla_steel)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_port").get(), 2)
                .pattern("TST")
                .pattern("SBS")
                .pattern("TST")
                .define('S', forgeIngot(Materials.hsla_steel))
                .define('T', forgeIngot(Materials.zinc))
                .define('B', CAULDRON)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgeIngot(Materials.hsla_steel)))
                .save(consumer);

        for(String type: coils.keySet()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_"+type+"_coil").get(), 1)
                    .pattern("SBS")
                    .pattern("SBS")
                    .pattern("SBS")
                    .define('S', forgeIngot(Materials.stainless_steel))
                    .define('B', forgeIngot(type))
                    .group(MODID+"_turbine")
                    .unlockedBy("item", has(forgeIngot(Materials.stainless_steel)))
                    .save(consumer);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_extreme_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', forgePlate(Materials.extreme))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgePlate(Materials.extreme)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_steel_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', forgePlate(Materials.steel))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgePlate(Materials.steel)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_basic_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', forgePlate(Materials.iron))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgePlate(Materials.iron)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_sic_sic_cmc_rotor_blade").get(), 2)
                .pattern(" P ")
                .pattern(" P ")
                .pattern(" P ")
                .define('P', forgePlate(Materials.sic_sic_cmc))
                .group(MODID+"_turbine")
                .unlockedBy("item", has(forgePlate(Materials.sic_sic_cmc)))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TURBINE_BLOCKS.get("turbine_glass").get(), 1)
                .requires(TURBINE_BLOCKS.get("turbine_casing").get())
                .requires(Tags.Items.GLASS)
                .group(MODID+"_turbine")
                .unlockedBy("item", has(TURBINE_BLOCKS.get("turbine_casing").get()))
                .save(consumer);

    }

    private void solarPanels(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/basic").get())
                .pattern("LQL")
                .pattern("PLP")
                .pattern("CSC")
                .define('Q', GLASS_PANE)
                .define('P', HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('L', Tags.Items.GEMS_LAPIS)
                .define('S', DAYLIGHT_DETECTOR)
                .define('C', NC_PARTS.get("coil_copper").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(NC_PARTS.get("coil_copper").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PCP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', ENERGY_BLOCKS.get("solar_panel/basic").get())
                .define('G', dustIngredient(Materials.quartz))
                .define('C', NC_PARTS.get("coil_copper").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/basic").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/du").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_du").get())
                .define('S', ENERGY_BLOCKS.get("solar_panel/advanced").get())
                .define('G', dustIngredient(Materials.energetic_blend))
                .define('M', NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ENERGY_BLOCKS.get("solar_panel/elite").get())
                .pattern("PGP")
                .pattern("SSS")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('S', ENERGY_BLOCKS.get("solar_panel/du").get())
                .define('G', dustIngredient(Materials.energetic_blend))
                .define('M', NC_PARTS.get("coil_magnesium_diboride").get())
                .group(MODID+"_solar_panels")
                .unlockedBy("item", has(ENERGY_BLOCKS.get("solar_panel/advanced").get()))
                .save(consumer);

    }

    private void materials(Consumer<FinishedRecipe> consumer) {

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_DUSTS.get(Materials.dimensional_blend).get(), 2)
                .requires(dustIngredient(Materials.enderium), 1)
                .requires(dustIngredient(Materials.emerald), 1)
                .requires(dustIngredient(Materials.lapis), 1)
                .group(MODID+"_dusts")
                .unlockedBy("dust", has(NC_DUSTS.get(Materials.enderium).get()))
                .save(consumer);

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeIngot(Materials.manganese_oxide)),
                        RecipeCategory.MISC,
                        NC_INGOTS.get(Materials.manganese).get(), 1.0f, 100)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(NC_INGOTS.get(Materials.manganese_oxide).get()).build()))
                .save(consumer, MODID+"_"+Materials.manganese+"_sm1");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeIngot(Materials.sodium_fluoride)),
                        RecipeCategory.MISC,
                        NC_DUSTS.get(Materials.sodium).get(), 1.0f, 100)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(NC_DUSTS.get(Materials.sodium_fluoride).get()).build()))
                .save(consumer, MODID+"_"+Materials.sodium+"_sm1");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeDust(Materials.rhodochrosite)),
                        RecipeCategory.MISC,
                        NCItems.NC_DUSTS.get(Materials.manganese_oxide).get(), 1.0f, 100)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(NCItems.NC_DUSTS.get(Materials.rhodochrosite).get()).build()))
                .save(consumer, MODID+"_"+Materials.manganese_oxide+"_sm1");

        for(String name: Materials.ingots().keySet()) {
            if(Materials.ingots().get(name).block) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NC_MATERIAL_BLOCKS.get(name).get())
                        .requires(Ingredient.of(forgeIngot(name)), 9)
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NCItems.NC_INGOTS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeBlock(name)))
                        .group(MODID+"_blocks")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer, name+"_from_block");
            }
            if(Materials.ingots().get(name).nugget) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NCItems.NC_INGOTS.get(name).get())
                        .requires(Ingredient.of(forgeNugget(name)), 9)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer,name+"_from_nugget");
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NCItems.NC_NUGGETS.get(name).get(), 9)
                        .requires(Ingredient.of(forgeIngot(name)), 1)
                        .group(MODID+"_ingots")
                        .unlockedBy("ingot", has(NCItems.NC_INGOTS.get(name).get()))
                        .save(consumer);
            }
            if(Materials.ingots().get(name).hasOre()) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeOre(name)),
                                RecipeCategory.MISC,
                        NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(name)).build()))
                        .save(consumer, MODID+"_"+name+"_ore");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeOre(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_ore");
            }

            if(Materials.ingots().get(name).chunk) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeChunk(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeChunk(name)).build()))
                        .save(consumer, MODID+"_"+name+"_raw");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeChunk(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeChunk(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_raw");
            }
            if(Materials.ingots().get(name).dust) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeDust(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeDust(name)).build()))
                        .save(consumer, MODID+"_"+name+"_dust");
                SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeDust(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 100)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeDust(name)).build()))
                        .save(consumer, MODID+":blast_"+name+"_dust");
            }

            if(Materials.ingots().get(name).plate) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgePlate(name)),
                                RecipeCategory.MISC,
                                NCItems.NC_INGOTS.get(name).get(), 1.0f, 200)
                        .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgePlate(name)).build()))
                        .save(consumer, MODID+"_"+name+"_plate");
            }
        }

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeDust(Materials.gold)),
                        RecipeCategory.MISC,
                        GOLD_INGOT, 1.0f, 200)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(Materials.gold)).build()))
                .save(consumer, MODID+"_"+Materials.gold+"_ingot");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeDust(Materials.gold)),
                        RecipeCategory.MISC,
                        GOLD_INGOT, 1.0f, 100)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(Materials.gold)).build()))
                .save(consumer, MODID+":blast_"+Materials.gold+"_ingot");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(forgeDust(Materials.copper)),
                        RecipeCategory.MISC,
                        COPPER_INGOT, 1.0f, 200)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(Materials.copper)).build()))
                .save(consumer, MODID+"_"+Materials.copper+"_ingot");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(forgeDust(Materials.copper)),
                        RecipeCategory.MISC,
                        COPPER_INGOT, 1.0f, 100)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of(forgeOre(Materials.copper)).build()))
                .save(consumer, MODID+":blast_"+Materials.copper+"_ingot");

    }

    private void processors(Consumer<FinishedRecipe> consumer)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get(Processors.SUBATOMIC_LIQUIFIER).get())
                .pattern("PYP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', NCProcessors.PROCESSORS.get("chemical_reactor").get())
                .define('Y', NCProcessors.PROCESSORS.get("centrifuge").get())
                .define('P', NC_PARTS.get("plate_extreme").get())
                .define('M', NCProcessors.PROCESSORS.get("centrifuge").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("plate_extreme").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("analyzer").get())
                .pattern("PYP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', CARTOGRAPHY_TABLE)
                .define('Y', ENDER_EYE)
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('M', NC_PARTS.get("motor").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(CAULDRON))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("pump").get())
                .pattern("PMP")
                .pattern("PCP")
                .pattern("PMP")
                .define('C', CAULDRON)
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('M', NC_PARTS.get("motor").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(CAULDRON))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("gas_scrubber").get())
                .pattern("PGP")
                .pattern("CEC")
                .pattern("PMP")
                .define('C', forgeDust(Materials.borax))
                .define('P', NC_PARTS.get("plate_elite").get())
                .define('E', forgeIngot(Materials.extreme))
                .define('M', NC_PARTS.get("motor").get())
                .define('G', IRON_BARS)
                .group(MODID+"_machines")
                .unlockedBy("item", has(forgeDust(Materials.borax)))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("nuclear_furnace").get())
                .pattern("PTP")
                .pattern("TFT")
                .pattern("PTP")
                .define('T', Tags.Items.INGOTS_IRON)
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('F', FURNACE)
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("plate_basic").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("leacher").get())
                .pattern("LRL")
                .pattern("FPF")
                .pattern("LSL")
                .define('P', NCProcessors.PROCESSORS.get("chemical_reactor").get())
                .define('S', NCProcessors.PROCESSORS.get("pump").get())
                .define('F', NC_PARTS.get("servo").get())
                .define('L', NC_PARTS.get("chassis").get())
                .define('R', NCProcessors.PROCESSORS.get("centrifuge").get())
                .group(MODID+"_processors")
                .unlockedBy("item", has(NCProcessors.PROCESSORS.get("chemical_reactor").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("manufactory").get())
                .pattern("LRL")
                .pattern("FPF")
                .pattern("LSL")
                .define('P', PISTON)
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('F', FLINT)
                .define('L', forgeIngot("lead"))
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(FLINT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("centrifuge").get())
                .pattern("LSL")
                .pattern("FPF")
                .pattern("LRL")
                .define('P', NC_PARTS.get("chassis").get())
                .define('S', forgeIngot("ferroboron"))
                .define('F', NC_PARTS.get("motor").get())
                .define('L', NC_PARTS.get("plate_advanced").get())
                .define('R', NC_PARTS.get("servo").get())
                .group(MODID+"_processors")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("chemical_reactor").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NC_PARTS.get("chassis").get())
                .define('S', NC_PARTS.get("servo").get())
                .define('F', NC_PARTS.get("motor").get())
                .define('L', NC_PARTS.get("plate_advanced").get())
                .define('X', Tags.Items.DUSTS_GLOWSTONE)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("alloy_smelter").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', BLAST_FURNACE)
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('F', Tags.Items.DUSTS_REDSTONE)
                .define('L', NC_PARTS.get("plate_basic").get())
                .define('X', Tags.Items.INGOTS_BRICK)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("melter").get())
                .pattern("LXL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NC_PARTS.get("chassis").get())
                .define('S', NC_PARTS.get("servo").get())
                .define('L', NC_PARTS.get("plate_advanced").get())
                .define('X', Tags.Items.INGOTS_NETHER_BRICK)
                .group(MODID+"_processors")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("ingot_former").get())
                .pattern("LFL")
                .pattern("XPX")
                .pattern("LSL")
                .define('P', NC_PARTS.get("chassis").get())
                .define('S', forgeIngot("tough_alloy"))
                .define('F', HOPPER)
                .define('L', NC_PARTS.get("plate_basic").get())
                .define('X', forgeIngot("ferroboron"))
                .group(MODID+"_processors")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("fuel_reprocessor").get())
                .pattern("PGP")
                .pattern("ECE")
                .pattern("PSP")
                .define('P', forgeIngot("tin_silver"))
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('G', Tags.Items.DUSTS_GLOWSTONE)
                .define('E', ENDER_PEARL)
                .define('C', NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("decay_hastener").get())
                .pattern("PBP")
                .pattern("TCT")
                .pattern("PAP")
                .define('C', forgeIngot("tin_silver")) //todo fission_controller
                .define('P', forgeIngot("boron"))
                .define('A', NC_PARTS.get("actuator").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('B', forgeBlock("boron"))
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("actuator").get()))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("isotope_separator").get())
                .pattern("PMP")
                .pattern("RCR")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('M', NC_PARTS.get("motor").get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("pressurizer").get())
                .pattern("PTP")
                .pattern("ACA")
                .pattern("PNP")
                .define('C', NC_PARTS.get("chassis").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('T', TERRACOTTA)
                .define('N', ANVIL)
                .define('A', NC_PARTS.get("actuator").get())
                .group(MODID)
                .unlockedBy("has_chassis", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

       /* ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("salt_mixer").get())
                .pattern("PSP")
                .pattern("BCB")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('S', NCItems.INGOT_STEEL.get())
                .define('B', Items.BUCKET)
                .define('M', NC_PARTS.get("motor").get())
                .define('C', NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(Items.BUCKET))
                .save(consumer);*/

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("fluid_enricher").get())
                .pattern("PHP")
                .pattern("LCL")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('L', LAPIS_LAZULI)
                .define('M', NC_PARTS.get("motor").get())
                .define('H', HOPPER)
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("plate_advanced").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("electrolyzer").get())
                .pattern("PGP")
                .pattern("SCS")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('G', forgeIngot("graphite"))
                .define('M', NC_PARTS.get("motor").get())
                .define('C', NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("motor").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("assembler").get())
                .pattern("PHP")
                .pattern("ACA")
                .pattern("PMP")
                .define('P', NC_PARTS.get("plate_basic").get())
                .define('H', forgeIngot("hard_carbon"))
                .define('A', NC_PARTS.get("actuator").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('M', NC_PARTS.get("motor").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("actuator").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("supercooler").get())
                .pattern("PDP")
                .pattern("HCH")
                .pattern("PSP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('D', forgeIngot("magnesium_diboride"))
                .define('H', forgeIngot("hard_carbon"))
                .define('S', NC_PARTS.get("servo").get())
                .define('C', NC_PARTS.get("chassis").get())
                .group(MODID+"_machines")
                .unlockedBy("item", has(NC_PARTS.get("servo").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("extractor").get())
                .pattern("PMP")
                .pattern("BCB")
                .pattern("PSP")
                .define('C', NC_PARTS.get("chassis").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('M', forgeIngot("magnesium"))
                .define('S', NC_PARTS.get("servo").get())
                .define('B', BUCKET)
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("crystallizer").get())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PUP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('U', CAULDRON)
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("steam_turbine").get())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', NC_PARTS.get("coil_copper").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('U', CAULDRON)
                .define('F', FURNACE)
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("irradiator").get())
                .pattern("PUP")
                .pattern("SCS")
                .pattern("PFP")
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('S', NC_PARTS.get("coil_magnesium_diboride").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('U', CHEST)
                .define('F', FURNACE)
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("fluid_infuser").get())
                .pattern("PBP")
                .pattern("GCG")
                .pattern("PSP")
                .define('B', BUCKET)
                .define('G', forgeIngot("gold"))
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('C', NC_PARTS.get("chassis").get())
                .define('S', NC_PARTS.get("servo").get())
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("chassis").get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NCProcessors.PROCESSORS.get("rock_crusher").get())
                .pattern("PMP")
                .pattern("ACA")
                .pattern("PTP")
                .define('C', NC_PARTS.get("chassis").get())
                .define('P', NC_PARTS.get("plate_advanced").get())
                .define('A', NC_PARTS.get("actuator").get())
                .define('T', forgeIngot("tough_alloy"))
                .define('M', NC_PARTS.get("motor").get())
                .group(MODID)
                .unlockedBy("has_item", has(NC_PARTS.get("motor").get()))
                .save(consumer);
    }
}