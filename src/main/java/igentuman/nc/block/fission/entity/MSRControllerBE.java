package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.compat.oc2.MSRDevice;
import igentuman.nc.handler.sided.MSRContentHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.MSRMultiblock;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.ReactorPebble;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.fission.MSRControllerBlock.POWERED;
import static igentuman.nc.compat.oc2.MSRDevice.DEVICE_CAPABILITY;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.handler.config.FissionConfig.MSR_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.ITEM_PROPERTIES;
import static igentuman.nc.setup.registration.NCFluids.NC_MATERIALS;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static net.minecraft.world.item.Items.AIR;

public class MSRControllerBE extends MultiblockControllerBE {

    public static final String NAME = "msr_controller";
    private static final int MIN_PEBBLES_FOR_CRITICALITY = 10;
    private static final double MIN_SALT_FOR_CRITICALITY = 100;
    public final SidedContentHandler contentHandler;

    @NBTField
    public double maxHeat = FISSION_CONFIG.HEAT_CAPACITY.getDefault();
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public int connectedPorts = 0;
    @NBTField
    public boolean forceShutdown = false;
    @NBTField
    public int fuelCellsCount = 0;
    @NBTField
    public int heatExchangerCount = 0;

    @NBTField public int pebbleCount = 0;
    @NBTField public double saltVolume = 0.0;      // mB cold FLiBe
    @NBTField public double hotSaltVolume = 0.0;   // mB hot FLiBe (product buffer)
    @NBTField public double temperature = 20.0;    // °C, average salt temp (safety only)
    @NBTField public double extraHeat = 0.0;       // unremoved fission-heat backlog
    @NBTField public double reactivity = 0.0;
    @NBTField public double impurity = 0.0;        // 0..1
    @NBTField public boolean isCritical = false;
    @NBTField public int saltInputRate = 64;       // buckets/tick, player-set cold-salt input cap
    @NBTField public int saltOutputRate = 64;      // buckets/tick, player-set hot-salt output cap

    public static final double T_AMBIENT = 20.0;
    public static final double MAX_TEMPERATURE = 2000.0;
    public static final double SELF_PRIME_CRITICALITY = 50.0;   // criticality below this self-primes (emits full irradiation)
    public static final double IRRADIATION_THRESHOLD = 10.0;    // avg effective irradiation yielding baseReactivity = 1.0 (TUNE)
    public static final double OPTIMAL_MODERATION = 200.0;   // mB cold salt per pebble at peak moderation
    public static final double MODERATION_FALLOFF = 0.002;   // moderation drop per mB away from optimum
    public static final double HEAT_PER_MB = 1.0;            // heat removed per mB cold→hot conversion
    public static final double GAMMA_HE = 2.0;               // high-enriched criticality decay
    public static final double GAMMA_LE = 0.5;               // low-enriched criticality decay
    public static final double AMBIENT_LOSS = 0.001;         // passive heat bleed so an idle core cools
    public static final double IMPURITY_RATE_PER_PEBBLE = 0.001;
    public static final double VOLUME_PER_PEBBLE = 100.0;

    private final HashSet<ReactorPebble> pebbles = new HashSet<>();
    private final List<ItemStack> depletedPebbles = new ArrayList<>();
    private List<ItemStack> allowedInputs;

    private boolean portsInitialized = false;
    private long lastTickTime = -1L;
    private boolean changed = false;

    public MSRControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
        contentHandler = new MSRContentHandler(
                1, 1,
                1, 1);
        contentHandler().setBlockEntity(this);
        // fluid slot 0 = cold FLiBe in, slot 1 = hot FLiBe out
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        // item slot 0 = pebble in, slot 1 = depleted out
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        for(int i = 0; i < contentHandler().fluidHandler.tanks.size(); i++) {
            contentHandler().fluidHandler.tanks.get(i).setCapacity(1000000);
        }
        contentHandler().setAllowedInputFluids(0, () -> List.of(new FluidStack(NC_MATERIALS.get("flibe_molten_salt").getStill(), 1000)));
        contentHandler().setAllowedInputItems(this::getAllowedInputItems);
        ((MSRContentHandler) contentHandler).setVolumeGate(this::freeVolume, VOLUME_PER_PEBBLE);
        ((MSRContentHandler) contentHandler).setRateGate(() -> saltInputRate * 1000, () -> saltOutputRate * 1000);

        // Kept for base-class compatibility (updateEnergyTier); the MSR produces no FE.
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100000000, 0, 100000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Override
    public int getBaseGTEnergyTier() {
        return 4;
    }

