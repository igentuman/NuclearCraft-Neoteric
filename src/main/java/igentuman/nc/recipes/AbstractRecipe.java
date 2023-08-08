package igentuman.nc.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static net.minecraft.world.item.Items.BARRIER;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class AbstractRecipe implements Recipe<IgnoredIInventory> {
    private final ResourceLocation id;
    public static String ID;

    protected double timeModifier = 1;
    protected double powerModifier = 1;
    protected double radiationModifier = 1;

    protected FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
    protected FluidStack[] outputFluids = new FluidStack[0];
    protected ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
    protected ItemStack[] outputItems = new ItemStack[0];

    protected List<FluidStack> resolvedInputFluids;
    protected List<FluidStack> resolvedOutputFluids;

    /**
     * @param id Recipe name.
     */
    protected AbstractRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
    }

    public NonNullList<Ingredient> getItemIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStackIngredient inputItem : inputItems) {
            ingredients.add(Ingredient.of(inputItem.getRepresentations().toArray(new ItemStack[inputItem.getRepresentations().size()])));
        }
        return ingredients;
    }
    public ItemStack getFirstItemStackIngredient(int id) {
        return inputItems[id].getRepresentations().get(0);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(ID).get();
    }

    @Override
    public @NotNull String getGroup() {
        return NCProcessors.PROCESSORS.get(ID).get().getName().getString();
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NCProcessors.PROCESSORS.get(ID).get());
    }

    @Override
    public @NotNull RecipeType<? extends AbstractRecipe> getType() {
        return NcRecipeType.ALL_RECIPES.get(ID).get();
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

    public boolean isIncomplete()
    {
        boolean empty = inputFluids.length == 0 && outputFluids.length == 0 && inputItems.length == 0 && outputItems.length == 0;
        if(empty) return true;
        for(ItemStackIngredient inputItem: inputItems) {
            if(inputItem.getRepresentations().isEmpty()
                    || inputItem.getRepresentations().get(0).getItem().equals(BARRIER)) {
                return true;
            }
        }
        for(FluidStackIngredient inputFluid: inputFluids) {
            if(inputFluid.getRepresentations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public List<ItemStack> getResultItems() {
        return List.of(outputItems);
    }

    public List<FluidStack> getInputFluids(int id) {
        if(inputFluids.length > id) return inputFluids[id].getRepresentations();
        return List.of(FluidStack.EMPTY);
    }

    public List<FluidStack> getOutputFluids(int id) {
        if(outputFluids.length > id) return List.of(outputFluids[id]);
        return List.of(FluidStack.EMPTY);
    }

    public double getTimeModifier() {
        return timeModifier;
    }

    public double getEnergy() {
        return powerModifier;
    }

    public double getRadiation() {
        return radiationModifier;
    }

    public boolean handleOutputs(SidedContentHandler contentHandler) {
        int i = contentHandler.inputItemSlots;
        for(ItemStack outputItem: outputItems) {
            if(!contentHandler.itemHandler.isValidForOutputSlot(i, outputItem)) {
                if(!contentHandler.itemHandler.canPushExcessItems(i, outputItem)) return false;
            }
            i++;
        }
        i = contentHandler.inputItemSlots;
        for(ItemStack outputItem: outputItems) {
            ItemStack toOutput = outputItem.copy();
            if(!contentHandler.itemHandler.insertItemInternal(i, toOutput, false).isEmpty()) {
                if(!contentHandler.itemHandler.pushExcessItems(i, toOutput).isEmpty()) {
                    return false;
                }
            }
            i++;
        }

        i = contentHandler.inputFluidSlots;
        for(FluidStack outputFluid: outputFluids) {
            if(!contentHandler.fluidCapability.isValidForOutputSlot(i, outputFluid)) {
                if(!contentHandler.fluidCapability.canPushExcessFluid(i, outputFluid)) return false;
            }
            i++;
        }
        i = contentHandler.inputFluidSlots;
        for(FluidStack outputFluid: outputFluids) {
            FluidStack toOutput = outputFluid.copy();
            if(!contentHandler.fluidCapability.insertFluidInternal(i, toOutput, false).isEmpty()) {
                if(!contentHandler.fluidCapability.pushExcessFluid(i, toOutput).isEmpty()) {
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

    public boolean test(SidedContentHandler contentHandler) {
        if(inputItems.length > 0 && inputFluids.length == 0) {
            return testItems(contentHandler.itemHandler);
        }
        if(inputFluids.length > 0 && inputItems.length == 0) {
            return testFluids(contentHandler.fluidCapability);
        }
        return testFluids(contentHandler.fluidCapability) && testItems(contentHandler.itemHandler);
    }

    private boolean testFluids(FluidCapabilityHandler fluidHandler) {
        for (int i = 0; i < inputFluids.length; i++) {
            if(!hasFluidInSlots(fluidHandler, inputFluids[i])) return false;
        }
        return true;
    }

    private boolean hasFluidInSlots(FluidCapabilityHandler fluidHandler, FluidStackIngredient fluid) {
        for(int i = 0; i < fluidHandler.inputSlots; i++) {
            if(fluid.test(fluidHandler.getFluidInSlot(i))) return true;
        }
        return false;
    }

    private boolean testItems(ItemCapabilityHandler itemHandler) {
        for (int i = 0; i < inputItems.length; i++) {
            if(!hasItemInSlots(itemHandler, inputItems[i])) return false;
        }
        return true;
    }

    private boolean hasItemInSlots(ItemCapabilityHandler itemHandler, ItemStackIngredient item) {
        for(int i = 0; i < itemHandler.inputSlots; i++) {
            if(item.test(itemHandler.getStackInSlot(i))) return true;
        }
        return false;
    }

    public ItemStack getOutputItem(int id) {
        if(getResultItems().size() > id) return getResultItems().get(id);
        return ItemStack.EMPTY;
    }

    public Ingredient getInputIngredient(int inputCounter) {
        if(getItemIngredients().size() > inputCounter) return getItemIngredients().get(inputCounter);
        return Ingredient.EMPTY;
    }

    public @NotNull ItemStack getResultItem() {
        return getOutputItem(0);
    }
}