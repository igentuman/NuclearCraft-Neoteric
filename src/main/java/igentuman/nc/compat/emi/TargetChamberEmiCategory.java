package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

public class TargetChamberEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE = 
            rl("textures/gui/accelerators/target_chamber_controller.png");
    
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("target_chamber"),
            EmiStack.of(CATALYSTS.containsKey("target_chamber") ? 
                new ItemStack(TARGET_CHAMBER_BLOCKS.get("target_chamber_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final TargetChamberControllerBE.Recipe recipe;

    public TargetChamberEmiCategory(TargetChamberControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 160, 105);
        this.recipe = recipe;
        for (int i = 0; i < recipe.inputParticles.length; i++) {
            this.inputs.add(ParticleEmiStack.of(recipe.inputParticles[i]));
        }
        for (int i = 0; i < recipe.outputParticles.length; i++) {
            this.outputs.add(ParticleEmiStack.of(recipe.outputParticles[i]));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add background texture
        widgets.addTexture(TEXTURE, 1, 1, 160, 105, 10, 10);
        
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            widgets.addSlot(EmiIngredient.of(Arrays.stream(recipe.getItemIngredients().get(i).getItems()).map(EmiStack::of).toList()), 43 + 18 * i, 28).recipeContext(this);
        }
        
        if (recipe.inputParticles != null) {
            for (int i = 0; i < recipe.inputParticles.length; i++) {
                widgets.addSlot(ParticleEmiStack.of(recipe.inputParticles[i]), 8 + 18 * i, 36).recipeContext(this);
            }
        }
        
        if (recipe.outputParticles != null) {
            List<Vec2> positionMap = List.of(new Vec2(76, 5), new Vec2(136, 36), new Vec2(76, 68));
            for (int i = 0; i < recipe.outputParticles.length && i < positionMap.size(); i++) {
                Vec2 pos = positionMap.get(i);
                widgets.addSlot(ParticleEmiStack.of(recipe.outputParticles[i]), (int) pos.x, (int) pos.y).recipeContext(this);
            }
        }
        
        if (recipe.getInputFluids().length > 0) {
            widgets.addSlot(EmiStack.of(recipe.getInputFluids()[0].getRepresentations().get(0).getFluid()), 43, 45).recipeContext(this);
        }
        
        if (recipe.getOutputFluids().size() > 0) {
            widgets.addSlot(EmiStack.of(recipe.getOutputFluids().get(0).getFluid()), 101, 45).recipeContext(this);
        }
        
        widgets.addSlot(EmiStack.of(recipe.getResultItem()), 101, 28).recipeContext(this);
    }
}