package igentuman.nc.block.entity.fusion;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.compat.cc.NCFusionReactorPeripheral;
import igentuman.nc.compat.cc.NCSolidFissionReactorPeripheral;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.util.ModUtil.isCcLoaded;

public class FusionCoreBE <RECIPE extends FusionCoreBE.Recipe> extends FusionBE {

    @NBTField
    public double heat = 0;
    @NBTField
    public boolean isCasingValid = false;

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

    protected boolean initialized = false;

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

    private LazyOptional<NCFusionReactorPeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCFusionReactorPeripheral(this));
        }
        return peripheralCap.cast();
    }
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler.getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        if(isCcLoaded()) {
            return getPeripheral(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        initialized = false;
    }

    public double getMaxHeat() {
        return 10000000;
    }
    @Override
    public void tickServer() {
        if(!initialized) {
            initialized = true;
            FusionCoreBlock block = (FusionCoreBlock) getBlockState().getBlock();
            block.placeProxyBlocks(getBlockState(), level, worldPosition, this);
        }
        super.tickServer();
    }

    //todo implement
    public double getNetHeat() {
        return 0;
    }

    public boolean hasRecipe() {
        return recipeInfo.recipe() != null;
    }

    public double getRecipeProgress() {
        return 0;
    }

    public void disableForceShutdown() {
    }

    public void forceShutdown() {
    }

    public void voidFuel() {
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
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
            return FusionReactor.FUSION_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FusionReactor.FUSION_BLOCKS.get(codeId).get());
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
