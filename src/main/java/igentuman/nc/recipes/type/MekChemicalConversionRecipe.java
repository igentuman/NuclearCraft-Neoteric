package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.commonRl;
import static net.minecraft.world.item.Items.BUCKET;

public class MekChemicalConversionRecipe extends NcRecipe {

    public static class Type extends MekChemicalConversionRecipe {
        public Type() {
            super("mek_chemical", new ItemStackIngredient[0], new ItemStackIngredient[0], new FluidStackIngredient[0], new FluidStackIngredient[0], 1, 1, 1, 1);
        }
    }
    public ChemicalStack inputChemical;
    public FluidStack outputFluid;
    public MekChemicalConversionRecipe(String codeId, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(codeId, input, output, timeModifier, powerModifier, radiation, rarityModifier);
    }

    public MekChemicalConversionRecipe(ChemicalStack input, FluidStack outputFluid) {
        super("mek_chemical_conversion", new ItemStackIngredient[0], new ItemStackIngredient[0], 1, 1, 1, 1);
        this.inputChemical = input;
        this.outputFluid = outputFluid;
    }

    public static FluidStack getStackByTagCode(String name)
    {
        TagKey<Fluid> key = TagUtil.createKey(BuiltInRegistries.FLUID, commonRl(name));
        if(TagUtil.isTagEmpty(BuiltInRegistries.FLUID, key)) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        try {
            fluidStack = FluidStackIngredientCreator.INSTANCE
                    .from(key, 1000).getRepresentations().get(0);
        } catch (Exception e) {

        }
        return fluidStack;
    }

    public static FluidStack getFluidByChemical(Chemical chemical) {
        String name = chemical.getRegistryName().getPath();
        return getStackByTagCode(name);
    }

    public static List<MekChemicalConversionRecipe> getRecipes() {
        List<MekChemicalConversionRecipe> recipes = new ArrayList<>();

        for(Map.Entry<ResourceKey<Chemical>, Chemical> entry : MekanismAPI.CHEMICAL_REGISTRY.entrySet()) {
            FluidStack fluid = getFluidByChemical(entry.getValue());
            if(fluid.isEmpty()) continue;
            recipes.add(new MekChemicalConversionRecipe(new ChemicalStack(entry.getValue(), 1000), fluid));
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
    public void write(RegistryFriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeDouble(rarityModifier);
    }
}
