package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.fission.BoilingRecipe;
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
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public class BoilingRecipeCategory implements IRecipeCategory<BoilingRecipe> {

    public static final RecipeType<BoilingRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "fission_boiling", BoilingRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public BoilingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModEntries.get("fission_reactor_controller").item().get()));
    }

    @Override
    public RecipeType<BoilingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.fission_boiling");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BoilingRecipe recipe, IFocusGroup focuses) {
        SizedFluidIngredient in = recipe.input();
        IRecipeSlotBuilder inSlot = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        for (FluidStack fs : in.getFluids()) {
            inSlot.addFluidStack(fs.getFluid(), in.amount());
        }
        inSlot.setFluidRenderer(Math.max(1, in.amount()), false, 16, 16);

        IRecipeSlotBuilder outSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5);
        int outAmount = 0;
        for (FluidStack fs : recipe.output().members()) {
            outSlot.addFluidStack(fs.getFluid(), fs.getAmount());
            outAmount = fs.getAmount();
        }
        if (outAmount > 0) outSlot.setFluidRenderer(outAmount, false, 16, 16);
    }

    @Override
    public void draw(BoilingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8, 0x404040, false);
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("gui.nuclearcraft.fission_boiling.heat", recipe.heatRequired())
                        .withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0x404040, false);
    }
}
