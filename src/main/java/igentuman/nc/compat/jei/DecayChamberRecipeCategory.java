package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.compat.jei.particle.ParticleType;
import igentuman.nc.recipe.particle.DecayChamberRecipe;
import igentuman.nc.setup.ModEntries;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

@SuppressWarnings("removal")
public class DecayChamberRecipeCategory implements IRecipeCategory<DecayChamberRecipe> {

    public static final RecipeType<DecayChamberRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "decay_chamber", DecayChamberRecipe.class);

    public static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/decay_chamber_controller.png");

    private static final int WIDTH = 130;
    private static final int HEIGHT = 100;

    private final IDrawable background;
    private final IDrawable icon;

    public DecayChamberRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 10, 10, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("decay_chamber_controller").item().get()));
    }

    @Override
    public RecipeType<DecayChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return __("block." + NuclearCraft.MODID + ".decay_chamber_controller");
    }

    @Override
    public IDrawable getBackground() {
        return background;
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
    public void setRecipe(IRecipeLayoutBuilder builder, DecayChamberRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 58, 27)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInput()));

        List<Vec2> outputPositions = List.of(new Vec2(91, 4), new Vec2(91, 27), new Vec2(91, 50));
        int i = 0;
        for (var output : recipe.particleOutputs()) {
            if (output.isEmpty() || i >= outputPositions.size()) { i++; continue; }
            Vec2 pos = outputPositions.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, (int) pos.x, (int) pos.y)
                    .addIngredient(ParticleType.PARTICLE, output);
            i++;
        }
    }

    @Override
    public void draw(DecayChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, __("jei.nuclearcraft.particle.energy", formatParticleEnergy(recipe.releasedEnergyKeV())), 4, 65, 0x404040, false);
    }
}
