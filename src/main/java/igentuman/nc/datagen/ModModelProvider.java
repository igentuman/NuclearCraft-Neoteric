package igentuman.nc.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.RedstoneDimmerBlock;
import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block.energy.EnergyBlock;
import igentuman.nc.block.pipe.PipeBlock;
import igentuman.nc.api.pipe.PipeConnection;
import igentuman.nc.block.pipe.PipeConnectorBlock;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.ToolSetEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import igentuman.nc.client.item.ResoniteCrystalTint;
import igentuman.nc.client.item.CraftingPatternItemModel;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static net.minecraft.client.data.models.BlockModelGenerators.condition;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

/**
 * Generates blockstates, block/item models and client item definitions for every NuclearCraft
 * block and item. Handwritten assets in {@code src/main/resources} (blockstates, models) are never
 * overwritten: blocks with a handwritten blockstate are excluded from the known blocks, and items
 * without a generated model point at their handwritten {@code item/<id>} model.
 */
public class ModModelProvider extends ModelProvider {
    private static final String TRANSPARENT_BLOCKS =
            ".*glass|.*cell.*|.*photon.*|.*event_horizon_stabilizer.*|.*quantum_transformer.*";

    private final ResourceManager existing;

    /** Buffered outputs: the old providers let a later write replace an earlier one for the same id. */
    private final Map<Identifier, ModelInstance> models = new LinkedHashMap<>();
    private final Map<Block, BlockModelDefinitionGenerator> blockStates = new LinkedHashMap<>();
    private final Map<Item, ItemModel.Unbaked> itemDefinitions = new LinkedHashMap<>();

    public ModModelProvider(PackOutput output, ResourceManager existing) {
        super(output, MODID);
        this.existing = existing;
    }

