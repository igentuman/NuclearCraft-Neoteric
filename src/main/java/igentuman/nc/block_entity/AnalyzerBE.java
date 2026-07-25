package igentuman.nc.block_entity;

import igentuman.nc.item.CrystalAnalysis;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.recipe.OreVeinRecipe;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.insitu_leaching.OreVeinProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.HashMap;
import java.util.Map;

/** Analyzer processor BE; converts paper/maps/crystals into analyzed variants with vein NBT injected onto outputs. */
public class AnalyzerBE extends UniversalProcessorBE {

    protected final Map<Long, OreVeinRecipe> veinsCache = new HashMap<>();

    @NBTField
    protected BlockPos alreadySearched = null;

    @NBTField
    protected ItemStack savedInput = ItemStack.EMPTY;

    public AnalyzerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        if (worldPosition.equals(alreadySearched)) {
            return;
        }
        if (recipeInfo.recipe == null && contentHandler.hasItemCapability()) {
            ItemStack input = contentHandler.getItemHandler().getStackInSlot(0);
            if (!input.isEmpty()) {
                savedInput = input.copy();
            }
        }
        super.serverTick();
        if (recipeInfo.justProduced) {
            handleRecipeOutput();
        }
    }

    /** Injects vein name, position, and analysis flag NBT onto output items when the recipe completes. */
    protected void handleRecipeOutput() {
        if (!(recipeInfo.lastRecipe instanceof UniversalProcessorRecipe upr)) return;
        if (upr.getItemInputs().isEmpty()) return;
        SizedIngredient input0 = upr.getItemInputs().get(0);
        int outputSlot = 1;

        if (input0.test(new ItemStack(Items.PAPER))) {
            handlePaperOutput(outputSlot);
        } else if (input0.test(new ItemStack(Items.FILLED_MAP))) {
            handleMapOutput(outputSlot);
        } else if (savedInput.getItem() instanceof ResoniteCrystalItem) {
            handleCrystalOutput(outputSlot);
        }
    }

    private void handlePaperOutput(int outputSlot) {
        OreVeinRecipe vein = getVein();
        alreadySearched = worldPosition;
        ItemStack output = contentHandler.getItemHandler().getStackInSlot(outputSlot);
        if (output.isEmpty()) return;
        CompoundTag tag = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (vein == null) {
            tag.putString("vein", "nc.ore_vein.none");
        } else {
            tag.putString("vein", "nc.ore_vein." + vein.getId().getPath());
        }
        tag.putLong("pos", worldPosition.asLong());
        tag.putBoolean("is_nc_analyzed", true);
        output.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void handleCrystalOutput(int outputSlot) {
        ItemStack output = contentHandler.getItemHandler().getStackInSlot(outputSlot);
        if (output.isEmpty() || !(output.getItem() instanceof ResoniteCrystalItem)) return;
        if (!ResoniteCrystalItem.isAnalyzed(output) && level != null) {
            CrystalAnalysis.applyAnalysis(output, level.getRandom());
            setChanged();
        }
    }

    private void handleMapOutput(int outputSlot) {
        ItemStack output = contentHandler.getItemHandler().getStackInSlot(outputSlot);
        if (output.isEmpty()) return;
        if (!savedInput.isEmpty() && savedInput.has(DataComponents.MAP_ID)) {
            output.set(DataComponents.MAP_ID, savedInput.get(DataComponents.MAP_ID));
        }
        CompoundTag tag = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("is_nc_analyzed", true);
        output.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        savedInput = ItemStack.EMPTY;
    }

    /** Queries the OreVeinProvider for this chunk's vein, using a cache to avoid repeated lookups. */
    protected OreVeinRecipe getVein() {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        long pos = ChunkPos.asLong(worldPosition);
        if (!veinsCache.containsKey(pos)) {
            ChunkPos chunkPos = new ChunkPos(worldPosition);
            veinsCache.put(pos, OreVeinProvider.get(serverLevel).getVeinForChunk(chunkPos.x, chunkPos.z));
        }
        return veinsCache.get(pos);
    }
}
