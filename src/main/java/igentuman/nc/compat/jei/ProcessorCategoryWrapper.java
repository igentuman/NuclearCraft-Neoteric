package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.gui.element.bar.ProgressBar.bars;
import static igentuman.nc.compat.GlobalVars.*;

public class ProcessorCategoryWrapper<T extends AbstractRecipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE = rl("textures/gui/processor_jei.png");

    private final IDrawable background;
    private IDrawable progressBackground;
    private final IDrawable icon;
    private  IDrawable[] slots;
    protected RecipeType<T> recipeType;
    private IGuiHelper guiHelper;
    private final ProcessorPrefab processor;
    private int xShift = -25;
    private int yShift = -38;
    HashMap<Integer, TickTimer> timer = new HashMap<>();
    HashMap<Integer, IDrawable> arrow = new HashMap<>();
    int height = 22;
    public ProcessorCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        processor = Processors.all().get(getRecipeType().getUid().getPath());
        if(processor.getSlotsConfig().isDoubleSlotHeight()) {
            height = 45;
            yShift+= 11;
        }
        if(processor.getSlotsConfig().hasThreeRows()) {
            xShift -= 8;
        }
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 150, height);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("nc_jei_cat."+getRecipeType().getUid().getPath());
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
                     double mouseY) {
        int d = (int) ((recipe.getTimeModifier()*(double) processor.config().getTime())/2);
        int fluidsOut = processor.getSlotsConfig().getOutputFluids();
        int itemsOut = processor.getSlotsConfig().getOutputItems();

        int barXshift = 0;
        if(fluidsOut + itemsOut == 3 || fluidsOut + itemsOut == 6) {
            barXshift = -8;
        }
        int extraXshift = 0;
        if(fluidsOut + itemsOut > 6) {
            extraXshift = -20;
        }

        if(arrow.containsKey(d)) {
            int barHeight = 16;
            if(processor.progressBar > 14) {
                barHeight = 36;
            }
            progressBackground.draw(stack, 47+xShift+25+barXshift+extraXshift, height/2-barHeight/2);
            arrow.get(d).draw(stack, 47+xShift+25+barXshift+extraXshift, height/2-barHeight/2);
        }

        for(int i = 0; i < slots.length; i++) {
            if(slots[i] != null) {
                int[] pos = processor.getSlotsConfig().getSlotPositions().get(i);
                slots[i].draw(stack, pos[0]+xShift-1+barXshift, pos[1]+yShift-1);
            }
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, T recipe, @NotNull IFocusGroup focuses) {

        int itemIdx = 0;
        int inputCounter = 0;
        int inputFluidCounter = 0;
        int putFluidCounter = 0;
        int outputCounter = 0;
        int fluidsOut = processor.getSlotsConfig().getOutputFluids();
        int itemsOut = processor.getSlotsConfig().getOutputItems();

        int d = (int) ((recipe.getTimeModifier()*(double)processor.config().getTime())/2);
        if(!timer.containsKey(d)) {
            timer.put(d, new TickTimer(d, 36, true));
        }
        if(!arrow.containsKey(d)) {
            int xoffset = bars.get(processor.progressBar)[0];
            int yoffset = bars.get(processor.progressBar)[1];
            int barHeight = 15;
            if(processor.progressBar > 14) {
                barHeight = 36;
            }
            this.progressBackground = guiHelper.createDrawable(rl("textures/gui/progress.png"), xoffset, yoffset, 36, barHeight);
            arrow.put(d, guiHelper.drawableBuilder(rl("textures/gui/progress.png"), xoffset, yoffset-barHeight-1, 36, barHeight)
                    .buildAnimated(timer.get(d), IDrawableAnimated.StartDirection.LEFT));
        }
        int barXshift = 0;
        if(fluidsOut + itemsOut == 3 || fluidsOut + itemsOut == 6) {
            barXshift = -8;
        }

        slots = new IDrawable[processor.getSlotsConfig().getSlotPositions().size()];
        for(int[] pos: processor.getSlotsConfig().getSlotPositions()) {
            if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_in")) {

                builder.addSlot(RecipeIngredientRole.INPUT, pos[0]+xShift+barXshift, pos[1]+yShift).addIngredients(recipe.getInputIngredient(inputCounter));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 0, 18, 18);
                itemIdx++;
                inputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_out")) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0]+xShift+barXshift, pos[1]+yShift).addItemStack(recipe.getOutputItem(outputCounter));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 36, 18, 18);
                itemIdx++;
                outputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_in")) {
                if(!recipe.getInputFluids(inputFluidCounter).get(0).equals(FluidStack.EMPTY)) {
                    builder.addSlot(RecipeIngredientRole.INPUT, pos[0]+xShift+barXshift, pos[1]+yShift)
                        .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInputFluids(inputFluidCounter))
                        .setFluidRenderer(recipe.getInputFluids(inputFluidCounter).get(0).getAmount(), false, 16, 16);;
                }
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
                itemIdx++;
                inputFluidCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_out")) {
                if (!recipe.getOutputFluids(putFluidCounter).get(0).equals(FluidStack.EMPTY)) {
                    builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0] + xShift + barXshift, pos[1] + yShift)
                            .addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutputFluids(putFluidCounter))
                            .setFluidRenderer(recipe.getOutputFluids(putFluidCounter).get(0).getAmount(), false, 16, 16);
                }
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 36, 18, 18);
                itemIdx++;
                putFluidCounter++;
            }
        }
    }
}
