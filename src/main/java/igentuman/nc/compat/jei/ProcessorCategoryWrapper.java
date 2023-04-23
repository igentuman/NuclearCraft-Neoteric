package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.setup.processors.ProcessorPrefab;
import igentuman.nc.setup.processors.Processors;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.*;

public class ProcessorCategoryWrapper<T extends NcRecipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE = rl("textures/gui/processor_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private  IDrawable[] slots;
    protected RecipeType<T> recipeType;
    private IGuiHelper guiHelper;
    private final ProcessorPrefab processor;
    private int xShift = -25;
    private int yShift = -25;

    public ProcessorCategoryWrapper(IGuiHelper guiHelper, RecipeType<T> recipeType) {
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        processor = Processors.all().get(getRecipeType().getUid().getPath());
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 140, 45);
        if(CATALYSTS.containsKey(getRecipeType().getUid().getPath())) {
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        }
        arrow = guiHelper.drawableBuilder(rl("textures/gui/progress_jei.png"), 0, 0, 36, 15)
                .buildAnimated(Processors.all().get(getRecipeType().getUid().getPath()).config().getTime(),
                IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("nc_jei_cat."+getRecipeType().getUid().getPath());
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
                     double mouseY) {
        arrow.draw(stack, 48, 15);
        for(int i = 0; i < slots.length; i++) {
            if(slots[i] != null) {
                int[] pos = processor.getSlotsConfig().getSlotPositions().get(i);
                slots[i].draw(stack, pos[0]+xShift-1, pos[1]+yShift-1);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {

        int itemIdx = 0;
        int inputCounter = 0;
        int inputFluidCounter = 0;
        int putFluidCounter = 0;
        int outputCounter = 0;


        slots = new IDrawable[processor.getSlotsConfig().getSlotPositions().size()];
        for(int[] pos: processor.getSlotsConfig().getSlotPositions()) {
            if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_in")) {
                builder.addSlot(RecipeIngredientRole.INPUT, pos[0]+xShift, pos[1]+yShift).addIngredients(recipe.getItemIngredients().get(inputCounter));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 0, 18, 18);
                itemIdx++;
                inputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_out")) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0]+xShift, pos[1]+yShift).addItemStack(recipe.getResultItems().get(outputCounter));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 36, 18, 18);
                itemIdx++;
                outputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_in")) {
                builder.addSlot(RecipeIngredientRole.INPUT, pos[0]+xShift, pos[1]+yShift)
                        .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInputFluids(inputFluidCounter))
                        .setFluidRenderer(8000, false, 16, 16);
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
                itemIdx++;
                inputFluidCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_out")) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0]+xShift, pos[1]+yShift)
                        .addIngredients(ForgeTypes.FLUID_STACK, recipe.getOutputFluids(putFluidCounter))
                        .setFluidRenderer(8000, false, 16, 16);
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 36, 18, 18);
                itemIdx++;
                putFluidCounter++;
            }
        }
    }
}
