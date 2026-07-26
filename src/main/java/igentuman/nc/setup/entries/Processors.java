package igentuman.nc.setup.entries;

import igentuman.nc.block_entity.AnalyzerBE;
import igentuman.nc.block_entity.IrradiatorBE;
import igentuman.nc.block_entity.LeacherBE;
import igentuman.nc.block_entity.PumpBE;
import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.recipe.OreVeinRecipeSerializer;
import igentuman.nc.registration.ModEntryBuilder;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addProcessor;

/** Declares all single-block machine processors with their item/fluid slots and progress bars. */
public class Processors extends ModEntries {

    public static final String GAS_SCRUBBER = "gas_scrubber";
    public static final String PUMP = "pump";
    public static final String NUCLEAR_FURNACE = "nuclear_furnace";
    public static final String MANUFACTORY = "manufactory";
    public static final String ALLOY_SMELTER = "alloy_smelter";
    public static final String ASSEMBLER = "assembler";
    public static final String CENTRIFUGE = "centrifuge";
    public static final String CHEMICAL_REACTOR = "chemical_reactor";
    public static final String CRYSTALLIZER = "crystallizer";
    public static final String FUEL_REPROCESSOR = "fuel_reprocessor";
    public static final String DECAY_HASTENER = "decay_hastener";
    public static final String ELECTROLYZER = "electrolyzer";
    public static final String EXTRACTOR = "extractor";
    public static final String FLUID_ENRICHER = "fluid_enricher";
    public static final String FLUID_INFUSER = "fluid_infuser";
    public static final String INGOT_FORMER = "ingot_former";
    public static final String ISOTOPE_SEPARATOR = "isotope_separator";
    public static final String MELTER = "melter";
    public static final String PRESSURIZER = "pressurizer";
    public static final String ROCK_CRUSHER = "rock_crusher";
    public static final String STEAM_TURBINE = "steam_turbine";
    public static final String SUPERCOOLER = "supercooler";
    public static final String SUBATOMIC_LIQUIFIER = "subatomic_liquifier";
    public static final String IRRADIATOR = "irradiator";
    public static final String LEACHER = "leacher";
    public static final String ANALYZER = "analyzer";

    public static void processors() {
        oreVeinRecipes();
        irradiator();
        proc(GAS_SCRUBBER, 1, 0, 1, 0, 0);
        pump();
        proc(NUCLEAR_FURNACE, 0, 2, 0, 1, 0);
        proc(MANUFACTORY, 0, 1, 0, 1, 13);
        proc(ALLOY_SMELTER, 0, 2, 0, 1, 0);
        proc(ASSEMBLER, 0, 6, 0, 1, 15);
        proc(CENTRIFUGE, 1, 0, 6, 0, 16);
        proc(CHEMICAL_REACTOR, 2, 0, 2, 0, 5);
        proc(CRYSTALLIZER, 1, 0, 0, 1, 6);
        proc(DECAY_HASTENER, 0, 1, 0, 1, 0);
        proc(ELECTROLYZER, 1, 0, 4, 0, 0);
        proc(EXTRACTOR, 0, 1, 1, 1, 7);
        proc(FLUID_ENRICHER, 1, 1, 1, 0, 0);
        proc(FLUID_INFUSER, 1, 1, 0, 1, 4);
        proc(FUEL_REPROCESSOR, 0, 1, 0, 8, 16);
        proc(INGOT_FORMER, 1, 0, 0, 1, 4);
        proc(ISOTOPE_SEPARATOR, 0, 1, 0, 2, 10);
        proc(MELTER, 0, 1, 1, 0, 0);
        proc(PRESSURIZER, 0, 1, 0, 1, 9);
        proc(ROCK_CRUSHER, 0, 1, 0, 3, 12);
        proc(STEAM_TURBINE, 1, 0, 1, 0, 4);
        proc(SUPERCOOLER, 1, 0, 1, 0, 11);
        proc(SUBATOMIC_LIQUIFIER, 1, 1, 1, 0, 0);
        leacher();
        analyzer();
    }

    private static void oreVeinRecipes() {
        add("nc_ore_veins")
                .withRecipes(
                        () -> net.minecraft.world.item.crafting.RecipeType.<igentuman.nc.recipe.OreVeinRecipe>simple(rl("nc_ore_veins")),
                        () -> new OreVeinRecipeSerializer()
                )
                .build();
    }

    private static void pump() {
        ModEntryBuilder b = addProcessor(PUMP)
                .blockEntity(PumpBE::new);
        b.itemCap(0, 1);
        b.catalysts(CatalystType.ENERGY, CatalystType.SPEED);
        b.fluidCap(1, 1, 0);
        b.withLayout(SlotsLayout.forProcessor(1, 0, 1, 1))
                .progressBar(0)
                .build();
    }

    private static void leacher() {
        ModEntryBuilder b = addProcessor(LEACHER)
                .blockEntity(LeacherBE::new);
        b.itemCap(1, 0);
        b.internalInputs();
        b.fluidCap(1, 1, 0);
        b.catalysts(CatalystType.ORE_SOURCE);
        b.withLayout(SlotsLayout.forProcessor(1, 1, 0, 1))
                .progressBar(0)
                .build();
    }

    private static void analyzer() {
        ModEntryBuilder b = addProcessor(ANALYZER)
                .blockEntity(AnalyzerBE::new);
        b.itemCap(1, 1);
        b.catalysts(CatalystType.ENERGY, CatalystType.SPEED);
        b.withLayout(SlotsLayout.ONE_TO_ONE)
                .progressBar(0)
                .build();
    }

    private static void irradiator() {
        ModEntryBuilder b = addProcessor(IRRADIATOR)
                .blockEntity(IrradiatorBE::new);
        b.itemCap(1, 1);
        b.fluidCap(1, 1, 0);
        b.catalysts(CatalystType.ENERGY, CatalystType.SPEED);
        b.withLayout(SlotsLayout.forProcessor(1, 1, 1, 1))
                .progressBar(0)
                .build();
    }

    private static void proc(String name, int inFluids, int inItems, int outFluids, int outItems, int progressBar) {
        ModEntryBuilder b = addProcessor(name);
        if (inItems > 0 || outItems > 0) {
            b.itemCap(inItems, outItems);
        }
        if (inFluids > 0 || outFluids > 0) {
            b.fluidCap(inFluids, outFluids, 0);
        }
        b.catalysts(CatalystType.ENERGY, CatalystType.SPEED);
        b.withLayout(SlotsLayout.forProcessor(inItems, inFluids, outItems, outFluids))
                .progressBar(progressBar)
                .build();
    }
}
