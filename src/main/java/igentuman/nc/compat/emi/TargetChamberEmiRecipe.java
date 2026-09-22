package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

public class TargetChamberEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/target_chamber_controller.png");

    private static final int WIDTH = 160;
    private static final int HEIGHT = 107;

    private final long releasedEnergyKeV;
    private final List<EmiIngredient> itemInputWidgets = new ArrayList<>();
    private final List<EmiIngredient> fluidInputWidgets = new ArrayList<>();
    private final List<EmiStack> itemOutputWidgets = new ArrayList<>();
    private final List<EmiStack> fluidOutputWidgets = new ArrayList<>();
    private final EmiIngredient beamInput;
    private final List<ParticleEmiStack> byproducts = new ArrayList<>();

    private static final List<Vec2> BYPRODUCT_POSITIONS = List.of(new Vec2(76, 5), new Vec2(136, 36), new Vec2(76, 68));

    public TargetChamberEmiRecipe(EmiRecipeCategory category, ResourceLocation id, TargetChamberRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.releasedEnergyKeV = recipe.releasedEnergyKeV();

        for (SizedIngredient si : recipe.itemInputs()) {
            itemInputWidgets.add(EmiIngredient.of(Arrays.stream(si.ingredient().getItems())
                    .map(stack -> (EmiIngredient) EmiStack.of(stack.copyWithCount(si.count())))
                    .toList()));
        }
        for (SizedFluidIngredient sfi : recipe.fluidInputs()) {
            fluidInputWidgets.add(EmiIngredient.of(Arrays.stream(sfi.getFluids())
                    .map(fs -> (EmiIngredient) EmiStack.of(fs.getFluid(), sfi.amount()))
                    .toList()));
        }
        for (ItemOutput out : recipe.itemOutputs()) {
            itemOutputWidgets.add(out.members().isEmpty() ? EmiStack.EMPTY : EmiStack.of(out.members().getFirst()));
        }
        for (FluidOutput out : recipe.fluidOutputs()) {
            List<FluidStack> members = out.members();
            fluidOutputWidgets.add(members.isEmpty() ? EmiStack.EMPTY : EmiStack.of(members.getFirst().getFluid(), members.getFirst().getAmount()));
        }

        this.beamInput = EmiIngredient.of(ParticleCompatUtil.stacksOf(recipe.particleInput()).stream()
                .map(stack -> (EmiIngredient) ParticleEmiStack.of(stack))
                .toList());
        for (var byproduct : recipe.particleOutputs()) {
            if (byproduct.isEmpty()) continue;
            byproducts.add(ParticleEmiStack.of(byproduct));
        }

        this.inputs.addAll(itemInputWidgets);
        this.inputs.addAll(fluidInputWidgets);
        this.inputs.add(beamInput);
        this.outputs.addAll(itemOutputWidgets);
        this.outputs.addAll(fluidOutputWidgets);
        for (ParticleEmiStack byproduct : byproducts) {
            this.outputs.add(byproduct);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, WIDTH, HEIGHT, 10, 10);

        int x = 43;
        for (EmiIngredient in : itemInputWidgets) {
            widgets.addSlot(in, x, 28);
            x += 18;
        }
        int fluidX = 43;
        for (EmiIngredient in : fluidInputWidgets) {
            widgets.addSlot(in, fluidX, 45);
            fluidX += 18;
        }

        int outX = 101;
        for (EmiStack out : itemOutputWidgets) {
            widgets.addSlot(out, outX, 28).recipeContext(this);
            outX += 18;
        }
        int fluidOutX = 101;
        for (EmiStack out : fluidOutputWidgets) {
            widgets.addSlot(out, fluidOutX, 45).recipeContext(this);
            fluidOutX += 18;
        }

        widgets.addSlot(beamInput, 8, 36);

        for (int i = 0; i < byproducts.size() && i < BYPRODUCT_POSITIONS.size(); i++) {
            Vec2 pos = BYPRODUCT_POSITIONS.get(i);
            widgets.addSlot(byproducts.get(i), (int) pos.x, (int) pos.y).recipeContext(this);
        }

        widgets.addText(__("jei.nuclearcraft.particle.energy", formatParticleEnergy(releasedEnergyKeV)), 4, 77, 0x404040, false);
    }
}
