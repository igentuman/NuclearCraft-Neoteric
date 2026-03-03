package igentuman.nc.compat.jei;

import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.compat.jei.ingredient.ParticleType;
import igentuman.nc.util.Units;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.world.item.Items.BARRIER;

@SuppressWarnings("removal")
public class TargetChamberCategoryWrapper<T extends TargetChamberControllerBE.Recipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/accelerators/target_chamber_controller.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected RecipeType<T> recipeType;

    IGuiHelper guiHelper;
    private T currentRecipe;

    public TargetChamberCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(TEXTURE, 10, 10, 160, 107);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TARGET_CHAMBER_BLOCKS.get("target_chamber_controller").get()));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BARRIER));
        }
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return __("nc_jei_cat."+getRecipeType().getUid().getPath());
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        this.currentRecipe = recipe;
        
        for(int i = 0; i < recipe.getItemIngredients().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 43+18*i, 28).addIngredients(recipe.getItemIngredients().get(i));
        }

        for(int i = 0; i < recipe.inputParticles.length; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8+18*i, 36).addIngredient(ParticleType.Particle, recipe.inputParticles[i]);
        }
        List<Vec2> positionMap = List.of(new Vec2(76, 5), new Vec2(136, 36), new Vec2(76, 68));
        for(int i = 0; i < recipe.outputParticles.length; i++) {
            Vec2 pos = positionMap.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, (int) pos.x, (int) pos.y).addIngredient(ParticleType.Particle, recipe.outputParticles[i]);
        }

        if(recipe.getInputFluids().length > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 43, 45)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, recipe.getInputFluids(0))
                    .setFluidRenderer(recipe.getInputFluids()[0].getAmount(), false, 16, 16);
            guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
        }

        if(!recipe.getOutputFluids().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 45)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, recipe.getOutputFluids(0))
                    .setFluidRenderer(recipe.getOutputFluids().get(0).getAmount(), false, 16, 16);
            guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 28).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawLabels(recipe, guiGraphics);
    }

    private void drawLabels(T recipe, GuiGraphics guiGraphics) {
        if (recipe == null || recipe.inputParticles == null || recipe.inputParticles.length == 0) {
            return;
        }
        var font = Minecraft.getInstance().font;
        var inputParticle = recipe.inputParticles[0];
        
        int labelY = 77;
        int labelX = 0;
        
        long minEnergy = inputParticle.getMeanEnergy()*1000;
        long maxEnergy = recipe.maxEnergy*1000;
        String energyLabel = __("label.nuclearcraft.energy_range", Units.getSIFormat(minEnergy, "eV"), Units.getSIFormat(maxEnergy, "eV")).getString();
        if(minEnergy == maxEnergy) {
            energyLabel = __("label.nuclearcraft.energy", Units.getSIFormat(minEnergy, "eV")).getString();
        }
        // Cross-section
        guiGraphics.drawString(font, __("tooltip.nuclearcraft.particlestack.focus", Units.getSIFormat(inputParticle.getFocus(), "")), labelX, labelY, 0xFFFFFF);
        guiGraphics.drawString(font, __("label.nuclearcraft.cross_section", String.format("%.1f", recipe.crossSection*100)), labelX, labelY + 10, 0xFFFFFF);
        guiGraphics.drawString(font, energyLabel, labelX, labelY + 20, 0xFFFFFF);
    }
}
