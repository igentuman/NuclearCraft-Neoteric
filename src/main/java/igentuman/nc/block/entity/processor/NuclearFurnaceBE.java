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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.util.TagUtil.getItemsByTagKey;

public class NuclearFurnaceBE extends NCProcessorBE<NuclearFurnaceBE.Recipe> {
    public NuclearFurnaceBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.NUCLEAR_FURNACE);
    }

    @NBTField
    public int burnTime = 400;

    @Override
    public boolean canProcessRecipe() {
        List<Item> ingots = getItemsByTagKey("forge:ingots/uranium");
        boolean hasFuel = ingots.contains(contentHandler.itemHandler.getStackInSlot(0).getItem()) || ingots.contains(contentHandler.itemHandler.getStackInSlot(1).getItem());
        if(hasFuel) {
            burnTime--;
            if(burnTime <= 0) {
                burnTime = 400;
                if(ingots.contains(contentHandler.itemHandler.getStackInSlot(0).getItem())) {
                    contentHandler.itemHandler.extractItem(0, 1, false);
                } else if(ingots.contains(contentHandler.itemHandler.getStackInSlot(1).getItem())) {
                    contentHandler.itemHandler.extractItem(1, 1, false);
                }
            }
        }
        return hasFuel;
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
