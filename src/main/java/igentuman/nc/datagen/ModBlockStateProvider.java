package igentuman.nc.datagen;

import igentuman.nc.block.UniversalProcessorBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/** Generates blockstates and block/item models for blocks, processors, heat sinks, and materials. */
public class ModBlockStateProvider extends BlockStateProvider {
    private final ExistingFileHelper existingFileHelper;

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    private boolean modelExists(String path) {
        return existingFileHelper.exists(
                ResourceLocation.fromNamespaceAndPath(MODID, path),
                net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
                ".json", "models"
        );
    }

    private boolean blockStateExists(String path) {
        return existingFileHelper.exists(
                ResourceLocation.fromNamespaceAndPath(MODID, path),
                net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
                ".json", "blockstates"
        );
    }

    private boolean textureExists(String path) {
        return existingFileHelper.exists(
                ResourceLocation.fromNamespaceAndPath(MODID, path),
                net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
                ".png", "textures"
        );
    }

    @Override
    public void registerStatesAndModels() {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            heatSinkBlock(entry.block());
        }

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                String path = BuiltInRegistries.BLOCK.getKey(entry.block().get()).getPath();
                if (blockStateExists(path)) continue;
                if (path.equals("expl")) {
                    explBlock(entry.block());
                } else if (path.equals("expl_proxy")) {
                    ModelFile proxy = models().getExistingFile(rl("block/fusion/core_proxy"));
                    simpleBlock(entry.block().get(), proxy);
                    itemModels().getBuilder("item/" + path).parent(proxy);
                } else if (isFusionBlock(path)) {
                    fusionBlock(entry.block(), path);
                } else if (entry.block().get() instanceof UniversalProcessorBlock) {
                    processorBlock(entry.block());
                } else if (entry.block().get().defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    horizontalBlockWithItem(entry.block());
                } else {
                    blockWithItem(entry.block());
                }
            }
            if (entry.materialEntry() instanceof MaterialEntry materialEntry) {
                if (materialEntry.hasOre()) {
                    String path = BuiltInRegistries.BLOCK.getKey(materialEntry.oreBlock().get()).getPath();
                    if (!blockStateExists(path)) {
                        blockWithItem(materialEntry.oreBlock(), "material");
                    }
                }
                if (materialEntry.hasBlock()) {
                    String path = BuiltInRegistries.BLOCK.getKey(materialEntry.storageBlock().get()).getPath();
                    if (!blockStateExists(path)) {
                        blockWithItem(materialEntry.storageBlock(), "material");
                    }
                }
                if (materialEntry.hasFluid()) {
                    var fluidBlock = materialEntry.materialFluid().fluidBlock();
                    String path = BuiltInRegistries.BLOCK.getKey(fluidBlock.get()).getPath();
                    if (!blockStateExists(path)) {
                        // Particle-only model; the fluid renderer draws the liquid itself.
                        ResourceLocation particle = materialEntry.materialFluid().fluidType().get() instanceof MaterialFluidType mft
                                ? mft.getStillTexture()
                                : ResourceLocation.withDefaultNamespace("block/water_still");
                        ModelFile fluidModel = models().getBuilder(path)
                                .texture("particle", particle);
                        getVariantBuilder(fluidBlock.get())
                                .forAllStates(state -> ConfiguredModel.builder().modelFile(fluidModel).build());
                    }
                }
            }
        }
    }

    private void stateBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(modelFunc.apply(state)).build());
    }

    private void horizontalBlockWithItem(DeferredBlock<Block> deferredBlock) {
        Block block = deferredBlock.get();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        path = applySpecialRules(path);
        // models() only auto-prepends the block/ folder when the name has no slash.
        // applySpecialRules can introduce slashes (e.g. fission/controller), so prefix explicitly.
        String modelName = ModelProvider.BLOCK_FOLDER + "/" + path;
        boolean hasSide = textureExists("block/" + path + "/side");
        boolean hasFront = textureExists("block/" + path + "/front");
        boolean hasTop = textureExists("block/" + path + "/top");
        boolean hasBottom = textureExists("block/" + path + "/bottom");
        boolean hasSubfolderTextures = hasSide || hasFront || hasTop || hasBottom;

        // fallback: single texture without subfolder (e.g. block/example_machine.png)
        boolean hasSingleTexture = textureExists("block/" + path);

        ModelFile model;
        if (hasSubfolderTextures) {
            // use available subfolder textures, falling back to side for missing ones
            ResourceLocation side = rl("block/" + path + "/" + (hasSide ? "side" : "front"));
            ResourceLocation front = rl("block/" + path + "/" + (hasFront ? "front" : "side"));
            ResourceLocation top = hasTop ? rl("block/" + path + "/top") : side;
            ResourceLocation bottom = hasBottom ? rl("block/" + path + "/bottom") : top;
            model = models().orientableWithBottom(modelName, side, front, top, bottom);
        } else if (hasSingleTexture) {
            // single texture for all faces, use orientable with same texture everywhere
            ResourceLocation texture = rl("block/" + path);
            model = models().orientable(modelName, texture, texture, texture);
        } else {
            // Placeholder until textures exist; keeps datagen and rendering valid.
            ResourceLocation placeholder = ResourceLocation.withDefaultNamespace("block/iron_block");
            model = models().orientable(modelName, placeholder, placeholder, placeholder);
        }
        if(path.matches(".*glass|.*cell.*|.*photon.*|.*event_horizon_stabilizer.*|.*quantum_transformer.*")) {
            ((BlockModelBuilder) model).renderType(ResourceLocation.tryBuild("minecraft","cutout"));
        }
        horizontalBlock(block, model);
        itemModels().getBuilder("item/" + BuiltInRegistries.BLOCK.getKey(block).getPath()).parent(model);
    }

    private String applySpecialRules(String path) {
        if (path.contains("fission_reactor_")) {
            path = path.replace("fission_reactor_", "fission/");
            if (path.contains("port")) {
                path += "/front";
            }
        }
        if (path.contains("fusion_reactor_")) {
            path = path.replace("fusion_reactor_", "fusion/");
        }
        return path;
    }

    private void processorBlock(DeferredBlock<Block> deferredBlock) {
        Block block = deferredBlock.get();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();

        ResourceLocation side = rl("block/processor/side");
        ResourceLocation top = rl("block/processor/top");
        ResourceLocation bottom = rl("block/processor/bottom");
        ResourceLocation front = rl("block/processor/" + path);
        ResourceLocation frontPowered = textureExists("block/processor/" + path + "_powered")
                ? rl("block/processor/" + path + "_powered") : front;

        ModelFile idle = models().orientableWithBottom(path, side, front, bottom, top);
        ModelFile powered = models().orientableWithBottom(path + "_powered", side, frontPowered, bottom, top);

        getVariantBuilder(block).forAllStates(state -> {
            boolean on = state.getValue(UniversalProcessorBlock.POWERED);
            int yRot = switch (state.getValue(UniversalProcessorBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(on ? powered : idle).rotationY(yRot).build();
        });

        itemModels().getBuilder("item/" + path).parent(idle);
    }



    @Override
    public @NonNull ResourceLocation blockTexture(Block block) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        String path = name.getPath();
        path = applySpecialRules(path);
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + path);
    }

    private void blockWithItem(DeferredBlock<Block> deferredBlock) {
        Block block = deferredBlock.get();
        ModelFile model = cubeAll(deferredBlock.get());
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if(path.matches(".*glass|.*cell.*|.*photon.*|.*event_horizon_stabilizer.*|.*quantum_transformer.*")) {
            ((BlockModelBuilder) model).renderType(ResourceLocation.tryBuild("minecraft","cutout"));
        }
        simpleBlock(block, model);
        itemModels().getBuilder("item/" + path).parent(model);
    }

    private void blockWithItem(DeferredBlock<Block> deferredBlock, String subfolder) {
        Block block = deferredBlock.get();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "block/" + subfolder + "/" + path);
        ModelFile model = models().cubeAll(path, texture);
        simpleBlock(block, model);
        itemModels().getBuilder("item/" + path).parent(model);
    }

    private boolean isFusionBlock(String path) {
        return path.startsWith("fusion_reactor_")
                || path.endsWith("_electromagnet")
                || path.endsWith("_electromagnet_slope")
                || path.endsWith("_rf_amplifier");
    }

    private void fusionBlock(DeferredBlock<Block> deferredBlock, String path) {
        Block block = deferredBlock.get();

        if (path.equals("fusion_reactor_core_proxy")) {
            ModelFile proxy = models().getExistingFile(rl("block/fusion/core_proxy"));
            simpleBlock(block, proxy);
            itemModels().getBuilder("item/" + path).parent(proxy);
            return;
        }

        if (path.endsWith("_electromagnet_slope")) {
            ModelFile slope = models().getExistingFile(rl("block/electromagnet/" + path));
            getVariantBuilder(block).forAllStates(state -> {
                FrontAndTop dir = state.getValue(BlockStateProperties.ORIENTATION);
                return ConfiguredModel.builder()
                        .modelFile(slope)
                        .rotationX(dir.front() == Direction.DOWN ? 180 : 0)
                        .rotationY((((int) dir.top().toYRot()) + 180) % 360)
                        .build();
            });
            itemModels().getBuilder("item/" + path).parent(slope);
            return;
        }

        ResourceLocation texture;
        boolean glass = path.equals("fusion_reactor_glass");
        if (path.equals("fusion_reactor_casing")) {
            texture = rl("block/fusion/fusion_reactor_casing");
        } else if (glass) {
            texture = rl("block/fusion/fusion_reactor_casing_glass");
        } else if (path.equals("fusion_reactor_connector")) {
            texture = rl("block/fusion/fusion_reactor_connector");
        } else if (path.equals("fusion_reactor_core")) {
            texture = rl("block/fusion/fusion_core/core_centre");
        } else if (path.endsWith("_electromagnet")) {
            texture = rl("block/electromagnet/" + path);
        } else if (path.endsWith("_rf_amplifier")) {
            texture = rl("block/rf_amplifier/" + path);
        } else {
            texture = rl("block/fusion/fusion_reactor_casing");
        }

        ModelFile model = models().cubeAll(path, texture);
        if (glass) {
            ((BlockModelBuilder) model).renderType(ResourceLocation.tryBuild("minecraft", "cutout"));
        }
        simpleBlock(block, model);
        if (path.equals("fusion_reactor_core")) {
            return; // custom item model shipped in main resources - don't overwrite it
        }
        itemModels().getBuilder("item/" + path).parent(model);
    }

    private void explBlock(DeferredBlock<Block> deferredBlock) {
        Block block = deferredBlock.get();
        ModelFile model = models().getExistingFile(rl("block/expl"));
        simpleBlock(block, model);
        // custom block/item models shipped in main resources - don't overwrite them
    }

    private void heatSinkBlock(DeferredBlock<HeatSinkBlock> deferredBlock) {
        Block block = deferredBlock.get();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        String textureName = path.endsWith("_heat_sink") ? path.substring(0, path.length() - "_heat_sink".length()) : path;
        ResourceLocation texture = rl("block/heat_sink/" + textureName);
        ModelFile model = models().cubeAll(path, texture);
        simpleBlock(block, model);
        itemModels().getBuilder("item/" + path).parent(model);
    }
}
