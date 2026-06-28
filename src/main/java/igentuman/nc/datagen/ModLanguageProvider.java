package igentuman.nc.datagen;

import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.FuelEntry;
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

    private void labels() {
        add("screen.nuclearcraft.side_config", "Side Configuration");
        add("screen.nuclearcraft.slot_selection", "Select Slot");
        add("screen.nuclearcraft.multiblock.assembled", "Assembled");
        add("screen.nuclearcraft.multiblock.not_assembled", "Not Assembled");
        add("tooltip.fluid.empty", "Empty");
        add("death.attack.acid", "%1$s dissolved in acid");
        add("tooltip.nuclearcraft.fuel.forge_energy", "Power: %s");
        add("tooltip.nuclearcraft.fuel.criticality", "Criticality: %s N/t");
        add("tooltip.nuclearcraft.fuel.heat", "Heat: %s H/t");
        add("tooltip.nuclearcraft.fuel.depletion", "Depletion: %s");
        add("tooltip.nuclearcraft.fuel.efficiency", "Efficiency: %s%%");
    }
}
