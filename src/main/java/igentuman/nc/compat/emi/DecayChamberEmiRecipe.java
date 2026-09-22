package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.recipe.particle.DecayChamberRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

public class DecayChamberEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/decay_chamber_controller.png");
    private static final List<Vec2> OUTPUT_POSITIONS = List.of(new Vec2(91, 4), new Vec2(91, 27), new Vec2(91, 50));

    private static final int WIDTH = 130;
    private static final int HEIGHT = 100;

    private final long releasedEnergyKeV;
    private final EmiIngredient particleInput;
    private final List<ParticleEmiStack> particleOutputs = new ArrayList<>();

    public DecayChamberEmiRecipe(EmiRecipeCategory category, ResourceLocation id, DecayChamberRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.releasedEnergyKeV = recipe.releasedEnergyKeV();

        this.particleInput = EmiIngredient.of(ParticleCompatUtil.stacksOf(recipe.particleInput()).stream()
                .map(stack -> (EmiIngredient) ParticleEmiStack.of(stack))
                .toList());
        for (var output : recipe.particleOutputs()) {
            if (output.isEmpty()) continue;
            particleOutputs.add(ParticleEmiStack.of(output));
        }

        this.inputs.add(particleInput);
        this.outputs.addAll(particleOutputs);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, WIDTH, HEIGHT, 10, 10);

        widgets.addSlot(particleInput, 58, 27);

        for (int i = 0; i < particleOutputs.size() && i < OUTPUT_POSITIONS.size(); i++) {
            Vec2 pos = OUTPUT_POSITIONS.get(i);
            widgets.addSlot(particleOutputs.get(i), (int) pos.x, (int) pos.y).recipeContext(this);
        }

        widgets.addText(__("jei.nuclearcraft.particle.energy", formatParticleEnergy(releasedEnergyKeV)), 4, 65, 0x404040, false);
    }
}
