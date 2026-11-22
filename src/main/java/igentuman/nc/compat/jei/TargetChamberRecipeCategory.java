package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ParticleCompatUtil;
import igentuman.nc.compat.jei.particle.ParticleType;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import igentuman.nc.setup.ModEntries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;
import static igentuman.nc.util.TextUtils.numberFormat;

@SuppressWarnings("removal")
public class TargetChamberRecipeCategory implements IRecipeCategory<RecipeHolder<TargetChamberRecipe>> {

    public static final IRecipeHolderType<TargetChamberRecipe> TYPE =
            IRecipeHolderType.create(NuclearCraft.rl("target_chamber"));

    public static final Identifier TEXTURE = rl("textures/gui/accelerators/target_chamber_controller.png");

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
    public IRecipeHolderType<TargetChamberRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TargetChamberRecipe> holder, IFocusGroup focuses) {
        TargetChamberRecipe recipe = holder.value();
        int x = 43;
        for (SizedIngredient si : recipe.itemInputs()) {
            JeiRecipeSlots.addItems(builder.addSlot(RecipeIngredientRole.INPUT, x, 28), si);
            x += 18;
        }
        int fluidX = 43;
        for (SizedFluidIngredient sfi : recipe.fluidInputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, fluidX, 45);
            JeiRecipeSlots.addFluids(slot, sfi);
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
                JeiRecipeSlots.addFluid(slot, fs);
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
                    .add(ParticleType.PARTICLE, byproduct);
            i++;
        }
    }

    @Override
    public void draw(RecipeHolder<TargetChamberRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        TargetChamberRecipe recipe = holder.value();
        background.draw(graphics);
        var font = Minecraft.getInstance().font;
        var input = recipe.particleInput();
        graphics.text(font, __("tooltip.nuclearcraft.particlestack.focus", numberFormat(input.minimumFocus())),
                0, 77, 0xFF404040, false);
        graphics.text(font, __("label.nuclearcraft.cross_section",
                String.format(Locale.ROOT, "%.1f", recipe.crossSection() * 100)),
                0, 87, 0xFF404040, false);
        var energyLabel = input.minimumEnergyKeV() == input.maximumEnergyKeV()
                ? __("label.nuclearcraft.energy", formatParticleEnergy(input.minimumEnergyKeV()))
                : __("label.nuclearcraft.energy_range", formatParticleEnergy(input.minimumEnergyKeV()),
                        formatParticleEnergy(input.maximumEnergyKeV()));
        graphics.text(font, energyLabel, 0, 97, 0xFF404040, false);
    }
}
