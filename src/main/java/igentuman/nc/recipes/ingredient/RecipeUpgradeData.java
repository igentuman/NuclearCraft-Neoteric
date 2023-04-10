package igentuman.nc.recipes.ingredient;

import igentuman.nc.util.ItemDataUtils;
import igentuman.nc.util.annotation.ParametersAreNotNullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@ParametersAreNotNullByDefault
public interface RecipeUpgradeData<TYPE extends RecipeUpgradeData<TYPE>> {

    @Nullable
    TYPE merge(TYPE other);

    /**
     * @return {@code false} if it failed to apply to the stack due to being invalid
     */
    boolean applyToStack(ItemStack stack);


    @Nullable
    private static <TYPE extends RecipeUpgradeData<TYPE>> TYPE getContainerUpgradeData(@NotNull ItemStack stack, String key, Function<ListTag, TYPE> creator) {
        ListTag containers = ItemDataUtils.getList(stack, key);
        return containers.isEmpty() ? null : creator.apply(containers);
    }

}