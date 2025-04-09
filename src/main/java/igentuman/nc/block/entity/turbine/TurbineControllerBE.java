package igentuman.nc.block.entity.turbine;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import igentuman.nc.compat.cc.NCTurbinePeripheral;

import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCSounds.TURBINE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static net.minecraft.core.particles.ParticleTypes.*;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class TurbineControllerBE extends MultiblockControllerBE {

    public static String NAME = "turbine_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public Direction orientation = Direction.NORTH;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public int realFlow = 0;
    @NBTField
    public double coilsEfficiency = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    protected boolean forceShutdown = false;
    @NBTField
    public int activeCoils = 0;
    @NBTField
    public float flow = 0;
    @NBTField
    public float rotationSpeed = 0;
    @NBTField
    public int blades = 0;
    @NBTField
    public double efficiency = 0;
    protected Direction facing;
    public Recipe recipe;
    public HashMap<String, Recipe> cachedRecipes = new HashMap<>();
    private List<FluidStack> allowedInputs;

    public TurbineControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(TurbineRegistration.TURBINE_BE.get(NAME).get(), pPos, pBlockState);
        multiblock = new TurbineMultiblock(this);
        contentHandler = new SidedContentHandler(
                0, 0,
                1, 1, 1000, 10000);
        contentHandler().fluidCapability.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidCapability.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setBlockEntity(this);
        contentHandler().setAllowedInputFluids(0, this::getAllowedInputFluids);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    @Override
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public BlockPos getBlockPosForSteam()
    {
        if(!getMultiblock().isFormed()) {
            getMultiblock().validate();
        }
        BlockPos start = worldPosition;
        if(!getMultiblock().bearingPositions.isEmpty()) {
            for (int i = 0; i < getMultiblock().bearingPositions.size(); i++) {
                start = new BlockPos(getMultiblock().bearingPositions.get(i));
                BlockEntity be = getLevel().getExistingBlockEntity(start.relative(orientation));
                if(!(be instanceof TurbineRotorBE)) {
                    return start;
                }
            }
        }
        return start;
    }

    @Override
    public Recipe getRecipe() {
        if(contentHandler().fluidCapability.tanks.get(0).isEmpty()) return null;
        return (Recipe) super.getRecipe();
    }


    private LazyOptional<NCTurbinePeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new NCTurbinePeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    protected void playRunningSound() {
        if(isRemoved() || (currentSound != null && !currentSound.getLocation().equals(TURBINE.get().getLocation()))) {
            SoundHandler.stopTileSound(getBlockPos());
            currentSound = null;
        }
        if((currentSound == null || !Minecraft.getInstance().getSoundManager().isActive(currentSound))) {
            if(currentSound != null && currentSound.getLocation().equals(TURBINE.get().getLocation())) {
                return;
            }

            playSoundCooldown = 20;
            currentSound = SoundHandler.startTileSound(TURBINE.get(), SoundSource.BLOCKS, 0.2f, level.getRandom(), getBlockPos());
        }
    }

    public void tickClient() {
        if(!isCasingValid || !isInternalValid) {
            stopSound();
            return;
        }
        if(rotationSpeed > 0) {
            spawnSteamParticles();
            playRunningSound();
        } else {
            stopSound();
        }
    }
    protected int reValidateCounter = 0;



    public void tickServer() {
        rotationSpeed = 0;
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        changed = false;
        super.tickServer();
        boolean wasPowered = powered;
        handleValidation();
        trackChanges(wasPowered, powered);
        controllerEnabled = (hasRedstoneSignal() || controllerEnabled) && getMultiblock().isFormed();
        controllerEnabled = !forceShutdown && controllerEnabled;

        if (getMultiblock().isFormed()) {
            trackChanges(contentHandler().tick());
            if(controllerEnabled) {
                powered = processRecipe();
                trackChanges(powered);
            } else {
                powered = false;
            }
            handleMeltdown();
        }
        refreshCacheFlag = !getMultiblock().isFormed()  || level.getGameTime() % 100 == 0;
        if(wasPowered != powered) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
        }
        if(refreshCacheFlag || changed) {
            try {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }

        controllerEnabled = false;
    }

    public List<FluidStack> getAllowedInputFluids()
    {
        if(allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for(NcRecipe recipe: NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel())) {
                for(FluidStackIngredient ingredient: recipe.getInputFluids()) {
                    allowedInputs.addAll(ingredient.getRepresentations());
                }
            }
        }
        return allowedInputs;
    }

    @Override
    public TurbineMultiblock getMultiblock() {
        if(multiblock == null) {
            multiblock = new TurbineMultiblock(this);
        }
        return (TurbineMultiblock) multiblock;
    }


    private void handleValidation() {
        if(multiblock == null) return;
        if (level.getGameTime() % 40 == 0) {
            ValidationResult wasResult = validationResult;
            boolean wasFormed = getMultiblock().isFormed();
            if (!wasFormed || !isInternalValid || !isCasingValid) {
                activeCoils = 0;
                coilsEfficiency = 0;
                flow = 0;
                getMultiblock().validate();
                isCasingValid = getMultiblock().isOuterValid();
                if (isCasingValid) {
                    isInternalValid = getMultiblock().isInnerValid();
                }
                powered = false;
                changed = true;
            }
            validationResult = getMultiblock().validationResult;
            if (validationResult.id != wasResult.id) {
                changed = true;
            }
            if (activeCoils != getMultiblock().activeCoils) {
                changed = true;
                activeCoils = getMultiblock().activeCoils;
                coilsEfficiency = getMultiblock().coilsEfficiency;
            }

            if (flow != getMultiblock().flow) {
                changed = true;
                flow = getMultiblock().flow;
                blades = getMultiblock().blades;
            }
            height = getMultiblock().height();
            width = getMultiblock().width();
            depth = getMultiblock().depth();
            trackChanges(wasFormed, getMultiblock().isFormed());
        }
    }

    public float bladesEfficiency()
    {
        if(blades == 0) return 0;
        return flow/blades;
    }

    public float getEfficiencyRate() {
        return (float) (Math.log10(activeCoils) * coilsEfficiency * bladesEfficiency())/1000f;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private void handleMeltdown() {

    }

    public void setRemoved() {
        super.setRemoved();
        if(getLevel().isClientSide()) {
            return;
        }
        if(getMultiblock() != null) {
            getMultiblock().onControllerRemoved();
        }
    }

    private boolean processRecipe() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().fluidCapability.getFluidInSlot(0).isEmpty()) {
                recipeInfo().clear();
            }
        }
        if (!hasRecipe()) {
            updateRecipe();
        }
        if (hasRecipe()) {
            return process();
        }
        return false;
    }

    public List<BlockPos> getBlocks(BlockPos pos, Direction.Axis axis) {
        List<BlockPos> positions = new ArrayList<>();
        int y = pos.getY();
        int z = pos.getZ();
        int x = pos.getX();
        switch (axis) {
            case X:
                // Generate positions around the BlockPos on the YZ plane
                positions.add(pos.offset(0, -1, -1));
                positions.add(pos.offset(0, -1, 1));
                positions.add(pos.offset(0, 1, 1));
                positions.add(pos.offset(0, 1, -1));
                break;
            case Y:
                // Generate positions around the BlockPos on the XZ plane
                positions.add(pos.offset( -1, 0,-1));
                positions.add(pos.offset( -1, 0, 1));
                positions.add(pos.offset( 1, 0, 1));
                positions.add(pos.offset( 1, 0, -1));
                break;
            case Z:
                // Generate positions around the BlockPos on the XY planed
                positions.add(pos.offset(-1, -1, 0));
                positions.add(pos.offset(1, -1, 0));
                positions.add(pos.offset(1, 1, 0));
                positions.add(pos.offset(-1, 1, 0));
                break;
        }

        return positions;
    }

    private void spawnSteamParticles() {
        if (level.isClientSide && level.getGameTime() % 4 == 0) {
            BlockPos pos = getBlockPosForSteam().relative(orientation.getOpposite(), 1);
            for(BlockPos source:  getBlocks(pos, getMultiblock().turbineDirection.getAxis())){
                for (int i = 0; i < 3; i++) {
                    double x = source.getX() + 0.4f + level.random.nextGaussian() * 0.2;
                    double y = source.getY() + 0.7f + level.random.nextGaussian() * 0.2;
                    double z = source.getZ() - 0.4f + level.random.nextGaussian() * 0.2;
                    float ySpeed = 0;
                    float zSpeed = 0;
                    float xSpeed = 0;
                    switch (getMultiblock().turbineDirection) {
                        case UP:
                            ySpeed = 0.2f + (float)(height)*0.02f;
                            break;
                        case DOWN:
                            ySpeed = -0.1f - (float)(height)*0.02f;
                            break;
                        case NORTH:
                            zSpeed = -0.1f - (float)(depth)*0.02f;
                            z += 0.5;
                            break;
                        case SOUTH:
                            zSpeed = 0.1f + (float)(depth)*0.02f;
                            z += 0.5;
                            break;
                        case EAST:
                            xSpeed = 0.1f + (float)(width)*0.02f;
                            z += 0.5;
                            break;
                        case WEST:
                            xSpeed = -0.1f - (float)(width)*0.02f;
                            z += 0.5;
                            break;
                    }
                    level.addParticle(CLOUD, x, y, z, xSpeed, ySpeed, zSpeed);
                }
            }
        }
    }

    private boolean process() {
        recipeInfo().process(1);
        flow = Math.max(1, flow);
        float realFlow = (float)getRealFlow();
        if(realFlow > 0) {
            this.realFlow = (int) Math.min(realFlow, getMaxFlow());
        }
        rotationSpeed = (rotationSpeed*4+realFlow/(flow*TURBINE_CONFIG.BLADE_FLOW.get()))/5f;
        energyStorage().addEnergy(calculateEnergy());
        efficiency = calculateEfficiency();
        handleRecipeOutput();
        contentHandler().fluidCapability.tanks.get(0).drain(this.realFlow, EXECUTE);

        return true;
    }

    private float getMaxFlow() {
        return flow*TURBINE_CONFIG.BLADE_FLOW.get();
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo().recipe();
            }
            if (recipe.handleOutputs(contentHandler())) {
                recipeInfo().clear();
                if(contentHandler().fluidCapability.getFluidInSlot(0).isEmpty()) {
                    recipe = null;
                }
            } else {
                recipeInfo().stuck = true;
            }
            setChanged();
        }
    }

    public float coilsDrag()
    {
        return (float) Math.max(1, (100/coilsEfficiency * Math.log(Math.log10(activeCoils+4)+2)));
    }

    public int getRealFlow()
    {
        int wasFlow = realFlow;
        float cleanFlow = Math.min(flow*TURBINE_CONFIG.BLADE_FLOW.get(), getFluidTank(0).getFluidAmount());
        realFlow = (int) (cleanFlow / coilsDrag());
        if(wasFlow != realFlow) {
            changed = true;
        }
        return realFlow;
    }

    private int calculateEnergy() {
        int wasEnergy = energyPerTick;
        energyPerTick = (int)(realFlow*TURBINE_CONFIG.ENERGY_GEN.get()*getEfficiencyRate()*ENERGY_GENERATION.GENERATION_MULTIPLIER.get()/7);
        if(wasEnergy != energyPerTick) {
            changed = true;
        }
        return energyPerTick;
    }


    private void updateRecipe() {
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe)recipeInfo().recipe()).getBaseTime();
            recipeInfo().energy = recipeInfo().recipe.getEnergy();
            recipeInfo().be = this;
            //recipe.consumeInputs(contentHandler);
        }
    }

    public boolean recipeIsStuck() {
        return recipeInfo().isStuck();
    }

    public boolean hasRecipe() {
        return recipeInfo().recipe() != null;
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public double calculateEfficiency() {
        return (double) energyPerTick / (recipeInfo.energy / 100);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo().ticksProcessed > 0 && !recipeInfo().isCompleted();
    }

    public int getActiveCoils() {
        return activeCoils;
    }

    public int getFlow() {
        return (int) flow;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public int getFlowRatio() {
        return (int) (((float) realFlow / getMaxFlow())*100);
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(TurbineControllerBE.NAME, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return TurbineControllerBE.NAME;
        }

        @Override
        public @NotNull String getGroup() {
            return TurbineControllerBE.NAME;
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(TURBINE_BLOCKS.get(getCodeId()).get());
        }

        public int getBaseTime() {
            return (int) Math.max(1, timeModifier);
        }

        public double getEnergy() { return Math.max(1, powerModifier); }

        public double ratio = 1D;

        @Override
        public boolean consumeInputs(SidedContentHandler contentHandler, int parallelProcessing) {
            TurbineControllerBE be = (TurbineControllerBE)contentHandler.blockEntity;
            int flow = be.realFlow;
            ratio = (double)flow/(double)getInputFluids(0).get(0).getAmount();
            FluidStack holded = contentHandler.fluidCapability.getFluidInSlot(0).copy();
            holded.setAmount(flow);
            contentHandler.fluidCapability.holdedInputs.add(holded);
            contentHandler.fluidCapability.tanks.get(0).drain(flow, EXECUTE);
            return false;
        }

        @Override
        public boolean handleOutputs(SidedContentHandler contentHandler) {
            FluidStack outputFluid = outputFluids[0].getRepresentations().get(0);
            FluidStack toOutput = outputFluid.copy();
            TurbineControllerBE be = (TurbineControllerBE)contentHandler.blockEntity;
            int flow = be.realFlow;
            ratio = (double)flow/(double)getInputFluids(0).get(0).getAmount();
            int toPush = (int) (outputFluid.getAmount()*ratio);
            toOutput.setAmount(toPush);
            return contentHandler.fluidCapability.insertFluidInternal(1, toOutput, true).getAmount() != toPush;
        }
    }
}
