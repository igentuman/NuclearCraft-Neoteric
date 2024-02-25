package igentuman.nc.recipes.type;

import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.util.TagUtil;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.registration.impl.GasDeferredRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.item.Items.BUCKET;

public class MekChemicalConversionRecipe extends NcRecipe {

    public static class Type extends MekChemicalConversionRecipe {
        public Type() {
            super(rl("mek_chemical"), new ItemStackIngredient[0], new ItemStackIngredient[0], new FluidStackIngredient[0], new FluidStackIngredient[0], 1, 1, 1, 1);
        }
    }
    public ChemicalStack<?> inputChemical;
    public FluidStack outputFluid;
    public MekChemicalConversionRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(id, input, output, timeModifier, powerModifier, radiation, rarityModifier);
    }

    public MekChemicalConversionRecipe(ChemicalStack<?> input, FluidStack outputFluid) {
        super(new ResourceLocation("mek_chemical_conversion"), new ItemStackIngredient[0], new ItemStackIngredient[0], 1, 1, 1, 1);
        this.inputChemical = input;
        this.outputFluid = outputFluid;
    }

    public static FluidStack getStackByTagCode(String name)
    {
        ITagManager<Fluid> tagManager = TagUtil.manager(ForgeRegistries.FLUIDS);
        TagKey<Fluid> key = tagManager.createTagKey(new ResourceLocation("forge", name));
        ITag<Fluid> fluidITag = TagUtil.tag(ForgeRegistries.FLUIDS, key);
        if(fluidITag.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        try {
            fluidStack = FluidStackIngredientCreator.INSTANCE
                    .from(fluidITag.getKey(), 1000).getRepresentations().get(0);
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

        for(Map.Entry<ResourceKey<Gas>, Gas> gas: MekanismAPI.gasRegistry().getEntries()) {
            FluidStack fluid = getFluidByGas(gas.getValue());
            if(fluid.isEmpty()) continue;
            recipes.add(new MekChemicalConversionRecipe(new GasStack(gas.getValue(), 1000), fluid));
        }
        for(Map.Entry<ResourceKey<Slurry>, Slurry> slurry: MekanismAPI.slurryRegistry().getEntries()) {
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
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeDouble(rarityModifier);
    }
}
