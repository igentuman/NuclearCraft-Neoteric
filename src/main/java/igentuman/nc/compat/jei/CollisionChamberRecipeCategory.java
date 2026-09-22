package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.compat.jei.particle.ParticleType;
import igentuman.nc.recipe.particle.CollisionChamberRecipe;
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
public class CollisionChamberRecipeCategory implements IRecipeCategory<CollisionChamberRecipe> {

    public static final RecipeType<CollisionChamberRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "collision_chamber", CollisionChamberRecipe.class);

    public static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/collision_chamber_controller.png");

    private static final int WIDTH = 150;
    private static final int HEIGHT = 113;

    private final IDrawable background;
    private final IDrawable icon;

    public CollisionChamberRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 10, 10, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("collision_chamber_controller").item().get()));
    }

    @Override
    public RecipeType<CollisionChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return __("block." + NuclearCraft.MODID + ".collision_chamber_controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CollisionChamberRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 36, 35)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInputs().get(0)));
        builder.addSlot(RecipeIngredientRole.INPUT, 104, 35)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInputs().get(1)));

        List<Vec2> outputPositions = List.of(new Vec2(39, 4), new Vec2(101, 4), new Vec2(101, 66), new Vec2(39, 66));
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
    public void draw(CollisionChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, __("jei.nuclearcraft.particle.energy", formatParticleEnergy(recipe.releasedEnergyKeV())), 4, 85, 0x404040, false);
    }
}
