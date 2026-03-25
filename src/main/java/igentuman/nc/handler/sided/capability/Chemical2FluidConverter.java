package igentuman.nc.handler.sided.capability;

import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.commonRl;

/**
 * Unified chemical-to-fluid converter for Mekanism 10.7.x+.
 * Replaces the old Gas2FluidConverter and Slurry2FluidConverter classes
 * which were split by the pre-unification Gas/Slurry/InfuseType/Pigment API.
 */
public class Chemical2FluidConverter implements IChemicalHandler {

    private FluidCapabilityHandler fluidCapability;
    private Direction side;

    @Override
    public int getChemicalTanks() {
        return 1;
    }

    @Override
    public @NotNull ChemicalStack getChemicalInTank(int tank) {
        return ChemicalStack.EMPTY;
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return 100;
    }

    @Override
    public boolean isValid(int tank, @NotNull ChemicalStack stack) {
        return false;
    }

    private String specialConvertRules(String input) {
        if (input.matches("clean_[a-z]+")) {
            return input.substring(6) + "_clean_slurry";
        }
        if (input.matches("dirty_[a-z]+")) {
            return input.substring(6) + "_slurry";
        }
        return input;
    }

    private final HashMap<Chemical, Fluid> chemicalFluidMap = new HashMap<>();

    private FluidStack convert(ChemicalStack stack) {
        int amount = (int) stack.getAmount();
        if (amount <= 0) amount = 1000;
        Chemical chemical = stack.getChemical();
        if (chemicalFluidMap.containsKey(chemical)) {
            if (!(chemicalFluidMap.get(chemical) instanceof EmptyFluid)) {
                return new FluidStack(chemicalFluidMap.get(chemical), amount);
            }
        }
        String name = stack.getTypeRegistryName().getPath();
        name = specialConvertRules(name);
        TagKey<Fluid> key = TagUtil.createKey(BuiltInRegistries.FLUID, commonRl(name));
        if (TagUtil.isTagEmpty(BuiltInRegistries.FLUID, key)) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack;
        try {
            fluidStack = FluidStackIngredientCreator.INSTANCE
                    .from(key, amount).getRepresentations().get(0);
        } catch (Exception e) {
            return FluidStack.EMPTY;
        }

        chemicalFluidMap.put(chemical, fluidStack.getFluid());
        return new FluidStack(chemicalFluidMap.get(chemical), (int) stack.getAmount());
    }

    @Override
    public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
        FluidStack fluidStack = convert(stack);
        if (fluidStack.isEmpty()) return stack;
        for (int i = 0; i < fluidCapability.inputSlots; i++) {
            if (!fluidCapability.haveAccessFromSide(side, i)) continue;
            if (fluidCapability.isValidForInputSlot(i, fluidStack)) {
                boolean doInsert = action.execute();
                FluidStack inserted = fluidCapability.insertFluidInternal(i, fluidStack, doInsert);
                ChemicalStack result = stack.copy();
                result.setAmount(inserted.getAmount());
                return result;
            }
        }
        return stack;
    }

    @Override
    public @NotNull ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
        return ChemicalStack.EMPTY;
    }

    public void setFluidHandler(FluidCapabilityHandler fluidCapability) {
        this.fluidCapability = fluidCapability;
    }

    public Chemical2FluidConverter forSide(Direction side) {
        this.side = side;
        return this;
    }
}
