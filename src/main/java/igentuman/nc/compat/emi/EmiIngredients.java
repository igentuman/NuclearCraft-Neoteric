package igentuman.nc.compat.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

import java.util.List;

/** Builds EMI stacks from native ingredient displays, keeping counts, amounts and component patches. */
final class EmiIngredients {
    private EmiIngredients() {}

    /** Display context backed by the client level's registries, or the static registries before join. */
    static ContextMap context() {
        Minecraft mc = Minecraft.getInstance();
        RegistryAccess registries = mc.level != null
                ? mc.level.registryAccess()
                : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return new ContextMap.Builder()
                .withParameter(SlotDisplayContext.REGISTRIES, registries)
                .create(SlotDisplayContext.CONTEXT);
    }

    static List<ItemStack> itemOptions(Ingredient ingredient, int count) {
        return ingredient.display().resolveForStacks(context()).stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(count))
                .toList();
    }

    static List<FluidStack> fluidOptions(FluidIngredient ingredient, int amount) {
        return ingredient.display().resolve(context(), FluidStackContentsFactory.INSTANCE)
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithAmount(amount))
                .toList();
    }

    static EmiIngredient items(Ingredient ingredient, int count) {
        return ingredientOf(itemOptions(ingredient, count).stream().map(EmiStack::of).toList());
    }

    static EmiIngredient items(SizedIngredient ingredient) {
        return items(ingredient.ingredient(), ingredient.count());
    }

    static EmiIngredient fluids(SizedFluidIngredient ingredient) {
        return ingredientOf(fluidOptions(ingredient.ingredient(), ingredient.amount()).stream()
                .map(EmiIngredients::fluid).toList());
    }

    static EmiStack fluid(FluidStack stack) {
        return stack.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount());
    }

    static EmiStack item(ItemStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack);
    }

    static EmiIngredient itemMembers(List<ItemStack> members) {
        return ingredientOf(members.stream().map(EmiIngredients::item).toList());
    }

    static EmiIngredient fluidMembers(List<FluidStack> members) {
        return ingredientOf(members.stream().map(EmiIngredients::fluid).toList());
    }

    static EmiStack firstItem(List<ItemStack> members) {
        return members.isEmpty() ? EmiStack.EMPTY : item(members.getFirst());
    }

    static EmiStack firstFluid(List<FluidStack> members) {
        return members.isEmpty() ? EmiStack.EMPTY : fluid(members.getFirst());
    }

    static EmiIngredient ingredientOf(List<EmiStack> stacks) {
        List<EmiStack> present = stacks.stream().filter(stack -> !stack.isEmpty()).toList();
        if (present.isEmpty()) return EmiStack.EMPTY;
        if (present.size() == 1) return present.getFirst();
        return EmiIngredient.of(present);
    }
}
