package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ParticleCompatUtil;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

@SuppressWarnings("removal")
public class TargetChamberRecipeCategory implements IRecipeCategory<TargetChamberRecipe> {

    public static final RecipeType<TargetChamberRecipe> TYPE =
            RecipeType.create(NuclearCraft.MODID, "target_chamber", TargetChamberRecipe.class);

    public static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/target_chamber_controller.png");

    private static final int WIDTH = 160;
    private static final int HEIGHT = 107;

    private final IDrawable background;
    private final IDrawable icon;

    public TargetChamberRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 10, 10, WIDTH, HEIGHT);
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
    public IDrawable getBackground() {
        return background;
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
        int x = 43;
        for (SizedIngredient si : recipe.itemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, 28)
                    .addItemStacks(java.util.Arrays.stream(si.ingredient().getItems())
                            .map(stack -> stack.copyWithCount(si.count()))
                            .toList());
            x += 18;
        }
        int fluidX = 43;
        for (SizedFluidIngredient sfi : recipe.fluidInputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, fluidX, 45);
            for (FluidStack fs : sfi.getFluids()) {
                slot.addFluidStack(fs.getFluid(), sfi.amount());
            }
            slot.setFluidRenderer(sfi.amount(), false, 16, 16);
            fluidX += 18;
        }

        int outX = 101;
        for (ItemOutput out : recipe.itemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outX, 28).addItemStacks(out.members());
            outX += 18;
        }
        int fluidOutX = 101;
        for (FluidOutput out : recipe.fluidOutputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, fluidOutX, 45);
            var members = out.members();
            int amount = members.isEmpty() ? 0 : members.getFirst().getAmount();
            for (FluidStack fs : members) {
                slot.addFluidStack(fs.getFluid(), fs.getAmount());
            }
            if (amount > 0) slot.setFluidRenderer(amount, false, 16, 16);
            fluidOutX += 18;
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 8, 36)
                .addIngredients(ParticleType.PARTICLE, ParticleCompatUtil.stacksOf(recipe.particleInput()));

        List<Vec2> byproductPositions = List.of(new Vec2(76, 5), new Vec2(136, 36), new Vec2(76, 68));
        int i = 0;
        for (var byproduct : recipe.particleOutputs()) {
            if (byproduct.isEmpty() || i >= byproductPositions.size()) { i++; continue; }
            Vec2 pos = byproductPositions.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, (int) pos.x, (int) pos.y)
                    .addIngredient(ParticleType.PARTICLE, byproduct);
            i++;
        }
    }

    @Override
    public void draw(TargetChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, __("jei.nuclearcraft.particle.energy", formatParticleEnergy(recipe.releasedEnergyKeV())), 4, 49, 0x404040, false);
    }
}
