package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.ReactorPebble;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.fission.MSRControllerBlock.POWERED;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.handler.config.FissionConfig.MSR_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.ITEM_PROPERTIES;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;
import static net.minecraft.world.item.Items.AIR;

public class MSRControllerBE extends MultiblockControllerBE {

    public static final String NAME = "msr_controller";
    public final SidedContentHandler contentHandler;
    private Direction facing;

    @NBTField
    public double maxHeat = FISSION_CONFIG.HEAT_CAPACITY.getDefault();
    @NBTField
    public double heat = 0;
    @NBTField
    public boolean powered = false;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public int connectedPorts = 0;
    @NBTField
    public boolean forceShutdown = false;
    @NBTField
    public int fuelCellsCount = 0;
    @NBTField
    public int heatExchangerCount = 0;
    @NBTField
    public double boilingPenalty = 0;
    @NBTField
    public int steamPerTick = 0;

    @NBTField public int pebbleCount = 0;
    @NBTField public double saltVolume = 0.0;      // mB
    @NBTField public double coolantVolume = 0.0;   // mB
    @NBTField public double depletedVolume = 0.0;  // mB
    @NBTField public double temperature = 20.0;    // °C
    @NBTField public double reactivity = 0.0;
    @NBTField public double pressure = 0.0;
    @NBTField public double impurity = 0.0;        // 0..1
    @NBTField public boolean isCritical = false;
    @NBTField public boolean portsLocked = false;

    // MSR Constants from MSR.md
    public static final double T_AMBIENT = 20.0;
    public static final double MAX_TEMPERATURE = 2000.0;
    public static final double PRESSURE_MAX = 150.0;
    public static final double PRESSURE_UNLOCK = 120.0;
    public static final double PRESSURE_PER_DEGREE = 0.015;
    public static final double PRESSURE_PER_DEPLETED_MB = 0.008;
    public static final double OPTIMAL_DENSITY = 0.025;
    public static final double CONCENTRATION_MODIFIER = 0.08;
    public static final double MIN_SALT_FOR_CRITICALITY = 500.0;
    public static final int MIN_PEBBLES_FOR_CRITICALITY = 20;

    private HashSet<ReactorPebble> pebbles = new HashSet<>();
    
    protected List<FissionControllerBE.FissionBoilingRecipe> coolantRecipes;
    protected FissionControllerBE.FissionBoilingRecipe boilingRecipe;
    
    private boolean portsInitialized = false;
    private long lastTickTime = -1L;
    private boolean changed = false;

    public MSRControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
        contentHandler = new SidedContentHandler(
                1, 1,
                4, 4);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(2, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidHandler.setGlobalMode(3, SlotModePair.SlotMode.PUSH);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        for(int i = 0; i < 4; i++) {
            contentHandler().fluidHandler.tanks.get(i).setCapacity(50000);
        }
    }

