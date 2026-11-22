package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.compat.jei.particle.ParticleType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class ParticleInfoCategory implements IRecipeCategory<ParticleInfoRecipe> {

    public static final IRecipeType<ParticleInfoRecipe> TYPE =
            IRecipeType.create(NuclearCraft.MODID, "particle_info", ParticleInfoRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;

    private final IDrawable icon;

    public ParticleInfoCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(ParticleType.PARTICLE, new ParticleStack(rl("proton"), 1, 0, 0));
    }

    @Override
    public IRecipeType<ParticleInfoRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category." + NuclearCraft.MODID + ".particle_info");
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
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleInfoRecipe recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(ParticleType.PARTICLE, recipe.getStack());
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).add(ParticleType.PARTICLE, recipe.getStack());
    }

    @Override
    public void draw(ParticleInfoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        DecimalFormat df = new DecimalFormat("#.##");
        ParticleDefinition definition = recipe.getDefinition();

        graphics.text(font, __(definition.translationKey()), 20, 2, 0xFFFFFFFF);
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.7F, 0.7F);
        graphics.textWithWordWrap(font, __("gui.nuclearcraft.jei.particle.mass",
                scaledFormat(definition.massMeV()) + " MeV/c^2"), 0, 30, 220, 0xFF404040);
        graphics.textWithWordWrap(font, __("gui.nuclearcraft.jei.particle.charge", df.format(definition.charge())), 0, 40, 220, 0xFF404040);
        graphics.textWithWordWrap(font, __("gui.nuclearcraft.jei.particle.spin", df.format(definition.spin())), 0, 50, 220, 0xFF404040);
        graphics.textWithWordWrap(font, __("gui.nuclearcraft.jei.particle.colour", recipe.interactsWithStrong()), 0, 60, 220, 0xFF404040);
        graphics.textWithWordWrap(font, __("gui.nuclearcraft.jei.particle.weak", recipe.interactsWithWeak()), 0, 70, 220, 0xFF404040);
        graphics.textWithWordWrap(font, __("nuclearcraft.particle." + recipe.getParticleId().getPath() + ".desc"), 0, 90, 220, 0xFF404040);
        graphics.pose().popMatrix();
    }
}
