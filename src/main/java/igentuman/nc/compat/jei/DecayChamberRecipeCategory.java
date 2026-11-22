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
public class DecayChamberRecipeCategory implements IRecipeCategory<RecipeHolder<DecayChamberRecipe>> {

    public static final IRecipeHolderType<DecayChamberRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("decay_chamber"));

    public static final Identifier TEXTURE = rl("textures/gui/accelerators/decay_chamber_controller.png");

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
    public IRecipeHolderType<DecayChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return __("block." + NuclearCraft.MODID + ".decay_chamber_controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DecayChamberRecipe> holder, IFocusGroup focuses) {
        DecayChamberRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 58, 27)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInput()));

        List<Vec2> outputPositions = List.of(new Vec2(91, 4), new Vec2(91, 27), new Vec2(91, 50));
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
    public void draw(RecipeHolder<DecayChamberRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        DecayChamberRecipe recipe = holder.value();
        background.draw(graphics);
        var font = Minecraft.getInstance().font;
        var input = recipe.particleInput();
        graphics.text(font, __("tooltip.nuclearcraft.particlestack.focus", numberFormat(input.minimumFocus())),
                0, 65, 0xFF404040, false);
        graphics.text(font, __("label.nuclearcraft.cross_section",
                String.format(Locale.ROOT, "%.1f", recipe.crossSection() * 100)),
                0, 75, 0xFF404040, false);
        Component energyLabel;
        if (input.maximumEnergyKeV() == Long.MAX_VALUE) {
            energyLabel = input.minimumEnergyKeV() == 0
                    ? __("label.nuclearcraft.energy_any")
                    : __("label.nuclearcraft.energy_minimum", formatParticleEnergy(input.minimumEnergyKeV()));
        } else if (input.minimumEnergyKeV() == input.maximumEnergyKeV()) {
            energyLabel = __("label.nuclearcraft.energy", formatParticleEnergy(input.minimumEnergyKeV()));
        } else {
            energyLabel = __("label.nuclearcraft.energy_range", formatParticleEnergy(input.minimumEnergyKeV()),
                    formatParticleEnergy(input.maximumEnergyKeV()));
        }
        graphics.text(font, energyLabel, 0, 85, 0xFF404040, false);
    }
}