    // --- known content ---

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder ->
                !blockStateExists(holder.unwrapKey().orElseThrow().identifier().getPath()));
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return super.getKnownItems();
    }

    // --- resource checks against handwritten assets ---

    private boolean exists(String prefix, String path, String suffix) {
        return existing.getResource(Identifier.fromNamespaceAndPath(MODID, prefix + "/" + path + suffix)).isPresent();
    }

    private boolean modelExists(String path) {
        return exists("models", path, ".json");
    }

    private boolean blockStateExists(String path) {
        return exists("blockstates", path, ".json");
    }

    private boolean textureExists(String path) {
        return exists("textures", path, ".png");
    }

    // --- entry point ---

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        models.clear();
        blockStates.clear();
        itemDefinitions.clear();

        registerBlocks();
        registerItems();
        registerFallbackItemDefinitions();

        models.forEach(blockModels.modelOutput);
        blockStates.values().forEach(blockModels.blockStateOutput);
        itemDefinitions.forEach(itemModels.itemModelOutput::accept);
    }

    // ===================================================================================
    // Blocks
    // ===================================================================================

    private void registerBlocks() {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            heatSinkBlock(entry.block().get());
        }

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                Block block = entry.block().get();
                String path = path(block);
                if (blockStateExists(path)) continue;
                if (path.equals("engineers_crafting_table")) {
                    engineersCrafterBlock(block, path);
                } else if (path.equals("expl")) {
                    // Custom block/item models shipped in main resources.
                    simpleBlock(block, rl("block/expl"));
                } else if (path.equals("expl_proxy")) {
                    Identifier proxy = rl("block/fusion/core_proxy");
                    simpleBlock(block, proxy);
                    blockItem(block, proxy);
                } else if (path.equals("redstone_dimmer")) {
                    redstoneDimmerBlock(block, path);
                } else if (path.equals("pipe")) {
                    pipes(block);
                } else if (path.equals("pipe_connector")) {
                    pipeConnector(block);
                } else if (isFusionBlock(path)) {
                    fusionBlock(block, path);
                } else if (isTurbineBlock(path)) {
                    turbineBlock(block, path);
                } else if (isHeatExchangerBlock(path)) {
                    heatExchangerBlock(block, path);
                } else if (isMsrBlock(path)) {
                    msrBlock(block, path);
                } else if (isAcceleratorControlBlock(path)) {
                    acceleratorControlBlock(block, path);
                } else if (isParticleChamberControlBlock(path)) {
                    particleChamberControlBlock(block, path);
                } else if (path.endsWith("_cooler")) {
                    coolerBlock(block, path);
                } else if (block instanceof EnergyBlock) {
                    energyBlock(block, path);
                } else if (block instanceof UniversalProcessorBlock) {
                    processorBlock(block, path);
                } else if (block.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    horizontalBlockWithItem(block);
                } else {
                    blockWithItem(block);
                }
            }
            if (entry.materialEntry() instanceof MaterialEntry material) {
                if (material.hasOre()) materialBlock(material.oreBlock().get());
                if (material.hasDeepslateOre()) materialBlock(material.deepslateOreBlock().get());
                if (material.hasBlock()) materialBlock(material.storageBlock().get());
                fluidBlockState(material);
            }
        }

        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            for (MaterialEntry material : fuel.fluids()) {
                fluidBlockState(material);
            }
        }

        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            for (MaterialEntry material : isotope.fluids()) {
                fluidBlockState(material);
            }
        }
    }

    private void fluidBlockState(MaterialEntry material) {
        if (!material.hasFluid()) return;
        Block fluidBlock = material.materialFluid().fluidBlock().get();
        String path = path(fluidBlock);
        if (blockStateExists(path)) return;
        Identifier particle = material.materialFluid().fluidType().get() instanceof MaterialFluidType type
                ? type.getStillTexture()
                : Identifier.withDefaultNamespace("block/water_still");
        Identifier model = particleOnly(path, particle);
        forAllStates(fluidBlock, state -> plainVariant(model));
    }

    // --- pipes ---

    private void pipes(Block block) {
        Identifier coreTex = rl("block/pipe_core");

        Identifier core = rawModel("pipe/core", textures("pipe", coreTex, "particle", coreTex),
                element(4, 4, 4, 12, 12, 12, "#pipe"));
        Identifier arm = rawModel("pipe/arm", textures("particle", "#pipe"),
                element(4, 4, 0, 12, 12, 4, "#pipe"));
        Identifier armX = childModel("pipe/arm_x", arm, "pipe", rl("block/pipe_x"));
        Identifier armY = childModel("pipe/arm_y", arm, "pipe", rl("block/pipe_y"));
        Identifier armZ = childModel("pipe/arm_z", arm, "pipe", rl("block/pipe_z"));
        Identifier inventory = rawModel("pipe/inventory",
                textures("core", coreTex, "arm", rl("block/pipe_z"), "particle", coreTex),
                element(4, 4, 4, 12, 12, 12, "#core"),
                element(4, 4, 0, 12, 12, 4, "#arm"),
                element(4, 4, 12, 12, 12, 16, "#arm"));

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(plainVariant(core));
        pipeArm(generator, armY, PipeBlock.UP, 270, 0);
        pipeArm(generator, armY, PipeBlock.DOWN, 90, 0);
        pipeArm(generator, armZ, PipeBlock.NORTH, 0, 0);
        pipeArm(generator, armZ, PipeBlock.SOUTH, 0, 180);
        pipeArm(generator, armX, PipeBlock.EAST, 0, 90);
        pipeArm(generator, armX, PipeBlock.WEST, 0, 270);
        blockState(generator);
        blockItem(block, inventory);
    }

    private void pipeArm(MultiPartGenerator generator, Identifier arm, BooleanProperty property, int rotX, int rotY) {
        generator.with(condition().term(property, true), variant(arm, rotX, rotY));
    }

    private void pipeConnector(Block block) {
        Identifier core = rl("block/pipe/core");
        Identifier armX = rl("block/pipe/arm_x");
        Identifier armY = rl("block/pipe/arm_y");
        Identifier armZ = rl("block/pipe/arm_z");

        Identifier collar = rawModel("pipe/connector/collar", textures("particle", "#pipe"),
                element(3, 3, 0, 13, 13, 2, "#pipe"));
        Identifier collarX = childModel("pipe/connector/collar_x", collar, "pipe", rl("block/pipe_x"));
        Identifier collarY = childModel("pipe/connector/collar_y", collar, "pipe", rl("block/pipe_y"));
        Identifier collarZ = childModel("pipe/connector/collar_z", collar, "pipe", rl("block/pipe_z"));
        Identifier inventory = rawModel("pipe/connector/inventory",
                textures("core", rl("block/pipe_core"), "arm", rl("block/pipe_z"), "particle", rl("block/pipe_core")),
                element(4, 4, 4, 12, 12, 12, "#core"),
                element(4, 4, 0, 12, 12, 4, "#arm"),
                element(3, 3, 0, 13, 13, 2, "#arm"),
                element(4, 4, 12, 12, 12, 16, "#arm"));

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(plainVariant(core));
        connectorArm(generator, armY, PipeConnectorBlock.UP, 270, 0);
        connectorArm(generator, armY, PipeConnectorBlock.DOWN, 90, 0);
        connectorArm(generator, armZ, PipeConnectorBlock.NORTH, 0, 0);
        connectorArm(generator, armZ, PipeConnectorBlock.SOUTH, 0, 180);
        connectorArm(generator, armX, PipeConnectorBlock.EAST, 0, 90);
        connectorArm(generator, armX, PipeConnectorBlock.WEST, 0, 270);
        connectorCollar(generator, collarY, PipeConnectorBlock.UP, 270, 0);
        connectorCollar(generator, collarY, PipeConnectorBlock.DOWN, 90, 0);
        connectorCollar(generator, collarZ, PipeConnectorBlock.NORTH, 0, 0);
        connectorCollar(generator, collarZ, PipeConnectorBlock.SOUTH, 0, 180);
        connectorCollar(generator, collarX, PipeConnectorBlock.EAST, 0, 90);
        connectorCollar(generator, collarX, PipeConnectorBlock.WEST, 0, 270);
        blockState(generator);
        blockItem(block, inventory);
    }

    private void connectorArm(MultiPartGenerator generator, Identifier arm, EnumProperty<PipeConnection> property, int rotX, int rotY) {
        generator.with(condition().term(property, PipeConnection.PIPE, PipeConnection.MACHINE), variant(arm, rotX, rotY));
    }

    private void connectorCollar(MultiPartGenerator generator, Identifier collar, EnumProperty<PipeConnection> property, int rotX, int rotY) {
        generator.with(condition().term(property, PipeConnection.MACHINE), variant(collar, rotX, rotY));
    }

    // --- generic block shapes ---

    private void horizontalBlockWithItem(Block block) {
        String registryPath = path(block);
        String path = applySpecialRules(registryPath);
        boolean hasSide = textureExists("block/" + path + "/side");
        boolean hasFront = textureExists("block/" + path + "/front");
        boolean hasTop = textureExists("block/" + path + "/top");
        boolean hasBottom = textureExists("block/" + path + "/bottom");
        boolean hasSubfolderTextures = hasSide || hasFront || hasTop || hasBottom;
        // Fallback: single texture without subfolder (e.g. block/example_machine.png).
        boolean hasSingleTexture = textureExists("block/" + path);

        Identifier model;
        Identifier poweredModel;
        if (hasSubfolderTextures) {
            // Use the available subfolder textures, falling back to side for missing ones.
            Identifier side = rl("block/" + path + "/" + (hasSide ? "side" : "front"));
            Identifier front = rl("block/" + path + "/" + (hasFront ? "front" : "side"));
            Identifier top = hasTop ? rl("block/" + path + "/top") : side;
            Identifier bottom = hasBottom ? rl("block/" + path + "/bottom") : top;
            model = orientableWithBottom(path, side, front, bottom, top);
            String poweredFrontPath = "block/" + path + "/" + registryPath + "_powered";
            Identifier poweredFront = textureExists(poweredFrontPath) ? rl(poweredFrontPath) : front;
            poweredModel = block instanceof MultiblockControllerBlock
                    ? orientableWithBottom(path + "_powered", side, poweredFront, bottom, top) : model;
        } else if (hasSingleTexture) {
            Identifier texture = rl("block/" + path);
            model = orientable(path, texture, texture, texture);
            poweredModel = model;
        } else {
            // Placeholder until textures exist; keeps datagen and rendering valid.
            Identifier placeholder = Identifier.withDefaultNamespace("block/iron_block");
            model = orientable(path, placeholder, placeholder, placeholder);
            poweredModel = model;
        }
        if (block instanceof MultiblockControllerBlock) controllerVariants(block, model, poweredModel);
        else horizontalBlock(block, model);
        blockItem(block, model);
    }

    private String applySpecialRules(String path) {
        if (path.contains("fission_reactor_")) {
            path = path.replace("fission_reactor_", "fission/");
            if (path.contains("port")) {
                path += "/front";
            }
        }
        if (path.contains("turbine_")) {
            path = path.replace("turbine_", "turbine/");
            if (path.contains("port")) {
                path += "/front";
            }
        }
        if (path.contains("fusion_reactor_")) {
            path = path.replace("fusion_reactor_", "fusion/");
        }
        if (path.equals("accelerator_casing") || path.equals("accelerator_casing_glass")
                || path.equals("electromagnet_yoke") || path.equals("particle_beam")) {
            path = "accelerator/" + path;
        }
        switch (path) {
            case "target_chamber_casing" -> path = "particle_chamber/casing";
            case "target_chamber_casing_glass" -> path = "particle_chamber/glass";
            case "target_chamber_camera" -> path = "particle_chamber/camera";
            case "silicon_tracker" -> path = "particle_chamber/silicon_tracker";
            case "bubble_chamber" -> path = "particle_chamber/bubble_chamber";
            case "wire_chamber" -> path = "particle_chamber/wire_chamber";
            case "em_calorimeter" -> path = "particle_chamber/em_calorimeter";
            case "hadron_calorimeter" -> path = "particle_chamber/hadron_calorimeter";
            default -> { }
        }
        return path;
    }

    private void redstoneDimmerBlock(Block block, String path) {
        Identifier side = rl("block/processor/side");
        Identifier top = rl("block/processor/top");
        Identifier bottom = rl("block/processor/bottom");
        Identifier back = rl("block/processor/back");
        Identifier[] levels = new Identifier[16];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = cube(path + "/" + i, bottom, top, rl("block/" + path + "/" + i), back, side, side, side);
        }
        forAllStates(block, state -> variant(levels[state.getValue(RedstoneDimmerBlock.LEVEL)], 0,
                ((int) state.getValue(RedstoneDimmerBlock.FACING).toYRot() + 180) % 360),
                RedstoneDimmerBlock.LEFT, RedstoneDimmerBlock.RIGHT);
        blockItem(block, levels[0]);
    }

    private void processorBlock(Block block, String path) {
        Identifier idle;
        Identifier powered;
        if (modelExists("block/" + path)) {
            idle = rl("block/" + path);
            powered = modelExists("block/" + path + "_powered") ? rl("block/" + path + "_powered") : idle;
        } else {
            Identifier side = rl("block/processor/side");
            Identifier top = rl("block/processor/top");
            Identifier bottom = rl("block/processor/bottom");
            Identifier front = rl("block/processor/" + path);
            Identifier frontPowered = textureExists("block/processor/" + path + "_powered")
                    ? rl("block/processor/" + path + "_powered") : front;
            idle = orientableWithBottom(path, side, front, bottom, top);
            powered = orientableWithBottom(path + "_powered", side, frontPowered, bottom, top);
        }
        forAllStates(block, state -> variant(state.getValue(UniversalProcessorBlock.POWERED) ? powered : idle,
                0, yRotation(state.getValue(UniversalProcessorBlock.FACING))));
        blockItem(block, idle);
    }

    /** Default cube model at {@code block/<registry path>} with the special-ruled texture. */
    private void blockWithItem(Block block) {
        String path = path(block);
        Identifier model = cubeAll(path, rl("block/" + applySpecialRules(path)));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private void materialBlock(Block block) {
        String path = path(block);
        if (blockStateExists(path)) return;
        Identifier model = cubeAll(path, rl("block/material/" + path));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private boolean isFusionBlock(String path) {
        return path.startsWith("fusion_reactor_")
                || path.endsWith("_electromagnet")
                || path.endsWith("_electromagnet_slope")
                || path.endsWith("_rf_amplifier");
    }

    private void fusionBlock(Block block, String path) {
        if (path.equals("fusion_reactor_core_proxy")) {
            Identifier proxy = rl("block/fusion/core_proxy");
            simpleBlock(block, proxy);
            blockItem(block, proxy);
            return;
        }

        if (path.endsWith("_electromagnet_slope")) {
            Identifier slope = rl("block/electromagnet/" + path);
            forAllStates(block, state -> {
                FrontAndTop dir = state.getValue(BlockStateProperties.ORIENTATION);
                return variant(slope, dir.front() == Direction.DOWN ? 180 : 0,
                        (((int) dir.top().toYRot()) + 180) % 360);
            });
            blockItem(block, slope);
            return;
        }

        if (path.equals("fusion_reactor_core")) {
            // Custom item model shipped in main resources.
            Identifier core = rl("block/fusion/core_center");
            controllerVariants(block, core, core);
            return;
        }

        Identifier texture;
        if (path.equals("fusion_reactor_casing")) {
            texture = rl("block/fusion/fusion_reactor_casing");
        } else if (path.equals("fusion_reactor_glass")) {
            texture = rl("block/fusion/fusion_reactor_casing_glass");
        } else if (path.equals("fusion_reactor_connector")) {
            texture = rl("block/fusion/fusion_reactor_connector");
        } else if (path.endsWith("_electromagnet")) {
            texture = rl("block/electromagnet/" + path);
        } else if (path.endsWith("_rf_amplifier")) {
            texture = rl("block/rf_amplifier/" + path);
        } else {
            texture = rl("block/fusion/fusion_reactor_casing");
        }
        Identifier model = cubeAll(path, texture);
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private boolean isTurbineBlock(String path) {
        return path.startsWith("turbine_") || path.equals("dummy_turbine_blade");
    }

    private void turbineBlock(Block block, String path) {
        switch (path) {
            case "turbine_controller" -> {
                Identifier model = orientableWithBottom("turbine_controller",
                        rl("block/turbine/controller/side"),
                        rl("block/turbine/controller/turbine_controller"),
                        rl("block/turbine/controller/bottom"),
                        rl("block/turbine/controller/top"));
                Identifier powered = orientableWithBottom("turbine_controller_powered",
                        rl("block/turbine/controller/side"),
                        rl("block/turbine/controller/turbine_controller_powered"),
                        rl("block/turbine/controller/bottom"),
                        rl("block/turbine/controller/top"));
                controllerVariants(block, model, powered);
                blockItem(block, model);
                return;
            }
            case "turbine_port" -> {
                Identifier model = cubeAll("turbine_port", rl("block/turbine/port/front"));
                simpleBlock(block, model);
                blockItem(block, model);
                return;
            }
            case "turbine_rotor_shaft" -> {
                Identifier model = rl("block/turbine/turbine_rotor_shaft");
                forAllStates(block, state -> {
                    Direction dir = state.getValue(BlockStateProperties.FACING);
                    int x = switch (dir) {
                        case UP -> 270;
                        case DOWN -> 90;
                        default -> 0;
                    };
                    int y = switch (dir) {
                        case EAST -> 270;
                        case SOUTH -> 180;
                        case WEST -> 90;
                        default -> 0;
                    };
                    return variant(model, x, y);
                });
                blockItem(block, model);
                return;
            }
            default -> { }
        }

        Identifier model = cubeAll(path, rl("block/turbine/" + path.substring("turbine_".length())));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private boolean isHeatExchangerBlock(String path) {
        return path.equals("heat_exchanger") || path.startsWith("heat_exchanger_");
    }

    private void heatExchangerBlock(Block block, String path) {
        if (path.equals("heat_exchanger_controller")) {
            Identifier model = orientableWithBottom("heat_exchanger_controller",
                    rl("block/heat_exchanger/controller/side"),
                    rl("block/heat_exchanger/controller/front"),
                    rl("block/heat_exchanger/controller/bottom"),
                    rl("block/heat_exchanger/controller/top"));
            Identifier powered = orientableWithBottom("heat_exchanger_controller_powered",
                    rl("block/heat_exchanger/controller/side"),
                    rl("block/heat_exchanger/controller/heat_exchanger_controller_powered"),
                    rl("block/heat_exchanger/controller/bottom"),
                    rl("block/heat_exchanger/controller/top"));
            controllerVariants(block, model, powered);
            blockItem(block, model);
            return;
        }
        Identifier model = switch (path) {
            case "heat_exchanger_hot_coolant_port" -> cubeAll(path,
                    rl("block/heat_exchanger/port/heat_exchanger_hot_coolant_port"));
            case "heat_exchanger_cold_coolant_port" -> cubeAll(path,
                    rl("block/heat_exchanger/port/heat_exchanger_cold_coolant_port"));
            case "heat_exchanger_radiator" -> cubeAll(path, rl("block/heat_exchanger/radiator"));
            case "heat_exchanger_casing" -> cubeAll(path, rl("block/heat_exchanger/casing"));
            default -> cubeAll("heat_exchanger", rl("block/heat_exchanger/casing_ctm"));
        };
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private boolean isMsrBlock(String path) {
        return path.equals("msr_controller") || path.equals("msr_fuel_cell") || path.equals("msr_port");
    }

    private void msrBlock(Block block, String path) {
        switch (path) {
            case "msr_controller" -> {
                Identifier model = orientableWithBottom("msr_controller",
                        rl("block/fission/msr_controller/side"),
                        rl("block/fission/controller/msr_controller"),
                        rl("block/fission/msr_controller/bottom"),
                        rl("block/fission/msr_controller/top"));
                Identifier powered = orientableWithBottom("msr_controller_powered",
                        rl("block/fission/msr_controller/side"),
                        rl("block/fission/controller/msr_controller_powered"),
                        rl("block/fission/msr_controller/bottom"),
                        rl("block/fission/msr_controller/top"));
                controllerVariants(block, model, powered);
                blockItem(block, model);
            }
            case "msr_fuel_cell" -> {
                Identifier model = cubeAll("msr_fuel_cell", rl("block/fission/msr_fuel_cell"));
                simpleBlock(block, model);
                blockItem(block, model);
            }
            case "msr_port" -> {
                Identifier model = cubeAll("msr_port", rl("block/fission/port/front"));
                simpleBlock(block, model);
                blockItem(block, model);
            }
            default -> { }
        }
    }

    private boolean isAcceleratorControlBlock(String path) {
        return path.equals("linear_accelerator_controller") || path.equals("ring_accelerator_controller")
                || path.equals("beam_diverter_controller") || path.equals("accelerator_port")
                || path.equals("accelerator_beam_port") || path.equals("accelerator_ion_source_port");
    }

    private void acceleratorControlBlock(Block block, String path) {
        switch (path) {
            case "linear_accelerator_controller", "ring_accelerator_controller", "beam_diverter_controller" ->
                    controllerBlock(block, path, "accelerator/controller");
            case "accelerator_beam_port" -> beamPortBlock(block, path, "accelerator/accelerator_beam_port");
            default -> simplePortBlock(block, path, "accelerator/" + path + "/" + path);
        }
    }

    private boolean isParticleChamberControlBlock(String path) {
        return path.equals("target_chamber_controller") || path.equals("decay_chamber_controller")
                || path.equals("collision_chamber_controller") || path.equals("target_chamber_port")
                || path.equals("target_chamber_beam_port");
    }

    private void particleChamberControlBlock(Block block, String path) {
        switch (path) {
            case "target_chamber_controller", "decay_chamber_controller", "collision_chamber_controller" ->
                    controllerBlock(block, path, "particle_chamber/" + path);
            case "target_chamber_beam_port" -> beamPortBlock(block, path, "particle_chamber/beam_port");
            default -> simplePortBlock(block, path, "particle_chamber/port/" + path);
        }
    }

    /** Facing controller block: shared side/top/bottom, per-name front texture, e.g. block/{family}/{path}. */
    private void controllerBlock(Block block, String path, String family) {
        Identifier model = orientableWithBottom(path,
                rl("block/" + family + "/side"),
                rl("block/" + family + "/" + path),
                rl("block/" + family + "/bottom"),
                rl("block/" + family + "/top"));
        Identifier poweredFront = textureExists("block/" + family + "/" + path + "_powered")
                ? rl("block/" + family + "/" + path + "_powered")
                : rl("block/" + family + "/" + path);
        Identifier powered = orientableWithBottom(path + "_powered",
                rl("block/" + family + "/side"), poweredFront,
                rl("block/" + family + "/bottom"), rl("block/" + family + "/top"));
        controllerVariants(block, model, powered);
        blockItem(block, model);
    }

    private void controllerVariants(Block block, Identifier idle, Identifier powered) {
        forAllStates(block, state -> variant(state.getValue(MultiblockControllerBlock.POWERED) ? powered : idle,
                0, yRotation(state.getValue(MultiblockControllerBlock.FACING))));
    }

    /** Plain service port with no mode texture: single representative texture on every face. */
    private void simplePortBlock(Block block, String path, String texturePath) {
        Identifier model = cubeAll(path, rl("block/" + texturePath));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private void coolerBlock(Block block, String path) {
        String textureName = path.substring(0, path.length() - "_cooler".length());
        Identifier model = cubeAll(path, rl("block/accelerator/cooler/" + textureName));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    /** Beam port with a PORT_MODE (input/output/disabled) property driving the front face texture. */
    @SuppressWarnings("unchecked")
    private void beamPortBlock(Block block, String path, String family) {
        Identifier down = rl("block/" + family + "/bottom");
        Identifier up = rl("block/" + family + "/top");
        Identifier south = rl("block/" + family + "/back");
        Identifier side = rl("block/" + family + "/side");
        Identifier inputModel = cube(path + "_input", down, up, rl("block/" + family + "/input"), south, side, side, side);
        Identifier outputModel = cube(path + "_output", down, up, rl("block/" + family + "/output"), south, side, side, side);
        Identifier disabledModel = cube(path + "_disabled", down, up, rl("block/" + family + "/disabled"), south, side, side, side);

        EnumProperty<BeamPortMode> modeProperty =
                (EnumProperty<BeamPortMode>) block.getStateDefinition().getProperty("port_mode");
        forAllStates(block, state -> {
            Identifier model = switch (state.getValue(modeProperty)) {
                case INPUT -> inputModel;
                case OUTPUT -> outputModel;
                case DISABLED -> disabledModel;
            };
            return variant(model, 0, yRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        });
        blockItem(block, disabledModel);
    }

    private void engineersCrafterBlock(Block block, String path) {
        Identifier model = cubeColumn(path, rl("block/" + path + "/side"), rl("block/" + path + "/top"));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private void energyBlock(Block block, String path) {
        Identifier model;
        if (path.equals("decay_generator")) {
            model = cubeAll(path, rl("block/energy/decay_generator"));
        } else if (path.endsWith("_rtg")) {
            String metal = path.substring(0, path.length() - "_rtg".length());
            Identifier side = rl("block/energy/rtg/" + metal + "/side");
            model = cubeBottomTop(path, side, side, rl("block/energy/rtg/" + metal + "/top"));
        } else {
            String tier = path.substring("solar_panel_".length());
            Identifier side = rl("block/energy/solar_panel/" + tier + "_side");
            model = cubeBottomTop(path, side, side, rl("block/energy/solar_panel/" + tier + "_top"));
        }
        simpleBlock(block, model);
        blockItem(block, model);
    }

    private void heatSinkBlock(Block block) {
        String path = path(block);
        String textureName = path.endsWith("_heat_sink") ? path.substring(0, path.length() - "_heat_sink".length()) : path;
        Identifier model = cubeAll(path, rl("block/heat_sink/" + textureName));
        simpleBlock(block, model);
        blockItem(block, model);
    }

    // ===================================================================================
    // Items
    // ===================================================================================

    private void registerItems() {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            String name = entry.name();
            // Chassis and compression charge use handwritten item models (fallback definitions).
            boolean handwritten = name.contains("chassis") || name.contains("compression_charge");
            if (!handwritten && entry.hasItem() && !entry.hasBlock() && !entry.customModel()) {
                Item item = entry.item().get();
                if (name.endsWith("_spawn_egg")) {
                    spawnEgg(item, name);
                } else if (name.equals("qnp") || name.equals("q36_quantite_disruptor")) {
                    // Handwritten item models; fallback definitions.
                } else if (name.equals("crafting_pattern")) {
                    // Blank patterns show the flat texture; encoded ones show their output (custom model type).
                    Identifier model = ModelTemplates.FLAT_ITEM.create(itemModel(item),
                            TextureMapping.layer0(material(rl("item/crafting_pattern"))), models::put);
                    itemDefinitions.put(item, new CraftingPatternItemModel.Unbaked(ItemModelUtils.plainModel(model)));
                } else if (name.equals("resonite_crystal")) {
                    Identifier model = ModelTemplates.FLAT_ITEM.create(itemModel(item),
                            TextureMapping.layer0(material(rl("item/" + name))), models::put);
                    itemDefinitions.put(item, ItemModelUtils.tintedModel(model, new ResoniteCrystalTint()));
                } else if (item instanceof ParticleSourceItem) {
                    flatItem(item, "particle_sources/" + name);
                } else if (name.contains("collector")) {
                    collectorItem(item, name);
                } else {
                    // Item components are not bound during datagen; food textures live under item/food/.
                    String texture = textureExists("item/food/" + name) ? "food/" + name : name;
                    flatItem(item, texture);
                }
            }
            if (entry.toolSetEntry() instanceof ToolSetEntry toolSet) {
                handheldItem(toolSet.sword(), "tools/" + toolSet.name + "_sword");
                handheldItem(toolSet.pickaxe(), "tools/" + toolSet.name + "_pickaxe");
                handheldItem(toolSet.axe(), "tools/" + toolSet.name + "_axe");
                handheldItem(toolSet.shovel(), "tools/" + toolSet.name + "_shovel");
                handheldItem(toolSet.hoe(), "tools/" + toolSet.name + "_hoe");
            }
            if (entry.armorSetEntry() instanceof ArmorSetEntry armorSet) {
                flatItem(armorSet.helmet().get(), "armor/" + armorSet.name + "_helmet");
                flatItem(armorSet.chestplate().get(), "armor/" + armorSet.name + "_chestplate");
                flatItem(armorSet.leggings().get(), "armor/" + armorSet.name + "_leggings");
                flatItem(armorSet.boots().get(), "armor/" + armorSet.name + "_boots");
            }
            if (entry.materialEntry() instanceof MaterialEntry material) {
                if (material.hasIngot()) flatItem(material.ingot().get(), "material/ingot/" + material.name);
                if (material.hasGem()) flatItem(material.gem().get(), "material/gem/" + material.name);
                if (material.hasRawOre()) flatItem(material.rawOre().get(), "material/raw/" + material.name);
                if (material.hasDust()) flatItem(material.dust().get(), "material/dust/" + material.name);
                if (material.hasPlate()) flatItem(material.plate().get(), "material/plate/" + material.name);
                if (material.hasNugget()) flatItem(material.nugget().get(), "material/nugget/" + material.name);
                bucket(material);
            }
        }
        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            // Texture path keeps the slashed element/mass layout (e.g. isotope/uranium/238);
            // the model is named after the registered item id (uranium_238).
            isotope.variants().forEach((suffix, item) ->
                    flatItem(item.get(), "material/isotope/" + isotope.name + suffix));
            for (MaterialEntry material : isotope.fluids()) bucket(material);
        }
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            String name = fuel.name.replace("-", "_");
            fuel.fuelItems().forEach((variant, item) ->
                    flatItem(item.get(), "fuel/" + fuel.group + "/" + name + variant));
            fuel.depletedItems().forEach((variant, item) ->
                    flatItem(item.get(), "fuel/" + fuel.group + "/depleted/" + name + variant));
            for (MaterialEntry material : fuel.fluids()) bucket(material);
        }
    }

    /**
     * Items without a generated definition use their handwritten {@code item/<id>} model, as the
     * implicit item model did before; block items without one fall back to {@code block/<id>}.
     */
    private void registerFallbackItemDefinitions() {
        super.getKnownItems().map(Holder::value).forEach(item -> {
            if (itemDefinitions.containsKey(item)) return;
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (item instanceof BlockItem && !modelExists("item/" + path)) return;
            itemDefinitions.put(item, ItemModelUtils.plainModel(rl("item/" + path)));
        });
    }

    private void flatItem(Item item, String texture) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(itemModel(item),
                TextureMapping.layer0(material(rl("item/" + texture))), models::put);
        itemDefinitions.put(item, ItemModelUtils.plainModel(model));
    }

    private void handheldItem(DeferredItem<? extends Item> item, String texture) {
        Identifier model = ModelTemplates.FLAT_HANDHELD_ITEM.create(itemModel(item.get()),
                TextureMapping.layer0(material(rl("item/" + texture))), models::put);
        itemDefinitions.put(item.get(), ItemModelUtils.plainModel(model));
    }

    private void collectorItem(Item item, String name) {
        Identifier model = ModelTemplates.CUBE_ALL.create(itemModel(item),
                TextureMapping.cube(material(rl("item/collector/" + collectorTextureName(name)))), models::put);
        itemDefinitions.put(item, ItemModelUtils.plainModel(model));
    }

    private static String collectorTextureName(String itemName) {
        String base = itemName.replace("_collector", "");
        if (base.startsWith("compact_")) {
            return base.replace("compact_", "") + "_compact";
        }
        if (base.startsWith("dense_")) {
            return base.replace("dense_", "") + "_dense";
        }
        return base;
    }

    /**
     * Spawn eggs: the removed {@code item/template_spawn_egg} parent is replaced by the mod's own
     * base/overlay textures, tinted with the original egg colors.
     */
    private void spawnEgg(Item item, String name) {
        int base = name.contains("boss") ? 0x344c38 : 0x7e9680;
        int spots = name.contains("boss") ? 0x9bcf67 : 0xc5d1c5;
        Identifier model = ModelTemplates.TWO_LAYERED_ITEM.create(itemModel(item),
                new TextureMapping()
                        .put(TextureSlot.LAYER0, material(rl("item/spawn_egg")))
                        .put(TextureSlot.LAYER1, material(rl("item/spawn_egg_overlay"))),
                models::put);
        itemDefinitions.put(item, ItemModelUtils.tintedModel(model,
                ItemModelUtils.constantTint(base), ItemModelUtils.constantTint(spots)));
    }

    /** Bucket definition: NeoForge fluid container model over the vanilla bucket (fluid tint is built in). */
    private void bucket(MaterialEntry material) {
        if (!material.hasFluid()) return;
        var fluid = material.materialFluid();
        Item bucket = fluid.bucket().asItem();
        if (bucket == Items.AIR) return;
        Fluid source = fluid.source().get();
        var textures = new DynamicFluidContainerModel.Textures(
                Optional.empty(),
                Optional.of(material(Identifier.withDefaultNamespace("item/bucket"))),
                Optional.of(material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"))),
                Optional.empty());
        itemDefinitions.put(bucket, new DynamicFluidContainerModel.Unbaked(
                textures, source, material.fluidDefinition.isGas, true, true));
    }

    // ===================================================================================
    // Output helpers
    // ===================================================================================

    private static String path(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private static Identifier itemModel(Item item) {
        return rl("item/" + BuiltInRegistries.ITEM.getKey(item).getPath());
    }

    private static Material material(Identifier texture) {
        return new Material(texture);
    }

    private void blockItem(Block block, Identifier model) {
        Item item = block.asItem();
        if (item != Items.AIR) {
            itemDefinitions.put(item, ItemModelUtils.plainModel(model));
        }
    }

    // --- block models ---

    private Identifier cubeAll(String path, Identifier texture) {
        return ModelTemplates.CUBE_ALL.create(rl("block/" + path), TextureMapping.cube(material(texture)), models::put);
    }

    private Identifier orientableWithBottom(String path, Identifier side, Identifier front, Identifier bottom, Identifier top) {
        return ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(rl("block/" + path), new TextureMapping()
                .put(TextureSlot.SIDE, material(side))
                .put(TextureSlot.FRONT, material(front))
                .put(TextureSlot.BOTTOM, material(bottom))
                .put(TextureSlot.TOP, material(top)), models::put);
    }

    private Identifier orientable(String path, Identifier side, Identifier front, Identifier top) {
        return ModelTemplates.CUBE_ORIENTABLE.create(rl("block/" + path), new TextureMapping()
                .put(TextureSlot.SIDE, material(side))
                .put(TextureSlot.FRONT, material(front))
                .put(TextureSlot.TOP, material(top)), models::put);
    }

    /** Six-face {@code block/cube}; {@code particle} is emitted only when given (the old {@code cube} had none). */
    private Identifier cube(String path, Identifier down, Identifier up, Identifier north, Identifier south,
                            Identifier east, Identifier west, Identifier particle) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.DOWN, material(down))
                .put(TextureSlot.UP, material(up))
                .put(TextureSlot.NORTH, material(north))
                .put(TextureSlot.SOUTH, material(south))
                .put(TextureSlot.EAST, material(east))
                .put(TextureSlot.WEST, material(west));
        ModelTemplate template = CUBE_NO_PARTICLE;
        if (particle != null) {
            mapping.put(TextureSlot.PARTICLE, material(particle));
            template = ModelTemplates.CUBE;
        }
        return template.create(rl("block/" + path), mapping, models::put);
    }

    private static final ModelTemplate CUBE_NO_PARTICLE = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/cube")), Optional.empty(),
            TextureSlot.DOWN, TextureSlot.UP, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.EAST, TextureSlot.WEST);

    private Identifier cubeBottomTop(String path, Identifier side, Identifier bottom, Identifier top) {
        return ModelTemplates.CUBE_BOTTOM_TOP.create(rl("block/" + path), new TextureMapping()
                .put(TextureSlot.SIDE, material(side))
                .put(TextureSlot.BOTTOM, material(bottom))
                .put(TextureSlot.TOP, material(top)), models::put);
    }

    private Identifier cubeColumn(String path, Identifier side, Identifier end) {
        return ModelTemplates.CUBE_COLUMN.create(rl("block/" + path), new TextureMapping()
                .put(TextureSlot.SIDE, material(side))
                .put(TextureSlot.END, material(end)), models::put);
    }

    private Identifier particleOnly(String path, Identifier particle) {
        return ModelTemplates.PARTICLE_ONLY.create(rl("block/" + path),
                TextureMapping.particle(material(particle)), models::put);
    }

    /** Model with only a parent and one texture variable, e.g. a pipe arm per axis. */
    private Identifier childModel(String path, Identifier parent, String slot, Identifier texture) {
        TextureSlot textureSlot = TextureSlot.create(slot);
        return new ModelTemplate(Optional.of(parent), Optional.empty(), textureSlot)
                .create(rl("block/" + path), new TextureMapping().put(textureSlot, material(texture)), models::put);
    }

    /**
     * Element model written as raw JSON: the pipe models use texture references ({@code #pipe})
     * as texture values, which {@link Material} cannot express.
     */
    private Identifier rawModel(String path, JsonObject textures, JsonObject... elements) {
        Identifier id = rl("block/" + path);
        models.put(id, () -> {
            JsonObject root = new JsonObject();
            JsonArray list = new JsonArray();
            for (JsonObject element : elements) list.add(element.deepCopy());
            root.add("elements", list);
            root.add("textures", textures.deepCopy());
            return root;
        });
        return id;
    }

    private static JsonObject textures(Object... keyValues) {
        JsonObject textures = new JsonObject();
        for (int i = 0; i < keyValues.length; i += 2) {
            textures.addProperty((String) keyValues[i], keyValues[i + 1].toString());
        }
        return textures;
    }

    private static JsonObject element(int x1, int y1, int z1, int x2, int y2, int z2, String texture) {
        JsonObject element = new JsonObject();
        element.add("from", vec(x1, y1, z1));
        element.add("to", vec(x2, y2, z2));
        JsonObject faces = new JsonObject();
        for (Direction direction : Direction.values()) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", texture);
            faces.add(direction.getSerializedName(), face);
        }
        element.add("faces", faces);
        return element;
    }

    private static JsonArray vec(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    // --- blockstates ---

    private void blockState(BlockModelDefinitionGenerator generator) {
        blockStates.put(generator.block(), generator);
    }

    /** Single unconditional variant (key {@code ""}), as the old {@code simpleBlock}. */
    private void simpleBlock(Block block, Identifier model) {
        blockState(new StateVariants(block, Map.of("", plainVariant(model))));
    }

    /** One variant per state of the block, keyed by every property except {@code ignored}. */
    private void forAllStates(Block block, Function<BlockState, MultiVariant> mapper, Property<?>... ignored) {
        Set<Property<?>> skip = Set.of(ignored);
        Map<String, MultiVariant> variants = new HashMap<>();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            String key = state.getProperties().stream()
                    .filter(property -> !skip.contains(property))
                    .sorted(Comparator.comparing(Property::getName))
                    .map(property -> property.getName() + "=" + valueName(state, property))
                    .collect(Collectors.joining(","));
            variants.putIfAbsent(key, mapper.apply(state));
        }
        blockState(new StateVariants(block, variants));
    }

    private void horizontalBlock(Block block, Identifier model) {
        forAllStates(block, state -> variant(model, 0,
                ((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360));
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static int yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static MultiVariant variant(Identifier model, int rotX, int rotY) {
        MultiVariant variant = plainVariant(model);
        if (rotX != 0) variant = variant.with(rotation(VariantMutator.X_ROT, rotX));
        if (rotY != 0) variant = variant.with(rotation(VariantMutator.Y_ROT, rotY));
        return variant;
    }

    private static VariantMutator rotation(VariantMutator.VariantProperty<Quadrant> axis, int degrees) {
        return axis.withValue(Quadrant.parseJson(degrees));
    }

    /** Variant map keyed exactly like the old Forge variant builder (properties sorted by name). */
    private record StateVariants(Block block, Map<String, MultiVariant> variants) implements BlockModelDefinitionGenerator {
        @Override
        public BlockStateModelDispatcher create() {
            Map<String, BlockStateModel.Unbaked> unbaked = new HashMap<>();
            variants.forEach((key, variant) -> unbaked.put(key, variant.toUnbaked()));
            return new BlockStateModelDispatcher(
                    Optional.of(new BlockStateModelDispatcher.SimpleModelSelectors(unbaked)),
                    Optional.empty(), Optional.empty());
        }
    }
}
