package igentuman.nc.block_entity.fission;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.MsrControllerContainer;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.MsrCache;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.ReactorPebble;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.radiation.source.Corium;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MsrControllerBE extends MultiblockControllerBE {

    private static final int MIN_PEBBLES_FOR_CRITICALITY = 10;
    private static final double MIN_SALT_FOR_CRITICALITY = 100;
    public static final double T_AMBIENT = 20.0;
    public static final double MAX_TEMPERATURE = 2000.0;
    public static final double SELF_PRIME_CRITICALITY = 50.0;
    public static final double IRRADIATION_THRESHOLD = 50.0;
    public static final double OPTIMAL_MODERATION = 3000.0;
    public static final double MAX_REACTIVITY = 10.0;
    public static final double TEMP_REACTIVITY_THRESHOLD = 2000.0;
    public static final double TEMP_MAX = 5000.0;
    public static final double HEAT_PER_MB = 0.10;
    public static final double GAMMA_HE = 2.0;
    public static final double GAMMA_LE = 0.5;
    public static final double AMBIENT_LOSS = 0.001;
    public static final double IMPURITY_RATE_PER_PEBBLE = 0.001;
    private static final int OVERHEAT_LIMIT = 600;

    @NBTField public double maxHeat = 0;
    @NBTField public double heatPerTick = 0;
    @NBTField public double avgIrradiation = 0;
    @NBTField public double saltVolume = 0.0;
    @NBTField public double hotSaltVolume = 0.0;
    @NBTField public double temperature = T_AMBIENT;
    @NBTField public double reactivity = 0.0;
    @NBTField public double impurity = 0.0;
    @NBTField public double depletion = 0;
    @NBTField public int overheatTimer = 0;
    @NBTField public boolean enabledByController = false;

    @NBTField(syncToClient = true) public int saltInputRate = 64;
    @NBTField(syncToClient = true) public int saltOutputRate = 64;
    @NBTField(syncToClient = true) public int reactivitySync = 0;
    @NBTField(syncToClient = true) public int temperatureSync = 0;
    @NBTField(syncToClient = true) public int depletionSync = 0;
    @NBTField(syncToClient = true) public int pebbleCountSync = 0;
    @NBTField(syncToClient = true) public int fuelCellsCountSync = 0;
    @NBTField(syncToClient = true) public int overheatTimerSync = 0;
    @NBTField(syncToClient = true) public boolean isCritical = false;
    @NBTField(syncToClient = true) public boolean powered = false;

    public int fuelCellsCount = 0;
    public int pebbleCount = 0;

    private final HashSet<ReactorPebble> pebbles = new HashSet<>();
    private final List<ItemStack> depletedPebbles = new ArrayList<>();
    private List<ItemStack> allowedInputs;
    private boolean validatorsReady = false;

    public MsrControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureTankValidators();
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }

        if (formed && mbInstance != null && mbInstance.cache instanceof MsrCache mc) {
            updateStats(mc);
            resizeTanks();
            if (isActive()) {
                syncInternalState();
                updateSimulation(serverLevel, mc);
                handleIO();
            } else {
                idle();
            }
        } else {
            clearStats();
            idle();
        }
        pushSyncMirrors();

        if (wasChanged) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (formed && powered) {
            playSound(NCSounds.MSR_RUNNING.get(), 0.4f);
        } else {
            stopSound();
        }
    }

    private boolean isActive() {
        if (enabledByController) return true;
        return level == null || !level.hasNeighborSignal(worldPosition);
    }

    private void updateStats(MsrCache mc) {
        if (fuelCellsCount != mc.fuelCellCount) {
            fuelCellsCount = mc.fuelCellCount;
            wasChanged = true;
        }
    }

    private void clearStats() {
        if (fuelCellsCount != 0) {
            fuelCellsCount = 0;
            wasChanged = true;
        }
    }

    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    private ItemStackHandler itemStacks() {
        return contentHandler.hasItemCapability() ? contentHandler.getItemHandler().getInternalHandler() : null;
    }

    private void ensureTankValidators() {
        if (validatorsReady) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        Fluid cold = ModEntries.fluidOf("flibe_molten_salt");
        Fluid hot = ModEntries.fluidOf("flibe_hot_molten_salt");
        tanks.setTankValidator(0, fs -> cold != null && fs.getFluid() == cold);
        tanks.setTankValidator(1, fs -> hot != null && fs.getFluid() == hot);
        validatorsReady = true;
    }

    private void resizeTanks() {
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        int vol = (int) Math.min(Integer.MAX_VALUE, globalVolume());
        if (vol < 1) vol = 1;
        tanks.setTankCapacity(0, vol);
        tanks.setTankCapacity(1, vol);
    }

    private void syncInternalState() {
        FluidStackHandler tanks = fluidTanks();
        if (tanks != null) {
            saltVolume = tanks.getFluidInTank(0).getAmount();
            hotSaltVolume = tanks.getFluidInTank(1).getAmount();
        }
        pebbleCount = pebbles.size();
    }

    private void updateSimulation(ServerLevel sl, MsrCache mc) {
        markDirty();
        if (fuelCellsCount < 1) {
            isCritical = false;
            powered = false;
            return;
        }

        double totalIrradiation = 0;
        for (ReactorPebble pebble : pebbles) {
            double effIrr = (pebble.irradiation + pebble.effectiveIrradiation() * 2) / 3;
            if (pebble.criticality < SELF_PRIME_CRITICALITY) {
                totalIrradiation += effIrr;
            } else {
                totalIrradiation += 100.0 / (pebble.irradiation * 2 + 50) * effIrr;
            }
        }

        double saltPerPebble = (saltVolume + hotSaltVolume) / Math.max(1, pebbleCount);
        double moderation = Math.pow(OPTIMAL_MODERATION / Math.max(1.0, saltPerPebble), 0.2);
        if (depletion == 0 && saltVolume == 0) {
            moderation = 0;
        }
        avgIrradiation = (totalIrradiation / Math.max(1, pebbleCount)) * moderation;

        double baseReactivity = avgIrradiation / IRRADIATION_THRESHOLD;
        double tempPenalty = temperature > TEMP_REACTIVITY_THRESHOLD
                ? (temperature - TEMP_REACTIVITY_THRESHOLD) / (TEMP_MAX - TEMP_REACTIVITY_THRESHOLD)
                : 0.0;
        double tempFactor = Math.max(0.0, 1.0 - tempPenalty);
        double targetReactivity = Math.max(0.0, Math.min(MAX_REACTIVITY, baseReactivity * tempFactor));
        reactivity = (reactivity + targetReactivity) / 2.0;

        isCritical = reactivity >= 0.3
                && pebbleCount >= MIN_PEBBLES_FOR_CRITICALITY
                && saltVolume >= MIN_SALT_FOR_CRITICALITY;
        powered = isCritical;

        double gv = globalVolume();
        double initialHeat = T_AMBIENT
                + (gv > 0 ? (hotSaltVolume / gv) * 600.0 + (saltVolume / gv) * 300.0 : 0.0);

        if (!isCritical && reactivity < 0.3) {
            temperature = (temperature + initialHeat) / 2.0;
            return;
        }

        double heatProduced = 0;
        depletion = 0;
        for (ReactorPebble pebble : new HashSet<>(pebbles)) {
            double effReactivity = reactivity + sl.getRandom().nextDouble();
            heatProduced += pebble.getHeat() * effReactivity;
            pebble.tick(effReactivity);
            depletion += pebble.ticksProcessed / pebble.ticks;
            if (pebble.isDepleted()) {
                pebbles.remove(pebble);
                if (!pebble.outputStack.isEmpty()) {
                    depletedPebbles.add(pebble.outputStack.copy());
                }
                impurity = Math.min(1.0, impurity + IMPURITY_RATE_PER_PEBBLE);
            }
        }
        depletion = pebbles.isEmpty() ? 0 : depletion / pebbles.size();
        heatPerTick = (heatPerTick * 9 + heatProduced) / 10.0;

        double maxConversion = heatPerTick / HEAT_PER_MB;
        double converted = 0;
        FluidStackHandler tanks = fluidTanks();
        if (tanks != null) {
            double hotRoom = tanks.getTankCapacity(1) - hotSaltVolume;
            double rateCap = Math.min(saltInputRate, saltOutputRate) * 1000.0;
            converted = Math.min(Math.min(maxConversion, saltVolume), Math.min(hotRoom, rateCap));
            if (converted > 0) {
                Fluid hot = ModEntries.fluidOf("flibe_hot_molten_salt");
                tanks.drainTank(0, (int) converted, IFluidHandler.FluidAction.EXECUTE);
                if (hot != null) {
                    tanks.fillTank(1, new FluidStack(hot, (int) converted), IFluidHandler.FluidAction.EXECUTE);
                }
                saltVolume -= converted;
                hotSaltVolume += converted;
            }
        }

        double conversionEfficiency = maxConversion > 0 ? converted / maxConversion : 1.0;
        double backlog = (1.0 - conversionEfficiency) * heatPerTick;
        temperature = Math.max(0.0, Math.min(TEMP_MAX, (temperature * 49 + initialHeat + backlog) / 50.0));

        if (temperature >= MAX_TEMPERATURE) {
            overheatTimer++;
            if (overheatTimer > OVERHEAT_LIMIT) {
                meltdown(sl, mc);
                overheatTimer = 0;
            }
        } else {
            overheatTimer = Math.max(0, overheatTimer - 1);
        }
    }

    private void idle() {
        reactivity = Math.max(0, reactivity - 0.1);
        heatPerTick = 0;
        isCritical = false;
        powered = false;
        if (temperature > T_AMBIENT) {
            temperature = Math.max(T_AMBIENT, temperature - AMBIENT_LOSS * temperature - 1);
        }
        overheatTimer = Math.max(0, overheatTimer - 1);
    }

    private void handleIO() {
        consumeInputs();
        outputDepleted();
    }

    private void consumeInputs() {
        ItemStackHandler items = itemStacks();
        if (items == null) return;
        ItemStack in = items.getStackInSlot(0);
        if (in.isEmpty() || !(in.getItem() instanceof ItemFuel fuel)) return;
        if (!"_tr".equals(fuel.variant)) return;
        if (!hasSpaceForPebbles()) return;
        FuelDef def = fuel.def();
        int ticks = Math.max(1, def.depletion);
        double gamma = def.name.toLowerCase().startsWith("l") ? GAMMA_LE : GAMMA_HE;
        double irradiation = Math.pow(def.heat / 100.0 + 200.0 / Math.max(1, def.depletion) + 0.5, 1.5) * 2;
        pebbles.add(ReactorPebble.make(ticks, depletedFor(def), def.criticality, def.heat, gamma, irradiation));
        items.extractItem(0, 1, false);
        markDirty();
    }

    private ItemStack depletedFor(FuelDef def) {
        FissionFuelEntry entry = ModEntries.FISSION_FUEL.get(def.group + "/" + def.name);
        if (entry == null) return ItemStack.EMPTY;
        DeferredItem<Item> depleted = entry.depletedItems().get("_tr");
        return depleted == null ? ItemStack.EMPTY : new ItemStack(depleted.get());
    }

    private void outputDepleted() {
        if (depletedPebbles.isEmpty()) return;
        ItemStackHandler items = itemStacks();
        if (items == null) return;
        Iterator<ItemStack> it = depletedPebbles.iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            ItemStack remainder = items.insertItem(1, stack.copy(), false);
            if (remainder.isEmpty()) {
                it.remove();
                markDirty();
            } else if (remainder.getCount() != stack.getCount()) {
                stack.setCount(remainder.getCount());
                markDirty();
                break;
            } else {
                break;
            }
        }
    }

    private void meltdown(ServerLevel sl, MsrCache mc) {
        BlockState corium = Corium.MOLTEN_CORIUM.get().defaultFluidState().createLegacyBlock();
        long[] cells;
        try {
            Long[] snapshot = mc.fuelCells.toArray(new Long[0]);
            cells = new long[snapshot.length];
            for (int i = 0; i < snapshot.length; i++) cells[i] = snapshot[i];
        } catch (Exception e) {
            cells = new long[0];
        }
        for (long key : cells) {
            sl.setBlock(BlockPos.of(key), corium, Block.UPDATE_ALL);
        }

        ItemStackHandler items = itemStacks();
        ItemStack fuelStack = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
        spawnMeltdownRadiation(sl, worldPosition, fuelStack, fuelCellsCount);

        double radius = Multiblocks.fissionExplosionRadius;
        if (radius > 0) {
            sl.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    (float) radius, Level.ExplosionInteraction.BLOCK);
        }

        pebbles.clear();
        depletedPebbles.clear();
        FluidStackHandler tanks = fluidTanks();
        if (tanks != null) {
            tanks.voidTank(0);
            tanks.voidTank(1);
        }
        if (items != null) items.setStackInSlot(0, ItemStack.EMPTY);
        reactivity = 0;
        heatPerTick = 0;
        temperature = T_AMBIENT;
        isCritical = false;
        powered = false;
        markDirty();
    }

    private void spawnMeltdownRadiation(ServerLevel sl, BlockPos pos, ItemStack fuelStack, int fuelCells) {
        if (fuelCells <= 0 || fuelStack.isEmpty()) return;
        RadiationProfile fuelProfile = RadiationBindings.of(fuelStack);
        if (fuelProfile.isEmpty()) return;
        long now = sl.getGameTime();
        RadiationProfile leftover = RadiationProfile.empty();
        leftover.mergeAtoms(fuelProfile, fuelCells, now);
        WorldSourceRegistry registry = WorldSourceRegistry.get(sl);
        registry.register(new LeftOverRadSource(sl, pos, leftover, now, true));
        RadiationProfile chunk = leftover.copy(now);
        chunk.reduceAtoms(2);
        registry.setChunkRadiation(pos, RadiationProfile.empty(), RadiationProfile.empty(), chunk);
    }

    private void pushSyncMirrors() {
        int r = (int) Math.round(reactivity * 100);
        int t = (int) Math.round(temperature);
        int d = (int) Math.round(depletion * 100);
        int pc = pebbles.size();
        if (r != reactivitySync || t != temperatureSync || d != depletionSync
                || pc != pebbleCountSync || fuelCellsCount != fuelCellsCountSync
                || overheatTimer != overheatTimerSync) {
            reactivitySync = r;
            temperatureSync = t;
            depletionSync = d;
            pebbleCountSync = pc;
            fuelCellsCountSync = fuelCellsCount;
            overheatTimerSync = overheatTimer;
            wasChanged = true;
        }
    }

    public void handleSliderUpdate(int buttonId, int value) {
        switch (buttonId) {
            case 0 -> saltInputRate = Math.max(0, value);
            case 1 -> saltOutputRate = Math.max(0, value);
        }
        setChanged();
    }

    public void voidFuel() {
        pebbles.clear();
        depletedPebbles.clear();
        pebbleCount = 0;
        ItemStackHandler items = itemStacks();
        if (items != null) items.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
        markDirty();
    }

    public double globalVolume() {
        return fuelCellsCount * (double) Multiblocks.msrVolumePerFuelCell;
    }

    public int getMaxPebbleCapacity() {
        return fuelCellsCount * Math.max(1, Multiblocks.msrPebblesPerFuelCell);
    }

    public boolean hasSpaceForPebbles() {
        return pebbles.size() < getMaxPebbleCapacity();
    }

    public int getPebbleCount() {
        return pebbles.size();
    }

    public List<ItemStack> getAllowedInputItems() {
        if (allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for (FissionFuelEntry entry : ModEntries.FISSION_FUEL.values()) {
                DeferredItem<Item> tr = entry.fuelItems().get("_tr");
                if (tr != null) allowedInputs.add(new ItemStack(tr.get()));
            }
        }
        return allowedInputs;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag pebblesTag = new ListTag();
        for (ReactorPebble pebble : pebbles) {
            pebblesTag.add(pebble.serializeNBT(registries));
        }
        tag.put("pebbles", pebblesTag);
        ListTag depletedTag = new ListTag();
        for (ItemStack stack : depletedPebbles) {
            depletedTag.add(stack.saveOptional(registries));
        }
        tag.put("depletedPebbles", depletedTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pebbles.clear();
        if (tag.contains("pebbles")) {
            ListTag list = tag.getList("pebbles", 10);
            for (int i = 0; i < list.size(); i++) {
                ReactorPebble pebble = new ReactorPebble();
                pebble.deserializeNBT(registries, list.getCompound(i));
                pebbles.add(pebble);
            }
        }
        depletedPebbles.clear();
        if (tag.contains("depletedPebbles")) {
            ListTag list = tag.getList("depletedPebbles", 10);
            for (int i = 0; i < list.size(); i++) {
                depletedPebbles.add(ItemStack.parseOptional(registries, list.getCompound(i)));
            }
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MsrControllerContainer(containerId, playerInventory, this, containerData);
    }
}
