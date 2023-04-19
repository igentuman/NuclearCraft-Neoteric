package igentuman.nc.setup.processors;


import igentuman.nc.block.entity.processor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@SuppressWarnings("ALL")
public class Processors {

    private static HashMap<String, ProcessorPrefab> all = new HashMap<>();
    private static HashMap<String, ProcessorPrefab> registered = new HashMap<>();

    public static HashMap<String, ProcessorPrefab> all() {
        if(all.isEmpty()) {
            all.put("nuclear_furnace",
                    ProcessorBuilder
                            .make("nuclear_furnace", 0, 1, 0, 1)
                            .blockEntity(NuclearFurnaceBE::new)
                            .build()
            );
            all.put("manufactory",
                    ProcessorBuilder
                            .make("manufactory", 0, 1, 0, 1)
                            .blockEntity(ManufactoryBE::new)
                            .progressBar(1)
                            .build()
            );
            all.put("alloy_smelter",
                    ProcessorBuilder
                            .make("alloy_smelter", 0, 2, 0, 1)
                            .blockEntity(AlloySmelterBE::new)
                            .build()
            );
            all.put("assembler",
                    ProcessorBuilder
                            .make("assembler", 0, 4, 0, 1)
                            .blockEntity(AssemblerBE::new)
                            .build()
            );
            all.put("centrifuge",
                    ProcessorBuilder
                            .make("centrifuge", 1, 0, 6, 0)
                            .blockEntity(CentrifugeBE::new)
                            .build()
            );
            all.put("chemical_reactor",
                    ProcessorBuilder
                            .make("chemical_reactor", 2, 0, 2, 0)
                            .blockEntity(ChemicalReactorBE::new)
                            .build()
            );
            all.put("crystallizer",
                    ProcessorBuilder
                            .make("crystallizer", 1, 0, 0, 1)
                            .blockEntity(CrystalizerBE::new)
                            .build()
            );
            all.put("decay_hastener",
                    ProcessorBuilder
                            .make("decay_hastener", 0, 1, 0, 1)
                            .blockEntity(DecayHastenerBE::new)
                            .build()
            );
            all.put("electrolyzer",
                    ProcessorBuilder
                            .make("electrolyzer", 1, 0, 4, 0)
                            .blockEntity(ElectrolyzerBE::new)
                            .build()
            );
            all.put("extractor",
                    ProcessorBuilder
                            .make("extractor", 0, 1, 0, 1)
                            .blockEntity(ExtractorBE::new)
                            .build()
            );
            all.put("fluid_enricher",
                    ProcessorBuilder
                            .make("fluid_enricher", 0, 1, 1, 1)
                            .blockEntity(FluidEnricherBE::new)
                            .build()
            );
            all.put("fluid_infuser",
                    ProcessorBuilder
                            .make("fluid_infuser", 1, 1, 0, 1)
                            .blockEntity(FluidInfuserBE::new)
                            .build()
            );
            all.put("fuel_reprocessor",
                    ProcessorBuilder
                            .make("fuel_reprocessor", 0, 1, 0, 8)
                            .blockEntity(FuelReprocessorBE::new)
                            .build()
            );
            all.put("ingot_former",
                    ProcessorBuilder
                            .make("ingot_former", 1, 0, 0, 1)
                            .blockEntity(IngotFormerBE::new)
                            .build()
            );
            all.put("irradiator",
                    ProcessorBuilder
                            .make("irradiator", 0, 1, 0, 1)
                            .blockEntity(IrradiatorBE::new)
                            .build()
            );
            all.put("isotope_separator",
                    ProcessorBuilder
                            .make("isotope_separator", 0, 1, 0, 2)
                            .blockEntity(IsotopeSeparatorBE::new)
                            .build()
            );
            all.put("melter",
                    ProcessorBuilder
                            .make("melter", 0, 1, 1, 0)
                            .blockEntity(MelterBE::new)
                            .build()
            );
            all.put("pressurizer",
                    ProcessorBuilder
                            .make("pressurizer", 0, 1, 0, 1)
                            .blockEntity(PressurizerBE::new)
                            .build()
            );
            all.put("rock_crusher",
                    ProcessorBuilder
                            .make("rock_crusher", 0, 1, 0, 3)
                            .blockEntity(RockCrusherBE::new)
                            .build()
            );
            all.put("steam_turbine",
                    ProcessorBuilder
                            .make("steam_turbine", 1, 0, 1, 0)
                            .blockEntity(SteamTurbineBE::new)
                            .build()
            );
            all.put("super_cooler",
                    ProcessorBuilder
                            .make("super_cooler", 1, 0, 1, 0)
                            .blockEntity(SuperCoolerBE::new)
                            .build()
            );
/*            all.put("quantum_transformer",
                    ProcessorBuilder
                            .make("quantum_transformer", 1, 0, 1, 0)
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
