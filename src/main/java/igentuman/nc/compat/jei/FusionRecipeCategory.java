package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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

import java.util.List;

import static igentuman.nc.util.TextUtils.scaledFormat;

/** JEI category for fusion fuel recipes with two fluid inputs and up to four fluid outputs. */
public class FusionRecipeCategory implements IRecipeCategory<FusionRecipe> {

    public static final RecipeType<FusionRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "fusion_reactor", FusionRecipe.class);

    private static final int WIDTH = 142;
    private static final int HEIGHT = 62;
    private static final int ENERGY_TEXT_Y = 49;
    private static final int[][] INPUT_POSITIONS = {{6, 4}, {28, 4}};
    private static final int[][] OUTPUT_POSITIONS = {{100, 4}, {122, 4}, {100, 26}, {122, 26}};

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public FusionRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("fusion_reactor_core").item().get()));
    }

    @Override
    public RecipeType<FusionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("screen.nuclearcraft.fusion_reactor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FusionRecipe recipe, IFocusGroup focuses) {
        addInput(builder, recipe.inputA(), INPUT_POSITIONS[0]);
        addInput(builder, recipe.inputB(), INPUT_POSITIONS[1]);

        int outputs = Math.min(recipe.outputs().size(), OUTPUT_POSITIONS.length);
        for (int i = 0; i < outputs; i++) {
            FluidOutput output = recipe.outputs().get(i);
            IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT,
                    OUTPUT_POSITIONS[i][0] + 1, OUTPUT_POSITIONS[i][1] + 1);
            List<FluidStack> members = output.members();
            for (FluidStack stack : members) {
                outputSlot.addFluidStack(stack.getFluid(), stack.getAmount());
            }
            if (!members.isEmpty()) {
                outputSlot.setFluidRenderer(Math.max(1, members.getFirst().getAmount()), false, 16, 16);
            }
        }
    }

    private static void addInput(IRecipeLayoutBuilder builder, SizedFluidIngredient ingredient, int[] position) {
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, position[0] + 1, position[1] + 1);
        for (FluidStack stack : ingredient.getFluids()) {
            inputSlot.addFluidStack(stack.getFluid(), ingredient.amount());
        }
        inputSlot.setFluidRenderer(Math.max(1, ingredient.amount()), false, 16, 16);
    }

    @Override
    public void draw(FusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        for (int[] position : INPUT_POSITIONS) {
            slot.draw(graphics, position[0], position[1]);
        }
        for (int i = 0; i < Math.min(recipe.outputs().size(), OUTPUT_POSITIONS.length); i++) {
            slot.draw(graphics, OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1]);
        }
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), 70, 9, 0x404040, false);
        Component energy = Component.translatable("gui.nuclearcraft.fusion.energy", scaledFormat(recipe.energy()))
                .withStyle(ChatFormatting.DARK_GRAY);
        int energyX = (WIDTH - Minecraft.getInstance().font.width(energy)) / 2;
        graphics.drawString(Minecraft.getInstance().font, energy, energyX, ENERGY_TEXT_Y, 0x404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, FusionRecipe recipe, IRecipeSlotsView recipeSlotsView,
                           double mouseX, double mouseY) {
        if (mouseX < 49 || mouseX > 97 || mouseY < 4 || mouseY > 46) return;
        tooltip.add(Component.translatable("gui.nuclearcraft.fusion.energy", scaledFormat(recipe.energy())));
        tooltip.add(Component.translatable("gui.nuclearcraft.fusion.temperature",
                scaledFormat(recipe.optimalTemperature())));
        tooltip.add(Component.translatable("gui.nuclearcraft.recipe.time", recipe.processTime()));
    }
}
