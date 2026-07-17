package igentuman.nc.content.processors;

import igentuman.api.nc.IProcessorRegistry;
import igentuman.nc.client.gui.processor.CreativeParticleSourceScreen;
import igentuman.nc.client.gui.processor.LeacherScreen;
import igentuman.nc.block.entity.processor.*;
import igentuman.nc.container.CreativeParticleSourceContainer;
import igentuman.nc.container.LeacherContainer;
import igentuman.nc.util.annotation.NCProcessorsRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.LOGGER;

@SuppressWarnings("ALL")
public class Processors {

    private final static HashMap<String, ProcessorPrefab> all = new HashMap<>();
    private final static HashMap<String, ProcessorPrefab> registered = new HashMap<>();
    public final static String GAS_SCRUBBER = "gas_scrubber";
    public final static String PUMP = "pump";
    public final static String ANALYZER = "analyzer";
    public final static String LEACHER = "leacher";
    public final static String NUCLEAR_FURNACE = "nuclear_furnace";
    public final static String MANUFACTORY = "manufactory";
    public final static String ALLOY_SMELTER = "alloy_smelter";
    public final static String ASSEMBLER = "assembler";
    public final static String CENTRIFUGE = "centrifuge";
    public final static String CHEMICAL_REACTOR = "chemical_reactor";
    public final static String CRYSTALLIZER = "crystallizer";
    public final static String FUEL_REPROCESSOR = "fuel_reprocessor";
    public final static String DECAY_HASTENER = "decay_hastener";
    public final static String ELECTROLYZER = "electrolyzer";
    public final static String EXTRACTOR = "extractor";
    public final static String FLUID_ENRICHER = "fluid_enricher";
    public final static String FLUID_INFUSER = "fluid_infuser";
    public final static String INGOT_FORMER = "ingot_former";
    public final static String IRRADIATOR = "irradiator";
    public final static String ISOTOPE_SEPARATOR = "isotope_separator";
    public final static String MELTER = "melter";
    public final static String PRESSURIZER = "pressurizer";
    public final static String ROCK_CRUSHER = "rock_crusher";
    public final static String STEAM_TURBINE = "steam_turbine";
    public final static String SUPERCOOLER = "supercooler";
    public final static String QUANTUM_TRANSFORMER = "quantum_transformer";
    public final static String SUBATOMIC_LIQUIFIER = "subatomic_liquifier";
    public final static String CREATIVE_PARTICLE_SOURCE = "creative_particle_source";

    @OnlyIn(Dist.CLIENT)
    public static void setScreen(String name, MenuScreens.ScreenConstructor constructor) {
        all.get(name).setScreenConstructor(constructor);
    }

    public static HashMap<String, ProcessorPrefab> all() {
        if(all.isEmpty()) {
            all.put(CREATIVE_PARTICLE_SOURCE,
                    ProcessorBuilder
                            .make(CREATIVE_PARTICLE_SOURCE, 0, 0, 0, 0)
                            .particle(1, 0)
                            .blockEntity(CreativeParticleSourceBE::new)
                            .container(CreativeParticleSourceContainer.class)
                            .build()
            );
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()-> Processors.setScreen(CREATIVE_PARTICLE_SOURCE, CreativeParticleSourceScreen::new));
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
                            .make(NUCLEAR_FURNACE, 0, 2, 0, 1)
                            .blockEntity(NuclearFurnaceBE::new)
                            .power(0)
                            .upgrades(false, false)
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
            all.put(SUBATOMIC_LIQUIFIER,
                    ProcessorBuilder
                            .make(SUBATOMIC_LIQUIFIER, 1, 1, 1, 0)
                            .blockEntity(SubatomicLiquifierBE::new)
                            .recipe(SubatomicLiquifierBE.Recipe::new)
                            .build()
            );

            scanForProcessorRegistries();
        }
        return all;
    }

    private static void scanForProcessorRegistries() {
        Type annotationType = Type.getType(NCProcessorsRegistry.class);

        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotationData : scanData.getAnnotations()) {
                if (annotationType.equals(annotationData.annotationType())) {
                    try {
                        Class<?> clazz = Class.forName(annotationData.memberName());
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        if (instance instanceof IProcessorRegistry registry) {
                            registry.registerProcessors(all);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load processor registry: " + annotationData.memberName(), e);
                    }
                }
            }
        }
    }

    public static HashMap<String, ProcessorPrefab> registered() {
        if(registered.isEmpty()) {
            for(String name: all().keySet()) {
                if (all().get(name).config().isRegistered()) {
                    registered.put(name,all().get(name));
                }
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
