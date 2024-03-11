package igentuman.nc.handler.sided.capability;

import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.Action;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;

public class Slurry2FluidConverter implements ISlurryHandler {

    private FluidCapabilityHandler fluidCapability;
    private Direction side;

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull SlurryStack getChemicalInTank(int tank) {
        return getEmptyStack();
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull SlurryStack stack) {

    }

    @Override
    public long getTankCapacity(int tank) {
        return 100;
    }

    @Override
    public boolean isValid(int tank, @NotNull SlurryStack stack) {
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

    private HashMap<Slurry, Fluid> gasFluidMap = new HashMap<>();

    private FluidStack convert(SlurryStack stack) {
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
    public @NotNull SlurryStack insertChemical(int tank, @NotNull SlurryStack stack, @NotNull Action action) {
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
    public @NotNull SlurryStack extractChemical(int tank, long amount, Action action) {
        return getEmptyStack();
    }

    public void setFluidHandler(FluidCapabilityHandler fluidCapability) {
        this.fluidCapability = fluidCapability;
    }

    public Slurry2FluidConverter forSide(Direction side) {
        this.side = side;
        return this;
    }

    @Override
    public @NotNull SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }
}
