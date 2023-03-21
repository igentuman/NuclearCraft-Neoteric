package igentuman.nc.datagen;

import igentuman.nc.setup.NCArmor;
import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.NCItems;
import igentuman.nc.setup.NCTools;
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
        addOres();
        addIngots();
        addPlates();
        addDusts();
        addNuggets();
        addGems();
        addParts();
        addChunks();
        addBlocks();
        addFood();
        addArmor();
        addRecords();
        addTools();
        addItems();
        addFuel();
        addTooltips();
    }

    private void addTooltips() {
        add("fuel.heat.descr","Base Heat Gen: %s H/t");
        add("fuel.depletion.descr","Base Depletion Time: %s sec");
        add("fuel.criticality.descr","Criticality Factor: %s N/t");
        add("fuel.efficiency.descr","Base Efficiency: %s%%");

    }

    private void addFuel() {
        for(List<String> name: NCItems.NC_FUEL.keySet()) {
            add(NCItems.NC_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
    }

    private String convertToName(String key)
    {
        String result = "";
        String[] parts = key.split("_");
        for(String l: parts) {
            if(result.isEmpty()) {
                result = l.substring(0, 1).toUpperCase() + l.substring(1);
            } else {
                result += " " + l.substring(0, 1).toUpperCase() + l.substring(1);
            }
        }
        return result;
    }

    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            add(NCBlocks.ORE_BLOCKS.get(ore).get().getName().getString(), convertToName(ore)+" Ore");
        }
    }

    private void addItems() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            add(NCItems.NC_ITEMS.get(name).get().getDescription().getString(), convertToName(name));
        }
    }

    private void addRecords() {
        for(String name: NCItems.NC_RECORDS.keySet()) {
            add(NCItems.NC_RECORDS.get(name).get().getDescription().getString(), convertToName(name));
        }
    }

    private void addTools()
    {
        add(NCTools.QNP.get(), "QNP");
        add(NCTools.MULTITOOL.get(), "Multitool");
        add(NCTools.GEIGER_COUNTER.get(), "Geiger Counter");
        add(NCTools.SPAXELHOE_TOUGH.get(), "Tough Spaxel");
    }

    private void addArmor() {
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
    
    private void addFood() {
        for(String name: NCItems.NC_FOOD.keySet()) {
            add(NCItems.NC_FOOD.get(name).get().getDescription().getString(), convertToName(name));
        }
    }

    private void addParts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            add(NCItems.NC_PARTS.get(name).get().getDescription().getString(), convertToName(name));
        }
    }

    private void addGems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            add(NCItems.NC_GEMS.get(name).get().getDescription().getString(), convertToName(name)+" Gem");
        }
    }

    private void addIngots() {
        for(String ingot: NCItems.NC_INGOTS.keySet()) {
            add(NCItems.NC_INGOTS.get(ingot).get().getDescription().getString(), convertToName(ingot)+" Ingot");
        }
    }

    private void addPlates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            add(NCItems.NC_PLATES.get(name).get().getDescription().getString(), convertToName(name)+" Plate");
        }
    }

    private void addDusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            add(NCItems.NC_DUSTS.get(name).get().getDescription().getString(), convertToName(name)+" Dust");
        }
    }

    private void addNuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            add(NCItems.NC_NUGGETS.get(name).get().getDescription().getString(), convertToName(name)+" Nugget");
        }
    }

    private void addChunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            add(NCItems.NC_CHUNKS.get(name).get().getDescription().getString(), convertToName(name)+" Chunk");
        }
    }

    private void addBlocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            add(NCBlocks.NC_BLOCKS.get(name).get().getName().getString(), convertToName(name)+" Block");
        }
    }
}