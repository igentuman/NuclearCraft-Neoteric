package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import igentuman.nc.util.Units;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.world.item.Items.BARRIER;

public class TargetChamberEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE = 
            rl("textures/gui/accelerators/target_chamber_controller.png");
    
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("target_chamber"),
            EmiStack.of(CATALYSTS.containsKey("target_chamber") ? 
                new ItemStack(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final TargetChamberControllerBE.Recipe recipe;

    public TargetChamberEmiCategory(TargetChamberControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 160, 107);
        this.recipe = recipe;
        for (int i = 0; i < recipe.inputParticles.length; i++) {
            this.inputs.add(ParticleEmiStack.of(recipe.inputParticles[i]));
        }
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            ItemStack[] items = recipe.getItemIngredients().get(i).getItems();
            this.inputs.add(EmiIngredient.of(Arrays.stream(items).map(EmiStack::of).toList()));
        }
        for (int i = 0; i < recipe.getInputFluids().length; i++) {
            List<FluidStack> fluids = recipe.getInputFluids(i);
            this.inputs.add(EmiIngredient.of(fluids.stream()
                    .map(fluid -> EmiStack.of(fluid.getFluid(), fluid.getAmount())).toList()));
        }
        for (int i = 0; i < recipe.getResultItems().size(); i++) {
            this.outputs.add(EmiStack.of(recipe.getResultItems().get(i)));
        }

        for (FluidStack fluid : recipe.getOutputFluids()) {
            if (!fluid.isEmpty()) {
                this.outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
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
        
        // Add labels (same as JEI version)
        addLabels(widgets);
    }
    
    private void addLabels(WidgetHolder widgets) {
        if (recipe == null || recipe.inputParticles == null || recipe.inputParticles.length == 0) {
            return;
        }
        
        var inputParticle = recipe.inputParticles[0];
        
        int labelY = 77;
        int labelX = 0;
        
        long minEnergy = inputParticle.getMeanEnergy() * 1000;
        long maxEnergy = recipe.maxEnergy * 1000;
        MutableComponent energyLabel = __("label.nuclearcraft.energy_range", Units.getSIFormat(minEnergy, "eV"), Units.getSIFormat(maxEnergy, "eV"));
        if (minEnergy == maxEnergy) {
            energyLabel = __("label.nuclearcraft.energy", Units.getSIFormat(minEnergy, "eV"));
        }
        
        widgets.addText(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(inputParticle.getFocus(), "")), labelX, labelY, 0xFFFFFF, false);
        widgets.addText(__("label.nuclearcraft.cross_section", String.format("%.1f", recipe.crossSection * 100)), labelX, labelY + 10, 0xFFFFFF, false);
        widgets.addText(energyLabel, labelX, labelY + 20, 0xFFFFFF, false);
    }
}