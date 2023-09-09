package igentuman.nc.block.entity.fusion;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;

public class FusionCoreBE <RECIPE extends FusionCoreBE.Recipe> extends FusionBE {

    @NBTField
    public double heat = 0;
    @NBTField
    public boolean isCasingValid = false;
    @NBTField
    public boolean isInternalValid = false;
    @NBTField
    public int size = 1;
    @NBTField
    public double heatSinkCooling = 0;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double heatMultiplier = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    protected boolean forceShutdown = false;

    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage = createEnergy();

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);
    public BlockPos errorBlockPos = BlockPos.ZERO;

    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public RecipeInfo recipeInfo = new RecipeInfo();
    public boolean controllerEnabled = false;
    public RECIPE recipe;
    public HashMap<String, RECIPE> cachedRecipes = new HashMap<>();

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public FusionCoreBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        contentHandler = new SidedContentHandler(
                0, 0,
                2, 2);
        contentHandler.setBlockEntity(this);
    }
    public double getMaxHeat() {
        return 10000000;
    }
    @Override
    public void tickServer() {
        super.tickServer();
    }

    //todo implement
    public double getNetHeat() {
        return 0;
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            ID = FissionControllerBE.NAME;
            CATALYSTS.put(ID, List.of(getToastSymbol()));
        }

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                fuelItem = (ItemFuel) getFirstItemStackIngredient(0).getItem();
            }
            return fuelItem;
        }

        @Override
        public @NotNull String getGroup() {
            return FissionReactor.MULTI_BLOCKS.get(ID).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FissionReactor.MULTI_BLOCKS.get(ID).get());
        }

        public int getDepletionTime() {
            return (int) (getFuelItem().depletion*20*timeModifier);
        }

        public double getEnergy() {
            return getFuelItem().forge_energy;
        }

        public double getHeat() {
            return getFuelItem().heat;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/10;
        }
    }
}
