package igentuman.nc.compat.jei;

import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.network.PacketCrafterFillGrid;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static igentuman.nc.setup.entries.Crafter.ENGINEERS_CRAFTING_TABLE_MENU;
import static igentuman.nc.util.TextUtils.__;

public class EngineersCrafterRecipeTransferHandler implements IRecipeTransferHandler<EngineersCrafterContainer, RecipeHolder<CraftingRecipe>> {

    private final IRecipeTransferHandlerHelper helper;

    public EngineersCrafterRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<EngineersCrafterContainer> getContainerClass() {
        return EngineersCrafterContainer.class;
    }

    @Override
    public Optional<MenuType<EngineersCrafterContainer>> getMenuType() {
        return Optional.of(ENGINEERS_CRAFTING_TABLE_MENU.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(EngineersCrafterContainer container, RecipeHolder<CraftingRecipe> recipe,
                                                         IRecipeSlotsView recipeSlots, Player player,
                                                         boolean maxTransfer, boolean doTransfer) {
        if (!container.canAssemble(recipe.value())) {
            Component message = __("jei.tooltip.transfer.no.ingredients");
            List<IRecipeSlotView> missing = findMissingSlots(container.availableItems(), recipeSlots);
            return missing.isEmpty()
                    ? helper.createUserErrorWithTooltip(message)
                    : helper.createUserErrorForMissingSlots(message, missing);
        }
        if (doTransfer) {
            PacketDistributor.sendToServer(new PacketCrafterFillGrid(container.getBlockPos(), recipe.id()));
        }
        return null;
    }

    private static List<IRecipeSlotView> findMissingSlots(List<ItemStack> pool, IRecipeSlotsView recipeSlots) {
        List<IRecipeSlotView> inputs = new ArrayList<>();
        Map<IRecipeSlotView, List<ItemStack>> options = new HashMap<>();
        for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            List<ItemStack> stacks = slot.getItemStacks().filter(s -> !s.isEmpty()).toList();
            if (stacks.isEmpty()) continue;
            inputs.add(slot);
            options.put(slot, stacks);
        }
        inputs.sort(Comparator.comparingInt(slot -> options.get(slot).size()));

        List<IRecipeSlotView> missing = new ArrayList<>();
        for (IRecipeSlotView slot : inputs) {
            List<ItemStack> accepted = options.get(slot);
            ItemStack match = pool.stream()
                    .filter(have -> !have.isEmpty() && accepted.stream().anyMatch(want -> ItemStack.isSameItem(have, want)))
                    .findFirst()
                    .orElse(null);
            if (match == null) missing.add(slot);
            else match.shrink(1);
        }
        return missing;
    }
}
