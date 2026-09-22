package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.recipe.particle.CollisionChamberRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

public class CollisionChamberEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/collision_chamber_controller.png");
    private static final List<Vec2> OUTPUT_POSITIONS = List.of(new Vec2(39, 4), new Vec2(101, 4), new Vec2(101, 66), new Vec2(39, 66));

    private static final int WIDTH = 150;
    private static final int HEIGHT = 113;

    private final long releasedEnergyKeV;
    private final EmiIngredient particleInputA;
    private final EmiIngredient particleInputB;
    private final List<ParticleEmiStack> particleOutputs = new ArrayList<>();

    public CollisionChamberEmiRecipe(EmiRecipeCategory category, ResourceLocation id, CollisionChamberRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.releasedEnergyKeV = recipe.releasedEnergyKeV();

        this.particleInputA = EmiIngredient.of(ParticleCompatUtil.stacksOf(recipe.particleInputs().get(0)).stream()
                .map(stack -> (EmiIngredient) ParticleEmiStack.of(stack))
                .toList());
        this.particleInputB = EmiIngredient.of(ParticleCompatUtil.stacksOf(recipe.particleInputs().get(1)).stream()
                .map(stack -> (EmiIngredient) ParticleEmiStack.of(stack))
                .toList());
        for (var output : recipe.particleOutputs()) {
            if (output.isEmpty()) continue;
            particleOutputs.add(ParticleEmiStack.of(output));
        }

        this.inputs.add(particleInputA);
        this.inputs.add(particleInputB);
        this.outputs.addAll(particleOutputs);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, WIDTH, HEIGHT, 10, 10);

        widgets.addSlot(particleInputA, 36, 35);
        widgets.addSlot(particleInputB, 104, 35);

        for (int i = 0; i < particleOutputs.size() && i < OUTPUT_POSITIONS.size(); i++) {
            Vec2 pos = OUTPUT_POSITIONS.get(i);
            widgets.addSlot(particleOutputs.get(i), (int) pos.x, (int) pos.y).recipeContext(this);
        }

        widgets.addText(__("jei.nuclearcraft.particle.energy", formatParticleEnergy(releasedEnergyKeV)), 4, 85, 0x404040, false);
    }
}
