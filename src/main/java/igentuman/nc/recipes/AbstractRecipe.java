package igentuman.nc.recipes;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.minecraft.block.Blocks.AIR;
import static net.minecraft.item.Items.BARRIER;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class AbstractRecipe implements IRecipe<IgnoredIInventory> {
    private final ResourceLocation id;
    public final String codeId;

    protected double timeModifier = 1;
    protected double powerModifier = 1;
    protected double radiationModifier = 1;

    public FluidStackIngredient[] getInputFluids() {
        return inputFluids;
    }

    public FluidStack[] getOutputFluids() {
        return outputFluids;
    }

    protected FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
    protected FluidStack[] outputFluids = new FluidStack[0];
    protected ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
    protected ItemStack[] outputItems = new ItemStack[0];

    protected List<FluidStack> resolvedInputFluids;
    protected List<FluidStack> resolvedOutputFluids;

    /**
     * @param id     Recipe name.
     */
    protected AbstractRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
        this.codeId = getCodeId();
    }

    public String getCodeId() {
        return id.getPath().split("/")[0];
    }


    public NonNullList<Ingredient> getItemIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStackIngredient inputItem : inputItems) {
            if(inputItem == null) continue;
            ingredients.add(Ingredient.of(inputItem.getRepresentations().toArray(new ItemStack[inputItem.getRepresentations().size()])));
        }
        return ingredients;
    }
    public ItemStack getFirstItemStackIngredient(int id) {
        return inputItems[id].getRepresentations().get(0);
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(codeId).get();
    }

    @Override
    public @NotNull String getGroup() {
        return codeId;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        Block proc = AIR;
        if(NCProcessors.PROCESSORS.containsKey(codeId)) {
            proc = NCProcessors.PROCESSORS.get(codeId).get();
        }
        return new ItemStack(proc);
    }

    @Override
    public @NotNull IRecipeType<? extends AbstractRecipe> getType() {
        return NcRecipeType.ALL_RECIPES.get(codeId);
    }

    public abstract void write(PacketBuffer buffer);

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(@NotNull IgnoredIInventory inv, @NotNull World world) {
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
            if(
                    inputItem == null ||
                    inputItem.getRepresentations().isEmpty()
                    || inputItem.getRepresentations().get(0).getItem().equals(BARRIER)) {
                return true;
            }
        }
        for(ItemStack output: outputItems) {
            if(output == null || output.isEmpty()
                    || output.getItem().equals(BARRIER)) {
                return true;
            }
        }
        for(FluidStackIngredient inputFluid: inputFluids) {
            if(inputFluid == null || inputFluid.getRepresentations().isEmpty()) {
                return true;
            }
        }
        for(FluidStack output: outputFluids) {
            if(output == null || output.isEmpty()) {
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
        return Arrays.asList(outputItems);
    }

    public List<FluidStack> getInputFluids(int id) {
        if(inputFluids.length > id) return inputFluids[id].getRepresentations();
        return Arrays.asList(FluidStack.EMPTY);
    }

    public List<FluidStack> getOutputFluids(int id) {
        if(outputFluids.length > id) return Arrays.asList(outputFluids[id]);
        return Arrays.asList(FluidStack.EMPTY);
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

    public void consumeInputs(SidedContentHandler contentHandler) {
        if(contentHandler.hasFluidCapability(null)) {
            for (FluidStackIngredient inputFluid : inputFluids) {
                int i = 0;
                assert contentHandler.fluidCapability != null;
                for(FluidTank tank : contentHandler.fluidCapability.tanks) {
                    if(contentHandler.inputFluidSlots <= i) break;
                    FluidStack fluidStack = tank.getFluid();
                    if(inputFluid.test(fluidStack)) {
                        FluidStack holded = fluidStack.copy();
                        holded.setAmount(inputFluid.getAmount());
                        contentHandler.fluidCapability.holdedInputs.add(holded);
                        contentHandler.fluidCapability.tanks.get(i).drain(inputFluid.getRepresentations().get(0).getAmount(), EXECUTE);
                        break;
                    }
                    i++;
                }
            }
        }
        if(contentHandler.hasItemCapability(null)) {
            for (ItemStackIngredient inputItem : inputItems) {
                assert contentHandler.itemHandler != null;
                for(int i = 0; i < inputItems.length; i++) {
                    if( ! inputItem.test(contentHandler.itemHandler.getStackInSlot(i))) {
                        continue;
                    }
                    ItemStack extracted = contentHandler.itemHandler.extractItemInternal(i, inputItem.getAmount(), false);
                    if (!extracted.isEmpty()) {
                        contentHandler.itemHandler.holdedInputs.add(
                                extracted
                        );
                        break;
                    }
                }
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
        if(outputItems.length == 0) return ItemStack.EMPTY;
        return outputItems[0] != null ? outputItems[0] : ItemStack.EMPTY;
    }
}