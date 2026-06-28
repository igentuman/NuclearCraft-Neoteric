package igentuman.nc.datagen;

import igentuman.nc.block.UniversalProcessorBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.MaterialFluidType;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;

import java.util.function.Function;

import static igentuman.nc.Main.MODID;
import static igentuman.nc.Main.rl;

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
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                String path = BuiltInRegistries.BLOCK.getKey(entry.block().get()).getPath();
                if (blockStateExists(path)) continue;
                if (entry.block().get() instanceof UniversalProcessorBlock) {
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
            model = models().orientableWithBottom(path, side, front, top, bottom);
        } else if (hasSingleTexture) {
            // single texture for all faces, use orientable with same texture everywhere
            ResourceLocation texture = rl("block/" + path);
            model = models().orientable(path, texture, texture, texture);
        } else {
            // Placeholder until textures exist; keeps datagen and rendering valid.
            ResourceLocation placeholder = ResourceLocation.withDefaultNamespace("block/iron_block");
            model = models().orientable(path, placeholder, placeholder, placeholder);
        }

        horizontalBlock(block, model);
        itemModels().getBuilder("item/" + path).parent(model);
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

    private void blockWithItem(DeferredBlock<Block> deferredBlock) {
        Block block = deferredBlock.get();
        ModelFile model = cubeAll(deferredBlock.get());
        simpleBlock(block, model);
        itemModels().getBuilder("item/" + BuiltInRegistries.BLOCK.getKey(block).getPath()).parent(model);
    }

    private void blockWithItem(DeferredBlock<Block> deferredBlock, String subfolder) {
        Block block = deferredBlock.get();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "block/" + subfolder + "/" + path);
        ModelFile model = models().cubeAll(path, texture);
        simpleBlock(block, model);
        itemModels().getBuilder("item/" + path).parent(model);
    }
}
