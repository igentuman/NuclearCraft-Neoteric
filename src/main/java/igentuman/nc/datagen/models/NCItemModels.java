package igentuman.nc.datagen.models;

import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.setup.registration.*;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.*;

public class NCItemModels extends ItemModelProvider {

    public NCItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        ores();
        blocks();
        multiblocks();
        processors();
        energyBlocks();
        chunks();
        ingots();
        nuggets();
        plates();
        dusts();
        gems();
        parts();
        records();
        food();
        armor();
        items();
        shielding();
        fuel();
        isotopes();
        storageBlocks();
        
        withExistingParent(NCBlocks.PORTAL_ITEM.getId().getPath(), modLoc("block/portal"));

        singleTexture(MULTITOOL.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/tool/"+MULTITOOL.getId().getPath()));

        singleTexture(SPAXELHOE_TOUGH.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/tool/"+SPAXELHOE_TOUGH.getId().getPath()));
        NCFluids.ALL_FLUID_ENTRIES.values().forEach(this::createBucket);
    }

    private void multiblocks() {
        for(String name: NCBlocks.MULTI_BLOCKS.keySet()) {
            withExistingParent(NCBlocks.MULTIBLOCK_ITEMS.get(name).getId().getPath(), modLoc("block/multiblock/"+name));
        }
        for(String name: FissionReactor.MULTI_BLOCKS.keySet()) {
            withExistingParent(FissionReactor.MULTIBLOCK_ITEMS.get(name).getId().getPath(), modLoc("block/multiblock/"+name));
        }
    }

    private void processors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            withExistingParent(NCProcessors.PROCESSOR_BLOCKS_ITEMS.get(name).getId().getPath(), modLoc("block/processor/"+name));
        }
    }

    private void energyBlocks() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            withExistingParent(NCEnergyBlocks.ENERGY_BLOCKS.get(name).getId().getPath(), modLoc("block/"+name.replace("/","_")));
        }
    }

    private void storageBlocks() {
        for(String name: BarrelBlocks.all().keySet()) {
            withExistingParent(name, modLoc("block/barrel/"+name));
        }
        for(String name: ContainerBlocks.all().keySet()) {
            withExistingParent(name, modLoc("block/container/"+name));
        }
    }

    private String name(ItemLike item)
    {
        return Registry.ITEM.getKey(item.asItem()).getPath();
    }

    private ResourceLocation forgeLoc(String s)
    {
        return new ResourceLocation("forge", s);
    }

    private void createBucket(NCFluids.FluidEntry entry)
    {
        withExistingParent(name(entry.getBucket()), forgeLoc("item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(entry.getStill())
                .flipGas(entry.flowing().get().getFluidType().getDensity() < 0)
                .applyTint(true);
    }


    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
            singleTexture(NCItems.NC_SHIELDING.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/rad_shielding/"+name));
        }
    }

    private void fuel() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            String depleted = "/";
            if(name.get(0).equals("depleted")) {
                depleted = "/depleted/";
            }

            String subPath = name.get(1)+depleted+name.get(2).replace("-","_");
            if(!name.get(3).isEmpty()) {
                subPath+="_"+name.get(3);
            }
            singleTexture(Fuel.NC_FUEL.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/fuel/"+subPath));
        }

        for(List<String> name: Fuel.NC_DEPLETED_FUEL.keySet()) {
            String depleted = "/";
            if(name.get(0).equals("depleted")) {
                depleted = "/depleted/";
            }

            String subPath = name.get(1)+depleted+name.get(2).replace("-","_");
            if(!name.get(3).isEmpty()) {
                subPath+="_"+name.get(3);
            }
            singleTexture(Fuel.NC_DEPLETED_FUEL.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/fuel/"+subPath));
        }
    }

    private void isotopes() {
        for(String name: Fuel.NC_ISOTOPES.keySet()) {
            singleTexture(Fuel.NC_ISOTOPES.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/isotope/"+name));
        }
    }

    private void items() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            if(name.contains("collector")) continue;
            singleTexture(NCItems.NC_ITEMS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/"+name));
        }
    }

    private void records() {
        for(String name: NCItems.NC_RECORDS.keySet()) {
            singleTexture(NCItems.NC_RECORDS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/record/"+name));
        }
    }

    private void armor() {
        singleTexture(HEV_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HEV_BOOTS.getId().getPath()));
        singleTexture(HEV_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HEV_CHEST.getId().getPath()));
        singleTexture(HEV_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HEV_PANTS.getId().getPath()));
        singleTexture(HEV_HELMET.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HEV_HELMET.getId().getPath()));

        singleTexture(HAZMAT_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_BOOTS.getId().getPath()));
        singleTexture(HAZMAT_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_CHEST.getId().getPath()));
        singleTexture(HAZMAT_MASK.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_MASK.getId().getPath()));
        singleTexture(HAZMAT_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_PANTS.getId().getPath()));

        singleTexture(TOUGH_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+TOUGH_BOOTS.getId().getPath()));
        singleTexture(TOUGH_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+TOUGH_CHEST.getId().getPath()));
        singleTexture(TOUGH_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+TOUGH_PANTS.getId().getPath()));
        singleTexture(TOUGH_HELMET.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+TOUGH_HELMET.getId().getPath()));

    }
    private void food() {
        for(String name: NCItems.NC_FOOD.keySet()) {
            if(name.contains("smore")) continue;
            singleTexture(NCItems.NC_FOOD.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/food/"+name));
        }
    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            if(name.matches("chassis|empty_frame|steel_frame")) continue;
            singleTexture(NCItems.NC_PARTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/part/"+name));
        }
    }

    private void gems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            singleTexture(NCItems.NC_GEMS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/gem/"+name));
        }
    }

    private void chunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            singleTexture(NCItems.NC_CHUNKS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/chunk/"+name));
        }
    }

    private void ingots() {
        for(String name: NCItems.NC_INGOTS.keySet()) {
            singleTexture(NCItems.NC_INGOTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/ingot/"+name));
        }
    }

    private void nuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            singleTexture(NCItems.NC_NUGGETS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/nugget/"+name));
        }
    }

    private void plates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            singleTexture(NCItems.NC_PLATES.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/plate/"+name));
        }
    }

    private void dusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            singleTexture(NCItems.NC_DUSTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/material/dust/"+name));
        }
    }
    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCK_ITEMS.keySet()) {
            withExistingParent(NCBlocks.ORE_BLOCK_ITEMS.get(ore).getId().getPath(), modLoc("block/ore/"+ore+"_ore"));
        }
    }

    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS_ITEMS.keySet()) {
            withExistingParent(NCBlocks.NC_BLOCKS_ITEMS.get(name).getId().getPath(), modLoc("block/material/"+name+"_block"));
        }
    }
}