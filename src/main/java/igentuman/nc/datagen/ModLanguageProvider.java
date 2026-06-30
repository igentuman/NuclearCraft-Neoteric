package igentuman.nc.datagen;

import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.FuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ToolSetEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static igentuman.nc.Main.MODID;
import static igentuman.nc.util.TextUtils.convertToName;

public class ModLanguageProvider  extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), MODID, locale);
    }

    @Override
    protected void addTranslations() {
        labels();
        heatSinks();
        for (String name : ModEntries.ENTRIES.keySet()) {
            if(ModEntries.get(name).hasBlock()) {
                add(ModEntries.get(name).block().get(), convertToName(name));
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
        for (FuelEntry fuel : ModEntries.FUELS.values()) {
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

    private void heatSinks() {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            add(entry.block().get(), convertToName(entry.name + "_heat_sink"));
        }
        add("tooltip.nuclearcraft.heat_sink.heat", "Cooling: %s H/t");
        add("tooltip.nuclearcraft.heat_sink.active", "Needs coolant fluid supply into reactor to work.");
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

    private void labels() {
        add("itemGroup.nuclearcraft.fission_reactor", "NuclearCraft: Fission Reactor");
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
        add("screen.nuclearcraft.fission.fe_per_tick", "Output");
        add("screen.nuclearcraft.fission.steam_per_tick", "Steam");
        add("screen.nuclearcraft.fission.mode.energy", "Energy");
        add("screen.nuclearcraft.fission.mode.steam", "Steam");
        add("message.nuclearcraft.redstone_mode", "Redstone mode: %s");
        add("message.nuclearcraft.redstone_mode.none", "Off");
        add("message.nuclearcraft.redstone_mode.energy", "Energy level");
        add("message.nuclearcraft.redstone_mode.heat", "Heat level");
        add("message.nuclearcraft.redstone_mode.progress", "Fuel progress");
        add("message.nuclearcraft.redstone_mode.items", "Fuel amount");
        add("message.nuclearcraft.redstone_mode.switch", "Activation switch");
        add("message.nuclearcraft.redstone_mode.moderator", "Moderation control");
        add("gui.nuclearcraft.fission_fuel", "Fission Reactor: Fuel");
        add("gui.nuclearcraft.fission_boiling", "Fission Reactor: Boiling");
        add("gui.nuclearcraft.fission_boiling.heat", "Heat: %s H");
        add("screen.nuclearcraft.boiling.capacity", "Capacity");
        add("screen.nuclearcraft.boiling.coolant", "Coolant");
        add("screen.nuclearcraft.boiling.hot_coolant", "Hot Coolant");
        add("screen.nuclearcraft.boiling.rate", "Boiling Rate");
        add("screen.nuclearcraft.cooling", "Cooling");
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

    }
}
