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
        if (name.startsWith("msr_")) {
            return "MSR " + convertToName(name.substring("msr_".length()));
        }
        if (name.endsWith("_rtg")) {
            return convertToName(name.substring(0, name.length() - "_rtg".length())) + " RTG";
        }
        return convertToName(name);
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

    private void labels() {
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
    }
}
