package igentuman.nc.datagen;

import igentuman.nc.NuclearCraft;
import igentuman.nc.setup.NCBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;

public class NCBlockStates extends BlockStateProvider {

    public NCBlockStates(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerPortal();
        registerOres();
        registerBlocks();
    }

    private void registerBlocks() {
        for(String ore: NCBlocks.NC_BLOCKS.keySet()) {
            simpleBlock(NCBlocks.NC_BLOCKS.get(ore).get(), model(NCBlocks.NC_BLOCKS.get(ore).get(), "material/block"));
        }
    }

    private void registerOres() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            simpleBlock(NCBlocks.ORE_BLOCKS.get(ore).get(), model(NCBlocks.ORE_BLOCKS.get(ore).get(), "ore"));
        }
    }

    private void registerPortal() {
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

}