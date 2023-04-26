package igentuman.nc.recipes.handler;

import igentuman.nc.block.entity.IBlockEntityHandler;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.InputRecipeCache.*;
import igentuman.nc.recipes.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemToItemRecipeHandler implements ItemRecipeLookupHandler<ItemStackToItemStackRecipe>, IBlockEntityHandler {

    private NuclearCraftBE blockEntity;

    public ItemToItemRecipeHandler(NuclearCraftBE blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public @Nullable ItemStackToItemStackRecipe getRecipe() {
        return findFirstRecipe(blockEntity.getItemInventory());
    }

    @Override
    public @NotNull INcRecipeTypeProvider<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return NcRecipeType.RECIPES.get(blockEntity.getName());
    }

    @Override
    public void onContentsChanged() {

    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
