package igentuman.nc.datagen;

import igentuman.nc.setup.*;
import igentuman.nc.setup.fuel.NCFuel;
import igentuman.nc.setup.materials.Ores;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.ModSetup.TAB_NAME;

public class NCLanguageProvider extends LanguageProvider {

    public NCLanguageProvider(DataGenerator gen, String locale) {
        super(gen, MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TAB_NAME, "NuclearCraft");
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
    }

    private void buckets() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            add(NCFluids.NC_MATERIALS.get(name).getBucket(), "Bucket of Melted " + convertToName(name));
        }
    }

    private void fluids() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            add("fluid_type."+NCFluids.NC_MATERIALS.get(name).type().getId().toLanguageKey(), "Melted " + convertToName(name));
        }
    }

    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
            add(NCItems.NC_SHIELDING.get(name).get(), convertToName(name));
        }
    }

    private void tooltips() {
        add("tooltip.press_shift_for_description","Press SHIFT for description");
        add("fuel.heat.descr","Base Heat Gen: %s H/t");
        add("fuel.forge_energy.descr","Forge Energy: %s FE/t");
        add("fuel.heat_boiling.descr","Boiling Reactor Heat: %s H/t");
        add("fuel.depletion.descr","Base Depletion Time: %s sec");
        add("fuel.criticality.descr","Criticality Factor: %s N/t");
        add("fuel.efficiency.descr","Base Efficiency: %s%%");
        add("fuel.description","Used in Fission Reactors. Use Fuel Ports to Load/Unload. \nActual FE generation depends on Reactor Efficiency.");
    }

    private void fuel() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            add(Fuel.NC_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
        for(List<String> name: Fuel.NC_DEPLETED_FUEL.keySet()) {
            add(Fuel.NC_DEPLETED_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
    }

    private String convertToName(String key)
    {
        StringBuilder result = new StringBuilder();
        String[] parts = key.split("_|/");
        for(String l: parts) {
            if(result.length() == 0) {
                result = new StringBuilder(l.substring(0, 1).toUpperCase() + l.substring(1));
            } else {
                result.append(" ").append(l.substring(0, 1).toUpperCase()).append(l.substring(1));
            }
        }
        return result.toString();
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
        add(NCTools.QNP.get(), "QNP");
        add(NCTools.MULTITOOL.get(), "Multitool");
        add(NCTools.GEIGER_COUNTER.get(), "Geiger Counter");
        add(NCTools.SPAXELHOE_TOUGH.get(), "Tough Spaxel");
    }

    private void armor() {
        add(NCArmor.TOUGH_HELMET.get(), "Tough Helmet");
        add(NCArmor.TOUGH_PANTS.get(), "Tough Pants");
        add(NCArmor.TOUGH_BOOTS.get(), "Tough Boots");
        add(NCArmor.TOUGH_CHEST.get(), "Tough Chest");
        
        add(NCArmor.HEV_HELMET.get(), "HEV Helmet");
        add(NCArmor.HEV_PANTS.get(), "HEV Pants");
        add(NCArmor.HEV_BOOTS.get(), "HEV Boots");
        add(NCArmor.HEV_CHEST.get(), "HEV Chest");

        add(NCArmor.HAZMAT_MASK.get(), "Hazmat Mask");
        add(NCArmor.HAZMAT_PANTS.get(), "Hazmat Pants");
        add(NCArmor.HAZMAT_BOOTS.get(), "Hazmat Boots");
        add(NCArmor.HAZMAT_CHEST.get(), "Hazmat Chest");
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
        for(String ingot: NCItems.NC_INGOTS.keySet()) {
            add(NCItems.NC_INGOTS.get(ingot).get(), convertToName(ingot)+" Ingot");
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
    }
}