    public void initializePorts() {
        if(portsInitialized) return;
        portsInitialized = true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public List<ItemStack> getAllowedInputItems() {
        if (allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for (NcRecipe recipe : NcRecipeType.getAllRecipesFor(getName(), getLevel())) {
                for (Ingredient ingredient : recipe.getItemIngredients()) {
                    allowedInputs.addAll(List.of(ingredient.getItems()));
                }
            }
        }
        return allowedInputs;
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
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            controllerEnabled = false;
            return;
        }

        lastTickTime = currentTick;
        changed = false;

        super.tickServer();

        handleValidation();

        if (getMultiblock().isFormed() && hasRedstoneSignal()) {
            initializePorts();
            ((MSRContentHandler) contentHandler).resetRateCounters();
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
        hotSaltVolume = contentHandler().fluidHandler.tanks.get(1).getFluidAmount();
        pebbleCount = pebbles.size();
    }

    private void updateSimulation() {
        // 1. Irradiation field (pass 1): each pebble emits neutron flux. Self-primed fuels (low
        //    criticality threshold) emit fully; higher-criticality fuels need driving flux and
        //    contribute a criticality-scaled fraction. Flux decays with burnup.
        double totalIrradiation = 0;
        for (ReactorPebble pebble : pebbles) {
            double effIrr = pebble.effectiveIrradiation();
            if (pebble.criticality < SELF_PRIME_CRITICALITY) {
                totalIrradiation += effIrr;
            } else {
                totalIrradiation += 100.0 / (pebble.criticality * 2 + 50) * effIrr;
            }
        }

        // 2. Average irradiation, tuned by FLiBe moderation (salt-per-pebble sweet spot).
        double saltPerPebble = saltVolume / Math.max(pebbleCount, 1);
        double moderation = Math.max(0.0, Math.min(1.0, 1.0 - Math.abs(saltPerPebble - OPTIMAL_MODERATION) * MODERATION_FALLOFF));
        double avgIrradiation = (totalIrradiation / Math.max(pebbleCount, 1)) * moderation;

        // 3. Reactivity from average irradiation, dampened by fission-product poisoning.
        double baseReactivity = avgIrradiation / IRRADIATION_THRESHOLD;
        reactivity = baseReactivity * (1.0 - impurity);

        // 4. Criticality gate: irradiation-driven reactivity first, then fixed floors
        isCritical = (baseReactivity >= 0.3) &&
                     (pebbleCount >= MIN_PEBBLES_FOR_CRITICALITY) &&
                     (saltVolume >= MIN_SALT_FOR_CRITICALITY);

        // 5. Fission heat + per-pebble burnup (all pebbles tick simultaneously)
        double heatProduced = 0;

        for (ReactorPebble pebble : new HashSet<>(pebbles)) {
            heatProduced += pebble.getHeat() * pebble.effectiveIrradiation() * reactivity;
            pebble.tick(reactivity);
            if (pebble.isDepleted()) {
                pebbles.remove(pebble);
                if (!pebble.outputStack.isEmpty()) {
                    depletedPebbles.add(pebble.outputStack.copy());
                }
                impurity = Math.min(1.0, impurity + IMPURITY_RATE_PER_PEBBLE);
                changed = true;
            }
        }
        heatPerTick = heatProduced;

        // 6. Convert cold FLiBe -> hot FLiBe (volume conserved). Heat that can't convert backs up.
        double hotRoom = contentHandler().fluidHandler.tanks.get(1).getCapacity() - hotSaltVolume;
        double converted = Math.min(Math.min(heatProduced / HEAT_PER_MB, saltVolume), hotRoom);
        if (converted > 0) {
            contentHandler().fluidHandler.tanks.get(0).drain((int) converted, IFluidHandler.FluidAction.EXECUTE);
            FluidStack hot = new FluidStack(NC_MATERIALS.get("flibe_hot_molten_salt").getStill(), (int) converted);
            contentHandler().fluidHandler.tanks.get(1).fill(hot, IFluidHandler.FluidAction.EXECUTE);
            saltVolume -= converted;
            hotSaltVolume += converted;
            changed = true;
        }

        // 7. Heat backlog -> average salt temperature (safety only, no buffer)
        double ambientLoss = (temperature - T_AMBIENT) * AMBIENT_LOSS;
        extraHeat = Math.max(0.0, extraHeat + heatProduced - converted * HEAT_PER_MB - ambientLoss);
        double saltMass = saltVolume + hotSaltVolume;
        temperature = Math.max(T_AMBIENT, T_AMBIENT + extraHeat / Math.max(saltMass, 1.0));

        // 8. Meltdown check
        if (temperature >= MAX_TEMPERATURE) {
            // TODO: triggerCatastrophicFailure()
        }
    }

    private void handleIO() {
        consumeInputs();
        outputDepleted();
    }

    private void outputDepleted() {
        if (depletedPebbles.isEmpty()) return;
        Iterator<ItemStack> it = depletedPebbles.iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            ItemStack remainder = contentHandler().itemHandler.insertItemInternal(1, stack.copy(), false);
            if (remainder.isEmpty()) {
                it.remove();
                changed = true;
            } else if (remainder.getCount() != stack.getCount()) {
                stack.setCount(remainder.getCount());
                changed = true;
                break;
            } else {
                break; // output slot full
            }
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
        ItemStack inputStack = contentHandler().itemHandler.getStackInSlot(0);
        if (inputStack.isEmpty() || !(inputStack.getItem() instanceof ItemFuel fuelItem)) {
            return;
        }
        if (!"_tr".equals(fuelItem.subType)) {
            return; // MSR only burns TRISO pebbles
        }
        if (!hasSpaceForPebbles() || freeVolume() < VOLUME_PER_PEBBLE) {
            return;
        }
        fuelItem.initDefinition();
        int ticks = Math.max(1, fuelItem.depletion() * 20);
        double gamma = fuelItem.name.toLowerCase().startsWith("l") ? GAMMA_LE : GAMMA_HE;
        if (consumePebble(ticks, depletedFor(fuelItem), fuelItem.criticality, fuelItem.heat, gamma, fuelItem.irradiation())) {
            inputStack.shrink(1);
            changed = true;
        }
    }

    private ItemStack depletedFor(ItemFuel fuelItem) {
        var depleted = FissionFuel.NC_DEPLETED_FUEL.get(List.of("depleted", fuelItem.group, fuelItem.name, "tr"));
        return depleted == null ? ItemStack.EMPTY : new ItemStack(depleted.get());
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
     * Total shared internal volume (mB), derived from reactor size.
     */
    public double globalVolume() {
        return fuelCellsCount * (double) MSR_CONFIG.VOLUME_PER_FUEL_CELL.get();
    }

    /**
     * Remaining shared internal volume (mB). Salt, coolant, live pebbles, buffered pebble items
     * and accumulated depleted material all draw from {@link #globalVolume()}. Reads live handler
     * state so multiple inserts in the same tick stay consistent.
     */
    public double freeVolume() {
        double cold = contentHandler().fluidHandler.tanks.get(0).getFluidAmount();
        double hot = contentHandler().fluidHandler.tanks.get(1).getFluidAmount();
        int bufferedPebbleItems = contentHandler().itemHandler.getStackInSlot(0).getCount();
        double pebbleVol = (pebbles.size() + depletedPebbles.size() + bufferedPebbleItems) * VOLUME_PER_PEBBLE;
        return globalVolume() - cold - hot - pebbleVol;
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
     * @param criticality the ignition threshold of the fuel (lower self-primes)
     * @param heat heat/tick at nominal reactivity
     * @param gamma burnup-decay exponent (HE high, LE low)
     * @param irradiation neutron flux/tick emitted at nominal reactivity
     * @return true if the pebble was successfully consumed, false if no space available
     */
    public boolean consumePebble(int ticks, ItemStack outputStack, double criticality, double heat, double gamma, double irradiation) {
        if (!hasSpaceForPebbles()) {
            return false;
        }
        pebbles.add(ReactorPebble.make(ticks, outputStack, criticality, heat, gamma, irradiation));
        changed = true;
        return true;
    }

    protected void trackChanges(boolean changed) {
        this.changed = this.changed || changed;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return contentHandler().getItemCapability(side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
        }
        if (isOC2Loaded() && cap == DEVICE_CAPABILITY) {
            return LazyOptional.of(() -> MSRDevice.createDevice(this)).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("pebbles")) {
            pebbles.clear();
            CompoundTag pebblesTag = tag.getCompound("pebbles");
            int size = pebblesTag.getInt("size");
            for (int i = 0; i < size; i++) {
                if (pebblesTag.contains("pebble_" + i)) {
                    ReactorPebble pebble = new ReactorPebble();
                    pebble.deserializeNBT(pebblesTag.get("pebble_" + i));
                    pebbles.add(pebble);
                }
            }
        }
        depletedPebbles.clear();
        if (tag.contains("depletedPebbles")) {
            CompoundTag depletedTag = tag.getCompound("depletedPebbles");
            int size = depletedTag.getInt("size");
            for (int i = 0; i < size; i++) {
                if (depletedTag.contains("depleted_" + i)) {
                    depletedPebbles.add(ItemStack.of(depletedTag.getCompound("depleted_" + i)));
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag pebblesTag = new CompoundTag();
        pebblesTag.putInt("size", pebbles.size());
        int i = 0;
        for (ReactorPebble pebble : pebbles) {
            pebblesTag.put("pebble_" + i, pebble.serializeNBT());
            i++;
        }
        tag.put("pebbles", pebblesTag);

        CompoundTag depletedTag = new CompoundTag();
        depletedTag.putInt("size", depletedPebbles.size());
        for (int j = 0; j < depletedPebbles.size(); j++) {
            depletedTag.put("depleted_" + j, depletedPebbles.get(j).serializeNBT());
        }
        tag.put("depletedPebbles", depletedTag);
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

    @Override
    public void handleSliderUpdate(int buttonId, int ratio) {
        switch (buttonId) {
            case 0 -> saltInputRate = Math.max(0, ratio);
            case 1 -> saltOutputRate = Math.max(0, ratio);
        }
        setChanged();
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
