package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import static igentuman.nc.util.TagUtil.getItemsByTagKey;

public class NuclearFurnaceBE extends NCProcessorBE<NuclearFurnaceBE.Recipe> {
    public NuclearFurnaceBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.NUCLEAR_FURNACE);
        contentHandler.itemHandler.setValidItemsForSlot(getFuelItems(), 1);
    }

    @NBTField
    public int burnTime = 0;
    private List<Item> ingots;

    private List<Item> getFuelItems() {
        if (ingots == null) {
            ingots = getItemsByTagKey("forge:ingots/uranium");
        }
        return ingots;
    }

    private void consumeFuel() {
        burnTime--;
        if (burnTime < 0) {

            boolean hasFuel = getFuelItems().contains(contentHandler.itemHandler.getStackInSlot(1).getItem());
            if (hasFuel) {
                burnTime = 400;
                if(getFuelItems().contains(contentHandler.itemHandler.getStackInSlot(1).getItem())) {
                    contentHandler.itemHandler.extractItem(1, 1, false);
                }
            }
        }

    }

    @Override
    public boolean canProcessRecipe() {
        consumeFuel();
        return burnTime > 0;
    }

    @Override
    public String getName() {
        return Processors.NUCLEAR_FURNACE;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.NUCLEAR_FURNACE;
        }
    }
}
