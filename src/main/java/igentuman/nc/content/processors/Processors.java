package igentuman.nc.content.processors;

import igentuman.nc.client.gui.processor.LeacherScreen;
import igentuman.nc.block.entity.processor.*;
import igentuman.nc.container.LeacherContainer;
import igentuman.nc.container.NCProcessorContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@SuppressWarnings("ALL")
public class Processors {

    private static HashMap<String, ProcessorPrefab> all = new HashMap<>();
    private static HashMap<String, ProcessorPrefab> registered = new HashMap<>();
    public static String GAS_SCRUBBER = "gas_scrubber";
    public static String PUMP = "pump";
    public static String ANALYZER = "analyzer";
    public static String LEACHER = "leacher";
    public static String NUCLEAR_FURNACE = "nuclear_furnace";
    public static String MANUFACTORY = "manufactory";
    public static String ALLOY_SMELTER = "alloy_smelter";
    public static String ASSEMBLER = "assembler";
    public static String CENTRIFUGE = "centrifuge";
    public static String CHEMICAL_REACTOR = "chemical_reactor";
    public static String CRYSTALLIZER = "crystallizer";
    public static String FUEL_REPROCESSOR = "fuel_reprocessor";
    public static String DECAY_HASTENER = "decay_hastener";
    public static String ELECTROLYZER = "electrolyzer";
    public static String EXTRACTOR = "extractor";
    public static String FLUID_ENRICHER = "fluid_enricher";
    public static String FLUID_INFUSER = "fluid_infuser";
    public static String INGOT_FORMER = "ingot_former";
    public static String IRRADIATOR = "irradiator";
    public static String ISOTOPE_SEPARATOR = "isotope_separator";
    public static String MELTER = "melter";
    public static String PRESSURIZER = "pressurizer";
    public static String ROCK_CRUSHER = "rock_crusher";
    public static String STEAM_TURBINE = "steam_turbine";
    public static String SUPERCOOLER = "supercooler";
    public static String QUANTUM_TRANSFORMER = "quantum_transformer";

    @OnlyIn(Dist.CLIENT)
    public static void setScreen(String name, MenuScreens.ScreenConstructor constructor) {
        all.get(name).setScreenConstructor(constructor);
    }

