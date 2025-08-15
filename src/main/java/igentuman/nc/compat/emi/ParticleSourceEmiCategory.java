package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import igentuman.nc.compat.jei.ParticleSourceRecipe;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class ParticleSourceEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE = rl("textures/gui/small_window.png");
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("particle_source_info"),
            ParticleEmiStack.of(new ParticleStack(Particles.alpha))
    );
    
    private final ParticleSourceRecipe recipe;

    public ParticleSourceEmiCategory(ParticleSourceRecipe recipe) {
        super(CATEGORY, recipe.getId(), 90, 40);
        this.recipe = recipe;
        
        // Add inputs
        if (recipe.item != null && !recipe.item.isEmpty()) {
            this.inputs.add(EmiStack.of(recipe.item));
        }
        
        if (recipe.fluid != null && !recipe.fluid.isEmpty()) {
            this.inputs.add(EmiStack.of(recipe.fluid.getFluid(), recipe.fluid.getAmount()));
        }
        
        // Add output
        this.outputs.add(ParticleEmiStack.of(recipe.getParticle()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int inputIndex = 0;
        
        // Add input slot (item or fluid)
        if (recipe.item != null && !recipe.item.isEmpty()) {
            widgets.addSlot(inputs.get(inputIndex), 10, 10);
            inputIndex++;
        }
        
        if (recipe.fluid != null && !recipe.fluid.isEmpty()) {
            widgets.addSlot(inputs.get(inputIndex), 10, 10);
        }
        
        // Add output slot for particle
        widgets.addSlot(outputs.get(0), 40, 10).recipeContext(this);
    }

    public static Component getTitle() {
        return __("nc_jei_cat.particle_source");
    }
}