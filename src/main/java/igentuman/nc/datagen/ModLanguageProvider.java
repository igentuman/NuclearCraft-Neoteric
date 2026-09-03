package igentuman.nc.datagen;

import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ToolSetEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCJukeboxSongs;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.convertToName;

/** Generates the en_us language file: display names for blocks, items, fluids, and GUI labels. */
public class ModLanguageProvider  extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), MODID, locale);
    }

    @Override
    protected void addTranslations() {
        labels();
        energy();
        heatSinks();
        msr();
        pipes();
        sounds();
        ponders();
        fissionDesigner();
        fuelInfo();

        add("processor.description.nuclear_furnace","Fast furnace that uses uranium ingots as fuel. Surprisingly safe, by furnace standards.");
        add("processor.description.alloy_smelter","Smelts and alloys items.");
        add("processor.description.centrifuge","Separates fluids into their components.");
        add("processor.description.fuel_reprocessor","Separates depleted fuel into its components.");
        add("processor.description.melter","Melts items into liquids.");
        add("processor.description.ingot_former","Forms solid items from molten liquids.");
        add("processor.description.crystallizer","Grows crystals from solutions.");
        add("processor.description.chemical_reactor","Mixes fluids and gases together.");
        add("processor.description.assembler","Assembles items from prepared components.");
        add("processor.description.decay_hastener","Accelerates the decay of radioactive materials. Half-lives are merely a suggestion.");
        add("processor.description.electrolyzer","Separates fluids and gases into their components.");
        add("processor.description.extractor","Extracts liquids from solid items.");
        add("processor.description.fluid_enricher","Enriches fluids and gases with solid items.");
        add("processor.description.fluid_infuser","Combines fluids with items to produce new items.");
        add("processor.description.irradiator","Transforms items and fluids using radiative flux. Must be placed in the reactor wall.");
        add("processor.description.isotope_separator","Splits items into isotopes.");
        add("processor.description.manufactory","Crushes items into dusts and other materials.");
        add("processor.description.pressurizer","Compresses items under high pressure.");
        add("processor.description.rock_crusher","Produces dusts from rocks.");
        add("processor.description.supercooler","Cools down fluids and gases.");
        add("processor.description.steam_turbine","Generates energy from steam pressure.");
        add("processor.description.gas_scrubber","Cleans contaminants from ventilation. Recommended in the unlikely event of a release.");
        add("processor.description.pump","Pumps fluids and gases from the environment.");
        add("processor.description.analyzer","Analyzes items and environmental samples.");
        add("processor.description.leacher","Leaches underground minerals with acids and pumps the slurry back to the surface.");
        add("processor.description.subatomic_liquifier","Decomposes elements into their subatomic constituents.");

        for (String name : NCJukeboxSongs.RECORDS.keySet()) {
            add("jukebox_song." + MODID + "." + name, convertToName(name));
        }
        for (String name : ModEntries.ENTRIES.keySet()) {
            if(ModEntries.get(name).hasBlock()) {
                add(ModEntries.get(name).block().get(), blockDisplayName(name));
                if(ModEntries.get(name).hasRecipes()) {
                    add("emi.category."+MODID+"."+name, convertToName(name));

                }
                continue;
            }
            if(ModEntries.get(name).hasItem()) {
                add(ModEntries.get(name).item().get(), convertToName(name));
                continue;
            }
            if (ModEntries.get(name).toolSetEntry() instanceof ToolSetEntry toolSet) {
                add(toolSet.sword().get(),   convertToName(toolSet.name + "_sword"));
                add(toolSet.pickaxe().get(), convertToName(toolSet.name + "_pickaxe"));
                add(toolSet.axe().get(),     convertToName(toolSet.name + "_axe"));
                add(toolSet.shovel().get(),  convertToName(toolSet.name + "_shovel"));
                add(toolSet.hoe().get(),     convertToName(toolSet.name + "_hoe"));
                continue;
            }
            if (ModEntries.get(name).armorSetEntry() instanceof ArmorSetEntry armorSet) {
                add(armorSet.helmet().get(),     convertToName(armorSet.name + "_helmet"));
                add(armorSet.chestplate().get(), convertToName(armorSet.name + "_chestplate"));
                add(armorSet.leggings().get(),   convertToName(armorSet.name + "_leggings"));
                add(armorSet.boots().get(),      convertToName(armorSet.name + "_boots"));
                continue;
            }
            if (ModEntries.get(name).materialEntry() instanceof MaterialEntry materialEntry) {
                if (materialEntry.hasOre()) {
                    add(materialEntry.oreBlock().get(), convertToName(materialEntry.name + "_ore"));
                }
                if (materialEntry.hasDeepslateOre()) {
                    add(materialEntry.deepslateOreBlock().get(), convertToName(materialEntry.name + "_deepslate_ore"));
                }
                if (materialEntry.hasBlock()) {
                    add(materialEntry.storageBlock().get(), convertToName(materialEntry.name + "_block"));
                }
                if (materialEntry.hasIngot()) {
                    add(materialEntry.ingot().get(), convertToName(materialEntry.name + "_ingot"));
                }
                if (materialEntry.hasGem()) {
                    add(materialEntry.gem().get(), convertToName(materialEntry.name + "_gem"));
                }
                if (materialEntry.hasRawOre()) {
                    add(materialEntry.rawOre().get(), convertToName("raw_" + materialEntry.name));
                }
                if (materialEntry.hasDust()) {
                    add(materialEntry.dust().get(), convertToName(materialEntry.name + "_dust"));
                }
                if (materialEntry.hasPlate()) {
                    add(materialEntry.plate().get(), convertToName(materialEntry.name + "_plate"));
                }
                if (materialEntry.hasNugget()) {
                    add(materialEntry.nugget().get(), convertToName(materialEntry.name + "_nugget"));
                }
                if (materialEntry.hasFluid()) {
                    var fluid = materialEntry.materialFluid();
                    String fluidName = materialEntry.fluidDefinition.resolveName(materialEntry.name);
                    add(fluid.bucket().get(), convertToName(fluidName + "_bucket"));
                    add("fluid_type.nuclearcraft." + fluidName, convertToName(fluidName));
                }
            }
        }
        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            isotope.variants().forEach((suffix, item) ->
                    add(item.get(), convertToName(isotope.itemId + suffix)));
            for (MaterialEntry mat : isotope.fluids()) {
                String fluidName = mat.fluidDefinition.resolveName(mat.name);
                add(mat.bucket().get(), convertToName(fluidName + "_bucket"));
                add("fluid_type.nuclearcraft." + fluidName, convertToName(fluidName));
            }
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            fuel.fuelItems().forEach((variant, item) ->
                    add(item.get(), fuelDisplayName("Fuel", fuel.group, fuel.name, variant)));
            fuel.depletedItems().forEach((variant, item) ->
                    add(item.get(), fuelDisplayName("Depleted", fuel.group, fuel.name, variant)));
            for (MaterialEntry mat : fuel.fluids()) {
                String fluidName = mat.fluidDefinition.resolveName(mat.name);
                add(mat.bucket().get(), convertToName(fluidName + "_bucket"));
                add("fluid_type.nuclearcraft." + fluidName, convertToName(fluidName));
            }
        }
    }

    /** Builds a readable fuel name, e.g. {@code "Fuel Uranium HEU-233 OX"}. */
    private static String fuelDisplayName(String typeWord, String group, String name, String variant) {
        StringBuilder sb = new StringBuilder(typeWord).append(' ')
                .append(convertToName(group)).append(' ')
                .append(name.toUpperCase());
        if (!variant.isEmpty()) {
            sb.append(' ').append(variant.substring(1).toUpperCase());
        }
        return sb.toString();
    }

    private static String blockDisplayName(String name) {
        if (name.equals("expl")) return "EXPL";
        if (name.equals("multiblock_builder")) return "Fission Reactor Builder";
        if (name.startsWith("msr_")) {
            return "MSR " + convertToName(name.substring("msr_".length()));
        }
        if (name.endsWith("_rtg")) {
            return convertToName(name.substring(0, name.length() - "_rtg".length())) + " RTG";
        }
        return convertToName(name);
    }

    private void ponders() {
        add("nuclearcraft.ponder.fission_reactor.header", "Fission Reactor");
        add("nuclearcraft.ponder.fission_reactor.text_1", "Walls are mainly made of Fission Reactor Casing or Reactor Glass.");
        add("nuclearcraft.ponder.fission_reactor.text_2", "Place the Fission Reactor Controller anywhere you like to form the structure.");
        add("nuclearcraft.ponder.fission_reactor.text_3", "The Reactor Port is a universal block allowing you to load/unload fuel and liquids, read or send redstone signals, and attach computers.");
        add("nuclearcraft.ponder.fission_reactor.text_4", "Use as many ports as you like.");
        add("nuclearcraft.ponder.fission_reactor.text_5", "Start the reactor with a redstone signal to the controller or port (make sure to select redstone mode in the port GUI).");
        add("nuclearcraft.ponder.fission_reactor.text_6", "There are no strict requirements for how internal reactor blocks must be placed.");
        add("nuclearcraft.ponder.fission_reactor.text_7", "The Fuel Cell block is used for energy and heat generation.");
        add("nuclearcraft.ponder.fission_reactor.text_8", "Place as many fuel cells as you like anywhere inside the reactor.");
        add("nuclearcraft.ponder.fission_reactor.text_9", "The resulting energy and heat generation is multiplied by the number of fuel cells.");
        add("nuclearcraft.ponder.fission_reactor.text_10", "It affects the fuel depletion speed at the same rate.");
        add("nuclearcraft.ponder.fission_reactor.text_11", "Another way to get more energy and heat is to attach moderator blocks to fuel cells.");
        add("nuclearcraft.ponder.fission_reactor.text_12", "Each moderator block face connected to a fuel cell increases FE generation by 17 % and the heat rate by 33 %.");
        add("nuclearcraft.ponder.fission_reactor.text_13", "Moderators between two fuel cells give an additional bonus.");
        add("nuclearcraft.ponder.fission_reactor.text_14", "The reactor will melt down without heatsinks. SL-1 was an isolated incident; do not be the second.");
        add("nuclearcraft.ponder.fission_reactor.text_15", "Each heatsink has specific placement rules to be active.");
        add("nuclearcraft.ponder.fission_reactor.text_16", "You are free to design your reactor as you like. Just make sure you place heatsinks according to their placement rules.");
        add("nuclearcraft.ponder.fission_reactor.text_17", "Fission Reactor irradiation feature.");
        add("nuclearcraft.ponder.fission_reactor.text_18", "An irradiation line is a set of three blocks in a row: Fuel Cell -> Moderator -> Irradiation Chamber.");
        add("nuclearcraft.ponder.fission_reactor.text_19", "Up to six irradiation lines for each Irradiation Chamber block.");
        add("nuclearcraft.ponder.fission_reactor.text_20", "Place the Irradiator anywhere in the reactor wall.");
        add("nuclearcraft.ponder.fission_reactor.text_21", "When the reactor is up and running, the Irradiator will use all irradiation lines to produce recipes.");
        add("nuclearcraft.ponder.fission_reactor.text_22", "Swap in a Pile-Driver Irradiation Chamber for 5x irradiation speed.");
        add("nuclearcraft.ponder.fusion_reactor.header", "Fusion Reactor");
        add("nuclearcraft.ponder.fusion_reactor.text_1", "The Fusion Core is the central part of the reactor.");
        add("nuclearcraft.ponder.fusion_reactor.text_2", "It automatically occupies a 3x3x3 volume around it.");
        add("nuclearcraft.ponder.fusion_reactor.text_3", "Add one Fusion Reactor Connector in each horizontal direction.");
        add("nuclearcraft.ponder.fusion_reactor.text_4", "You can have up to 10 connectors in each horizontal direction.");
        add("nuclearcraft.ponder.fusion_reactor.text_5", "Bigger ring - more energy and heat.");
        add("nuclearcraft.ponder.fusion_reactor.text_6", "Finally, build the Ring Chamber with a 3x3 cross-section.");
        add("nuclearcraft.ponder.fusion_reactor.text_7", "The chamber must be hollow to allow plasma to circulate.");
        add("nuclearcraft.ponder.fusion_reactor.text_8", "Fusion reactor functional blocks.");
        add("nuclearcraft.ponder.fusion_reactor.text_9", "Functional blocks must be placed anywhere in the corners of reactor ring.");
        add("nuclearcraft.ponder.fusion_reactor.text_10", "RF Amplifiers are used to heat the plasma. Insufficient amplification may prevent ignition.");
        add("nuclearcraft.ponder.fusion_reactor.text_11", "Electromagnets increase the plasma cross-section and stabilize the reaction.");
        add("nuclearcraft.ponder.fusion_reactor.text_12", "When the reactor is ready, charge it and pump in fuel and coolant.");
        add("nuclearcraft.ponder.fusion_reactor.text_13", "Start the reactor with a redstone signal to the Fusion Core. Signal strength directly affects RF amplification.");
        add("nuclearcraft.ponder.fusion_reactor.text_14", "RF amplification can also be adjusted in the reactor GUI.");
        add("nuclearcraft.ponder.target_chamber.text_1", "Target Chamber size can be from 5x5x5 up to 11x11x11.");
        add("nuclearcraft.ponder.target_chamber.text_2", "The center of the structure must be a Target Chamber Camera.");
        add("nuclearcraft.ponder.target_chamber.text_3", "Beam blocks must connect the camera to the beam ports in all 4 horizontal directions.");
        add("nuclearcraft.ponder.target_chamber.text_4", "Structure needs at least 1 input beam port and 3 output beam ports.");
        add("nuclearcraft.ponder.target_chamber.text_5", "Use a Multitool to change the port mode.");
        add("nuclearcraft.ponder.target_chamber.text_6", "Detectors must be placed around the camera to collect data.");
        add("nuclearcraft.ponder.target_chamber.text_7", "Add Target Chamber Ports for energy and item/fluid transport.");
        add("nuclearcraft.ponder.target_chamber.text_8", "Place the Target Chamber Controller on the casing.");
        add("nuclearcraft.ponder.target_chamber.text_9", "When the structure is valid, start it with redstone signal to controller block.");
        add("nuclearcraft.ponder.decay_chamber.text_1", "Decay Chamber size can be from 5x5x5 up to 11x11x11.");
        add("nuclearcraft.ponder.decay_chamber.text_2", "The center of the structure must be a Particle Chamber Camera.");
        add("nuclearcraft.ponder.decay_chamber.text_3", "Beam blocks must connect the camera to the beam ports in all 4 horizontal directions.");
        add("nuclearcraft.ponder.decay_chamber.text_4", "One beam port feeds the input beam; the others carry off the lighter particles split out of it.");
        add("nuclearcraft.ponder.decay_chamber.text_5", "Use a Multitool to change the port mode.");
        add("nuclearcraft.ponder.decay_chamber.text_6", "Detectors must be placed around the camera to collect data.");
        add("nuclearcraft.ponder.decay_chamber.text_7", "Add Particle Chamber Ports for energy and item/fluid transport.");
        add("nuclearcraft.ponder.decay_chamber.text_8", "Place the Decay Chamber Controller on the casing.");
        add("nuclearcraft.ponder.decay_chamber.text_9", "When the structure is valid, start it with redstone signal to controller block.");
        add("nuclearcraft.ponder.collision_chamber.text_1", "Collision Chambers are long boxes: 5 to 11 wide and tall, and 13 to 21 deep (17 by default).");
        add("nuclearcraft.ponder.collision_chamber.text_2", "A line of Particle Chamber Cameras runs the full length of the chamber, linked by Particle Beam blocks.");
        add("nuclearcraft.ponder.collision_chamber.text_3", "Both ends of that axis are beam ports in INPUT mode. Two opposing beams enter here and collide.");
        add("nuclearcraft.ponder.collision_chamber.text_4", "Four beam ports in OUTPUT mode sit on the side walls, two per wall. Collision products leave through them.");
        add("nuclearcraft.ponder.collision_chamber.text_5", "Each output port reaches a camera along a straight line of Particle Beam blocks.");
        add("nuclearcraft.ponder.collision_chamber.text_6", "Use a Multitool to switch a port between input and output mode.");
        add("nuclearcraft.ponder.collision_chamber.text_7", "Detectors fill the interior to raise efficiency at the cost of power.");
        add("nuclearcraft.ponder.collision_chamber.text_8", "Place the Collision Chamber Controller on the casing and add Particle Chamber Ports for energy and items.");
        add("nuclearcraft.ponder.collision_chamber.text_9", "Feed the controller a redstone signal to start the collision.");
        add("nuclearcraft.ponder.linear_accelerator.text_1", "One end needs an Ion Source Port or Particle Beam Port (Input).");
        add("nuclearcraft.ponder.linear_accelerator.text_2", "The opposite end needs a Beam Port (Output).");
        add("nuclearcraft.ponder.linear_accelerator.text_3", "RF Amplifiers increase particle energy. Place 8 blocks around a beam block.");
        add("nuclearcraft.ponder.linear_accelerator.text_4", "Electromagnets increase beam focus. Place 4 blocks around a beam block.");
        add("nuclearcraft.ponder.linear_accelerator.text_5", "Accelerator Coolers must be placed inside to regulate temperature.");
        add("nuclearcraft.ponder.linear_accelerator.text_6", "Finalize with Casing, Glass, Ports and a Controller.");
        add("nuclearcraft.ponder.linear_accelerator.text_7", "Connect beamline from beam output port to other structure.");
        add("nuclearcraft.ponder.linear_accelerator.text_8", "Provide redstone signal to controller block. Signal strength affects acceleration energy.");
        add("nuclearcraft.ponder.ring_accelerator.text_1", "Synchrotron casing forms a square torus, 5 blocks wide on each side.");
        add("nuclearcraft.ponder.ring_accelerator.text_2", "A continuous ring of Particle Beam blocks runs through the middle of all four sides.");
        add("nuclearcraft.ponder.ring_accelerator.text_3", "Place a dipole magnet at every corner: electromagnet above and below the beam, yokes around.");
        add("nuclearcraft.ponder.ring_accelerator.text_4", "Inside corners of the ring may host coolers to dump heat.");
        add("nuclearcraft.ponder.ring_accelerator.text_5", "Use Ring Accelerator Ports for energy, fluids and redstone, and Beam Ports for particle input/output. The generic Accelerator Port and Ion Source Port do not fit on a ring.");
        add("nuclearcraft.ponder.ring_accelerator.text_6", "Pipe in an existing beam at 5 MeV or higher, then power the controller. Redstone strength scales output energy.");
        add("nuclearcraft.ponder.beam_diverter.header", "Beam Diverter");
        add("nuclearcraft.ponder.beam_diverter.text_1", "The Beam Diverter is a fixed 5x5x5 cube of Accelerator Casing and Glass.");
        add("nuclearcraft.ponder.beam_diverter.text_2", "An Accelerator Beam Port sits at the center of each of the four walls.");
        add("nuclearcraft.ponder.beam_diverter.text_3", "Inside, a cross of Particle Beam blocks links the four ports through the center.");
        add("nuclearcraft.ponder.beam_diverter.text_4", "A dipole magnet - an electromagnet above and below the center, with yokes filling the rest - bends the beam.");
        add("nuclearcraft.ponder.beam_diverter.text_5", "Set exactly one port to Input and at least one to Output - with the Multitool, redstone, or a computer.");
        add("nuclearcraft.ponder.beam_diverter.text_6", "The diverter reroutes the incoming beam to the active output. A 90 degree turn costs energy that scales with dipole strength; a straight pass-through only loses focus.");
        add("nuclearcraft.ponder.beam_diverter.text_7", "Mount the Beam Diverter Controller in the casing and power it to start routing.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_1", "Kugelblitz Chamber size is 11x11x11.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_2", "All 6 walls must be perfectly symmetric.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_3", "The Kugelblitz Chamber Terminal is the main control block.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_4", "Chamber Ports are used for energy and item transport. Redstone input/output and computers.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_5", "Photon Concentrators must be placed at the center of all 6 walls.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_6", "Quantum Flux Regulators affect the Forge Energy output rate.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_7", "Event Horizon Stabilizers help maintain black hole stability.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_8", "Quantum Transformers improve the efficiency of transformation processes.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_9", "Finally, all 6 Excited Photon Lasers (EXPL) must be burst at the same time.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_10", "They all need to be fully charged and then activated with redstone or in their GUI.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_11", "When all 6 lasers burst simultaneously, a black hole forms inside the chamber. Containment is, optimistically, a solved problem.");
        add("nuclearcraft.ponder.turbine.header", "Turbine");
        add("nuclearcraft.ponder.turbine.text_1", "Walls are mainly made of Turbine Casing or Turbine Glass.");
        add("nuclearcraft.ponder.turbine.text_2", "Turbine can have vertical or horizontal orientation.");
        add("nuclearcraft.ponder.turbine.text_3", "Place the Turbine Controller anywhere in the casing to form the structure.");
        add("nuclearcraft.ponder.turbine.text_4", "Turbine Ports allow fluid and energy transfer.");
        add("nuclearcraft.ponder.turbine.text_5", "Bearings are placed at the center of the casing ends.");
        add("nuclearcraft.ponder.turbine.text_6", "The Rotor Shaft connects the two bearings.");
        add("nuclearcraft.ponder.turbine.text_7", "Attach Turbine Blades to the Rotor Shaft.");
        add("nuclearcraft.ponder.turbine.text_8", "Coils must be placed next to a bearing or another active coil.");
        add("nuclearcraft.ponder.turbine.text_9", "When turbine is ready, you can start it with redstone signal to controller.");
        add("nuclearcraft.ponder.heat_exchanger.header", "Heat Exchanger");
        add("nuclearcraft.ponder.heat_exchanger.text_1", "A cuboid shell of Heat Exchanger Casing, from 3x3x3 up to 11x11x11. Non-cube shapes are allowed.");
        add("nuclearcraft.ponder.heat_exchanger.text_2", "Place one Heat Exchanger Controller in the shell. It owns the shared heat buffer.");
        add("nuclearcraft.ponder.heat_exchanger.text_3", "Hot Coolant Ports take hot coolant in, return it cooled, and dump the heat into the buffer. Run a matched pair.");
        add("nuclearcraft.ponder.heat_exchanger.text_4", "Cold Coolant Ports condense spent steam back into water, drawing the stored heat to do it. Two of them as well.");
        add("nuclearcraft.ponder.heat_exchanger.text_5", "Radiators go on the top face and passively vent surplus heat, so the hot side never jams.");
        add("nuclearcraft.ponder.heat_exchanger.text_6", "Fill the interior with Heat Exchanger blocks. More blocks mean faster processing and a bigger heat buffer.");
        add("nuclearcraft.ponder.heat_exchanger.text_7", "Apply a redstone signal: the hot loop banks heat, the cold loop spends it. Both run at once on standby power.");
        add("nuclearcraft.ponder.molten_salt_reactor.header", "Molten Salt Reactor");
        add("nuclearcraft.ponder.molten_salt_reactor.text_1", "A cuboid shell of Reactor Casing edges and Reactor Glass walls, from 5x5x5 up to 26x26x26.");
        add("nuclearcraft.ponder.molten_salt_reactor.text_2", "Place one MSR Controller. It runs the reaction and owns the salt tanks and pebble slots.");
        add("nuclearcraft.ponder.molten_salt_reactor.text_3", "Ports move salt and pebbles in and out: cold salt in, hot salt out, pebbles in, depleted out.");
        add("nuclearcraft.ponder.molten_salt_reactor.text_4", "An Irradiator can sit in the wall and add irradiation to the chamber.");
        add("nuclearcraft.ponder.molten_salt_reactor.text_5", "Fill the entire interior with MSR Fuel Cells. More cells mean more salt volume and a bigger heat budget.");
        add("nuclearcraft.ponder.molten_salt_reactor.text_6", "Pipe cold FLiBe salt in, load TRISO pebbles, apply redstone. The core heats up and turns cold salt into hot salt - send it to a Heat Exchanger.");
    }

    private void energy() {
        add("block.nuclearcraft.uranium_rtg.desc", "Radioisotope thermoelectric generator. Produces a steady 112 FE/t and emits radiation.");
        add("block.nuclearcraft.americium_rtg.desc", "Radioisotope thermoelectric generator. Produces a steady 448 FE/t and emits radiation.");
        add("block.nuclearcraft.plutonium_rtg.desc", "Radioisotope thermoelectric generator. Produces a steady 1792 FE/t and emits radiation.");
        add("block.nuclearcraft.californium_rtg.desc", "Radioisotope thermoelectric generator. Produces a steady 4096 FE/t and emits radiation.");
        add("block.nuclearcraft.solar_panel_basic.desc", "Generates 28 FE/t while exposed to daytime sky.");
        add("block.nuclearcraft.solar_panel_advanced.desc", "Generates 112 FE/t while exposed to daytime sky.");
        add("block.nuclearcraft.solar_panel_du.desc", "Generates 448 FE/t while exposed to daytime sky.");
        add("block.nuclearcraft.solar_panel_elite.desc", "Generates 1792 FE/t while exposed to daytime sky.");
        add("block.nuclearcraft.decay_generator.desc", "Generates energy from adjacent radioactive blocks. Emits radiation and decays those blocks to lead over time.");
        add("tooltip.nuclearcraft.decay_generator_allowed", "Can be used with Decay Generator");
    }

    private void heatSinks() {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            add(entry.block().get(), convertToName(entry.name + "_heat_sink"));
        }
        add("tooltip.nuclearcraft.heat_sink.heat", "Cooling: %s H/t");
        add("tooltip.nuclearcraft.heat_sink.active", "Needs coolant fluid supply into reactor to work.");
        add("tooltip.nuclearcraft.turbine_coil.efficiency", "Efficiency: %s%%");
        add("tooltip.nuclearcraft.shift", "Hold Shift for placement rule");
        add("heat_sink.placement.rule", "Must be placed %s");
        add("heat_sink.atleast", "next to at least %s %s");
        add("heat_sink.atleasts", "next to at least %s %s blocks");
        add("heat_sink.between", "between %s %s");
        add("heat_sink.exact", "next to exactly %s %s");
        add("heat_sink.exacts", "next to exactly %s %s blocks");
        add("heat_sink.less_than", "next to less than %s %s");
        add("heat_sink.in_corner", "in the corner of %s %s blocks");
        add("heat_sink.or", "or");
        add("heat_sink.and", "and");
    }

    private void msr() {
        add("multiblock.nuclearcraft.molten_salt_reactor", "Molten Salt Reactor");
        add("sound_event.nuclearcraft.msr_running", "Molten Salt Reactor humming along");
        add("msr.status", "Status: %s");
        add("msr.critical", "CRITICAL");
        add("msr.subcritical", "SUBCRITICAL");
        add("msr.non_functional", "Non-functional");
        add("msr.reactivity", "Reactivity: %s");
        add("msr.temperature", "Temperature: %sK");
        add("msr.depletion", "Depletion: %s%%");
        add("msr.overheat", "Overheat: %ss");
        add("msr.input_rate.tooltip", "Molten salt input rate (buckets/tick). Cold FLiBe drawn into the reactor each tick.");
        add("msr.output_rate.tooltip", "Hot molten salt output rate (buckets/tick). Pumped out each tick — the reactor's only cooling. Run it too low and the core overheats.");
        add("reactor.fuel_cells", "Fuel Cells: %s");
        add("reactor.heating", "Heat Gen: %s H/t");
        add("gui.nc.msr.void_pebbles.tooltip", "Void fuel pebbles");
        add("tooltip.nc.msr_controller.descr", "Runs the Molten Salt Reactor: pumps FLiBe carrier salt past TRISO fuel pebbles, holds the chain reaction, and ships the heat out as hot salt. (No heat sinks, no moderators - just don't let the cooling stop.)");
        add("tooltip.nc.msr_port.descr", "Loads fuel pebbles and pipes molten salt in and out. Cold salt in, hot salt out - and that hot-salt flow is the core's only cooling.");
    }

    private void fissionDesigner() {
        add("tooltip.nc.fission_reactor_designer", "Design and simulate fission reactor layouts.");
        add("tooltip.nc.fission_reactor_plan.blank", "Empty plan - design one in the Fission Reactor Designer.");
        add("tooltip.nc.fission_reactor_plan.size", "Size: %sx%sx%s");
        add("tooltip.nc.fission_reactor_plan.fuel", "Fuel: %s");
        add("tooltip.nc.fission_reactor_plan.net_heat", "Net Heat: %s H/t");
        add("tooltip.nc.fission_reactor_plan.fe_gen", "Power: %s FE/t");
        add("nc.multiblock_builder.description", "Loads a Fission Reactor Plan and builds it, pulling blocks from adjacent containers.");
        add("nc.multiblock_builder.load_plan", "Load Plan");
        add("nc.multiblock_builder.build", "Build");
        add("nc.multiblock_builder.area_blocked", "Area blocked at %s, %s, %s");
        add("nc.multiblock_builder.no_containers", "No adjacent containers with building blocks found");
        add("nc.multiblock_builder.missing_blocks", "Missing %s x%s");
        add("nc.multiblock_builder.build_success", "Structure built successfully");
        add("nc.fission_designer.no_multitool", "You need to have a Multibuilder Tool in your inventory");
    }

    private void pipes() {
        add("block.nuclearcraft.pipe.desc", "Passive network conduit. Connects only to other pipes and connectors.");
        add("block.nuclearcraft.pipe_connector.desc", "Network I/O node. Interfaces the pipe network with adjacent machines. Right-click to configure; sneak-right-click to cycle mode.");
        add("gui.nuclearcraft.pipe.capability.item", "Items");
        add("gui.nuclearcraft.pipe.capability.fluid", "Fluids");
        add("gui.nuclearcraft.pipe.capability.energy", "Energy");
        add("gui.nuclearcraft.pipe.mode.disabled", "Mode: Disabled");
        add("gui.nuclearcraft.pipe.mode.pull", "Mode: Pull");
        add("gui.nuclearcraft.pipe.mode.push", "Mode: Push");
        add("gui.nuclearcraft.pipe.mode.default", "Mode: Default");
        add("gui.nuclearcraft.pipe.redstone.always", "Redstone: Always");
        add("gui.nuclearcraft.pipe.redstone.on_signal", "Redstone: On Signal");
    }

    private void sounds() {
        add("sound_event.nuclearcraft.item.charged", "Item charges");
        add("sound_event.nuclearcraft.laser.shoot", "Laser fires");
        add("sound_event.nuclearcraft.boss_hit", "Boss takes damage");
        add("sound_event.nuclearcraft.boss_idle", "Boss growls");
        add("sound_event.nuclearcraft.boss_angry", "Boss roars");
        add("sound_event.nuclearcraft.boss_action", "Boss attacks");
        add("sound_event.nuclearcraft.feral_ghoul.death", "Feral Ghoul dies");
        add("sound_event.nuclearcraft.feral_ghoul.idle", "Feral Ghoul growls");
        add("sound_event.nuclearcraft.fission_reactor", "Fission reactor hums");
        add("sound_event.nuclearcraft.fusion.charging", "Fusion reactor charges");
        add("sound_event.nuclearcraft.fusion.running", "Fusion reactor runs");
        add("sound_event.nuclearcraft.fusion.ready", "Fusion reactor ready");
        add("sound_event.nuclearcraft.fusion.switch", "Fusion reactor switches mode");
        add("sound_event.nuclearcraft.turbine", "Turbine spins");
        add("sound_event.nuclearcraft.blackhole.spawn", "Black hole forms");
        add("sound_event.nuclearcraft.blackhole.idle", "Black hole hums");
        add("sound_event.nuclearcraft.bomb.blast", "Bomb explodes");
        add("sound_event.nuclearcraft.anomaly.gravitational", "Gravitational Anomaly hums");
        add("sound_event.nuclearcraft.anomaly.electric", "Electric Anomaly crackles");
        add("sound_event.nuclearcraft.anomaly.radioactive", "Radioactive Anomaly hisses");
        add("sound_event.nuclearcraft.anomaly.burning", "Burning Anomaly crackles");
        add("sound_event.nuclearcraft.anomaly.psycho", "Psycho Anomaly shrieks");
        add("sound_event.nuclearcraft.anomaly.teleporting", "Teleporting Anomaly warps");
        add("sound_event.nuclearcraft.item.geiger_1", "Geiger counter clicks");
        add("sound_event.nuclearcraft.item.geiger_2", "Geiger counter clicks");
        add("sound_event.nuclearcraft.item.geiger_3", "Geiger counter clicks");
        add("sound_event.nuclearcraft.item.geiger_4", "Geiger counter clicks");
        add("sound_event.nuclearcraft.item.geiger_5", "Geiger counter clicks");
        add("sound_event.nuclearcraft.item.geiger_6", "Geiger counter clicks");
        add("music.wanderer", "Wanderer plays");
        add("music.end_of_the_world", "End of the World plays");
        add("music.money_for_nothing", "Money for Nothing plays");
        add("music.hyperspace", "Hyperspace plays");
    }

    private void labels() {
        add("nc.label.leacher_wrong_position", "Wrong Position");
        add("nc.label.leacher_no_source", "No Source");
        add("nc.label.leacher_no_acid", "No Acid");
        add("nc.label.leacher_pumps_error", "Pumps Error");
        add("leacher.tooltip.valid_pump", "Valid Pump");
        add("leacher.tooltip.invalid_pump", "Invalid Pump");
        add("tooltip.nc.chunk_position", "Chunk Position: %s");
        add("tooltip.nc.use_in_leacher", "Use in Leacher at this position");
        add("nc.ore_vein.none", "No Vein");
        add("nc.ore_vein.uraninite", "Uraninite Vein");
        add("nc.ore_vein.bornite", "Bornite Vein");
        add("nc.ore_vein.platinum", "Platinum Vein");
        add("nc.ore_vein.cobaltite", "Cobaltite Vein");
        add("nc.ore_vein.spodumene", "Spodumene Vein");
        add("nc.ore_vein.magnesite", "Magnesite Vein");
        add("nc.ore_vein.sphalerite", "Sphalerite Vein");
        add("nc.ore_vein.cassiterite", "Cassiterite Vein");
        add("nc.ore_vein.borax", "Borax Vein");
        add("tooltip.nc.charging_station", "Charges energy items from stored power and fills fluid items with Quantite Energy gas.");
        add("tooltip.nc.q36_mode", "Mode: %s");
        add("tooltip.nc.q36_mode.pulse", "Pulse");
        add("tooltip.nc.q36_mode.beam", "Beam");
        add("tooltip.nc.q36_charge", "Charge: %s / %s QE");
        add("tooltip.nc.q36_cooldown", "Recharging: %s ticks");
        add("tooltip.nc.q36_hint", "Right-click to switch mode. Left-click to fire.");
        add("entity.nuclearcraft.q36_pulse_projectile", "Quantite Pulse");
        add("entity.nuclearcraft.q36_energy_flash", "Energy Flash");
        add("sound_event.nuclearcraft.q36.beam_shot", "Q-36 Quantite Disruptor fires");
        add("sound_event.nuclearcraft.q36.pulse_shot", "Q-36 Quantite Pulse discharge");
        add("itemGroup.nuclearcraft.fission_reactor", "NuclearCraft: Fission Reactor");
        add("itemGroup.nuclearcraft.kugelblitz", "NuclearCraft: Kugelblitz Chamber");
        add("block.nuclearcraft.pu_239_bomb.desc", "Warning: arms on redstone signal and detonates after a short fuse. Not safe for dispenser placement.");
        add("screen.nuclearcraft.kugelblitz", "Kugelblitz Chamber");
        add("screen.nuclearcraft.kugelblitz.rate", "Transformation vs Energy Generation: %s%%");
        add("screen.nuclearcraft.kugelblitz.frequency", "Frequency: %s");
        add("screen.nuclearcraft.kugelblitz.mass", "Mass: %s");
        add("screen.nuclearcraft.kugelblitz.evaporation", "Evaporation: %s");
        add("screen.nuclearcraft.kugelblitz.feeding", "Feeding: %s");
        add("screen.nuclearcraft.kugelblitz.energy", "Output: %s FE/t");
        add("screen.nuclearcraft.kugelblitz.stability", "Stability: %s");
        add("screen.nuclearcraft.expl.charge", "Charge: %s");
        add("gui.nuclearcraft.button.burst", "Burst");
        add("screen.nuclearcraft.redstone_config_0", "Redstone: Active always");
        add("screen.nuclearcraft.redstone_config_1", "Redstone: Active on signal");
        add("screen.nuclearcraft.show_recipes", "Show Recipes");
        add("screen.nuclearcraft.side_config", "Side Configuration");
        add("screen.nuclearcraft.slot_selection", "Select Slot");
        add("screen.nuclearcraft.multiblock.assembled", "Assembled");
        add("screen.nuclearcraft.multiblock.not_assembled", "Not Assembled");
        add("screen.nuclearcraft.heat", "Heat");
        add("screen.nuclearcraft.heat_rate", "Heat Rate: %s/t");
        add("screen.nuclearcraft.net_heat", "Net Rate: %s/t");
        add("screen.nuclearcraft.cooldown_rate", "Cooling Rate: %s/t");
        add("screen.nuclearcraft.fission.energy", "Output");
        add("screen.nuclearcraft.fission.reactivity", "Reactivity");
        add("screen.nuclearcraft.fission.fuel_cells", "Fuel Cells");
        add("screen.nuclearcraft.fission.heat_sinks_count", "Heat Sinks");
        add("screen.nuclearcraft.fission.moderators_count", "Moderators");
        add("screen.nuclearcraft.fission.moderation_level", "Moderation");
        add("screen.nuclearcraft.fission.irradiators_connections", "Irradiator Connections");
        add("screen.nuclearcraft.fission.fe_per_tick", "Output");
        add("screen.nuclearcraft.fission.steam_per_tick", "Steam");
        add("screen.nuclearcraft.fission.mode.energy", "Energy");
        add("screen.nuclearcraft.fission.mode.steam", "Steam");
        add("screen.nuclearcraft.turbine", "Steam Turbine");
        add("screen.nuclearcraft.turbine.output", "Output");
        add("screen.nuclearcraft.turbine.real_flow", "Flow");
        add("screen.nuclearcraft.turbine.max_flow", "Max Flow");
        add("screen.nuclearcraft.turbine.ratio", "Ratio");
        add("screen.nuclearcraft.turbine.efficiency", "Efficiency");
        add("screen.nuclearcraft.turbine.active_coils", "Active Coils");
        add("screen.nuclearcraft.turbine.blades", "Blades");
        add("screen.nuclearcraft.turbine.max_output", "Max Output");
        add("itemGroup.nuclearcraft.heat_exchanger", "NuclearCraft: Heat Exchanger");
        add("multiblock.nuclearcraft.heat_exchanger", "Heat Exchanger");
        add("screen.nuclearcraft.heat_exchanger", "Heat Exchanger");
        add("screen.nuclearcraft.heat_exchanger.blocks", "Heat Exchangers: %s");
        add("screen.nuclearcraft.heat_exchanger.radiators", "Radiators: %s");
        add("screen.nuclearcraft.heat_exchanger.hot_cycle", "Hot Cycle: %s ops/t");
        add("screen.nuclearcraft.heat_exchanger.cold_cycle", "Cold Cycle: %s ops/t");
        add("screen.nuclearcraft.heat_exchanger.radiator_toggle.enable", "Enable radiators");
        add("screen.nuclearcraft.heat_exchanger.radiator_toggle.disable", "Disable radiators");
        add("gui.nuclearcraft.heat_exchanger", "Heat Exchanger");
        add("gui.nuclearcraft.heat_exchanger.heat_add", "Heat output: +%s H");
        add("gui.nuclearcraft.heat_exchanger.heat_remove", "Heat draw: -%s H");
        add("block.nuclearcraft.heat_exchanger_radiator.desc", "Vents %s H/t from the heat buffer. Passive: cools whenever the multiblock is formed.");
        add("container.nuclearcraft.engineers_encoder", "Pattern Encoder");
        add("tooltip.nuclearcraft.crafting_pattern.blank", "Blank");
        add("block.nuclearcraft.engineers_crafting_table.desc", "AE2-terminal-style auto-crafter that stores container-block items and aggregates their inventories.");
        add("screen.nuclearcraft.crafter.search", "Search...");
        add("screen.nuclearcraft.crafter.view", "View: %s");
        add("screen.nuclearcraft.crafter.mode.stored", "Stored");
        add("screen.nuclearcraft.crafter.mode.craftable", "Craftable");
        add("screen.nuclearcraft.crafter.mode.both", "Both");
        add("screen.nuclearcraft.crafter.stored", "In stock: %s");
        add("screen.nuclearcraft.crafter.craftable_tt", "Craftable");
        add("screen.nuclearcraft.crafter.encoder", "Open Pattern Encoder");
        add("screen.nuclearcraft.crafter.encode", "Encode recipe onto a blank pattern");
        add("screen.nuclearcraft.crafter.confirm_title", "Confirm Craft");
        add("screen.nuclearcraft.crafter.confirm", "Confirm");
        add("screen.nuclearcraft.crafter.cancel", "Cancel");
        add("screen.nuclearcraft.crafter.back", "Back");
        add("screen.nuclearcraft.crafter.qty", "QTY: %s");
        add("screen.nuclearcraft.crafter.denied_title", "Crafting Denied");
        add("screen.nuclearcraft.crafter.denied.entry", "%sx %s");
        add("screen.nuclearcraft.crafter.denied.more", "...and %s more");
        add("screen.nuclearcraft.crafter.denied.too_complex", "Bill of materials exceeds planner limits.");
        add("message.nuclearcraft.redstone_mode", "Redstone mode: %s");
        add("message.nuclearcraft.redstone_mode.none", "Off");
        add("message.nuclearcraft.redstone_mode.energy", "Energy level");
        add("message.nuclearcraft.redstone_mode.heat", "Heat level");
        add("message.nuclearcraft.redstone_mode.progress", "Fuel progress");
        add("message.nuclearcraft.redstone_mode.items", "Fuel amount");
        add("message.nuclearcraft.redstone_mode.switch", "Activation switch");
        add("message.nuclearcraft.redstone_mode.moderator", "Moderation control");
        add("gui.nuclearcraft.nuclear_blast", "Nuclear Blast");
        add("gui.nuclearcraft.nuclear_blast.chance", "Chance: %s");
        add("gui.nuclearcraft.fission_fuel", "Fission Reactor: Fuel");
        add("gui.nuclearcraft.fission_boiling", "Fission Reactor: Boiling");
        add("gui.nuclearcraft.fission_boiling.heat", "Heat: %s H");
        add("screen.nuclearcraft.boiling.capacity", "Capacity");
        add("screen.nuclearcraft.boiling.coolant", "Coolant");
        add("screen.nuclearcraft.boiling.hot_coolant", "Hot Coolant");
        add("screen.nuclearcraft.boiling.rate", "Boiling Rate");
        add("screen.nuclearcraft.cooling", "Cooling");

        add("message.nc.multitool.connected_to_tnt", "Linked to TNT at (%1$s, %2$s, %3$s)");
        add("message.nc.multitool.connected_to_bomb", "Linked to bomb at (%1$s, %2$s, %3$s)");
        add("message.nc.multitool.not_your_project", "Not your bomb — placement ownership required.");
        add("message.nc.multitool.armed_confirm", "Armed. Press again within 60 ticks to detonate.");
        add("message.nc.multitool.tnt_detonated", "Triggered TNT at (%1$s, %2$s, %3$s)");
        add("message.nc.multitool.bomb_detonated", "Triggered bomb at (%1$s, %2$s, %3$s)");
        add("tooltip.nc.multitool.desc", "Remote detonator: link a bomb or TNT, then use to fire");
        add("tooltip.nc.multitool.shift.desc", "Sneak to bypass block interactions while linking");
        add("tooltip.nc.multitool.connected_to_bomb", "Linked to bomb: (%1$s, %2$s, %3$s)");
        add("tooltip.nc.multitool.connected_to_tnt", "Linked to TNT: (%1$s, %2$s, %3$s)");
        add("commands.nuclearcraft.no_permission", "Missing required permission for this command.");
        add("commands.nuclearcraft.detonate.none", "No linked multitools to detonate.");
        add("commands.nuclearcraft.detonate.summary", "Detonated %1$s linked target(s).");
        add("screen.nuclearcraft.fission_reactor", "Fission Reactor");
        add("tooltip.nuclearcraft.wait", "Wait: %ss");
        add("tooltip.nuclearcraft.switch_to_boiling", "Switch to Boiling Mode");
        add("tooltip.nuclearcraft.switch_to_energy", "Switch to Energy Mode");
        add("tooltip.fluid.empty", "Empty");
        add("death.attack.acid", "%1$s dissolved in acid");
        add("tooltip.nuclearcraft.fuel.forge_energy", "Power: %s");
        add("tooltip.nuclearcraft.fuel.criticality", "Criticality: %s N/t");
        add("tooltip.nuclearcraft.fuel.heat", "Heat: %s H/t");
        add("tooltip.nuclearcraft.fuel.depletion", "Depletion: %s");
        add("tooltip.nuclearcraft.fuel.efficiency", "Efficiency: %s%%");
        add("tooltip.nuclearcraft.moderator","Fission Reactor moderator. Must be placed adjacent to a fuel cell. \n Each face adjacent to a fuel cell adds +%s%% efficiency and +%s%% heat gen.");

        add("itemGroup.nuclearcraft.fusion_reactor", "NuclearCraft: Fusion Reactor");
        add("screen.nuclearcraft.fusion_reactor", "Fusion Reactor");
        add("screen.nuclearcraft.fusion.charge", "Charge: %s%%");
        add("screen.nuclearcraft.fusion.plasma", "Plasma: %s MK");
        add("screen.nuclearcraft.fusion.heat", "Heat: %s H");
        add("screen.nuclearcraft.fusion.temperature", "Temperature: %s");
        add("screen.nuclearcraft.fusion.efficiency", "Efficiency: %s%%");
        add("screen.nuclearcraft.fusion.output", "Output: %s FE/t");
        add("screen.nuclearcraft.fusion.amplification", "Amplification: %s%%");
        add("screen.nuclearcraft.fusion.rf_amplifiers", "RF Amplifiers: %s%%");
        add("screen.nuclearcraft.fusion.rf_adjustment", "RF Adjustment: %s%%");
        add("screen.nuclearcraft.fusion.charging", "Charging: %s%%");
        add("message.nuclearcraft.redstone_mode.efficiency", "Efficiency level");
        add("message.nuclearcraft.redstone_mode.charge", "Charge level");
        add("message.nuclearcraft.redstone_mode.amplification", "RF amplification input");
        add("tooltip.nuclearcraft.fusion.amplification_down", "Decrease amplification");
        add("tooltip.nuclearcraft.fusion.amplification_up", "Increase amplification");
        add("tooltip.nuclearcraft.rf_amplifier.power", "Power: %s FE/t");
        add("tooltip.nuclearcraft.rf_amplifier.voltage", "Voltage: %s kV");
        add("tooltip.nuclearcraft.rf_amplifier.efficiency", "Efficiency: %s%%");
        add("tooltip.nuclearcraft.rf_amplifier.heat", "Heat: %s H/t");
        add("tooltip.nuclearcraft.rf_amplifier.max_temp", "Max Temp: %s kK");
        add("tooltip.nuclearcraft.rf_amplifier.not_found", "No RF Amplifiers");
        add("tooltip.nuclearcraft.electromagnet.magnetic_field", "Magnetic Field: %s T");
        add("tooltip.nuclearcraft.electromagnet.efficiency", "Efficiency: %s%%");
        add("tooltip.nuclearcraft.electromagnet.heat", "Heat: %s H/t");
        add("tooltip.nuclearcraft.electromagnet.max_temp", "Max Temp: %s kK");
        add("tooltip.nuclearcraft.electromagnet.power", "Power: %s FE/t");
        add("tooltip.nuclearcraft.electromagnet.not_found", "No Electromagnets");

        add("message.nuclearcraft.switch_side.mode", "Side mode: %s");
        add("tooltip.nuclearcraft.use_wrench", "Use a wrench to configure sides");
        add("tooltip.nuclearcraft.content_saved", "Contents are kept when broken");
        add("tooltip.nuclearcraft.magnet.on", "Auto-pickup: ON");
        add("tooltip.nuclearcraft.magnet.off", "Auto-pickup: OFF");
        add("tooltip.nuclearcraft.liquid_capacity", "Capacity: %s");
        add("tooltip.nuclearcraft.liquid_stored", "%s: %s / %s");
        add("tooltip.nuclearcraft.liquid_empty", "Empty (capacity %s)");
        add("tooltip.nuclearcraft.energy_capacity", "Capacity: %s");
        add("tooltip.nuclearcraft.energy_stored", "Energy: %s / %s");

        add("tooltip.nuclearcraft.hev.desc", "Grants extra protection and passive effects while charged");
        add("tooltip.nuclearcraft.hev.qe_charge", "QE Charge: %s / %s");

        add("tooltip.nc.qnp_mode", "Mode: %s");
        add("tooltip.nc.shift_rbm_to_change", "Sneak+Use to change");
        add("tooltip.mode.1", "One Block");
        add("tooltip.mode.3x3", "3x3");
        add("tooltip.mode.3x3x3", "3x3x3");
        add("tooltip.mode.5x5", "5x5");
        add("tooltip.mode.5x5x5", "5x5x5");
        add("tooltip.mode.7x7", "7x7");
        add("tooltip.mode.vein", "Vein");

        add("tooltip.nuclearcraft.energy_per_tick", "Energy per tick: %s");
        add("tooltip.nuclearcraft.speed_multiplier", "Speed Multiplier: %s");

        add("tooltip.nc.resonite_crystal.raw", "Unanalyzed");
        add("tooltip.nc.resonite_crystal.rarity", "Rarity: %s");
        add("tooltip.nc.resonite_crystal.effect", "Effect: %s");
        add("tooltip.nc.resonite_crystal.fe", "FE/t: %s");
        add("tooltip.nc.resonite_rarity.common", "Common");
        add("tooltip.nc.resonite_rarity.rare", "Rare");
        add("tooltip.nc.resonite_rarity.epic", "Epic");
        add("tooltip.nc.resonite_rarity.legendary", "Legendary");

        add("tooltip.nc.resonite_crystal.patron.noteclip", "Noteclip walked one of these past three checkpoints. The logbook never recorded him leaving.");
        add("tooltip.nc.resonite_crystal.patron.marcin212", "marcin212 swears a shard like this knit his ribs back after the medics had already filed the paperwork.");
        add("tooltip.nc.resonite_crystal.patron.personbelowrocks", "PersonBelowRocks pried a stuck blast door off its rails with one of these in his pocket. The door is still missing.");
        add("tooltip.nc.resonite_crystal.patron.tomdodd4598", "Dr. tomdodd4598 logged this specimen as 'remarkable' in the margins, right where the dosimeter trace flatlines.");
        add("tooltip.nc.resonite_crystal.patron.ethantabler", "ethantabler crossed an anomaly field on a hunch the size of this crystal. Walked out the far side whistling.");
        add("tooltip.nc.resonite_crystal.patron.endleon201", "endleon201 caught a round square in the vest, glanced at this, and marked it down as a good day.");
        add("tooltip.nc.resonite_crystal.patron.sancho_lucky", "sancho.lucky outran the morning blowout with this in hand. Barely. The Zone files 'barely' under 'survived'.");
        add("tooltip.nc.resonite_crystal.patron.cerusvi", "Cerusvi cleared a containment fence the manual flagged as uncrossable. The manual has since been revised.");
        add("tooltip.nc.resonite_crystal.patron.tocix9730", "tocix9730 filed a warranty claim from inside the fireball. Per company policy, it was approved.");

        add("entity.nuclearcraft.gravitational_anomaly", "Gravitational Anomaly");
        add("entity.nuclearcraft.electric_anomaly", "Electric Anomaly");
        add("entity.nuclearcraft.radioactive_anomaly", "Radioactive Anomaly");
        add("entity.nuclearcraft.burning_anomaly", "Burning Anomaly");
        add("entity.nuclearcraft.psycho_anomaly", "Psycho Anomaly");
        add("entity.nuclearcraft.teleporting_anomaly", "Teleporting Anomaly");
    }

    private void fuelInfo() {
        add("jei.category." + MODID + ".fuel_info", "Fuel Variants");
        add("jei.category." + MODID + ".isotope_info", "Isotope Forms");
        add("emi.category." + MODID + ".fuel_info", "Fuel Variants");
        add("emi.category." + MODID + ".isotope_info", "Isotope Forms");
        add("jei.nuclearcraft.fuel_info.title", "%s - Available Forms");
        add("jei.nuclearcraft.fuel_info.row", "FE/t %d | H/t %s | D %ds");
        add("jei.nuclearcraft.fuel_info.row_triso", "Criticality %d | H/t %s | D %ds");
        add("jei.nuclearcraft.isotope_info.title", "%s - Forms");
        add("fuel.variant.default", "Metal");
        add("fuel.variant.oxide", "Oxide");
        add("fuel.variant.nitride", "Nitride");
        add("fuel.variant.zirconium_alloy", "Zr");
        add("fuel.variant.triso", "TRISO");
    }
}
