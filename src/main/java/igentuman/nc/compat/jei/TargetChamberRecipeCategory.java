package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.compat.TargetChamberRecipeLayout;
import igentuman.nc.compat.jei.particle.ParticleType;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.compat.TargetChamberRecipeLayout.*;
import static igentuman.nc.util.TextUtils.__;

public class TargetChamberRecipeCategory implements IRecipeCategory<TargetChamberRecipe> {

    public static final RecipeType<TargetChamberRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "target_chamber", TargetChamberRecipe.class);

    private final IDrawable icon;

    public TargetChamberRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModEntries.get("target_chamber_controller").item().get()));
    }

    @Override
    public RecipeType<TargetChamberRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return __("block." + NuclearCraft.MODID + ".target_chamber_controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TargetChamberRecipe recipe, IFocusGroup focuses) {
        int x = INPUT_X;
        for (SizedIngredient si : recipe.itemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, ITEM_Y)
                    .addItemStacks(java.util.Arrays.stream(si.ingredient().getItems())
                            .map(stack -> stack.copyWithCount(si.count()))
                            .toList());
            x += SLOT_SPACING;
        }
        int fluidX = INPUT_X;
        for (SizedFluidIngredient sfi : recipe.fluidInputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, fluidX, FLUID_Y);
            for (FluidStack fs : sfi.getFluids()) {
                slot.addFluidStack(fs.getFluid(), sfi.amount());
            }
            slot.setFluidRenderer(sfi.amount(), false, 16, 16);
            fluidX += SLOT_SPACING;
        }

        int outX = OUTPUT_X;
        for (ItemOutput out : recipe.itemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outX, ITEM_Y).addItemStacks(out.members());
            outX += SLOT_SPACING;
        }
        int fluidOutX = OUTPUT_X;
        for (FluidOutput out : recipe.fluidOutputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, fluidOutX, FLUID_Y);
            var members = out.members();
            int amount = members.isEmpty() ? 0 : members.getFirst().getAmount();
            for (FluidStack fs : members) {
                slot.addFluidStack(fs.getFluid(), fs.getAmount());
            }
            if (amount > 0) slot.setFluidRenderer(amount, false, 16, 16);
            fluidOutX += SLOT_SPACING;
        }

        builder.addSlot(RecipeIngredientRole.INPUT, (int) BEAM_POSITION.x, (int) BEAM_POSITION.y)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInput()));

        for (int i = 0; i < recipe.particleOutputs().size() && i < BYPRODUCT_POSITIONS.size(); i++) {
            var byproduct = recipe.particleOutputs().get(i);
            if (byproduct.isEmpty()) continue;
            Vec2 pos = BYPRODUCT_POSITIONS.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, (int) pos.x, (int) pos.y)
                    .addIngredient(ParticleType.PARTICLE, byproduct);
        }
    }

    @Override
    public void draw(TargetChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        TargetChamberRecipeLayout.draw(graphics, recipe);
    }
}
