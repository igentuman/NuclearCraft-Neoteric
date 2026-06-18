package igentuman.nc.block.accelerator.entity;

import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.compat.cc.LinearAcceleratorPeripheral;
import igentuman.nc.compat.oc2.LinearAcceleratorDevice;
import igentuman.nc.content.particles.*;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorMultiblock;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.oc2.LinearAcceleratorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.util.Equations.*;
import static igentuman.nc.util.ModUtil.*;

public class LinearAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static String NAME = "linear_accelerator_controller";

    private LazyOptional<LinearAcceleratorPeripheral> peripheralCap;

    public Recipe recipe;

    public LinearAcceleratorControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Recipe getRecipe() {
        if(contentHandler().itemHandler.getStackInSlot(0).isEmpty()) return null;
        NcRecipe cachedRecipe = getCachedRecipe();
        if(cachedRecipe instanceof Recipe cRecipe) {
            return cRecipe;
        }
        if(!NcRecipeType.ALL_RECIPES.containsKey("accelerator")) return null;
        for(NcRecipe recipe: NcRecipeType.getAllRecipesFor("accelerator", getLevel())) {
            if(recipe.test(contentHandler())) {
                addToCache(recipe);
                return (Recipe) recipe;
            }
        }
        return null;
    }

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new LinearAcceleratorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
        }
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER && energyStorage() != null) {
                if (isGTEUCapEnabled()) {
                    if (side != null && sideConfig.get(side.ordinal()) != SideModeToggleable.SideMode.DISABLED)
                        return getGTEnergy(this, side).cast();
                } else {
                    return LazyOptional.empty();
                }
            }
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return getEnergy().cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }
        return LazyOptional.empty();
    }


    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            stopSound();
        }
    }

    @Override
    public LinearAcceleratorMultiblock getMultiblock() {
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new LinearAcceleratorMultiblock(this);
        }
        return (LinearAcceleratorMultiblock) multiblock;
    }

    @Override
    protected void handleMeltdown() {
        if(isAcceleratorTooHot()) {
            heatStored /= 2;
            quenchMagnets();
            controllerEnabled = false;
        }
    }

    @Override
    protected AbstractAcceleratorMultiblock getAcceleratorMultiblock() {
        return getMultiblock();
    }


    @Override
    public HashMap<String, String> getAnalyzeReport() {
        HashMap<String, String> report = new HashMap<>();
        //report.put("report.nc.1.accelerator.all_coolers", String.valueOf(getMultiblock().coolers.size()));
        //report.put("report.nc.2.accelerator.valid_coolers", String.valueOf(getMultiblock().validCoolers));
        return report;
    }

    @Override
    protected boolean accelerateParticle() {
        if (isAcceleratorTooHot()) {
            return false;
        }
        initialFocus = 0;
        hasParticle = false;
        if(energyStorage().getEnergyStored() < energyRequired) {
            return false;
        }
        if(particleStorage.getParticle() == null) {
            getParticleFromIonSource();
        }
        if(particleStorage.getParticle() == null) {
            return false;
        }
        if(!drainEnergy()) {
            return false;
        }
        ParticleStack particleStack = particleStorage.getParticle();
        if(particleStack == null || particleStack.isEmpty()) {
            return false;
        }
        particleStack.setFocus(focusGain(focus, particleStack)-focusLoss(beamLength, particleStack)+initialFocus);
        particleStack.setMeanEnergy((long)(linacEnergyGain(acceleratingVoltage, particleStack)*(accelerationEnergy)));
        particleStorage.setParticleStack(particleStack);
        internalHeating((long) ((heatRate*(accelerationEnergy)+heatRate)/2));
        hasParticle = true;
        getMultiblock().extractParticle(particleStack);
        particleStorage.clearServer();
        return true;
    }

    private void getParticleFromIonSource() {
        ItemStack stack = contentHandler().itemHandler.getStackInSlot(0);
        if(stack != null && !stack.isEmpty()) {
            if (stack.getItem() instanceof ParticleSourceItem sourceItem) {
                stack = sourceItem.use(stack, 10000);
                ParticleStack particle = sourceItem.getParticleStack(stack);
                if (particle != null) {
                    initialFocus = 0.4d;
                    particle.setAmount(10000);
                    particleStorage.setParticleStack(particle);
                    contentHandler().itemHandler.setStackInSlot(0, stack);
                }
            } else {
                ParticleStack particle = ParticleSources.getParticleFromItem(stack);
                if (particle != null && ParticleSources.getAmountStored(stack) >= 10000/stack.getCount()) {
                    initialFocus = 0.4d;
                    particle.setAmount(10000);
                    particleStorage.setParticleStack(particle);
                    ParticleSources.use(stack, 10000/stack.getCount());
                    if(ParticleSources.getAmountStored(stack) < 10000/stack.getCount()) {
                        stack = ItemStack.EMPTY;
                    }
                    contentHandler().itemHandler.setStackInSlot(0, stack);
                }
            }
        }else {
            FluidStack fluidStack = contentHandler().fluidHandler.getFluidInSlot(0);
            if (fluidStack != null && !fluidStack.isEmpty()) {
                ParticleStack particle = ParticleSources.getParticleFromFluid(fluidStack);
                if (particle != null) {
                    initialFocus = 0.4d;
                    particle.setAmount(10000);
                    particleStorage.setParticleStack(particle);
                    contentHandler().fluidHandler.tanks.get(0).drain(1, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public int getDepth() {
        return Math.max(depth, width);
    }

    public int getWidth() {
        return Math.min(width, depth);
    }

    public int getHeight() {
        return height;
    }


    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> LinearAcceleratorDevice.createDevice(this)).cast();
    }


    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return "accelerator";
        }

        @Override
        public @NotNull String getGroup() {
            return "accelerator";
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(ACCELERATOR_BLOCKS.get(NAME).get());
        }

        public int getBaseTime() {
            return (int) (timeModifier * 50);
        }

        public double getEnergy() { return powerModifier * 1000; }
    }
}
