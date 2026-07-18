package igentuman.nc.compat.jei;

import igentuman.nc.compat.jei.ingredient.ParticleType;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.util.Units;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.text.DecimalFormat;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/**
 * source https://github.com/Lach01298/QMD
 */
@SuppressWarnings("removal")
public class ParticleInfoCategory implements IRecipeCategory<ParticleRecipe> {
    public static final ResourceLocation UID = rl("particle_info");
    public static final RecipeType<ParticleRecipe> TYPE = RecipeType.create(MODID, "particle_info", ParticleRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public ParticleInfoCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 120);
        this.icon = guiHelper.createDrawableIngredient(ParticleType.Particle, new ParticleStack(Particles.proton));
        this.title = Component.translatable("jei.category." + MODID + ".particle_info");
    }
    
    @Override
    public RecipeType<ParticleRecipe> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return title;
    }
    
    @Override
    public IDrawable getBackground() {
        return background;
    }
    
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleRecipe recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(ParticleType.Particle, recipe.getIngredient());
        // 1.19.2 JEI: use addSlot(role, x, y) instead of addInputSlot()
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredient(ParticleType.Particle, recipe.getIngredient());
    }
    
    @Override
    public void draw(ParticleRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        // Draw structure name
        Font font = Minecraft.getInstance().font;
        DecimalFormat df = new DecimalFormat("#.##");
        font.draw(poseStack, __(recipe.getName()), 20f, 2f, 0xFFFFFFFF);
        poseStack.pushPose();
        poseStack.scale(0.7F, 0.7F, 1F);
        drawWordWrap(font, poseStack, __("gui.nuclearcraft.jei.particle.mass", Units.getSIFormat(recipe.getMass(),6,"eV/c^2")), 0, 30, 220, Color.darkGray.getRGB());
        drawWordWrap(font, poseStack, __("gui.nuclearcraft.jei.particle.charge", df.format(recipe.getCharge())), 0, 40, 220, Color.darkGray.getRGB());
        drawWordWrap(font, poseStack, __("gui.nuclearcraft.jei.particle.spin", recipe.getSpin()), 0, 50, 220, Color.darkGray.getRGB());
        drawWordWrap(font, poseStack, __("gui.nuclearcraft.jei.particle.colour", recipe.interactsWithStrong()), 0, 60, 220, Color.darkGray.getRGB());
        drawWordWrap(font, poseStack, __("gui.nuclearcraft.jei.particle.weak", recipe.interactsWithWeak()), 0, 70, 220, Color.darkGray.getRGB());
        drawWordWrap(font, poseStack, __("nuclearcraft.particle."+recipe.output.getParticle().getName()+".desc"), 0, 90, 220, Color.darkGray.getRGB());
        poseStack.popPose();
    }

    private void drawWordWrap(Font font, PoseStack poseStack, net.minecraft.network.chat.Component text, int x, int y, int maxWidth, int color) {
        java.util.List<FormattedCharSequence> lines = font.split(text, maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            font.draw(poseStack, lines.get(i), (float)x, (float)(y + i * font.lineHeight), color);
        }
    }

}