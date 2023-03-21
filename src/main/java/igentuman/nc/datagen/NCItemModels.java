package igentuman.nc.datagen;

import igentuman.nc.NuclearCraft;
import igentuman.nc.setup.NCArmor;
import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.NCItems;
import igentuman.nc.setup.NCTools;
import igentuman.nc.setup.materials.Nuggets;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

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
        registerGems();
        registerParts();
        registerRecords();
        registerFood();
        registerArmor();
        registerItems();
        registerFuel();
        withExistingParent(NCBlocks.PORTAL_ITEM.getId().getPath(), modLoc("block/portal"));

        singleTexture(NCTools.MULTITOOL.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/tool/"+NCTools.MULTITOOL.getId().getPath()));

        singleTexture(NCTools.SPAXELHOE_TOUGH.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/tool/"+NCTools.SPAXELHOE_TOUGH.getId().getPath()));
    }

    private void registerFuel() {
        for(List<String> name: NCItems.NC_FUEL.keySet()) {
            String depleted = "/";
            if(name.get(0).equals("depleted")) {
                depleted = "/depleted/";
            }

            String subPath = name.get(1)+depleted+name.get(2).replace("-","_");
            if(!name.get(3).isEmpty()) {
                subPath+="_"+name.get(3);
            }
            singleTexture(NCItems.NC_FUEL.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/fuel/"+subPath));
        }
    }

    private void registerItems() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            singleTexture(NCItems.NC_ITEMS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/"+name));
        }
    }

    private void registerRecords() {
        for(String name: NCItems.NC_RECORDS.keySet()) {
            singleTexture(NCItems.NC_RECORDS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/record/"+name));
        }
    }

    private void registerArmor() {
        singleTexture(NCArmor.HEV_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HEV_BOOTS.getId().getPath()));
        singleTexture(NCArmor.HEV_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HEV_CHEST.getId().getPath()));
        singleTexture(NCArmor.HEV_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HEV_PANTS.getId().getPath()));
        singleTexture(NCArmor.HEV_HELMET.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HEV_HELMET.getId().getPath()));

        singleTexture(NCArmor.HAZMAT_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HAZMAT_BOOTS.getId().getPath()));
        singleTexture(NCArmor.HAZMAT_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HAZMAT_CHEST.getId().getPath()));
        singleTexture(NCArmor.HAZMAT_MASK.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HAZMAT_MASK.getId().getPath()));
        singleTexture(NCArmor.HAZMAT_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.HAZMAT_PANTS.getId().getPath()));

        singleTexture(NCArmor.TOUGH_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.TOUGH_BOOTS.getId().getPath()));
        singleTexture(NCArmor.TOUGH_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.TOUGH_CHEST.getId().getPath()));
        singleTexture(NCArmor.TOUGH_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.TOUGH_PANTS.getId().getPath()));
        singleTexture(NCArmor.TOUGH_HELMET.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+NCArmor.TOUGH_HELMET.getId().getPath()));

    }
    private void registerFood() {
        for(String name: NCItems.NC_FOOD.keySet()) {
            singleTexture(NCItems.NC_FOOD.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/food/"+name));
        }
    }

    private void registerParts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            singleTexture(NCItems.NC_PARTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/part/"+name));
        }
    }

    private void registerGems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            singleTexture(NCItems.NC_GEMS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/gem/"+name));
        }
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