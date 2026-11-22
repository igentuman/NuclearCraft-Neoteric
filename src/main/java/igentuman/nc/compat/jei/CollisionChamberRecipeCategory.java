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
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;
import static igentuman.nc.util.TextUtils.numberFormat;

@SuppressWarnings("removal")
public class CollisionChamberRecipeCategory implements IRecipeCategory<RecipeHolder<CollisionChamberRecipe>> {

    public static final IRecipeHolderType<CollisionChamberRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("collision_chamber"));

    public static final Identifier TEXTURE = rl("textures/gui/accelerators/collision_chamber_controller.png");

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
    public IRecipeHolderType<CollisionChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return __("block." + NuclearCraft.MODID + ".collision_chamber_controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CollisionChamberRecipe> holder, IFocusGroup focuses) {
        CollisionChamberRecipe recipe = holder.value();
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
                    .add(ParticleType.PARTICLE, output);
            i++;
        }
    }

    @Override
    public void draw(RecipeHolder<CollisionChamberRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        CollisionChamberRecipe recipe = holder.value();
        background.draw(graphics);
        var font = Minecraft.getInstance().font;
        var input = recipe.particleInputs().getFirst();
        graphics.text(font, __("tooltip.nuclearcraft.particlestack.focus", numberFormat(input.minimumFocus())),
                0, 85, 0xFF404040, false);
        graphics.text(font, __("label.nuclearcraft.cross_section",
                String.format(Locale.ROOT, "%.1f", recipe.crossSection() * 100)),
                0, 95, 0xFF404040, false);
        var energyLabel = input.minimumEnergyKeV() == input.maximumEnergyKeV()
                ? __("label.nuclearcraft.energy", formatParticleEnergy(input.minimumEnergyKeV()))
                : __("label.nuclearcraft.energy_range", formatParticleEnergy(input.minimumEnergyKeV()),
                        formatParticleEnergy(input.maximumEnergyKeV()));
        graphics.text(font, energyLabel, 0, 105, 0xFF404040, false);
    }
}
