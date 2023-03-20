package igentuman.nc.datagen;

import igentuman.nc.NuclearCraft;
import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.NCItems;
import igentuman.nc.setup.materials.Nuggets;
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
        registerBlocks();
        registerChunks();
        registerIngots();
        registerNuggets();
        registerPlates();
        registerDusts();
        withExistingParent(NCBlocks.PORTAL_ITEM.getId().getPath(), modLoc("block/portal"));
    }

    private void registerChunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            singleTexture(NCItems.NC_CHUNKS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/chunk/"+name));
        }
    }

    private void registerIngots() {
        for(String name: NCItems.NC_INGOTS.keySet()) {
            singleTexture(NCItems.NC_INGOTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/ingot/"+name));
        }
    }

    private void registerNuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            singleTexture(NCItems.NC_NUGGETS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/nugget/"+name));
        }
    }

    private void registerPlates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            singleTexture(NCItems.NC_PLATES.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/plate/"+name));
        }
    }

    private void registerDusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            singleTexture(NCItems.NC_DUSTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/dust/"+name));
        }
    }
    private void registerOres() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            withExistingParent(NCBlocks.ORE_BLOCK_ITEMS.get(ore).getId().getPath(), modLoc("block/"+ore+"_ore"));
        }
    }

    private void registerBlocks() {
        for(String name: NCBlocks.NC_BLOCKS_ITEMS.keySet()) {
            withExistingParent(NCBlocks.NC_BLOCKS_ITEMS.get(name).getId().getPath(), modLoc("block/"+name+"_block"));
        }
    }
}