package igentuman.nc.datagen;

import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.setup.registration.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCK;
import static igentuman.nc.util.TextUtils.convertToName;

public class NCLanguageProvider extends LanguageProvider {

    public NCLanguageProvider(DataGenerator gen, String locale) {
        super(gen, MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + MODID+"_items", "NuclearCraft Items");
        add("itemGroup." + MODID+"_blocks", "NuclearCraft Blocks");
        add("itemGroup." + MODID+"_fission_reactor", "NuclearCraft Fission Reactor");
        ores();
        ingots();
        plates();
        dusts();
        nuggets();
        gems();
        parts();
        chunks();
        blocks();
        food();
        armor();
        records();
        tools();
        items();
        fuel();
        tooltips();
        isotopes();
        shielding();
        buckets();
        fluids();
        processors();
        energyBlocks();
        multiblocks();
        labels();
        messages();
        storageBlocks();
    }

    private void messages() {
        add("message.nc.player_radiation_contamination", "Radiation Dose: %s");
        add("message.nc.geiger_radiation_measure", "Radiation Level: %s");
        add("death.attack.radiation", "Died of Radiation Poisoning");
    }
    private void sounds() {
        add("sound_event.nuclearcraft.item.geiger_1", "Geiger Counter Ticks Level 1 Intensity");
        add("sound_event.nuclearcraft.item.geiger_2", "Geiger Counter Ticks Level 2 Intensity");
        add("sound_event.nuclearcraft.item.geiger_3", "Geiger Counter Ticks Level 3 Intensity");
        add("sound_event.nuclearcraft.item.geiger_4", "Geiger Counter Ticks Level 4 Intensity");
        add("sound_event.nuclearcraft.item.geiger_5", "Geiger Counter Ticks Level 5 Intensity");
    }

    private void labels() {
        add("nc.label.leacher_wrong_position", "Wrong Position");
        add("nc.label.leacher_no_source", "No Data Source");
        add("nc.label.leacher_no_acid", "No Acid");
        add("block.nuclearcraft.glowing_mushroom", "Glowing Mushroom");
        add("fusion_core", "Fusion Reactor Core");
        add("nc_jei_cat.fusion_core", "Fusion Reactor");

        add("reactor.size", "Reactor size: %sx%sx%s");
        add("fission.casing.wrong.block", "Wrong block at: %s");
        add("fission_reactor.efficiency", "Efficiency: %s%%");
        add("fission_reactor.net_heat", "Net Heat: %s H/t");
        add("fission.casing.reactor_incomplete", "Reactor Incomplete");
        add("fission_reactor.heat_multiplier", "Heat Multiplier: %sx");

        add("processor_side_config.title", "Select Slot");
        add("processor_slot_mode.title", "Slot Mode");

        add("message.heat_sink.valid0", "This one is looking good");
        add("message.heat_sink.valid1", "I like this one");
        add("message.heat_sink.valid2", "This heat sink design shows promise");
        add("message.heat_sink.valid3", "The heat dissipation capability looks good");
        add("message.heat_sink.valid4", "The thermal conductivity appears to be efficient");
        add("message.heat_sink.valid5", "Attention to detail is evident in this design");
        add("message.heat_sink.valid6", "This heat sink design seems promising for our project");
        add("message.heat_sink.valid7", "Attention to detail is impressive");
        add("message.heat_sink.valid8", "This is a perfect fit for our application");
        add("message.heat_sink.valid9", "This meets our standards very well");

        add("message.heat_sink.invalid0", "Not sure if it's valid");
        add("message.heat_sink.invalid1", "Maybe you should check it again");
        add("message.heat_sink.invalid2", "Hm...");
        add("message.heat_sink.invalid3", "Will it explode? -Shouldn't");
        add("message.heat_sink.invalid4", "This doesn't seem to meet our standards.");
        add("message.heat_sink.invalid5", "There are some concerns about the validity of this.");
        add("message.heat_sink.invalid6", "I have some reservations about this.");
        add("message.heat_sink.invalid7", "This may not be suitable for our project.");
        add("message.heat_sink.invalid8", "This may need some significant revisions before it can be considered valid.");
        add("message.heat_sink.invalid9", "More work may be needed before this can be considered valid.");

        add("nc_jei_cat.fission_reactor_controller", "Fission Reactor Fuel Depletion");
        add("nc_jei_cat.nc_ore_veins", "Ore Veins");
        add("container.nc.storage", "Item Storage Container");
    }

