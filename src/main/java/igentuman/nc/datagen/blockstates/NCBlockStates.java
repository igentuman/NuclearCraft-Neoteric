package igentuman.nc.datagen.blockstates;

import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.block.BatteryBlockLoader.BATTERY_LOADER;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
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
        turbine();
        storageBlocks();
        fusionReactor();
    }

    private void turbine() {
        for (String name: TurbineRegistration.turbine) {
            if(name.matches(".*port.*")) {
                horizontalBlock(TURBINE_BLOCKS.get("turbine_" + name).get(), multiBlockModel(TURBINE_BLOCKS.get("turbine_" + name).get(), "turbine/" + name));
            } else if(name.matches(".*controller.*")) {
                horizontalBlock(TURBINE_BLOCKS.get("turbine_" + name).get(),
                        st -> controllerModel(st, sidedModel(TURBINE_BLOCKS.get("turbine_" + name).get(), "turbine/controller"))
                );
            } else {
                if(name.contains("slope")) {
                    orientationalBlock(TURBINE_BLOCKS.get("turbine_" +name).get(), $ -> models().getExistingFile(rl("block/multiblock/turbine_"+name)));
                } else {
                    if(name.contains("blade")) {
                        faceBlock(TURBINE_BLOCKS.get("turbine_" + name).get(), $ -> models().getExistingFile(rl("block/multiblock/turbine_"+name)));
                    } else {
                        simpleBlock(TURBINE_BLOCKS.get("turbine_" + name).get(), multiBlockModel(TURBINE_BLOCKS.get("turbine_" + name).get(), "turbine/" + name));
                    }
                }
            }
        }

        for(String type: TurbineRegistration.coils.keySet()) {
            simpleBlock(TURBINE_BLOCKS.get("turbine_" + type + "_coil").get(), multiBlockModel(TURBINE_BLOCKS.get("turbine_" + type + "_coil").get(), "turbine/" + type + "_coil"));
        }
    }

    public static int[] getRotationByDirection(Direction dir) {
        int[] result = new int[2];
        switch (dir) {
            case UP -> {
                result[0] = 270;
                result[1] = 0;
            }
            case DOWN -> {
                result[0] = 90;
                result[1] = 0;
            }
            case NORTH -> {
                result[0] = 0;
                result[1] = 0;
            }
            case EAST -> {
                result[0] = 0;
                result[1] = 270;
            }
            case SOUTH -> {
                result[0] = 0;
                result[1] = 180;
            }
            case WEST -> {
                result[0] = 0;
                result[1] = 90;
            }
        }
        return result;
    }
    public void faceBlock(Block block, Function<BlockState, ModelFile> modelFunc) {

        getVariantBuilder(block)
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(modelFunc.apply(state))
                        .rotationX(getRotationByDirection(state.getValue(BlockStateProperties.FACING))[0])
                        .rotationY(getRotationByDirection(state.getValue(BlockStateProperties.FACING))[1])
                        .build()
                );
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
            simpleBlock(FISSION_BLOCKS.get(name+"_heat_sink").get(), multiBlockModel(FISSION_BLOCKS.get(name+"_heat_sink").get(), "heat_sink/"+name));
        }
    }

    private void fissionReactor() {
        for (String name: FissionBlocks.reactor) {
            if(name.matches(".*port.*")) {
                horizontalBlock(FISSION_BLOCKS.get("fission_reactor_" + name).get(), multiBlockModel(FISSION_BLOCKS.get("fission_reactor_" + name).get(), "fission/" + name));
            } else if(name.matches(".*controller.*")) {
                horizontalBlock(FISSION_BLOCKS.get("fission_reactor_" + name).get(),
                        st -> controllerModel(st, sidedModel(FISSION_BLOCKS.get("fission_reactor_" + name).get(), "fission/controller"))
                );
            } else {
                if(name.contains("slope")) {
                    orientationalBlock(FISSION_BLOCKS.get("fission_reactor_" +name).get(), $ -> models().getExistingFile(rl("block/multiblock/fission_reactor_"+name)));
                } else {
                    simpleBlock(FISSION_BLOCKS.get("fission_reactor_" + name).get(), multiBlockModel(FISSION_BLOCKS.get("fission_reactor_" + name).get(), "fission/" + name));
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
        for(String name: PROCESSORS.keySet()) {
            horizontalBlock(
                    PROCESSORS.get(name).get(),
                    st -> processorModel(st, sidedModel(PROCESSORS.get(name).get(),
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
        String type = "";
        if(st.getBlock() == FISSION_BLOCKS.get("fission_reactor_controller").get()) {
            type = "fission";
        } else if(st.getBlock() == TURBINE_BLOCKS.get("turbine_controller").get()) {
            type = "turbine";
        }
        BlockModelBuilder result = models()
                .getBuilder("block/multiblock/"+key(st.getBlock()).getPath()+powered)
                .texture("north", "block/"+type+"/controller/"+key(st.getBlock()).getPath()+powered)
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
        for(String name: NC_MATERIAL_BLOCKS.keySet()) {
            simpleBlock(NC_MATERIAL_BLOCKS.get(name).get(), model(NC_MATERIAL_BLOCKS.get(name).get(), "material/fluid"));
        }
    }
    private void blocks() {
        for(String name: NC_BLOCKS.keySet()) {
            simpleBlock(NC_BLOCKS.get(name).get(), model(NC_BLOCKS.get(name).get(), "material/block"));
        }
        for(String name: NC_ELECTROMAGNETS.keySet()) {
            if(name.contains("slope")) {
                orientationalBlock(NC_ELECTROMAGNETS.get(name).get(), $ -> models().getExistingFile(rl("block/electromagnet/"+name)));
            } else {
                simpleBlock(NC_ELECTROMAGNETS.get(name).get(), model(NC_ELECTROMAGNETS.get(name).get(), "electromagnet"));
            }
        }

        for(String name: NC_RF_AMPLIFIERS.keySet()) {
            simpleBlock(NC_RF_AMPLIFIERS.get(name).get(), model(NC_RF_AMPLIFIERS.get(name).get(), "rf_amplifier"));
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
        for(String ore: ORE_BLOCKS.keySet()) {
            simpleBlock(ORE_BLOCKS.get(ore).get(), model(ORE_BLOCKS.get(ore).get(), "ore"));
        }
    }

    private void portal() {
        Block block = PORTAL_BLOCK.get();
        ResourceLocation side = modLoc("block/portal");
        ResourceLocation top = modLoc("block/portal");
        simpleBlock(block, models().cube(PORTAL_BLOCK.getId().getPath(), side, top, side, side, side, side));
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
        if (subPath.matches(".*fission.*|.*fusion.*|.*port.*|.*controller.*")) {
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