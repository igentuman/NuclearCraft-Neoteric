package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class NcRecipe implements Recipe<IgnoredIInventory> {
    private final ResourceLocation id;

    protected ItemStackIngredient input;
    protected FluidStackIngredient inputFluid;
    protected FluidStack outFluid;

    protected ItemStack output;
    protected double timeModifier = 1;
    protected double powerModifier = 1;
    protected double radiationModifier = 1;

    protected FluidStackIngredient[] inputFluids;
    protected FluidStack[] outputFluids;

    protected ItemStackIngredient[] inputItems;
    protected ItemStack[] outputItems;

    /**
     * @param id Recipe name.
     */
    protected NcRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
    }

    public NonNullList<Ingredient> getItemIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStackIngredient inputItem : inputItems) {
            ingredients.add(Ingredient.of(inputItem.getRepresentations().toArray(new ItemStack[inputItem.getRepresentations().size()])));
        }
        return ingredients;
    }

    public abstract void write(FriendlyByteBuf buffer);

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(@NotNull IgnoredIInventory inv, @NotNull Level world) {
        return !isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public abstract boolean isIncomplete();

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
    public List<ItemStack> getResultItems() {
        return List.of(getResultItem());
    }

    @NotNull
    public FluidStack getResultFluid() {
        return FluidStack.EMPTY;
    }

    public List<FluidStack> getResultFluids() {
        return List.of(getResultFluid());
    }

    public List<FluidStack> getInputFluids(int id) {
        NonNullList<FluidStack> ingredients = NonNullList.create();
        int i = 0;
        for (FluidStackIngredient inFluid : inputFluids) {
            if(i == id) ingredients.addAll(inFluid.getRepresentations());
            i++;
        }
        return ingredients;
    }

    public List<FluidStack> getOutputFluids(int id) {
        NonNullList<FluidStack> ingredients = NonNullList.create();
        int i = 0;
        for (FluidStack outFluid : outputFluids) {
            if(i == id) ingredients.add(outFluid);
            i++;
        }
        return ingredients;
    }

    public double getTimeModifier() {
        return timeModifier*20;
    }

    public double getEnergy() {
        return powerModifier;
    }

    public double getRadiation() {
        return radiationModifier;
    }

    public boolean handleOutputs(SidedContentHandler contentHandler) {
        int i = contentHandler.inputItemSlots;
        for(ItemStack outputItem: getResultItems()) {
            if(!contentHandler.itemHandler.isValidForOutputSlot(i, outputItem)) {
                if(!contentHandler.itemHandler.canPushExcessItems(i, outputItem)) return false;
            }
            i++;
        }
        i = contentHandler.inputItemSlots;
        for(ItemStack outputItem: getResultItems()) {
            if(!contentHandler.itemHandler.insertItemInternal(i, outputItem, false).isEmpty()) {
                if(!contentHandler.itemHandler.pushExcessItems(i, outputItem).isEmpty()) {
                    NuclearCraft.LOGGER.error("Failed to push excess items from recipe output.");
                    return false;
                }
            }
            i++;
        }
        contentHandler.clearHolded();
        return true;
    }

    public void extractInputs(SidedContentHandler contentHandler) {
        int i = 0;
        if(contentHandler.hasFluidCapability(null)) {
            for (FluidStackIngredient inputFluid : inputFluids) {
                for (FluidStack fluidStack : inputFluid.getRepresentations()) {
                    if (fluidStack.isFluidEqual(contentHandler.fluidCapability.tanks.get(i).getFluid())) {
                        contentHandler.fluidCapability.holdedInputs.add(fluidStack.copy());
                        contentHandler.fluidCapability.tanks.get(i).drain(fluidStack, EXECUTE);
                    }
                }
                i++;
            }
        }
        i=0;
        if(contentHandler.hasItemCapability(null)) {
            for (ItemStackIngredient inputItem : inputItems) {
                for (ItemStack itemStack : inputItem.getRepresentations()) {
                    contentHandler.itemHandler.holdedInputs.add(itemStack.copy());
                    contentHandler.itemHandler.extractItemInternal(i, itemStack.getCount(), false);
                }
                i++;
            }
        }
    }
}