    public static HashMap<String, ProcessorPrefab> all() {
        if(all.isEmpty()) {
            all.put(GAS_SCRUBBER,
                    ProcessorBuilder
                            .make(GAS_SCRUBBER, 1, 0, 1, 0)
                            .blockEntity(GasScrubberBE::new)
                            .recipe(GasScrubberBE.Recipe::new)
                            .build()
            );
            all.put(ANALYZER,
                    ProcessorBuilder
                            .make(ANALYZER, 0, 1, 0, 1)
                            .blockEntity(AnalyzerBE::new)
                            .recipe(AnalyzerBE.Recipe::new)
                            .build()
            );
            all.put(LEACHER,
                    ProcessorBuilder
                            .make(LEACHER, 1, 1, 1, 0)
                            .blockEntity(LeacherBE::new)
                            .recipe(LeacherBE.Recipe::new)
                            .container(LeacherContainer.class)
                            .setHiddenSlots(1)
                            .withCatalyst()
                            .build()
            );
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()-> Processors.setScreen(LEACHER, LeacherScreen::new));

            all.put(PUMP,
                    ProcessorBuilder
                            .make(PUMP, 0, 1, 1, 0)
                            .blockEntity(PumpBE::new)
                            .recipe(PumpBE.Recipe::new)
                            .build()
            );
            all.put(NUCLEAR_FURNACE,
                    ProcessorBuilder
                            .make(NUCLEAR_FURNACE, 0, 1, 0, 1)
                            .blockEntity(NuclearFurnaceBE::new)
                            .build()
            );
            all.put(MANUFACTORY,
                    ProcessorBuilder
                            .make(MANUFACTORY, 0, 1, 0, 1)
                            .blockEntity(ManufactoryBE::new)
                            .recipe(ManufactoryBE.Recipe::new)
                            .progressBar(13)
                            .build()
            );
            all.put(ALLOY_SMELTER,
                    ProcessorBuilder
                            .make(ALLOY_SMELTER, 0, 2, 0, 1)
                            .blockEntity(AlloySmelterBE::new)
                            .recipe(AlloySmelterBE.Recipe::new)
                            .build()
            );
            all.put(ASSEMBLER,
                    ProcessorBuilder
                            .make(ASSEMBLER, 0, 6, 0, 1)
                            .blockEntity(AssemblerBE::new)
                            .recipe(AssemblerBE.Recipe::new)
                            .progressBar(15)
                            .build()
            );
            all.put(CENTRIFUGE,
                    ProcessorBuilder
                            .make(CENTRIFUGE, 1, 0, 6, 0)
                            .blockEntity(CentrifugeBE::new)
                            .recipe(CentrifugeBE.Recipe::new)
                            .progressBar(16)
                            .build()
            );
            all.put(CHEMICAL_REACTOR,
                    ProcessorBuilder
                            .make(CHEMICAL_REACTOR, 2, 0, 2, 0)
                            .blockEntity(ChemicalReactorBE::new)
                            .recipe(ChemicalReactorBE.Recipe::new)
                            .progressBar(5)
                            .build()
            );
            all.put(CRYSTALLIZER,
                    ProcessorBuilder
                            .make(CRYSTALLIZER, 1, 0, 0, 1)
                            .blockEntity(CrystalizerBE::new)
                            .recipe(CrystalizerBE.Recipe::new)
                            .progressBar(6)
                            .build()
            );
            all.put(DECAY_HASTENER,
                    ProcessorBuilder
                            .make(DECAY_HASTENER, 0, 1, 0, 1)
                            .blockEntity(DecayHastenerBE::new)
                            .recipe(DecayHastenerBE.Recipe::new)
                            .progressBar(0)
                            .build()
            );
            all.put(ELECTROLYZER,
                    ProcessorBuilder
                            .make(ELECTROLYZER, 1, 0, 4, 0)
                            .blockEntity(ElectrolyzerBE::new)
                            .recipe(ElectrolyzerBE.Recipe::new)
                            .build()
            );
            all.put(EXTRACTOR,
                    ProcessorBuilder
                            .make(EXTRACTOR, 0, 1, 1, 1)
                            .blockEntity(ExtractorBE::new)
                            .recipe(ExtractorBE.Recipe::new)
                            .progressBar(7)
                            .build()
            );
            all.put(FLUID_ENRICHER,
                    ProcessorBuilder
                            .make(FLUID_ENRICHER, 1, 1, 1, 0)
                            .blockEntity(FluidEnricherBE::new)
                            .recipe(FluidEnricherBE.Recipe::new)
                            .build()
            );
            all.put(FLUID_INFUSER,
                    ProcessorBuilder
                            .make(FLUID_INFUSER, 1, 1, 0, 1)
                            .blockEntity(FluidInfuserBE::new)
                            .recipe(FluidInfuserBE.Recipe::new)
                            .progressBar(4)
                            .build()
            );
            all.put(FUEL_REPROCESSOR,
                    ProcessorBuilder
                            .make(FUEL_REPROCESSOR, 0, 1, 0, 8)
                            .blockEntity(FuelReprocessorBE::new)
                            .recipe(FuelReprocessorBE.Recipe::new)
                            .progressBar(16)
                            .build()
            );
            all.put(INGOT_FORMER,
                    ProcessorBuilder
                            .make(INGOT_FORMER, 1, 0, 0, 1)
                            .blockEntity(IngotFormerBE::new)
                            .recipe(IngotFormerBE.Recipe::new)
                            .progressBar(4)
                            .build()
            );
            all.put(IRRADIATOR,
                    ProcessorBuilder
                            .make(IRRADIATOR, 1, 1, 1, 1)
                            .blockEntity(IrradiatorBE::new)
                            .recipe(IrradiatorBE.Recipe::new)
                            .progressBar(14)
                            .upgrades(false, false)
                            .power(0)
                            .build()
            );
            all.put(ISOTOPE_SEPARATOR,
                    ProcessorBuilder
                            .make(ISOTOPE_SEPARATOR, 0, 1, 0, 2)
                            .blockEntity(IsotopeSeparatorBE::new)
                            .recipe(IsotopeSeparatorBE.Recipe::new)
                            .progressBar(10)
                            .build()
            );
            all.put(MELTER,
                    ProcessorBuilder
                            .make(MELTER, 0, 1, 1, 0)
                            .blockEntity(MelterBE::new)
                            .recipe(MelterBE.Recipe::new)
                            .progressBar(0)
                            .build()
            );
            all.put(PRESSURIZER,
                    ProcessorBuilder
                            .make(PRESSURIZER, 0, 1, 0, 1)
                            .blockEntity(PressurizerBE::new)
                            .recipe(PressurizerBE.Recipe::new)
                            .progressBar(9)
                            .build()
            );
            all.put(ROCK_CRUSHER,
                    ProcessorBuilder
                            .make(ROCK_CRUSHER, 0, 1, 0, 3)
                            .blockEntity(RockCrusherBE::new)
                            .recipe(RockCrusherBE.Recipe::new)
                            .progressBar(12)
                            .build()
            );
            all.put(STEAM_TURBINE,
                    ProcessorBuilder
                            .make(STEAM_TURBINE, 1, 0, 1, 0)
                            .blockEntity(SteamTurbineBE::new)
                            .recipe(SteamTurbineBE.Recipe::new)
                            .progressBar(4)
                            .upgrades(false, false)
                            .build()

            );
            all.put(SUPERCOOLER,
                    ProcessorBuilder
                            .make(SUPERCOOLER, 1, 0, 1, 0)
                            .blockEntity(SuperCoolerBE::new)
                            .recipe(SuperCoolerBE.Recipe::new)
                            .progressBar(11)
                            .build()
            );
/*            all.put(QUANTUM_TRANSFORMER,
                    ProcessorBuilder
                            .make(QUANTUM_TRANSFORMER, 1, 0, 1, 0)
                            .blockEntity(QuantumTransformerBE::new)
                            .build()
            );*/
        }
        return all;
    }

    public static HashMap<String, ProcessorPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).isRegistered())
                    registered.put(name,all().get(name));
            }
        }
        return registered;
    }

    public static List<Boolean> initialRegistered() {
        List<Boolean> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(true);
        }
        return tmp;
    }

    public static List<Integer> initialPower() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).power);
        }
        return tmp;
    }

    public static List<Integer> initialTime() {
        List<Integer> tmp = new ArrayList<>();
        for(String name: all().keySet()) {
            tmp.add(all().get(name).time);
        }
        return tmp;
    }
}
