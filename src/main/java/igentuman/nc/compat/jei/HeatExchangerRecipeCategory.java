package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe;
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

/** JEI category displaying a heat exchanger recipe with signed heat transfer. */
public class HeatExchangerRecipeCategory implements IRecipeCategory<HeatExchangerRecipe> {

    public static final RecipeType<HeatExchangerRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "heat_exchanger", HeatExchangerRecipe.class);

    private static final int WIDTH = 124;
    private static final int HEIGHT = 46;

    private final IDrawable background;
    private final IDrawableStatic slot;
    private final IDrawable icon;

    public HeatExchangerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModEntries.get("heat_exchanger_controller").item().get()));
    }

    @Override
    public RecipeType<HeatExchangerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.nuclearcraft.heat_exchanger");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HeatExchangerRecipe recipe, IFocusGroup focuses) {
        SizedFluidIngredient in = recipe.input();
        IRecipeSlotBuilder inSlot = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        for (FluidStack fs : in.getFluids()) {
            inSlot.addFluidStack(fs.getFluid(), in.amount());
        }
        inSlot.setFluidRenderer(Math.max(1, in.amount()), false, 16, 16);

        IRecipeSlotBuilder outSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5);
        FluidStack out = recipe.output().resolve();
        if (!out.isEmpty()) {
            outSlot.addFluidStack(out.getFluid(), out.getAmount());
            outSlot.setFluidRenderer(out.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(HeatExchangerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8, 0x404040, false);
        Component heat = recipe.heat() >= 0
                ? Component.translatable("gui.nuclearcraft.heat_exchanger.heat_add", recipe.heat())
                : Component.translatable("gui.nuclearcraft.heat_exchanger.heat_remove", -recipe.heat());
        graphics.drawString(Minecraft.getInstance().font, heat.copy().withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0x404040, false);
    }
}
