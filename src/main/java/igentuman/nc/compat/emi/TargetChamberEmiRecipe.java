package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.compat.TargetChamberRecipeLayout;
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

import static igentuman.nc.compat.TargetChamberRecipeLayout.*;

public class TargetChamberEmiRecipe extends BasicEmiRecipe {

    private final TargetChamberRecipe recipe;
    private final List<EmiIngredient> itemInputWidgets = new ArrayList<>();
    private final List<EmiIngredient> fluidInputWidgets = new ArrayList<>();
    private final List<EmiStack> itemOutputWidgets = new ArrayList<>();
    private final List<EmiStack> fluidOutputWidgets = new ArrayList<>();
    private final EmiIngredient beamInput;
    private final List<ParticleEmiStack> byproducts = new ArrayList<>();

    public TargetChamberEmiRecipe(EmiRecipeCategory category, ResourceLocation id, TargetChamberRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.recipe = recipe;

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
            byproducts.add(ParticleEmiStack.of(byproduct));
        }

        this.inputs.addAll(itemInputWidgets);
        this.inputs.addAll(fluidInputWidgets);
        this.inputs.add(beamInput);
        this.outputs.addAll(itemOutputWidgets);
        this.outputs.addAll(fluidOutputWidgets);
        for (ParticleEmiStack byproduct : byproducts) {
            if (!byproduct.isEmpty()) this.outputs.add(byproduct);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (graphics, mouseX, mouseY, delta) ->
                TargetChamberRecipeLayout.draw(graphics, recipe));

        int x = INPUT_X;
        for (EmiIngredient in : itemInputWidgets) {
            widgets.addSlot(in, x - 1, ITEM_Y - 1).drawBack(false);
            x += SLOT_SPACING;
        }
        int fluidX = INPUT_X;
        for (EmiIngredient in : fluidInputWidgets) {
            widgets.addSlot(in, fluidX - 1, FLUID_Y - 1).drawBack(false);
            fluidX += SLOT_SPACING;
        }

        int outX = OUTPUT_X;
        for (EmiStack out : itemOutputWidgets) {
            widgets.addSlot(out, outX - 1, ITEM_Y - 1).drawBack(false).recipeContext(this);
            outX += SLOT_SPACING;
        }
        int fluidOutX = OUTPUT_X;
        for (EmiStack out : fluidOutputWidgets) {
            widgets.addSlot(out, fluidOutX - 1, FLUID_Y - 1).drawBack(false).recipeContext(this);
            fluidOutX += SLOT_SPACING;
        }

        widgets.addSlot(beamInput, (int) BEAM_POSITION.x - 1, (int) BEAM_POSITION.y - 1).drawBack(false);

        for (int i = 0; i < byproducts.size() && i < BYPRODUCT_POSITIONS.size(); i++) {
            ParticleEmiStack byproduct = byproducts.get(i);
            if (byproduct.isEmpty()) continue;
            Vec2 pos = BYPRODUCT_POSITIONS.get(i);
            widgets.addSlot(byproduct, (int) pos.x - 1, (int) pos.y - 1).drawBack(false).recipeContext(this);
        }
    }
}
