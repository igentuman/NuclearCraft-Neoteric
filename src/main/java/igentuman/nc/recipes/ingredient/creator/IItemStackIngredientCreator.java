package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

import java.util.Objects;


@NothingNullByDefault
public interface IItemStackIngredientCreator extends IIngredientCreator<Item, ItemStack, ItemStackIngredient> {

    @Override
    default ItemStackIngredient from(ItemStack instance) {
        Objects.requireNonNull(instance, "ItemStackIngredients cannot be created from a null ItemStack.");
        return from(instance, instance.getCount());
    }

    /**
     * Creates an Item Stack Ingredient that matches a given item stack with a specified amount.
     *
     * @param stack  Item stack to match.
     * @param amount Amount needed.
     *
     * @apiNote If the amount needed is the same as the stack's size, {@link #from(ItemStack)} can be used instead.
     */
    default ItemStackIngredient from(ItemStack stack, int amount) {
        Objects.requireNonNull(stack, "ItemStackIngredients cannot be created from a null ItemStack.");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty stack.");
        }
        stack = stack.copy();
        //Support NBT that is on the stack in case it matters
        // Note: Only bother making it an NBT ingredient if the stack has NBT, otherwise there is no point in doing the extra checks
        Ingredient ingredient = Ingredient.of(stack);
        return from(stack, amount);
    }

    default ItemStackIngredient from(IItemProvider item) {
        return from(item, 1);
    }

    default ItemStackIngredient from(IItemProvider item, int amount) {
        return from(new ItemStack(item), amount);
    }

    @Override
    default ItemStackIngredient from(Item item, int amount) {
        return from((IItemProvider) item, amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given Item tag.
     *
     * @param tag Tag to match.
     */
    default ItemStackIngredient from(Tags.IOptionalNamedTag<Item> tag) {
        return from(tag, 1);
    }

    @Override
    default ItemStackIngredient from(Tags.IOptionalNamedTag<Item> tag, int amount) {
        Objects.requireNonNull(tag, "ItemStackIngredients cannot be created from a null tag.");
        return from(tag, amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient.
     *
     * @param ingredient Ingredient to match.
     */

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     */
    ItemStackIngredient from(Ingredient ingredient, int amount);
}