    public void initializePorts() {
        if(portsInitialized) return;
        portsInitialized = true;
        for(MultiblockPortBE port: getMultiblock().getPorts()) {
            port.pushPull();
        }
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
    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            return;
        }
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            controllerEnabled = false;
            return;
        }

        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        changed = false;

        super.tickServer();

        handleValidation();

        if (getMultiblock().isFormed()) {
            initializePorts();
            trackChanges(contentHandler().tick());
            
            // 1. Sync internal state
            syncInternalState();
            
            // 2. Simulation loop
            updateSimulation();
            
            // 3. Handle I/O
            handleIO();
            
            // 4. Block State Update
            updateBlockState();
        } else {
            powered = false;
            isCritical = false;
        }

        if(changed || currentTick % 40 == 0) {
            updateState();
        }
    }

    private void syncInternalState() {
        saltVolume = contentHandler().fluidHandler.tanks.get(0).getFluidAmount();
        coolantVolume = contentHandler().fluidHandler.tanks.get(1).getFluidAmount();
        // depletedVolume is accumulated internally and drained to tank 2
        pebbleCount = pebbles.size();
    }

    private void updateSimulation() {
        // 0. Compute concentration & reactivity modifiers
        double pDensity = (double) pebbleCount / Math.max(saltVolume, 1.0);
        double concentrationFactor = 1.0 + (pDensity - OPTIMAL_DENSITY) * CONCENTRATION_MODIFIER;
        
        // 1. Check criticality
        isCritical = (pebbleCount >= MIN_PEBBLES_FOR_CRITICALITY) && 
                     (saltVolume >= MIN_SALT_FOR_CRITICALITY) && 
                     !portsLocked && enabledByController;

        // 2. Reactivity & Feedback
        double thermalFeedback = Math.max(0.1, Math.min(2.0, 1.0 - (temperature - 600.0) * 0.001));
        double impurityFeedback = 1.0 - impurity;
        reactivity = thermalFeedback * impurityFeedback * concentrationFactor;

        // 3. Fission & Heat
        double totalHeatProduced = 0;
        double totalEnergyProduced = 0;
        
        if (isCritical) {
            for (ReactorPebble pebble : new HashSet<>(pebbles)) {
                pebble.tick(reactivity);
                totalHeatProduced += pebble.getHeat() * reactivity;
                totalEnergyProduced += pebble.getPower() * reactivity;
                
                if (pebble.isDepleted()) {
                    pebbles.remove(pebble);
                    depletedVolume += 10.0; // 10mB of waste per pebble
                    impurity = Math.min(1.0, impurity + 0.001);
                }
            }
        }
        
        heatPerTick = totalHeatProduced;
        //energyPerTick = (int) totalEnergyProduced;
        
        // 4. Temperature update
        double cooling = coolantVolume * 0.5 * (temperature - T_AMBIENT) / 100.0; // Simple cooling
        double netHeat = totalHeatProduced - cooling;
        temperature += netHeat / 1000.0; // Simplified thermal mass
        temperature = Math.max(T_AMBIENT, temperature);
        
        // 5. Pressure calculation
        pressure = (temperature - T_AMBIENT) * PRESSURE_PER_DEGREE + depletedVolume * PRESSURE_PER_DEPLETED_MB;
        
        // 6. Port lock logic
        if (pressure >= PRESSURE_MAX) {
            portsLocked = true;
        } else if (pressure <= PRESSURE_UNLOCK) {
            portsLocked = false;
        }

        // 7. Meltdown check
        if (temperature >= MAX_TEMPERATURE) {
            // Meltdown logic here
        }
    }

    private void handleIO() {
        if (!portsLocked) {
            consumeInputs();
            drainWaste();
        }
    }

    private void drainWaste() {
        if (depletedVolume > 0) {
            // Attempt to drain depletedVolume to Tank 2
            int toDrain = (int) Math.min(depletedVolume, 1000.0);
            if (toDrain <= 0) return;
            FluidStack waste = new FluidStack(NC_MATERIALS.get("irradiated_sodium").getStill(), toDrain);
            int filled = contentHandler().fluidHandler.tanks.get(2).fill(waste, IFluidHandler.FluidAction.EXECUTE);
            depletedVolume -= filled;
            if (filled > 0) changed = true;
        }
    }

    private void updateBlockState() {
        boolean wasPowered = powered;
        powered = isCritical;
        if (wasPowered != powered) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
            changed = true;
        }
    }

    private void updateState() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public void consumeInputs() {
        // Item Input
        ItemStack inputStack = contentHandler().itemHandler.getStackInSlot(0);
        if (!inputStack.isEmpty() && inputStack.getItem() instanceof ItemFuel fuelItem) {
            if (hasSpaceForPebbles()) {
                fuelItem.initDefinition();
                if (consumePebble(fuelItem.depletion(), ItemStack.EMPTY, temperature, fuelItem.criticality, fuelItem.forge_energy, fuelItem.heat)) {
                    inputStack.shrink(1);
                    changed = true;
                }
            }
        }
    }

    /**
     * Checks if there is space available to consume more pebbles.
     * Maximum capacity is fuelCellsCount * pebblesPerFuelCell (configurable).
     * @return true if there's space for more pebbles, false otherwise
     */
    public boolean hasSpaceForPebbles() {
        int maxPebbles = getMaxPebbleCapacity();
        return pebbles.size() < maxPebbles;
    }

    /**
     * Gets the maximum number of pebbles that can be stored.
     * Calculated as: fuelCellsCount * configured pebbles per fuel cell.
     * @return maximum pebble capacity
     */
    public int getMaxPebbleCapacity() {
        return fuelCellsCount * MSR_CONFIG.PEBBLES_PER_FUEL_CELL.get();
    }

    /**
     * Gets the current number of pebbles stored.
     * @return current pebble count
     */
    public int getPebbleCount() {
        return pebbles.size();
    }

    /**
     * Consumes a pebble by creating a new ReactorPebble instance and adding it to the pebbles set.
     * This should only be called after verifying space with hasSpaceForPebbles().
     * 
     * @param ticks the number of ticks until depletion
     * @param outputStack the item that will be left after depletion
     * @param temperature the initial temperature of the pebble
     * @param criticality the criticality value of the pebble
     * @param power energy output per tick
     * @param heatGen heat generation per tick
     * @return true if the pebble was successfully consumed, false if no space available
     */
    public boolean consumePebble(int ticks, ItemStack outputStack, double temperature, double criticality, double power, double heatGen) {
        if (!hasSpaceForPebbles()) {
            return false;
        }
        
        ReactorPebble pebble = ReactorPebble.make(ticks, outputStack, temperature, criticality, power, heatGen);
        pebbles.add(pebble);
        changed = true;
        return true;
    }

    protected void trackChanges(boolean changed) {
        this.changed = this.changed || changed;
    }

    @Override
    public MSRMultiblock getMultiblock() {
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new MSRMultiblock(this);
        }
        return (MSRMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    public boolean hasRedstoneSignal() {
        if(currentTick % 10 == 0) {
            hasRedstoneSignal = getLevel().hasNeighborSignal(getBlockPos());
        }
        return enabledByController || hasRedstoneSignal;
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            CATALYSTS.put(codeId, List.of(getToastSymbol()));
        }

        @Override
        public String getCodeId() {
            return NAME;
        }

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                Item item = getFirstItemStackIngredient(0).getItem();
                if( !(item instanceof ItemFuel) && !item.equals(AIR)) {
                    fuelItem = new ItemFuel(ITEM_PROPERTIES, item.toString(), "", "");
                    return fuelItem;
                }
                Item item1 = getFirstItemStackIngredient(0).getItem();
                if(item1 instanceof ItemFuel) {
                    fuelItem  = (ItemFuel) item1;
                }
            }
            if(fuelItem.def == null) {
                fuelItem.initDefinition();
            }
            return fuelItem;
        }

        @Override
        public @NotNull String getGroup() {
            return FISSION_BLOCKS.get(codeId).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FISSION_BLOCKS.get(codeId).get());
        }

        public int getDepletionTime() {
            if(getFuelItem() == null) return 0;
            return (int) (getFuelItem().depletion()*20*timeModifier);
        }

        public double getEnergy() {
            if(getFuelItem() == null) return 0;
            return getFuelItem().forge_energy;
        }

        public double getHeat() {
            if(getFuelItem() == null) return 0;
            return getFuelItem().heat;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/20;
        }

        public double getCriticality() {
            return getFuelItem().criticality;
        }
    }
}
