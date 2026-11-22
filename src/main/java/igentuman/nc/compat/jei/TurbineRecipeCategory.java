package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.turbine.TurbineRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** JEI category for multiblock turbine fluid conversion recipes. */
public class TurbineRecipeCategory implements IRecipeCategory<RecipeHolder<TurbineRecipe>> {

    public static final IRecipeHolderType<TurbineRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("turbine"));

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public TurbineRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("turbine_controller").item().get()));
    }

    @Override
    public IRecipeHolderType<TurbineRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("screen.nuclearcraft.turbine");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TurbineRecipe> holder, IFocusGroup focuses) {
        TurbineRecipe recipe = holder.value();
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        JeiRecipeSlots.addFluids(input, recipe.input());
        input.setFluidRenderer(Math.max(1, recipe.input().amount()), false, 16, 16);

        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5);
        List<FluidStack> members = recipe.output().members();
        for (FluidStack stack : members) {
            JeiRecipeSlots.addFluid(output, stack);
        }
        if (!members.isEmpty()) {
            output.setFluidRenderer(Math.max(1, members.getFirst().getAmount()), false, 16, 16);
        }
    }

    @Override
    public void draw(RecipeHolder<TurbineRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {
        TurbineRecipe recipe = holder.value();
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.text(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8,
                0xFF404040, false);
        graphics.text(Minecraft.getInstance().font,
                Component.translatable("gui.nuclearcraft.turbine.power", Math.round(recipe.powerModifier() * 100) + "%")
                        .withStyle(ChatFormatting.DARK_GRAY), 4, 27, 0xFF404040, false);
    }
}
