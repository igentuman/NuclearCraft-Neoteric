package igentuman.nc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.compat.jei.util.TickTimer;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.gui.element.bar.ProgressBar.bars;
import static igentuman.nc.compat.GlobalVars.*;
@SuppressWarnings("all")
public class ProcessorCategoryWrapper<T extends NcRecipe> implements IRecipeCategory<T> {
    public final static ResourceLocation TEXTURE = rl("textures/gui/processor_jei.png");

    private final IDrawable background;
    private IDrawable progressBackground;
    private final IDrawable icon;
    private  IDrawable[] slots;
    protected RecipeType<T> recipeType;
    private IGuiHelper guiHelper;
    private final ProcessorPrefab processor;
    private int xShift = -26;
    private int yShift = -39;
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
            this.icon = guiHelper.createDrawableIngredient(CATALYSTS.get(getRecipeType().getUid().getPath()).get(0));
        } else{
            this.icon = guiHelper.createDrawableIngredient(ItemStack.EMPTY);
        }
    }

    public @NotNull RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public @NotNull Component getTitle() {
        return new TranslatableComponent("nc_jei_cat."+getRecipeType().getUid().getPath());
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
    public void setIngredients(T t, IIngredients iIngredients) {
        if(t.getInputFluids().length > 0) {
            List<FluidStack> inputFluids = new ArrayList<>();
            for (FluidStackIngredient fluidStack : t.getInputFluids()) {
                inputFluids.add(fluidStack.getRepresentations().get(0));
            }
            iIngredients.setInputs(VanillaTypes.FLUID, inputFluids);
        }
        if(t.getOutputFluids().length > 0) {
            iIngredients.setOutputs(VanillaTypes.FLUID, Arrays.stream(t.getOutputFluids()).toList());
        }
        if(t.getItemIngredients().size() > 0) {
            List<ItemStack> inputItems = new ArrayList<>();
            for (Ingredient ingredient : t.getItemIngredients()) {
                inputItems.add(ingredient.getItems()[0]);
            }
            iIngredients.setInputs(VanillaTypes.ITEM, inputItems);
        }
        if(t.getResultItems().size() > 0) {
            iIngredients.setOutputs(VanillaTypes.ITEM, t.getResultItems());
        }
    }

    @Override
    public void draw(T recipe, PoseStack stack, double mouseX,
                     double mouseY) {
        int d = (int) ((recipe.getTimeModifier()*(double) processor.config().getTime())/2);
        int fluidsOut = processor.getSlotsConfig().getOutputFluids();
        int itemsOut = processor.getSlotsConfig().getOutputItems();
        int itemsIn = processor.getSlotsConfig().getInputItems();
        int fluidsIn = processor.getSlotsConfig().getInputFluids();

        int barXshift = 0;
        if(fluidsOut + itemsOut == 3 || fluidsOut + itemsOut == 6) {
            barXshift = -8;
        }
        int extraXshift = 0;
        if(fluidsOut + itemsOut > 6) {
            extraXshift = -20;
        }
        if(itemsIn + fluidsIn > 5) {
            extraXshift = 20;
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
    public @NotNull List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
        List<Component> lines = new ArrayList<>();
        if(mouseX > 47+xShift+25 && mouseX < 47+xShift+25+36 && mouseY > height/2-16/2 && mouseY < height/2+16/2) {
            lines.add(new TranslatableComponent("processor.recipe.duration", (int)(recipe.getTimeModifier()*(double) processor.config().getTime())).withStyle(ChatFormatting.AQUA));
            lines.add(new TranslatableComponent("processor.recipe.power", (int)(recipe.getEnergy()*(double) processor.config().getPower())).withStyle(ChatFormatting.RED));
            if(recipe.getRadiation() != 1D) {
                lines.add(new TranslatableComponent("processor.recipe.radiation", recipe.getRadiation()*1000).withStyle(ChatFormatting.GREEN));
            }
        }
        return lines;
    }

    @Override
    public ResourceLocation getUid() {
        return recipeType.getUid();
    }

    @Override
    public Class<? extends T> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, T recipe, IIngredients iIngredients) {

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
        int fluidTankCapacity = 16;
        for(FluidStackIngredient fluidStack: recipe.getInputFluids()) {
            fluidTankCapacity = Math.max(fluidTankCapacity, fluidStack.getAmount());
        }
        for(FluidStack fluidStack: recipe.getOutputFluids()) {
            fluidTankCapacity = Math.max(fluidTankCapacity, fluidStack.getAmount());
        }
        for(int[] pos: processor.getSlotsConfig().getSlotPositions()) {
            if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_in")) {
                iRecipeLayout.getItemStacks().init(itemIdx, true,  pos[0]+xShift-1+barXshift, pos[1]+yShift-1);
                iRecipeLayout.getItemStacks().set(itemIdx, iIngredients.getInputs(VanillaTypes.ITEM).get(itemIdx));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 0, 18, 18);
                itemIdx++;
                inputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("item_out")) {
                iRecipeLayout.getItemStacks().init(itemIdx, false,  pos[0]+xShift-1+barXshift, pos[1]+yShift-1);
                iRecipeLayout.getItemStacks().set(itemIdx, iIngredients.getOutputs(VanillaTypes.ITEM).get(itemIdx-inputCounter-inputFluidCounter));
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 0, 36, 18, 18);
                itemIdx++;
                outputCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_in")) {
                if(!recipe.getInputFluids(inputFluidCounter).get(0).equals(FluidStack.EMPTY)) {
                    iRecipeLayout.getFluidStacks().init(itemIdx, true,  pos[0]+xShift+barXshift, pos[1]+yShift);
                    iRecipeLayout.getFluidStacks().set(itemIdx, iIngredients.getInputs(VanillaTypes.FLUID).get(itemIdx));
                }
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 0, 18, 18);
                itemIdx++;
                inputFluidCounter++;
            } else if(processor.getSlotsConfig().getSlotType(itemIdx).contains("fluid_out")) {
                if (!recipe.getOutputFluids(putFluidCounter).get(0).equals(FluidStack.EMPTY)) {
                    iRecipeLayout.getFluidStacks().init(itemIdx, false,  pos[0]+xShift+barXshift, pos[1]+yShift);
                    iRecipeLayout.getFluidStacks().set(itemIdx, iIngredients.getOutputs(VanillaTypes.FLUID).get(itemIdx-inputCounter-inputFluidCounter));
                }
                slots[itemIdx] = guiHelper.createDrawable(rl("textures/gui/widgets.png"), 18, 36, 18, 18);
                itemIdx++;
                putFluidCounter++;
            }
        }
    }
}
