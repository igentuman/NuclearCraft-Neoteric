package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.PlacementRuleRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PlacementRuleCategory implements IRecipeCategory<PlacementRuleRecipe> {
    public static final IRecipeType<PlacementRuleRecipe> HEAT_SINK =
            IRecipeType.create(NuclearCraft.MODID, "heat_sink_placement", PlacementRuleRecipe.class);
    public static final IRecipeType<PlacementRuleRecipe> COOLER =
            IRecipeType.create(NuclearCraft.MODID, "cooler_placement", PlacementRuleRecipe.class);

    private final IRecipeType<PlacementRuleRecipe> type;
    private final IDrawable icon;

    public PlacementRuleCategory(IGuiHelper guiHelper, IRecipeType<PlacementRuleRecipe> type, String iconName) {
        this.type = type;
        var entry = ModEntries.get(iconName);
        ItemStack stack = entry != null && entry.hasItem() ? new ItemStack(entry.item().get()) : ItemStack.EMPTY;
        if (stack.isEmpty() && iconName.equals("empty_heat_sink")) {
            var sink = ModEntries.HEAT_SINKS.get("empty");
            if (sink != null) stack = new ItemStack(sink.block().get());
        }
        this.icon = guiHelper.createDrawableItemStack(stack);
    }

    @Override public IRecipeType<PlacementRuleRecipe> getRecipeType() { return type; }
    @Override public Component getTitle() {
        return Component.translatable("jei.category." + NuclearCraft.MODID + "." + type.getUid().getPath());
    }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 122; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PlacementRuleRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 4).add(recipe.part());
        for (int i = 0; i < recipe.conditions().size(); i++) {
            var alternatives = recipe.conditions().get(i).alternatives();
            if (!alternatives.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 4, 29 + i * 22).addItemStacks(alternatives);
            }
        }
    }

    @Override
    public void draw(PlacementRuleRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.text(font, Component.translatable("heat_sink.heat.descr", recipe.cooling()), 27, 8, 0xFFCC8A20, false);
        for (int i = 0; i < recipe.conditions().size(); i++) {
            graphics.text(font, recipe.conditions().get(i).description(), 27, 34 + i * 22, 0xFF404040, false);
        }
    }
}
