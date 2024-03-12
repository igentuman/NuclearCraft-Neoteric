package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;
import static net.minecraft.item.Items.BUCKET;

public class MekChemicalConversionRecipe extends NcRecipe {

    public static class Type extends MekChemicalConversionRecipe {
        public Type() {
            super(rl("mek_chemical"), new ItemStackIngredient[0], new ItemStack[0], new FluidStackIngredient[0], new FluidStack[0], 1, 1, 1, 1);
        }
    }
    public ChemicalStack<?> inputChemical;
    public FluidStack outputFluid;
    public MekChemicalConversionRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(id, input, output, timeModifier, powerModifier, radiation, rarityModifier);
    }

    public MekChemicalConversionRecipe(ChemicalStack<?> input, FluidStack outputFluid) {
        super(new ResourceLocation("mek_chemical_conversion"), new ItemStackIngredient[0], new ItemStack[0], 1, 1, 1, 1);
        this.inputChemical = input;
        this.outputFluid = outputFluid;
    }

    public static FluidStack getStackByTagCode(String name)
    {
        Tags.IOptionalNamedTag<Fluid> fluidITag = TagUtil.createFluidTagKey(new ResourceLocation("forge", "fluids/" + name));
        if(fluidITag.getValues().isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        try {
            fluidStack = FluidStackIngredientCreator.INSTANCE
                    .from(fluidITag.getName().getPath(), 1000).getRepresentations().get(0);
        } catch (Exception e) {

        }
        return fluidStack;
    }

    public static FluidStack getFluidBySlurry(Slurry gas) {
        String name = gas.getName();
        return getStackByTagCode(name);
    }

    public static FluidStack getFluidByGas(Gas gas) {
        String name = gas.getName();
        return getStackByTagCode(name);
    }

    public static List<MekChemicalConversionRecipe> getRecipes() {
        List<MekChemicalConversionRecipe> recipes = new ArrayList<>();

        for(Map.Entry<RegistryKey<Gas>, Gas> gas: MekanismAPI.gasRegistry().getEntries()) {
            FluidStack fluid = getFluidByGas(gas.getValue());
            if(fluid.isEmpty()) continue;
            recipes.add(new MekChemicalConversionRecipe(new GasStack(gas.getValue(), 1000), fluid));
        }
        for(Map.Entry<RegistryKey<Slurry>, Slurry> slurry: MekanismAPI.slurryRegistry().getEntries()) {
            FluidStack fluid = getFluidBySlurry(slurry.getValue());
            if(fluid.isEmpty()) continue;
            recipes.add(new MekChemicalConversionRecipe(new SlurryStack(slurry.getValue(), 1000), fluid));
        }

        return recipes;
    }

    @Override
    public @NotNull String getGroup() {
        return "mek_chemical_conversion";
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(BUCKET);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeDouble(rarityModifier);
    }
}
