package igentuman.nc.handler.sided.capability;

import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;

public class Gas2FluidConverter implements IGasHandler {

    private FluidCapabilityHandler fluidCapability;
    private Direction side;

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull GasStack getChemicalInTank(int tank) {
        return getEmptyStack();
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull GasStack stack) {

    }

    @Override
    public long getTankCapacity(int tank) {
        return 100;
    }

    @Override
    public boolean isValid(int tank, @NotNull GasStack stack) {
        return false;
    }

    private String specialConvertRules(String input)
    {
        if(input.matches("clean_[a-z]+")) {
            return input.substring(6)+"_clean_slurry";
        }
        if(input.matches("dirty_[a-z]+")) {
            return input.substring(6)+"_slurry";
        }
        return input;
    }

    private HashMap<Gas, Fluid> gasFluidMap = new HashMap<>();

    private FluidStack convert(GasStack stack) {
        int amount = (int)stack.getAmount();
        if(amount <= 0) amount = 1000;
        if(gasFluidMap.containsKey(stack.getType())) {
            if(!(gasFluidMap.get(stack.getType()) instanceof EmptyFluid)) {
                return new FluidStack(gasFluidMap.get(stack.getType()), amount);
            }
        }
        String name = stack.getTypeRegistryName().getPath();
        name = specialConvertRules(name);
        Tags.IOptionalNamedTag<Fluid> tag = TagUtil.createFluidTagKey(new ResourceLocation("forge", name));

        if(tag.getValues().isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        try {
            fluidStack = FluidStackIngredientCreator.INSTANCE
                    .from(tag.getName().getPath(), amount).getRepresentations().get(0);
        } catch (Exception e) {

        }

        gasFluidMap.put(stack.getType(), fluidStack.getFluid());
        return new FluidStack(gasFluidMap.get(stack.getType()), (int)stack.getAmount());
    }

    @Override
    public @NotNull GasStack insertChemical(int tank, @NotNull GasStack stack, @NotNull Action action) {
        FluidStack fluidStack = convert(stack);
        if(fluidStack.isEmpty()) return stack;
        for(int i = 0; i < fluidCapability.inputSlots; i++) {
            if(!fluidCapability.haveAccessFromSide(side, i)) continue;
            if(fluidCapability.isValidForInputSlot(i, fluidStack)) {
                boolean doInsert = action.execute();
                FluidStack inserted = fluidCapability.insertFluidInternal(i, fluidStack, doInsert);
                stack.setAmount(inserted.getAmount());
                return stack;
            }
        }
        return stack;
    }

    @Override
    public GasStack extractChemical(int tank, long amount, Action action) {
        return getEmptyStack();
    }

    public void setFluidHandler(FluidCapabilityHandler fluidCapability) {
        this.fluidCapability = fluidCapability;
    }

    public Gas2FluidConverter forSide(Direction side) {
        this.side = side;
        return this;
    }

    @Override
    public @NotNull GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }
}
