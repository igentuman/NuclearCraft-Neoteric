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
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

/** JEI category displaying a heat exchanger recipe with signed heat transfer. */
public class HeatExchangerRecipeCategory implements IRecipeCategory<RecipeHolder<HeatExchangerRecipe>> {

    public static final IRecipeHolderType<HeatExchangerRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("heat_exchanger"));

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
    public IRecipeHolderType<HeatExchangerRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<HeatExchangerRecipe> holder, IFocusGroup focuses) {
        HeatExchangerRecipe recipe = holder.value();
        SizedFluidIngredient in = recipe.input();
        IRecipeSlotBuilder inSlot = builder.addSlot(RecipeIngredientRole.INPUT, 7, 5);
        JeiRecipeSlots.addFluids(inSlot, in);
        inSlot.setFluidRenderer(Math.max(1, in.amount()), false, 16, 16);

        IRecipeSlotBuilder outSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 23, 5);
        FluidStack out = recipe.output().resolve();
        if (!out.isEmpty()) {
            JeiRecipeSlots.addFluid(outSlot, out);
            outSlot.setFluidRenderer(out.getAmount(), false, 16, 16);
        }
    }

    @Override
    public void draw(RecipeHolder<HeatExchangerRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        HeatExchangerRecipe recipe = holder.value();
        slot.draw(graphics, 6, 4);
        slot.draw(graphics, WIDTH - 24, 4);
        graphics.text(Minecraft.getInstance().font, Component.literal("→"), WIDTH / 2 - 3, 8, 0xFF404040, false);
        Component heat = recipe.heat() >= 0
                ? Component.translatable("gui.nuclearcraft.heat_exchanger.heat_add", recipe.heat())
                : Component.translatable("gui.nuclearcraft.heat_exchanger.heat_remove", -recipe.heat());
        graphics.text(Minecraft.getInstance().font, heat.copy().withStyle(ChatFormatting.DARK_GRAY), 4, 26, 0xFF404040, false);
    }
}
