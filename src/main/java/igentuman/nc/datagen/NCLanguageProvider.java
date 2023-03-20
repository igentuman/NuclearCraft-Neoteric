package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.NCItems;
import igentuman.nc.setup.materials.Ores;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

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
        addChunks();
        addBlocks();
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