    private void multiblocks() {
        for(String name: FissionReactor.MULTI_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(FissionReactor.MULTI_BLOCKS.get(name).get(), title);
        }
        for(String name: FusionReactor.FUSION_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(FusionReactor.FUSION_BLOCKS.get(name).get(), title);
        }
        add(FusionReactor.FUSION_CORE_PROXY.get(), "Fusion Reactor Core");
    }

    private void storageBlocks() {
        for(String name: STORAGE_BLOCK.keySet()) {
            String title = convertToName(name);
            add(STORAGE_BLOCK.get(name).get(), title);
        }
    }

    private void energyBlocks() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(), title);
        }
    }

    private void processors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            String title = convertToName(name);
            add(NCProcessors.PROCESSORS.get(name).get(), title);
            add("nc_jei_cat."+name, title);
        }
    }

    private void buckets() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            String molten = "";
            if(NC_INGOTS.containsKey(name)) {
                molten = "Molten ";
            }
            add(NCFluids.NC_MATERIALS.get(name).getBucket(), "Bucket of " + molten + convertToName(name));
        }
        for(String name: NCFluids.NC_GASES.keySet()) {
            add(NCFluids.NC_GASES.get(name).getBucket(), "Bucket of " + convertToName(name));
        }
    }

    private void fluids() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            String molten = "";
            if(NC_INGOTS.containsKey(name)) {
                molten = "Molten ";
            }
            add("fluid_type."+NCFluids.NC_MATERIALS.get(name).type().getId().toLanguageKey(), molten + convertToName(name));
        }
        for(String name: NCFluids.NC_GASES.keySet()) {
            add("fluid_type."+NCFluids.NC_GASES.get(name).type().getId().toLanguageKey(), convertToName(name));
        }
    }

    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
            add(NCItems.NC_SHIELDING.get(name).get(), convertToName(name)+" Shielding");
        }
    }

    private void tooltips() {

        add("processor.recipe.power","Process Power: %s FE/t");
        add("processor.recipe.duration","Process Duration: %s t");
        add("processor.recipe.radiation","Process Radiation: %s uRad");

        add("fusion_core.recipe.power","Base Energy Generation: %s FE/t");
        add("fusion_core.recipe.duration","Reaction Duration: %s t");
        add("fusion_core.recipe.radiation","Reaction Radiation: %s uRad");
        add("fusion_core.recipe.temperature","Optimal Temperature: %s MK");

        add("fission.recipe.power","Base Energy Generation: %s FE/t");
        add("fission.recipe.duration","Depletion Time: %s s");
        add("fission.recipe.radiation","Reaction Radiation: %s pRad");
        add("fission.recipe.heat","Heat Generation: %s H/t");

        add("tooltip.nc.rf_amplifier.power","Energy Required: %s FE/t");
        add("tooltip.nc.rf_amplifier.voltage","Voltage: %s kV");
        add("tooltip.nc.rf_amplifier.efficiency","Efficiency: %s%%");
        add("tooltip.nc.rf_amplifier.heat","Heat: %s H/t");
        add("tooltip.nc.rf_amplifier.max_temp","Max Temparature: %s kK");
        add("tooltip.nc.electromagnet.power","Energy Required: %s FE/t");
        add("tooltip.nc.electromagnet.magnetic_field","Magnetic Field: %s T");
        add("tooltip.nc.electromagnet.efficiency","Efficiency: %s%%");
        add("tooltip.nc.electromagnet.heat","Heat: %s H/t");
        add("tooltip.nc.electromagnet.max_temp","Max Temparature: %s kK");
        add("tooltip.nc.liquid_empty","Stored: 0 of %s");
        add("tooltip.nc.liquid_stored","Stored: %s %s / %s");
        add("tooltip.nc.liquid_capacity","Capacity: %s");
        add("effect.nuclearcraft.radiation_resistance","Radiation Resistance");
        add("leacher.tooltip.valid_pump","Pump - Ok");
        add("leacher.tooltip.invalid_pump","Pump - Fail");
        add("processor.description.alloy_smelter","Smelts and alloys items.");
        add("processor.description.centrifuge","Separates fluids into their components.");
        add("processor.description.fuel_reprocessor","Separates depleted fuel into components.");
        add("processor.description.melter","Melts items into liquids.");
        add("processor.description.ingot_former","Forms solid items from molten liquids.");
        add("processor.description.crystalizer","Grows crystals from solutions.");
        add("processor.description.chemical_reactor","Mixes fluids and gases together.");
        add("processor.description.assembler","Machine what assembles items.");
        add("processor.description.decay_hastener","Accelerates decay speed of radioactive materials.");
        add("processor.description.electrolyzer","Separates fluids and gases into their components.");
        add("processor.description.extractor","Extracts liquids from solid items.");
        add("processor.description.fluid_enricher","Enriches fluids and gases with solid items.");
        add("processor.description.fluid_infuser","Mixes fluids with items to get new items.");
        add("processor.description.irradiator","Transform items with the power of Radiative Flux.");
        add("processor.description.isotope_separator","Splits items into isotopes.");
        add("processor.description.manufactory","Crushes items into dusts and other materials.");
        add("processor.description.pressurizer","Compresses items with high pressure.");
        add("processor.description.rock_crusher","Produces dusts from rocks.");
        add("processor.description.supercooler","Cools down fluids and gases.");
        add("processor.description.steam_turbine","Produce energy with power of steam.");
        add("processor.description.gas_scrubber","Сleansing ventilation.");
        add("processor.description.pump","Pumps fluids and gasses from environment.");
        add("processor.description.analyzer","Used to analyze environment and items.");
        add("processor.description.leacher","Leaches undeground minerals with acids and pumps slurry back.");

        add("amount","Amount: %s");
        add("sound_event.nuclearcraft.item.charged","Item Charged");
        add("tooltip.nc.analyzed","Item analyze completed");
        add("tooltip.nc.shielding.desc","Combine with armor in crafting grid");
        add("tooltip.nc.rad_shielding","Rad Shielding LVL: %s");
        add("tooltip.nc.use_in_leacher","Item can be used in Leacher");
        add("tooltip.nc.energy_stored","Energy Stored: %s / %s");
        add("tooltip.nc.energy_capacity","Energy Capacity: %s");
        add("tooltip.nc.radiation","Radiation: %s");
        add("tooltip.nc.radiation_removal","Removes Radiation: %s");
        add("tooltip.press_shift_for_description","Press SHIFT for description");
        add("fuel.heat.descr","Base Heat Gen: %s H/t");
        add("message.nc.battery.side_config","Mode: %s");
        add("message.nc.barrel.side_config","Mode: %s");
        add("gui.nc.reactor_comparator_config.tooltip_1","Comparator: Energy Stored");
        add("gui.nc.reactor_comparator_config.tooltip_2","Comparator: Heat Stored");
        add("gui.nc.reactor_comparator_config.tooltip_3","Comparator: Depletion Progress");
        add("gui.nc.reactor_comparator_config.tooltip_4","Comparator: Fuel Left");
        add("gui.nc.reactor_comparator_strength.tooltip","Current Signal Strength: %s");
        add("gui.nc.redstone_config.tooltip_0","WORK MODE: IGNORE SIGNAL");
        add("gui.nc.redstone_config.tooltip_1","WORK MODE: ON SIGNAL");
        add("gui.nc.fluid_tank_renderer.amount_capacity","%s/%s mB");
        add("gui.nc.fluid_tank_renderer.amount","%s mB");
        add("fuel.forge_energy.descr","Forge Energy: %s FE/t");
        add("rtg.fe_generation","Energy Generation: %s FE/t");
        add("tooltip.nc.shift_rbm_to_change","Sneak+Use to change");
        add("tooltip.nc.qnp_mode","Mode: %s");
        add("tooltip.mode.one_block","One Block");
        add("tooltip.mode.3x3","3x3");
        add("tooltip.mode.3x3x3","3x3x3");
        add("tooltip.mode.5x5","5x5");
        add("tooltip.mode.vein","Vein");
        add("tooltip.nc.chunk_position","Chunk Position: %s");
        add("nc.ore_vein.borax","Vein of Borax");
        add("nc.ore_vein.bornite","Vein of Bornite");
        add("nc.ore_vein.cassiterite","Vein of Cassiterite");
        add("nc.ore_vein.cobaltite","Vein of Cobaltite");
        add("nc.ore_vein.magnesite","Vein of Magnesite");
        add("nc.ore_vein.platinum","Vein of Platinum");
        add("nc.ore_vein.sphalerite","Vein of Sphalerite");
        add("nc.ore_vein.spodumene","Vein of Spodumene");
        add("nc.ore_vein.uraninite","Vein of Uraninite");
        add("nc.ore_vein.none","Veins not found");
        add("nc.ore_vein.mixed","Vein of Mixed minerals");
        add("tooltip.nc.content_saved","Content Saved");
        add("fuel.heat_boiling.descr","Boiling Reactor Heat: %s H/t");
        add("fuel.depletion.descr","Base Depletion Time: %s sec");
        add("fuel.criticality.descr","Criticality Factor: %s N/t");
        add("fuel.efficiency.descr","Base Efficiency: %s%%");
        add("fuel.description","Used in Fission Reactors. Use Fuel Ports to Load/Unload. \r\nActual FE generation depends on Reactor Efficiency.");
        add("heat_sink.heat.descr", "Cooling Rate: %s H/t");
        add("heat_sink.placement.rule", "Must be placed %s");
        add("heat_sink.between", "between %s and %s");
        add("heat_sink.atleast", "next to at least %s %s");
        add("heat_sink.atleasts", "next to at least %s %s blocks");
        add("heat_sink.exact", "next to exact %s %s");
        add("heat_sink.exacts", "next to exact %s %s blocks");
        add("heat_sink.less_than", "next to less than %s %s blocks");
        add("heat_sink.in_corner", "in corner of %s %s blocks");
        add("heat_sink.or", "or");
        add("heat_sink.and", "and");
        add("heat_sink.placement.error", "Error during placement rule generation");
        add("reactor.interior.complete", "Interior Complete");
        add("reactor.interior.incomplete", "Interior Incomplete");
        add("reactor.casing.complete", "Reactor Casing Complete");
        add("reactor.casing.incomplete", "Reactor Casing Incomplete");
        add("energy.bar.amount", "Total FE: %s / %s");
        add("coolant.bar.amount", "Coolant: %s / %s mB");
        add("hot_coolant.bar.amount", "Heated Coolant: %s / %s mB");
        add("heat.bar.amount", "Total Heat: %s / %s K");
        add("tooltip.machine.progress", "Progress: %s%%");
        add("reactor.fuel_cells", "Fuel Cells: %s");
        add("fission.interior.no_fuel_cells", "No Fuel Cells Found");
        add("fission.interior.no_moderators", "No Moderators Found");
        add("fission.interior.no_heat_sink", "No Heat Sinks Found");
        add("side_config.up", "UP: ");
        add("side_config.down", "DOWN: ");
        add("side_config.left", "LEFT: ");
        add("side_config.right", "RIGHT: ");
        add("side_config.front", "FRONT: ");
        add("side_config.back", "BACK: ");
        add("side_config.input", "INPUT");
        add("side_config.pull", "PULL");
        add("side_config.output", "OUTPUT");
        add("side_config.push", "PUSH");
        add("side_config.push_excess", "PUSH EXCESS");
        add("side_config.disabled", "DISABLED");
        add("side_config.default", "DEFAULT");
        add("gui.nc.side_config.tooltip", "Side Config");

        add("speed.multiplier", "Speed Multiplier: x%s");
        add("energy.multiplier", "Energy Multiplier: x%s");
        add("energy.per_tick", "Energy Per Tick: %s FE/t");


        add("reactor.cooling", "Cooling: %s H/t");
        add("reactor.heating", "Heat Gen: %s H/t");
        add("reactor.net_heat", "Net Heat: %s H/t");
        add("reactor.forge_energy_per_tick", "FE Gen: %s FE/t");
        add("reactor.heat_sinks_count", "Active Heat Sinks: %s");
        add("reactor.moderators_count", "Active Moderators: %s");
        add("validation.structure.too_big", "Structure is too big");
        add("validation.structure.too_small", "Structure is too small");
        add("validation.structure.incomplete", "Incomplete");
        add("validation.structure.wrong_outer", "Wrong Casing at: %s");
        add("validation.structure.wrong_inner", "Wrong Block at: %s");
        add("validation.structure.too_many_controllers", "Too many controllers");
        add("validation.structure.no_controller", "No controllers");
        add("validation.structure.no_port", "No port found");
        add("validation.structure.valid", "Structure is Valid");
        add("solar_panel.fe_generation", "Daytime Gen: %s FE/t");
        add("fission_port.descr", "Used to transfer items/fluids/energy, for redstone control/output and computers.");
        add("irradiation_chamber.descr", "Used to irradiate items with neutron flux. \r\nHas to be placed in one line with moderator and fuel cell behind it.");
    }

    private void fuel() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            add(Fuel.NC_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
        for(List<String> name: Fuel.NC_DEPLETED_FUEL.keySet()) {
            add(Fuel.NC_DEPLETED_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            add(NCBlocks.ORE_BLOCKS.get(ore).get(), convertToName(ore)+" Ore");
        }
    }

    private void items() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            add(NCItems.NC_ITEMS.get(name).get(), convertToName(name));
        }
    }

    private void isotopes() {
        for(String name: Fuel.NC_ISOTOPES.keySet()) {
            add(Fuel.NC_ISOTOPES.get(name).get(), convertToName(name));
        }
    }

    private void records() {
        for(String name: NCItems.NC_RECORDS.keySet()) {
            add(NCItems.NC_RECORDS.get(name).get(), convertToName(name));
        }
    }

    private void tools()
    {
        add(QNP.get(), "QNP");
        add(MULTITOOL.get(), "Multitool");
        add(GEIGER_COUNTER.get(), "Geiger Counter");
        add(SPAXELHOE_TOUGH.get(), "Tough Spaxel");
        add(LITHIUM_ION_CELL.get(), "Lithium Ion Cell");
    }

    private void armor() {
        add(TOUGH_HELMET.get(), "Tough Helmet");
        add(TOUGH_PANTS.get(), "Tough Pants");
        add(TOUGH_BOOTS.get(), "Tough Boots");
        add(TOUGH_CHEST.get(), "Tough Chest");
        
        add(HEV_HELMET.get(), "HEV Helmet");
        add(HEV_PANTS.get(), "HEV Pants");
        add(HEV_BOOTS.get(), "HEV Boots");
        add(HEV_CHEST.get(), "HEV Chest");

        add(HAZMAT_MASK.get(), "Hazmat Mask");
        add(HAZMAT_PANTS.get(), "Hazmat Pants");
        add(HAZMAT_BOOTS.get(), "Hazmat Boots");
        add(HAZMAT_CHEST.get(), "Hazmat Chest");
    }
    
    private void food() {
        for(String name: NCItems.NC_FOOD.keySet()) {
            add(NCItems.NC_FOOD.get(name).get(), convertToName(name));
        }
    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            add(NCItems.NC_PARTS.get(name).get(), convertToName(name));
        }
    }

    private void gems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            add(NCItems.NC_GEMS.get(name).get(), convertToName(name)+" Gem");
        }
    }

    private void ingots() {
        for(String ingot: NC_INGOTS.keySet()) {
            add(NC_INGOTS.get(ingot).get(), convertToName(ingot)+" Ingot");
        }
    }

    private void plates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            add(NCItems.NC_PLATES.get(name).get(), convertToName(name)+" Plate");
        }
    }

    private void dusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            add(NCItems.NC_DUSTS.get(name).get(), convertToName(name)+" Dust");
        }
    }

    private void nuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            add(NCItems.NC_NUGGETS.get(name).get(), convertToName(name)+" Nugget");
        }
    }

    private void chunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            add(NCItems.NC_CHUNKS.get(name).get(), convertToName(name)+" Chunk");
        }
    }

    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            add(NCBlocks.NC_BLOCKS.get(name).get(), convertToName(name)+" Block");
        }
        for(String name: NCBlocks.NC_ELECTROMAGNETS.keySet()) {
            add(NCBlocks.NC_ELECTROMAGNETS.get(name).get(), convertToName(name));
        }
        for(String name: NCBlocks.NC_RF_AMPLIFIERS.keySet()) {
            add(NCBlocks.NC_RF_AMPLIFIERS.get(name).get(), convertToName(name));
        }
    }
}