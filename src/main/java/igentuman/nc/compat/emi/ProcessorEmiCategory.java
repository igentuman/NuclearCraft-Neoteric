package igentuman.nc.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static net.minecraft.world.item.Items.BARRIER;

public class ProcessorEmiCategory extends BasicEmiRecipe {
    public static final ResourceLocation TEXTURE = rl("textures/gui/processor_jei.png");
    
    private final NcRecipe recipe;
    private final ProcessorPrefab processor;
    private int xShift = -25;
    private int yShift = -28;
    private final int recipeHeight;

    public ProcessorEmiCategory(EmiRecipeCategory category, NcRecipe recipe, String processorName) {
        super(category, recipe.getId(), 150, 45);
        this.recipe = recipe;
        this.processor = Processors.all().get(processorName);
        
        // Adjust height based on processor configuration
        if (processor.getSlotsConfig().isDoubleSlotHeight()) {
            yShift = -25;
            this.recipeHeight = 45;
        } else {
            yShift = -38;
            this.recipeHeight = 22;
        }
        this.height = recipeHeight;
        
        // Add inputs
        for (int i = 0; i < recipe.getItemIngredients().size(); i++) {
            ItemStack[] items = recipe.getItemIngredients().get(i).getItems();
            this.inputs.add(EmiIngredient.of(Arrays.stream(items).map(EmiStack::of).toList()));
        }
        
        // Add fluid inputs
        for (int i = 0; i < recipe.getInputFluids().length; i++) {
            List<FluidStack> fluids = recipe.getInputFluids(i);
            this.inputs.add(EmiIngredient.of(fluids.stream()
                    .map(fluid -> EmiStack.of(fluid.getFluid(), fluid.getAmount())).toList()));
        }
        
        // Add outputs
        for (int i = 0; i < recipe.getResultItems().size(); i++) {
            this.outputs.add(EmiStack.of(recipe.getResultItems().get(i)));
        }
        
        // Add fluid outputs
        for (FluidStack fluid : recipe.getOutputFluids()) {
            if (!fluid.isEmpty()) {
                this.outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()));
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int itemIdx = 0;
        int inputCounter = 0;
        int inputFluidCounter = 0;
        int outputCounter = 0;
        int outputFluidCounter = 0;
        
        int fluidsOut = processor.getSlotsConfig().getOutputFluids();
        int itemsOut = processor.getSlotsConfig().getOutputItems();
        int itemsIn = processor.getSlotsConfig().getInputItems();
        int fluidsIn = processor.getSlotsConfig().getInputFluids();

        int barXshift = 0;
        if (fluidsOut + itemsOut == 3 || fluidsOut + itemsOut == 6) {
            barXshift = -8;
        }
        int extraXshift = 0;
        if (fluidsOut + itemsOut > 6) {
            extraXshift = -20;
        }
        if (itemsIn + fluidsIn > 5) {
            extraXshift = 20;
        }

        // Add slots based on processor configuration
        for (int[] pos : processor.getSlotsConfig().getSlotPositions()) {
            String slotType = processor.getSlotsConfig().getSlotType(itemIdx);
            
            if (slotType.contains("item_in") && inputCounter < inputs.size()) {
                widgets.addSlot(inputs.get(inputCounter), pos[0] + xShift + barXshift, pos[1] + yShift);
                inputCounter++;
            } else if (slotType.contains("item_out") && outputCounter < outputs.size()) {
                widgets.addSlot(outputs.get(outputCounter), pos[0] + xShift + barXshift, pos[1] + yShift).recipeContext(this);
                outputCounter++;
            } else if (slotType.contains("fluid_in") && inputFluidCounter < recipe.getInputFluids().length) {
                if (inputCounter < inputs.size()) {
                    widgets.addSlot(inputs.get(inputCounter), pos[0] + xShift + barXshift, pos[1] + yShift);
                    inputCounter++;
                }
                inputFluidCounter++;
            } else if (slotType.contains("fluid_out") && outputFluidCounter < recipe.getOutputFluids().size()) {
                if (!recipe.getOutputFluids().get(outputFluidCounter).isEmpty() && outputCounter < outputs.size()) {
                    widgets.addSlot(outputs.get(outputCounter), pos[0] + xShift + barXshift, pos[1] + yShift).recipeContext(this);
                    outputCounter++;
                }
                outputFluidCounter++;
            }
            itemIdx++;
        }

        // Add progress arrow
        int duration = (int) (recipe.getTimeModifier() * processor.config().getTime() * 10);
        widgets.addFillingArrow(47 + xShift + 25 + barXshift + extraXshift, recipeHeight / 2 - 8, duration);

    }

    public static EmiRecipeCategory createCategory(String processorName) {
        ResourceLocation id = rl(processorName);
        EmiStack icon;
        
        if (CATALYSTS.containsKey(processorName)) {
            icon = EmiStack.of(CATALYSTS.get(processorName).get(0));
        } else {
            icon = EmiStack.of(new ItemStack(BARRIER));
        }
        
        return new EmiRecipeCategory(id, icon);
    }

    public NcRecipe getRecipe() {
        return recipe;
    }
}