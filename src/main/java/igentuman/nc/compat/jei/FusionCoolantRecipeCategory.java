package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** JEI category for fusion reactor coolant conversion recipes. */
public class FusionCoolantRecipeCategory implements IRecipeCategory<FusionCoolantRecipe> {

    public static final RecipeType<FusionCoolantRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "fusion_coolant", FusionCoolantRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public FusionCoolantRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("fusion_reactor_core").item().get()));
    }

    @Override
    public RecipeType<FusionCoolantRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.fusion_coolant");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FusionCoolantRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        for (FluidStack stack : recipe.input().getFluids()) {
            input.addFluidStack(stack.getFluid(), recipe.input().amount());
        }
        input.setFluidRenderer(Math.max(1, recipe.input().amount()), false, 16, 16);

        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5);
        List<FluidStack> members = recipe.output().members();
        for (FluidStack stack : members) {
            output.addFluidStack(stack.getFluid(), stack.getAmount());
        }
        if (!members.isEmpty()) {
            output.setFluidRenderer(Math.max(1, members.getFirst().getAmount()), false, 16, 16);
        }
    }

    @Override
    public void draw(FusionCoolantRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8,
                0x404040, false);
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("gui.nuclearcraft.fusion_coolant.cooling", recipe.coolingRate())
                        .withStyle(ChatFormatting.DARK_GRAY), 4, 27, 0x404040, false);
    }
}
