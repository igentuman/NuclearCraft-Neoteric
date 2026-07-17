package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import igentuman.nc.util.Units;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.numberFormat;
import static net.minecraft.world.item.Items.BARRIER;

public class CollisionChamberEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE =
            rl("textures/gui/accelerators/collision_chamber_controller.png");

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            rl("collision_chamber"),
            EmiStack.of(CATALYSTS.containsKey("collision_chamber") ?
                new ItemStack(PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get()) :
                new ItemStack(BARRIER))
    );

    private final CollisionChamberControllerBE.Recipe recipe;

    public CollisionChamberEmiCategory(CollisionChamberControllerBE.Recipe recipe) {
        super(CATEGORY, recipe.getId(), 150, 120);
        this.recipe = recipe;
        if (recipe.inputParticles != null) {
            for (int i = 0; i < recipe.inputParticles.length; i++) {
                this.inputs.add(ParticleEmiStack.of(recipe.inputParticles[i]));
            }
        }
        if (recipe.outputParticles != null) {
            for (int i = 0; i < recipe.outputParticles.length; i++) {
                this.outputs.add(ParticleEmiStack.of(recipe.outputParticles[i]));
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, 150, 113, 10, 10);

        if (recipe.inputParticles != null) {
            List<Vec2> inputMap = List.of(new Vec2(36, 35), new Vec2(104, 35));
            for (int i = 0; i < Math.min(recipe.inputParticles.length, inputMap.size()); i++) {
                Vec2 pos = inputMap.get(i);
                widgets.addSlot(ParticleEmiStack.of(recipe.inputParticles[i]), (int) pos.x, (int) pos.y).recipeContext(this);
            }
        }

        if (recipe.outputParticles != null) {
            List<Vec2> positionMap = List.of(new Vec2(39, 4), new Vec2(101, 4), new Vec2(101, 66), new Vec2(39, 66));
            for (int i = 0; i < Math.min(recipe.outputParticles.length, positionMap.size()); i++) {
                Vec2 pos = positionMap.get(i);
                widgets.addSlot(ParticleEmiStack.of(recipe.outputParticles[i]), (int) pos.x, (int) pos.y).recipeContext(this);
            }
        }

        addLabels(widgets);
    }

    private void addLabels(WidgetHolder widgets) {
        if (recipe == null || recipe.inputParticles == null || recipe.inputParticles.length == 0) {
            return;
        }
        var inputParticle = recipe.inputParticles[0];

        int labelY = 85;
        int labelX = 0;

        long minEnergy = recipe.minEnergy * 1000;
        long maxEnergy = recipe.maxEnergy * 1000;
        MutableComponent energyLabel = __("label.nuclearcraft.energy_range", Units.getSIFormat(minEnergy, "eV"), Units.getSIFormat(maxEnergy, "eV"));
        if (minEnergy == maxEnergy) {
            energyLabel = __("label.nuclearcraft.energy", Units.getSIFormat(minEnergy, "eV"));
        }

        widgets.addText(__("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(inputParticle.getFocus(), "")), labelX, labelY, 0xFFFFFF, false);
        widgets.addText(__("label.nuclearcraft.cross_section", numberFormat(recipe.crossSection * 100)), labelX, labelY + 10, 0xFFFFFF, false);
        widgets.addText(energyLabel, labelX, labelY + 20, 0xFFFFFF, false);
    }
}
