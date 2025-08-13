package igentuman.nc.block.turbine.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.client.sound.SoundHandler;
import igentuman.nc.compat.cc.TurbinePeripheral;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.fission.FissionControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.handler.config.TurbineConfig.TURBINE_CONFIG;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCSounds.TURBINE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static net.minecraft.core.particles.ParticleTypes.CLOUD;
import static net.minecraft.world.level.block.Blocks.AIR;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class TurbineControllerBE extends MultiblockControllerBE {

    public static String NAME = "turbine_controller";
    public final SidedContentHandler contentHandler;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    @NBTField
    public BlockPos bearingPos = BlockPos.ZERO;
    @NBTField
    public Direction orientation = Direction.NORTH;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public int maxFlow = 0;
    @NBTField
    public int maxEnergy = 0;
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

        contentHandler = new SidedContentHandler(
                0, 0,
                1, 1, 1000, 10000);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.INPUT);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.OUTPUT);
        contentHandler().setBlockEntity(this);
        contentHandler().setAllowedInputFluids(0, this::getAllowedInputFluids);
        energyStorage = createEnergy();
        energyStorage.setInputEnergyTier(0)
                .setOutputEnergyTier(getBaseGTEnergyTier())
                .setInputAmperage(0)
                .setOutputAmperage(16);
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.TURBINE_ENERGY_TIER.get().ordinal();
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

    public void calculateMaxEnergy()
    {
        maxEnergy = (int)(Math.sqrt(((long)maxFlow+1)*((long)maxFlow+2)/2D)*TURBINE_CONFIG.ENERGY_GEN.get()*getEfficiencyRate()*ENERGY_GENERATION.GENERATION_MULTIPLIER.get());
    }

    public BlockPos getBlockPosForSteam()
    {
        BlockPos start = bearingPos.relative(orientation, 1);
        if(!getLevel().getBlockState(start).is(AIR)) {
            return start;
        } else {
            start = bearingPos.relative(orientation.getOpposite(), width-1);
        }
        return start;
    }

    public void voidFluidSlot(int slotId) {
        if(contentHandler() != null) {
            contentHandler().voidFluidSlot(slotId);
            setChanged();
        }
    }

    @Override
    public Recipe getRecipe() {
        if(contentHandler().fluidHandler.tanks.get(0).isEmpty()) return null;
        return (Recipe) super.getRecipe();
    }


    private LazyOptional<TurbinePeripheral> peripheralCap;

    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new TurbinePeripheral(this));
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
        playSound(TURBINE, 0.2f);
    }

    public void tickClient() {
        super.tickClient();
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
            setChanged();
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
        }
        if(refreshCacheFlag || changed) {
            try {
                setChanged();
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
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new TurbineMultiblock(this);
        }
        return (TurbineMultiblock) multiblock;
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

    private boolean processRecipe() {
        if(recipeInfo().recipe != null && recipeInfo().isCompleted()) {
            if(contentHandler().fluidHandler.getFluidInSlot(0).isEmpty()) {
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
        if (level.getGameTime() % Math.ceil(Math.log(1/(getRotationSpeed()+0.001D))+1) == 0) {
            BlockPos pos = getBlockPosForSteam();
            for(BlockPos source:  getBlocks(pos, orientation.getAxis())){
                for (int i = 0; i < 3; i++) {
                    double x = source.getX() + 0.4f + level.random.nextGaussian() * 0.2;
                    double y = source.getY() + 0.7f + level.random.nextGaussian() * 0.2;
                    double z = source.getZ() + 0.4f + level.random.nextGaussian() * 0.2;
                    float ySpeed = 0;
                    float zSpeed = 0;
                    float xSpeed = 0;
                    switch (orientation) {
                        case UP:
                            ySpeed = 0.05f + (float)(height-1)*0.03f;
                            break;
                        case DOWN:
                            ySpeed = -0.05f - (float)(height-1)*0.03f;
                            break;
                        case NORTH:
                            zSpeed = -0.05f - (float)(depth-1)*0.03f;
                            z -= 0.5;
                            break;
                        case SOUTH:
                            zSpeed = 0.05f + (float)(depth-1)*0.03f;
                            z -= 0.5;
                            break;
                        case EAST:
                            xSpeed = 0.05f + (float)(width-1)*0.03f;
                            z -= 0.5;
                            break;
                        case WEST:
                            xSpeed = -0.05f - (float)(width-1)*0.03f;
                            z -= 0.5;
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
        double realFlow = getRealFlow();
        if(realFlow > 0) {
            this.realFlow = (int) Math.min(realFlow, maxFlow);
        }
        rotationSpeed = (float) ((rotationSpeed*4+realFlow/(flow*TURBINE_CONFIG.BLADE_FLOW.get()))/5f);
        energyStorage().addEnergy(calculateEnergy());
        efficiency = calculateEfficiency();
        handleRecipeOutput();
        contentHandler().fluidHandler.tanks.get(0).drain(this.realFlow, EXECUTE);

        return true;
    }

    public void calculateMaxFlow() {
        maxFlow = (int) (flow*TURBINE_CONFIG.BLADE_FLOW.get()*(Math.pow(Math.log10(flow), 2.8)));
    }

    private void handleRecipeOutput() {
        if (hasRecipe() && recipeInfo().isCompleted()) {
            if(recipe == null) {
                recipe = (Recipe) recipeInfo().recipe();
            }
            if (recipe.handleOutputs(contentHandler())) {
                updateRecipe();
                if(contentHandler().fluidHandler.getFluidInSlot(0).isEmpty()) {
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
        double cleanFlow = Math.min(maxFlow, getFluidTank(0).getFluidAmount());
        realFlow = (int) (cleanFlow / coilsDrag());
        if(wasFlow != realFlow) {
            changed = true;
        }
        return realFlow;
    }

    private int calculateEnergy() {
        int wasEnergy = energyPerTick;
        energyPerTick = (int)(Math.sqrt(((long)getRealFlow()+1)*((long)getRealFlow()+2)/2D)*TURBINE_CONFIG.ENERGY_GEN.get()*getEfficiencyRate()*ENERGY_GENERATION.GENERATION_MULTIPLIER.get()*recipeInfo().energy/2);
        if(wasEnergy != energyPerTick) {
            changed = true;
        }
        return energyPerTick;
    }


    private void updateRecipe() {
        //check if last recipe is still valid
        if(recipe != null) {
            if(recipe.test(contentHandler())) {
                recipeInfo().ticksProcessed = 0;
                return;
            } else {
                recipeInfo().clear();
            }
        }
        recipe = getRecipe();
        if (recipe != null) {
            recipeInfo().setRecipe(recipe);
            recipeInfo().ticks = ((Recipe)recipeInfo().recipe()).getBaseTime();
            recipeInfo().energy = recipeInfo().recipe.getEnergy();
            recipeInfo().be = this;
            //recipe.consumeInputs(contentHandler);
        } else {
            recipeInfo().clear();
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
        return (int) (((float) realFlow / maxFlow)*100);
    }

    public void refresh() {
        double multiplier = ((double) Math.round(Math.log(height*width*depth)*10)/10)-1;
        contentHandler().fluidHandler.tanks.get(0).setCapacity((int) (Math.pow(multiplier, 2)*1_000_000));
        contentHandler().fluidHandler.tanks.get(1).setCapacity((int) (Math.pow(multiplier, 2)*1_000_000));
        calculateMaxFlow();
        calculateMaxEnergy();
        setChanged();
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
            FluidStack holded = contentHandler.fluidHandler.getFluidInSlot(0).copy();
            holded.setAmount(flow);
            contentHandler.fluidHandler.holdedInputs.add(holded);
            contentHandler.fluidHandler.tanks.get(0).drain(flow, EXECUTE);
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
            return contentHandler.fluidHandler.insertFluidInternal(1, toOutput, true).getAmount() != toPush;
        }
    }
}
