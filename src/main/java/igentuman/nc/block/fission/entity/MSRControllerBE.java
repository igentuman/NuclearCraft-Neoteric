package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.MultiblockPortBE;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.RecipeInfo;
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
                2, 2);
        contentHandler().setBlockEntity(this);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler().fluidHandler.tanks.get(0).setCapacity(10000);
        contentHandler().fluidHandler.tanks.get(1).setCapacity(10000);
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

        // Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        changed = false;

        super.tickServer();

        boolean wasFormed = getMultiblock().isFormed();
        boolean wasEnabled = controllerEnabled;
        boolean wasPowered = powered;

        handleValidation();

        // MSR controller logic
        if (getMultiblock().isFormed()) {
            initializePorts();
            trackChanges(contentHandler().tick());
            consumeInputs();
            // Check if reactor should run
            controllerEnabled = hasRedstoneSignal() && !forceShutdown;

            if (controllerEnabled) {
                powered = true;
                // Process fuel and generate heat
                processFuel();
                // Process boiling to convert heat to steam
                boil();
            } else {
                powered = false;
                // Cool down slowly and process remaining boiling
                coolDown();
            }
        }

        changed = powered != wasPowered || changed;
        refreshCacheFlag = !getMultiblock().isFormed();

        if(refreshCacheFlag || changed || currentTick % 40 == 0) {
            try {
                assert level != null;
                setChanged();
                if(powered != wasPowered) {
                    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, powered));
                }
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(POWERED, powered), Block.UPDATE_ALL);
            } catch (NullPointerException ignored) {}
        }
    }

    private void processFuel() {
        if(heatPerTick <= 0) {
            return;
        }

        // Add heat
        heat += heatPerTick;
        
        // Apply passive cooling
        double cooling = getPassiveCooling();
        heat -= cooling;
        
        // Ensure heat stays within bounds
        heat = Math.max(0, Math.min(heat, maxHeat));
        
        // Overheat check
        if(heat >= maxHeat * 0.9) {
            powered = false;
            forceShutdown = true;
        }
    }

    private void coolDown() {
        if(heat > 0) {
            double cooling = getPassiveCooling() * 2.0; // Cool twice as fast when shut down
            heat -= cooling;
        }
        
        // Process boiling
        boil();

        heat = Math.max(0, heat);

        if(heat < maxHeat * 0.7) {
            forceShutdown = false;
        }
    }

    private double getPassiveCooling() {
        // Passive cooling based on available heat capacity
        return maxHeat * 0.01;
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

    public void consumeInputs() {
        // Consume fuel pebbles from item input slot (slot 0)
        ItemStack inputStack = contentHandler().itemHandler.getStackInSlot(0);
        
        if (!inputStack.isEmpty() && inputStack.getItem() instanceof ItemFuel fuelItem) {
            fuelItem.initDefinition();
            
            // Check if there's space for more pebbles
            if (hasSpaceForPebbles()) {
                // Look up the recipe to get the correct depleted fuel output
                NcRecipe recipe = getRecipe();
                
                // Consume one pebble item
                // In MSR, pebbles are completely consumed and converted to depleted salt
                int depletionTicks = fuelItem.depletion();
                double initialTemperature = 20.0; // Start at room temperature
                double criticalityValue = fuelItem.criticality;
                
                // Get the depleted fuel from the recipe output
                ItemStack remainder = ItemStack.EMPTY;
                if (recipe != null && !recipe.getResultItems().isEmpty()) {
                    remainder = recipe.getResultItems().get(0).copy();
                } else {
                    return;
                }
                
                // Create and consume the pebble
                if (consumePebble(depletionTicks, remainder, initialTemperature, criticalityValue)) {
                    // Successfully consumed, extract one item from input slot
                    inputStack.shrink(1);
                    trackChanges(true);
                }
            }
        }

        // Fluid input (slot 0) is handled by the boiling system in the boil() method
        // The fluid handler manages fuel coolant input automatically through port operations
        FluidStack inputFluid = contentHandler().fluidHandler.getFluidInSlot(0);
        // Coolant fluid validation is done in hasCoolant() and boil() methods
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
     * @return true if the pebble was successfully consumed, false if no space available
     */
    public boolean consumePebble(int ticks, ItemStack outputStack, double temperature, double criticality) {
        if (!hasSpaceForPebbles()) {
            return false;
        }
        
        ReactorPebble pebble = ReactorPebble.make(ticks, outputStack, temperature, criticality);
        pebbles.add(pebble);
        changed = true;
        return true;
    }

    public void boil() {
        steamPerTick = 0;
        boilingPenalty = 0;
        
        if(!hasCoolant()) {
            return;
        }
        
        double cooling = getPassiveCooling();
        double heatEff = cooling * FISSION_CONFIG.BOILING_MULTIPLIER.get() / 100D;

        if(hasCoolant()) {
            FluidStack steam = boilingRecipe.getOutputFluids().get(0);
            FluidStack coolant = boilingRecipe.getInputFluids(0).get(0);
            double conversion = heatEff / boilingRecipe.conversionRate();
            FluidStack currentCoolant = contentHandler().fluidHandler.getFluidInSlot(0);
            FluidStack currentOutput = contentHandler().fluidHandler.getFluidInSlot(1);
            
            if(!steam.isFluidEqual(currentOutput) && !currentOutput.isEmpty()) {
                boilingPenalty = cooling;
                return;
            }
            
            double capacity = contentHandler().fluidHandler.tanks.get(1).getCapacity() - currentOutput.getAmount();
            int maxSteamOutput = (int) (steam.getAmount() * conversion);
            int ops = (int) (capacity / steam.getAmount());
            capacity = ops * steam.getAmount();
            int canGetAmount = (int) Math.min(maxSteamOutput, capacity);
            ops = canGetAmount / steam.getAmount();
            ops = Math.min(currentCoolant.getAmount() / coolant.getAmount(), ops);
            steamPerTick = Math.max(ops * steam.getAmount(), 0);
            
            if(steamPerTick == 0) {
                heat += heatPerTick;
                boilingPenalty = cooling * 0.75;
                return;
            }
            
            contentHandler().fluidHandler.tanks.get(0).drain(ops * coolant.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            FluidStack out = steam.copy();
            out.setAmount(ops * steam.getAmount());
            contentHandler().fluidHandler.tanks.get(1).fill(out, IFluidHandler.FluidAction.EXECUTE);
            changed = true;
            
            if(ops < Math.floor(conversion)) {
                boilingPenalty = cooling * (conversion / ops) - cooling;
            }
        } else {
            boilingPenalty = cooling * 0.75;
        }
    }

    public boolean hasCoolant() {
        FluidStack coolant = contentHandler().fluidHandler.getFluidInSlot(0);
        if(coolant.isEmpty()) {
            boilingRecipe = null;
            return false;
        }
        
        if(boilingRecipe == null) {
            for(FissionControllerBE.FissionBoilingRecipe recipe : getBoilingRecipes()) {
                if(recipe.getInputFluids()[0].test(coolant)) {
                    boilingRecipe = recipe;
                    return true;
                }
            }
        } else {
            if(!boilingRecipe.getInputFluids()[0].test(coolant)) {
                boilingRecipe = null;
                return false;
            }
        }
        return boilingRecipe instanceof FissionControllerBE.FissionBoilingRecipe;
    }

    public List<FissionControllerBE.FissionBoilingRecipe> getBoilingRecipes() {
        if(coolantRecipes == null) {
            coolantRecipes = (List<FissionControllerBE.FissionBoilingRecipe>) NcRecipeType.getAllRecipesFor("fission_boiling", getLevel());
        }
        return coolantRecipes;
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

        public double getCriticality() {
            if(getFuelItem() == null) return 0;
            return getFuelItem().criticality;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/20;
        }
    }
}