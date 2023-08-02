package igentuman.nc.recipes.handler;

import igentuman.nc.block.entity.IBlockEntityHandler;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.InputRecipeCache.DoubleItem;
import igentuman.nc.recipes.lookup.IDoubleRecipeLookupHandler;
import igentuman.nc.recipes.type.TwoItemStackToItemStackRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwoItemToItemRecipeHandler implements IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler<TwoItemStackToItemStackRecipe>, IBlockEntityHandler {

    private NuclearCraftBE blockEntity;

    public TwoItemToItemRecipeHandler(NuclearCraftBE blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public @Nullable TwoItemStackToItemStackRecipe getRecipe() {
        return findFirstRecipe(blockEntity.getItemInventory());
    }

    @Override
    public @NotNull INcRecipeTypeProvider<TwoItemStackToItemStackRecipe, DoubleItem<TwoItemStackToItemStackRecipe>> getRecipeType() {
        return NcRecipeType.TWO_ITEM_RECIPES.get(blockEntity.getName());
    }

    @Override
    public void onContentsChanged() {

    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
