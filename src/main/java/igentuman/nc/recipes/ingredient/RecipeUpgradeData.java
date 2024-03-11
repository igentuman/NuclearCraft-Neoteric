package igentuman.nc.recipes.ingredient;

import igentuman.nc.util.ItemDataUtils;
import igentuman.nc.util.annotation.ParametersAreNotNullByDefault;
import net.minecraft.nbt.ListNBT;
import net.minecraft.item.ItemStack;

import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

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
    public static <TYPE extends RecipeUpgradeData<TYPE>> TYPE getContainerUpgradeData(@NotNull ItemStack stack, String key, Function<ListNBT, TYPE> creator) {
        ListNBT containers = ItemDataUtils.getList(stack, key);
        return containers.isEmpty() ? null : creator.apply(containers);
    }

}