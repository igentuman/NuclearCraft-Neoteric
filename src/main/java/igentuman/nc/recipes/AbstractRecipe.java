package igentuman.nc.recipes;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;
import static igentuman.nc.util.NcUtils.getModId;
import static net.minecraft.world.item.Items.BARRIER;
import static net.minecraft.world.level.block.Blocks.AIR;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class AbstractRecipe implements Recipe<IgnoredIInventory> {

    private ResourceLocation id;
    public final String codeId;
    protected double timeModifier = 1;
    protected double powerModifier = 1;
    protected double radiationModifier = 1;
    protected FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
    protected FluidStackIngredient[] outputFluids = new FluidStackIngredient[0];
    protected ItemStackIngredient[] inputItems = new ItemStackIngredient[0];
    protected ItemStackIngredient[] outputItems = new ItemStackIngredient[0];
    protected List<ItemStack> cachedOutputItems;
    protected List<FluidStack> cachedOutputFluids;

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return getResultItem();
    }
    public FluidStackIngredient[] getInputFluids() {
        return inputFluids;
    }

    public List<FluidStack> getOutputFluids() {
        if(cachedOutputFluids == null) {
            cachedOutputFluids = new ArrayList<>();
            for (FluidStackIngredient outputFluid : outputFluids) {
                if(outputFluid.getRepresentations().size() == 1) {
                    cachedOutputFluids.add(outputFluid.getRepresentations().get(0));
                    continue;
                }
                resolve:
                for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
                    FluidStack flowing = null;
                    for(FluidStack fluid: outputFluid.getRepresentations()) {
                        if(getModId(fluid).equals(mod) || getModId(fluid).equals("minecraft")) {
                            if(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath().contains("_flowing")) {
                                flowing = fluid;
                                continue; //skipping flowing types
                            }
                            cachedOutputFluids.add(fluid.copy());
                            break resolve;
                        }
                    } //if no still found
                    if(flowing != null) {
                        cachedOutputFluids.add(flowing.copy());
                    }
                }
            }
        }
        return cachedOutputFluids;
    }


    /**
     * @param codeId  Recipe type identifier (e.g. "fission", "fusion_core").
     */
    protected AbstractRecipe(String codeId) {
        this.codeId = Objects.requireNonNull(codeId, "Recipe codeId cannot be null.");
    }

    public String getCodeId() {
        return codeId;
    }


    public NonNullList<Ingredient> getItemIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStackIngredient inputItem : inputItems) {
            if(inputItem == null) {
                ingredients.add(Ingredient.EMPTY);
                continue;
            }
            ingredients.add(Ingredient.of(inputItem.getRepresentations().toArray(new ItemStack[inputItem.getRepresentations().size()])));
        }
        return ingredients;
    }
    public ItemStack getFirstItemStackIngredient(int id) {
        ItemStack[] items = getInputIngredient(0).getItems();
        return items.length > id ? items[id] : ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        var entry = NcRecipeSerializers.SERIALIZERS.get(codeId);
        if (entry == null) throw new IllegalStateException("No serializer for recipe type: " + codeId);
        return entry.get();
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
    public @NotNull RecipeType<? extends AbstractRecipe> getType() {
        var entry = NcRecipeType.ALL_RECIPES.get(codeId);
        if (entry == null) throw new IllegalStateException("No recipe type registered for: " + codeId);
        return entry.get();
    }

    public abstract void write(RegistryFriendlyByteBuf buffer);

    @NotNull
    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
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
        boolean empty = (inputFluids.length == 0 && inputItems.length == 0) || (outputFluids.length == 0 && outputItems.length == 0);
        if(empty) return true;
        for(ItemStackIngredient inputItem: inputItems) {
            if(inputItem == null || inputItem.getRepresentations().isEmpty()
                    || inputItem.getRepresentations().get(0).is(BARRIER)) {
                return true;
            }
        }
        for(ItemStackIngredient output: outputItems) {
            if(output == null || output.getRepresentations().isEmpty()
                    || output.getRepresentations().get(0).is(BARRIER)) {
                return true;
            }
        }
        for(FluidStackIngredient inputFluid: inputFluids) {
            if(inputFluid == null || inputFluid.getRepresentations().isEmpty()) {
                return true;
            }
        }
        for(FluidStackIngredient output: outputFluids) {
            if(output == null || output.getRepresentations().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public List<ItemStack> getResultItems() {
        if(cachedOutputItems == null) {
            cachedOutputItems = new ArrayList<>();
            for (ItemStackIngredient outputItem : outputItems) {
                if(outputItem == null) continue;
                List<ItemStack> items = outputItem.getRepresentations();
                if(items.size() == 1) {
                    cachedOutputItems.add(items.get(0));
                    continue;
                }
                resolve:
                for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
                    for(ItemStack item: items) {
                        if(getModId(item).equals(mod)) {
                            cachedOutputItems.add(item);
                            break resolve;
                        }
                    }
                }
                if(cachedOutputItems.isEmpty()) {
                    for(ItemStack item: items) {
                        if(!item.isEmpty() && !item.is(BARRIER)) {
                            cachedOutputItems.add(item);
                            break;
                        }
                    }
                }
            }
        }
        return cachedOutputItems;
    }

    public List<FluidStack> getInputFluids(int id) {
        if(inputFluids.length > id) return inputFluids[id].getRepresentations();
        return List.of(FluidStack.EMPTY);
    }

    public List<FluidStack> getOutputFluids(int id) {
        List<FluidStack> outputs = getOutputFluids();
        return outputs.size() > id ? List.of(outputs.get(id)) : List.of(FluidStack.EMPTY);
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
        for(ItemStack outputItem: getResultItems()) {
            if(!contentHandler.itemHandler.insertItemInternal(i, outputItem, true).isEmpty()) {
                if(!contentHandler.itemHandler.canPushExcessItems(i, outputItem)) return false;
            }
            i++;
        }

        i = contentHandler.inputFluidSlots;
        for(FluidStack outputFluid: getOutputFluids()) {
            if(!contentHandler.fluidHandler.isValidForOutputSlot(i, outputFluid)) {
                if(!contentHandler.fluidHandler.canPushExcessFluid(i, outputFluid)) return false;
            }
            i++;
        }

        i = contentHandler.inputFluidSlots;
        for(FluidStack outputFluid: getOutputFluids()) {
            if(!contentHandler.fluidHandler.insertFluidInternal(i, outputFluid, true).isEmpty()) {
                if(!contentHandler.fluidHandler.pushExcessFluid(i, outputFluid).isEmpty()) {
                    return false;
                }
            }
            i++;
        }

        i = contentHandler.inputItemSlots;
        for(ItemStack outputItem: getResultItems()) {
            ItemStack toOutput = outputItem.copy();
            if(!contentHandler.itemHandler.insertItemInternal(i, toOutput, false).isEmpty()) {
                if(!contentHandler.itemHandler.pushExcessItems(i, toOutput).isEmpty()) {
                    return false;
                }
            }
            i++;
        }

        contentHandler.clearHolded();
        return true;
    }

    public boolean hasEnoughToConsume(SidedContentHandler contentHandler, int parallelProcessing) {
        if(contentHandler.hasFluidCapability(null)) {
            for (FluidStackIngredient inputFluid : inputFluids) {
                FluidStackIngredient toConsume = inputFluid.copy();
                toConsume.setAmount(inputFluid.getAmount() * parallelProcessing);
                int i = 0;
                assert contentHandler.fluidHandler != null;
                boolean found = false;
                for(FluidTank tank : contentHandler.fluidHandler.tanks) {
                    if(contentHandler.inputFluidSlots <= i) break;
                    FluidStack fluidStack = tank.getFluid();
                    if(toConsume.test(fluidStack)) {
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found) {
                    return false;
                }
            }
        }
        if(contentHandler.hasItemCapability(null)) {
            for (ItemStackIngredient inputItem : inputItems) {
                ItemStackIngredient toConsume = inputItem.copy();
                toConsume.setAmount(inputItem.getAmount() * parallelProcessing);
                assert contentHandler.itemHandler != null;
                boolean found = false;
                for(int i = 0; i < inputItems.length; i++) {
                    if(toConsume.test(contentHandler.itemHandler.getStackInSlot(i))) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean consumeInputs(SidedContentHandler contentHandler, int parallelProcessing) {
        if (!hasEnoughToConsume(contentHandler, parallelProcessing)) {
            return false;
        }
        if(contentHandler.hasFluidCapability(null)) {
            for (FluidStackIngredient inputFluid : inputFluids) {
                FluidStackIngredient toConsume = inputFluid.copy();
                toConsume.setAmount(inputFluid.getAmount() * parallelProcessing);
                int i = 0;
                assert contentHandler.fluidHandler != null;
                for(FluidTank tank : contentHandler.fluidHandler.tanks) {
                    if(contentHandler.inputFluidSlots <= i) break;
                    FluidStack fluidStack = tank.getFluid();
                    if(toConsume.test(fluidStack)) {
                        FluidStack holded = fluidStack.copy();
                        holded.setAmount(toConsume.getAmount());
                        contentHandler.fluidHandler.holdedInputs.add(holded);
                        contentHandler.fluidHandler.tanks.get(i).drain(holded.getAmount(), EXECUTE);
                        break;
                    }
                    i++;
                }
            }
        }
        if(contentHandler.hasItemCapability(null)) {
            for (ItemStackIngredient inputItem : inputItems) {
                assert contentHandler.itemHandler != null;
                ItemStackIngredient toConsume = inputItem.copy();
                toConsume.setAmount(inputItem.getAmount() * parallelProcessing);
                for(int i = 0; i < inputItems.length; i++) {
                    if( ! toConsume.test(contentHandler.itemHandler.getStackInSlot(i))) {
                        continue;
                    }
                    ItemStack extracted = contentHandler.itemHandler.extractItemInternal(i, toConsume.getAmount(), false);
                    if (!extracted.isEmpty()) {
                        contentHandler.itemHandler.holdedInputs.add(
                                extracted
                        );
                        break;
                    }
                }
            }
        }
        return true;
    }

    public boolean test(SidedContentHandler contentHandler) {
        if(inputItems.length > 0 && inputFluids.length == 0) {
            return testItems(contentHandler.itemHandler);
        }
        if(inputFluids.length > 0 && inputItems.length == 0) {
            return testFluids(contentHandler.fluidHandler);
        }
        return testFluids(contentHandler.fluidHandler) && testItems(contentHandler.itemHandler);
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
        return !getResultItems().isEmpty() ? getResultItems().get(0) : ItemStack.EMPTY;
    }

    public ItemStackIngredient[] getInputItems() {
        return inputItems;
    }

    public boolean handleOutputs(SidedContentHandler contentHandler, ItemStack outputItem) {
        int i = contentHandler.inputItemSlots;
        if(!contentHandler.itemHandler.insertItemInternal(i, outputItem, true).isEmpty()) {
            if(!contentHandler.itemHandler.canPushExcessItems(i, outputItem)) return false;
        }
        ItemStack toOutput = outputItem.copy();
        if(!contentHandler.itemHandler.insertItemInternal(i, toOutput, false).isEmpty()) {
            if(!contentHandler.itemHandler.pushExcessItems(i, toOutput).isEmpty()) {
                return false;
            }
        }
        contentHandler.clearHolded();
        return true;
    }
}