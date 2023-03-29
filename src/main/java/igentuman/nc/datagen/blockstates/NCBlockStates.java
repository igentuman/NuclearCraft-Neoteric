package igentuman.nc.datagen.blockstates;

import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;

public class NCBlockStates extends BlockStateProvider {

    public NCBlockStates(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        portal();
        ores();
        blocks();
        processors();
        solarPanels();
        materialFluidBlocks();
        heatSinks();
        fissionReactor();
    }

    private void heatSinks() {
        for (String name: FissionBlocks.heatsinks.keySet()) {
            simpleBlock(NCBlocks.MULTI_BLOCKS.get(name+"_heat_sink").get(), multiBlockModel(NCBlocks.MULTI_BLOCKS.get(name+"_heat_sink").get(), "heat_sink/"+name));
        }
    }

    private void fissionReactor() {
        for (String name: FissionBlocks.reactor) {
            simpleBlock(NCBlocks.MULTI_BLOCKS.get("fission_reactor_"+name).get(), multiBlockModel(NCBlocks.MULTI_BLOCKS.get("fission_reactor_"+name).get(), "fission/"+name));
        }
    }

    private void solarPanels() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            if(!name.contains("solar_panel")) continue;
            simpleBlock(
                    NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                    energyModel(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                            name));
        }
    }

    private void processors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            horizontalBlock(
                    NCProcessors.PROCESSORS.get(name).get(),
                    sidedModel(NCProcessors.PROCESSORS.get(name).get(),
                            "processor"));
        }
    }


    private void materialFluidBlocks() {
        for(String name: NCBlocks.NC_MATERIAL_BLOCKS.keySet()) {
            simpleBlock(NCBlocks.NC_MATERIAL_BLOCKS.get(name).get(), model(NCBlocks.NC_MATERIAL_BLOCKS.get(name).get(), "material/fluid"));
        }
    }
    private void blocks() {
        for(String name: NCBlocks.NC_BLOCKS.keySet()) {
            simpleBlock(NCBlocks.NC_BLOCKS.get(name).get(), model(NCBlocks.NC_BLOCKS.get(name).get(), "material/block"));
        }
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            simpleBlock(NCBlocks.ORE_BLOCKS.get(ore).get(), model(NCBlocks.ORE_BLOCKS.get(ore).get(), "ore"));
        }
    }

    private void portal() {
        Block block = NCBlocks.PORTAL_BLOCK.get();
        ResourceLocation side = modLoc("block/portal");
        ResourceLocation top = modLoc("block/portal");
        simpleBlock(block, models().cube(NCBlocks.PORTAL_BLOCK.getId().getPath(), side, top, side, side, side, side));
    }

    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public ModelFile model(Block block, String subPath) {
        ResourceLocation name = key(block);
        return models().cubeAll(
                        key(block).getPath(),
                        new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/" + name.getPath()));
    }

    public ModelFile multiBlockModel(Block block, String subPath) {
        ResourceLocation name = key(block);
        if(subPath.matches(".*controller|.*port.*")) {
            return sidedModel(block, subPath);
        }
        BlockModelBuilder m = models().cubeAll(
                key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath));
        if(subPath.matches(".*glass|.*cell.*")) {
            m.renderType(new ResourceLocation("translucent"));
        }
        return m;
    }


    public ModelFile sidedModel(Block block, String subPath) {
        ResourceLocation name = key(block);

        return models().cube(
                key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/top"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/bottom"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/" + name.getPath()),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/back"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/side")
        );
    }

    public ModelFile energyModel(Block block, String subPath) {
        ResourceLocation name = key(block);

        return models().cube(
                key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_top"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"_side")
        );
    }

}