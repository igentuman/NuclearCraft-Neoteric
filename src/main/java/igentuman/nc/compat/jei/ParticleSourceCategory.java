package igentuman.nc.compat.jei;

import igentuman.nc.compat.jei.ingredient.ParticleType;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

@SuppressWarnings("removal")
public class ParticleSourceCategory implements IRecipeCategory<ParticleSourceRecipe> {
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/small_window.png");
    public static final RecipeType<ParticleSourceRecipe> TYPE = RecipeType.create(MODID, "particle_source_info", ParticleSourceRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;


    IGuiHelper guiHelper;

    public ParticleSourceCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 10, 10, 90, 40);
        this.icon = guiHelper.createDrawableIngredient(ParticleType.Particle, new ParticleStack(Particles.alpha));
        }

    @Override
    public @NotNull RecipeType<ParticleSourceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return __("nc_jei_cat.particle_source");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleSourceRecipe recipe, IFocusGroup focuses) {
        if(recipe.item != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).addItemStack(recipe.item);
        }

        if(recipe.fluid != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                    .addFluidStack(recipe.fluid.getFluid(), recipe.fluid.getAmount())
                    .setFluidRenderer(recipe.fluid.getAmount(), false, 16, 16);
            guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 40, 10).addIngredient(ParticleType.Particle, recipe.getParticle());
    }
}
