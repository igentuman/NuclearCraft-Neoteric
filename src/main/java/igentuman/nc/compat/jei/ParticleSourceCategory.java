package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.compat.ParticleSourceRecipe;
import igentuman.nc.compat.jei.particle.ParticleType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static igentuman.nc.NuclearCraft.rl;

public class ParticleSourceCategory implements IRecipeCategory<ParticleSourceRecipe> {

    public static final RecipeType<ParticleSourceRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "particle_source_info", ParticleSourceRecipe.class);

    private final IDrawable icon;
    private final IDrawable slot;

    public ParticleSourceCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(ParticleType.PARTICLE, new ParticleStack(rl("alpha"), 1, 0, 0));
        slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<ParticleSourceRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.nuclearcraft.particle_source_info");
    }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public int getWidth() { return 90; }

    @Override
    public int getHeight() { return 40; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleSourceRecipe recipe, IFocusGroup focuses) {
        var input = builder.addSlot(RecipeIngredientRole.INPUT, 11, 11);
        if (!recipe.item().isEmpty()) {
            input.addItemStack(recipe.item());
        } else {
            input.addFluidStack(recipe.fluid().getFluid(), recipe.fluid().getAmount())
                    .setFluidRenderer(recipe.fluid().getAmount(), false, 16, 16);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 63, 11)
                .addIngredient(ParticleType.PARTICLE, recipe.output());
    }

    @Override
    public void draw(ParticleSourceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 10, 10);
        slot.draw(graphics, 62, 10);
        graphics.drawString(Minecraft.getInstance().font, "→", 42, 14, 0x404040, false);
    }
}
