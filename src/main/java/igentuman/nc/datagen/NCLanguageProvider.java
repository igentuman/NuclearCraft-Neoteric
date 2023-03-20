package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
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
    }


    private void addOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            add(NCBlocks.ORE_BLOCKS.get(ore).get().getName().getString(), ore.substring(0, 1).toUpperCase() + ore.substring(1).replace("_"," ")+" ore");
        }
    }
}