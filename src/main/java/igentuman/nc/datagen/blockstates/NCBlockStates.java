package igentuman.nc.datagen.blockstates;

import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.block.BatteryBlockLoader.BATTERY_LOADER;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCK;

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
        energyBlocks();
        rtgs();
        materialFluidBlocks();
        heatSinks();
        fissionReactor();
        storageBlocks();
        fusionReactor();
    }

    private void storageBlocks() {
        for(String name: BarrelBlocks.all().keySet()) {
            simpleBlock(
                    STORAGE_BLOCK.get(name).get(),
                    models().getExistingFile(modLoc("block/barrel/"+name)));
        }
        for(String name: ContainerBlocks.all().keySet()) {
            simpleBlock(
                    STORAGE_BLOCK.get(name).get(),
                    models().getExistingFile(modLoc("block/container/"+name)));
        }
    }

    private void heatSinks() {
        for (String name: FissionBlocks.heatsinks.keySet()) {
            simpleBlock(FissionReactor.MULTI_BLOCKS.get(name+"_heat_sink").get(), multiBlockModel(FissionReactor.MULTI_BLOCKS.get(name+"_heat_sink").get(), "heat_sink/"+name));
        }
    }

    private void fissionReactor() {
        for (String name: FissionBlocks.reactor) {
            if(name.matches(".*port.*")) {
                horizontalBlock(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(), multiBlockModel(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(), "fission/" + name));
            } else if(name.matches(".*controller.*")) {
                horizontalBlock(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(),
                        st -> controllerModel(st, sidedModel(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(), "fission/controller"))
                );
            } else {
                if(name.contains("slope")) {
                    orientationalBlock(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" +name).get(), $ -> models().getExistingFile(rl("block/multiblock/fission_reactor_"+name)));
                } else {
                    simpleBlock(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(), multiBlockModel(FissionReactor.MULTI_BLOCKS.get("fission_reactor_" + name).get(), "fission/" + name));
                }
            }
        }
    }


    private void fusionReactor() {
        //simpleBlock(FUSION_BLOCKS.get("fusion_core").get(), models().getExistingFile(rl("block/dummy")));
        getVariantBuilder(FUSION_BLOCKS.get("fusion_core").get())
                .partialState()
                .with(BlockStateProperties.POWERED, true)
                .modelForState()
                .modelFile(models().getExistingFile(rl("block/fusion/core_center")))
                .addModel()
                .partialState()
                .with(BlockStateProperties.POWERED, false)
                .modelForState()
                .modelFile(models().getExistingFile(rl("block/dummy")))
                .addModel();



        simpleBlock(FUSION_CORE_PROXY.get(), models().getExistingFile(rl("block/fusion/core_proxy")));
        simpleBlock(FUSION_BLOCKS.get("fusion_reactor_casing").get(), model(FUSION_BLOCKS.get("fusion_reactor_casing").get(),"fusion"));
        simpleBlock(FUSION_BLOCKS.get("fusion_reactor_casing_glass").get(), model(FUSION_BLOCKS.get("fusion_reactor_casing_glass").get(),"fusion"));
        simpleBlock(FUSION_BLOCKS.get("fusion_reactor_connector").get(), model(FUSION_BLOCKS.get("fusion_reactor_connector").get(),"fusion"));
    }


    private void rtgs() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            if(name.contains("rtg")) {
                String type = name.replace("_rtg", "");
                simpleBlock(
                        NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                        energyModel(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                                "rtg/"+type+"/"));
            }
        }
    }

    private void solarPanels() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            if(name.contains("solar_panel")) {
                simpleBlock(
                        NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                        energyModel(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                                name + "_"));
            }
        }
    }

    private void energyBlocks() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            if (name.matches(".*voltaic_pile|.*lithium_ion_battery")) {
                String tier = name.replaceAll("voltaic_pile|lithium_ion_battery", "");
                String category = name.replace(tier, "");
                simpleBlock(
                        NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                        energyModel(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(),
                                category + "/" + tier.replace("_", "") + "/"));
            }
        }
    }

    private void processors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            horizontalBlock(
                    NCProcessors.PROCESSORS.get(name).get(),
                    st -> processorModel(st, sidedModel(NCProcessors.PROCESSORS.get(name).get(),
                            "processor"))
                    );
        }
    }

    public BlockModelBuilder processorModel(BlockState st, ModelFile model) {
        String powered = st.getValue(BlockStateProperties.POWERED) ? "_powered" : "";
        BlockModelBuilder result = models()
                .getBuilder("block/processor/"+key(st.getBlock()).getPath()+powered)
                .texture("north", "block/processor/"+key(st.getBlock()).getPath()+powered)
                ;
        if(st.getValue(BlockStateProperties.POWERED)) {
            result.parent(model);
        }
        return result;
    }

    public BlockModelBuilder controllerModel(BlockState st, ModelFile model) {
        String powered = st.getValue(BlockStateProperties.POWERED) ? "_powered" : "";
        BlockModelBuilder result = models()
                .getBuilder("block/multiblock/"+key(st.getBlock()).getPath()+powered)
                .texture("north", "block/fission/controller/"+key(st.getBlock()).getPath()+powered)
                ;
        if(st.getValue(BlockStateProperties.POWERED)) {
            result.parent(model);
        }
        return result;
    }

    public void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block)
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(modelFunc.apply(state))
                        .rotationX(((int) state.getValue(BlockStateProperties.VERTICAL_DIRECTION).toYRot() + 180) % 360)
                        .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                        .build()
                );
    }

    public void horizontalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block)
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(modelFunc.apply(state))
                        .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                        .build()
                );
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
        for(String name: NCBlocks.NC_ELECTROMAGNETS.keySet()) {
            if(name.contains("slope")) {
                orientationalBlock(NCBlocks.NC_ELECTROMAGNETS.get(name).get(), $ -> models().getExistingFile(rl("block/electromagnet/"+name)));
            } else {
                simpleBlock(NCBlocks.NC_ELECTROMAGNETS.get(name).get(), model(NCBlocks.NC_ELECTROMAGNETS.get(name).get(), "electromagnet"));
            }
        }

        for(String name: NCBlocks.NC_RF_AMPLIFIERS.keySet()) {
            simpleBlock(NCBlocks.NC_RF_AMPLIFIERS.get(name).get(), model(NCBlocks.NC_RF_AMPLIFIERS.get(name).get(), "rf_amplifier"));
        }
    }

    public void orientationalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block)
                .forAllStates(state -> {
                    FrontAndTop dir = state.getValue(BlockStateProperties.ORIENTATION);
                    return ConfiguredModel.builder()
                            .modelFile(modelFunc.apply(state))
                            .rotationX(dir.front() == Direction.DOWN ? 180 : 0)
                            .rotationY((((int) dir.top().toYRot()) + 180) % 360)
                            .build();
                });
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
        String blockPath = "";

        switch (subPath) {
            case "ore":
                blockPath = "block/ore/";
                break;
            case "material/block":
                blockPath = "block/material/";
                break;
            case "processor":
                blockPath = "block/processor/";
                break;
            case "fusion":
                blockPath = "block/fusion/";
                break;
            case "electromagnet":
                blockPath = "block/electromagnet/";
                break;
            case "rf_amplifier":
                blockPath = "block/rf_amplifier/";
                break;
        }
        BlockModelBuilder model = models().cubeAll(
                blockPath+key(block).getPath(),
                        new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/" + name.getPath()));
        if(name.getPath().matches(".*glass|.*cell.*")) {
            model.renderType(new ResourceLocation("cutout"));
        }
        return model;
    }

    public ModelFile multiBlockModel(Block block, String subPath) {
        ResourceLocation name = key(block);
        if(subPath.matches(".*controller|.*port.*")) {
            return sidedModel(block, subPath);
        }
        BlockModelBuilder m = models().cubeAll(
                "block/multiblock/"+key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath));
        if(subPath.matches(".*glass|.*cell.*")) {
            m.renderType(new ResourceLocation("translucent"));
        }
        return m;
    }


    public ModelFile sidedModel(Block block, String subPath) {
        ResourceLocation name = key(block);
        String blockPath = "";
        switch (subPath) {
            case "ore":
                blockPath = "block/ore/";
                break;
            case "material/block":
                blockPath = "block/material/";
                break;
            case "processor":
                blockPath = "block/processor/";
                break;
        }
        if (subPath.matches(".*fission.*|.*fusion.*")) {
            blockPath = "block/multiblock/";
        }
        return models().cube(
                blockPath+key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/top"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/bottom"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/" + name.getPath()),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/back"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/"+subPath+"/side")
        ).texture("particle", ModelProvider.BLOCK_FOLDER + "/"+subPath+"/side");
    }

    public ModelFile energyModel(Block block, String subPath) {
        ResourceLocation name = key(block);

        BlockModelBuilder model =  models().cube(
                key(block).getPath(),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"top"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"side"),
                new ResourceLocation(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"side")
        );

        model.texture("particle", ModelProvider.BLOCK_FOLDER + "/energy/"+subPath+"top");
        if(subPath.matches(".*voltaic_pile.*|.*lithium_ion_battery.*")) {
            model.customLoader((blockModelBuilder, helper) -> new CustomLoaderBuilder<BlockModelBuilder>(BATTERY_LOADER, blockModelBuilder, helper) { });
        }
        return model;
    }

}