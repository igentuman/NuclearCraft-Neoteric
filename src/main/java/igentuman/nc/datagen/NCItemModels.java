package igentuman.nc.datagen;

import igentuman.nc.setup.NCBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static igentuman.nc.NuclearCraft.MODID;

public class NCItemModels extends ItemModelProvider {

    public NCItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerOres();

        withExistingParent(NCBlocks.PORTAL_ITEM.getId().getPath(), modLoc("block/portal"));

    }

    private void registerOres() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            withExistingParent(NCBlocks.ORE_BLOCK_ITEMS.get(ore).getId().getPath(), modLoc("block/"+ore+"_ore"));
        }
    }
}