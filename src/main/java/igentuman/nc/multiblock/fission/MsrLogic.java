package igentuman.nc.multiblock.fission;

import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.api.content.FuelDef;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.ReactorPebble;
import igentuman.nc.util.persistence.LegacyTagReaders;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.radiation.source.Corium;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import igentuman.nc.handler.item.CustomInventoryHandler;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MsrLogic extends AbstractMultiblockLogic<MsrCache> {

    private static final int MIN_PEBBLES_FOR_CRITICALITY = 10;
    private static final double MIN_SALT_FOR_CRITICALITY = 100;
    private static final double SELF_PRIME_CRITICALITY = 50.0;
    private static final double IRRADIATION_THRESHOLD = 50.0;
    private static final double OPTIMAL_MODERATION = 3000.0;
    private static final double MAX_REACTIVITY = 10.0;
    private static final double TEMP_REACTIVITY_THRESHOLD = 2000.0;
    private static final double TEMP_MAX = 5000.0;
    private static final double HEAT_PER_MB = 0.10;
    private static final double GAMMA_HE = 2.0;
    private static final double GAMMA_LE = 0.5;
    private static final double AMBIENT_LOSS = 0.001;
    private static final double IMPURITY_RATE_PER_PEBBLE = 0.001;
    private static final int OVERHEAT_LIMIT = 600;

    private final HashSet<ReactorPebble> pebbles = new HashSet<>();
    private final List<ItemStack> depletedPebbles = new ArrayList<>();
    private double temperature = MsrControllerBE.T_AMBIENT;
    private double reactivity;
    private double impurity;
    private double depletion;
    private double heatPerTick;
    private int overheatTimer;
    private boolean critical;

    @Override
    public void tickServer(ServerLevel level, BlockPos controllerPos, MsrCache cache) {
        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        if (!(blockEntity instanceof MsrControllerBE be)) return;
        if (be.isActive()) {
            simulate(level, be, cache);
            consumeInput(be, cache);
            outputDepleted(be);
            be.markDirty();
        } else {
            cool();
        }
        be.updateRuntimeDisplay(reactivity, temperature, depletion, pebbles.size(), overheatTimer, critical);
    }

    public void idle(MsrControllerBE be) {
        cool();
        be.updateRuntimeDisplay(reactivity, temperature, depletion, pebbles.size(), overheatTimer, critical);
    }

    private void cool() {
        reactivity = Math.max(0, reactivity - 0.1);
        heatPerTick = 0;
        critical = false;
        if (temperature > MsrControllerBE.T_AMBIENT) {
            temperature = Math.max(MsrControllerBE.T_AMBIENT, temperature - AMBIENT_LOSS * temperature - 1);
        }
        overheatTimer = Math.max(0, overheatTimer - 1);
    }

    private void simulate(ServerLevel level, MsrControllerBE be, MsrCache cache) {
        if (cache.fuelCellCount < 1) {
            critical = false;
            return;
        }
        FluidStackHandler tanks = be.fluidTanks();
        double saltVolume = tanks == null ? 0 : tanks.getFluidInTank(0).getAmount();
        double hotSaltVolume = tanks == null ? 0 : tanks.getFluidInTank(1).getAmount();
        int pebbleCount = pebbles.size();

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
        double avgIrradiation = (totalIrradiation / Math.max(1, pebbleCount)) * moderation;

        double baseReactivity = avgIrradiation / IRRADIATION_THRESHOLD;
        double tempPenalty = temperature > TEMP_REACTIVITY_THRESHOLD
                ? (temperature - TEMP_REACTIVITY_THRESHOLD) / (TEMP_MAX - TEMP_REACTIVITY_THRESHOLD)
                : 0.0;
        double tempFactor = Math.max(0.0, 1.0 - tempPenalty);
        double targetReactivity = Math.max(0.0, Math.min(MAX_REACTIVITY, baseReactivity * tempFactor));
        reactivity = (reactivity + targetReactivity) / 2.0;

        critical = reactivity >= 0.3
                && pebbleCount >= MIN_PEBBLES_FOR_CRITICALITY
                && saltVolume >= MIN_SALT_FOR_CRITICALITY;

        double gv = globalVolume(cache);
        double initialHeat = MsrControllerBE.T_AMBIENT
                + (gv > 0 ? (hotSaltVolume / gv) * 600.0 + (saltVolume / gv) * 300.0 : 0.0);

        if (!critical && reactivity < 0.3) {
            temperature = (temperature + initialHeat) / 2.0;
            return;
        }

        double heatProduced = 0;
        depletion = 0;
        for (ReactorPebble pebble : new HashSet<>(pebbles)) {
            double effReactivity = reactivity + level.getRandom().nextDouble();
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
        if (tanks != null) {
            double hotRoom = tanks.getTankCapacity(1) - hotSaltVolume;
            double rateCap = Math.min(be.saltInputRate, be.saltOutputRate) * 1000.0;
            // Efficiency uses the unrounded target, as in 1.21; only whole millibuckets move.
            double target = Math.min(Math.min(maxConversion, saltVolume), Math.min(hotRoom, rateCap));
            int amount = (int) target;
            Fluid hot = ModEntries.fluidOf("flibe_hot_molten_salt");
            if (amount <= 0) {
                converted = Math.max(0, target);
            } else if (hot != null) {
                FluidResource cold = tanks.getResource(0);
                try (var transaction = Transaction.openRoot()) {
                    if (!cold.isEmpty() && tanks.extract(0, cold, amount, transaction) == amount
                            && tanks.insert(1, FluidResource.of(hot), amount, transaction) == amount) {
                        transaction.commit();
                        converted = target;
                    }
                }
            }
        }

        double conversionEfficiency = maxConversion > 0 ? converted / maxConversion : 1.0;
        double backlog = (1.0 - conversionEfficiency) * heatPerTick;
        temperature = Math.max(0.0, Math.min(TEMP_MAX, (temperature * 49 + initialHeat + backlog) / 50.0));

        if (temperature >= MsrControllerBE.MAX_TEMPERATURE) {
            overheatTimer++;
            if (overheatTimer > OVERHEAT_LIMIT) {
                meltdown(level, be, cache);
                overheatTimer = 0;
            }
        } else {
            overheatTimer = Math.max(0, overheatTimer - 1);
        }
    }

    private void consumeInput(MsrControllerBE be, MsrCache cache) {
        CustomInventoryHandler items = be.itemStacks();
        if (items == null) return;
        ItemStack in = items.getStackInSlot(0);
        if (in.isEmpty() || !(in.getItem() instanceof ItemFuel fuel)) return;
        if (!"_tr".equals(fuel.variant)) return;
        if (pebbles.size() >= maxPebbles(cache)) return;
        FuelDef def = fuel.def();
        int ticks = Math.max(1, def.depletion);
        double gamma = def.name.toLowerCase().startsWith("l") ? GAMMA_LE : GAMMA_HE;
        double irradiation = Math.pow(def.heat / 100.0 + 200.0 / Math.max(1, def.depletion) + 0.5, 1.5) * 2;
        ReactorPebble pebble = ReactorPebble.make(ticks, depletedFor(def), def.criticality, def.heat, gamma, irradiation);
        try (var transaction = Transaction.openRoot()) {
            if (items.extract(0, ItemResource.of(in), 1, transaction) != 1) return;
            transaction.commit();
        }
        pebbles.add(pebble);
    }

    private ItemStack depletedFor(FuelDef def) {
        FissionFuelEntry entry = ModEntries.FISSION_FUEL.get(def.group + "/" + def.name);
        if (entry == null) return ItemStack.EMPTY;
        DeferredItem<Item> depleted = entry.depletedItems().get("_tr");
        return depleted == null ? ItemStack.EMPTY : new ItemStack(depleted.get());
    }

    private void outputDepleted(MsrControllerBE be) {
        if (depletedPebbles.isEmpty()) return;
        CustomInventoryHandler items = be.itemStacks();
        if (items == null) return;
        Iterator<ItemStack> it = depletedPebbles.iterator();
        while (it.hasNext()) {
            ItemStack stack = it.next();
            int inserted;
            try (var transaction = Transaction.openRoot()) {
                inserted = items.insert(1, ItemResource.of(stack), stack.getCount(), transaction);
                if (inserted <= 0) break;
                transaction.commit();
            }
            if (inserted == stack.getCount()) {
                it.remove();
            } else {
                stack.shrink(inserted);
                break;
            }
        }
    }

    private void meltdown(ServerLevel level, MsrControllerBE be, MsrCache cache) {
        BlockState corium = Corium.MOLTEN_CORIUM.get().defaultFluidState().createLegacyBlock();
        for (long key : cache.fuelCells) {
            level.setBlock(BlockPos.of(key), corium, Block.UPDATE_ALL);
        }
        BlockPos pos = be.getBlockPos();
        CustomInventoryHandler items = be.itemStacks();
        ItemStack fuelStack = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
        spawnMeltdownRadiation(level, pos, fuelStack, cache.fuelCellCount);

        double radius = Multiblocks.fissionExplosionRadius;
        if (radius > 0) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    (float) radius, Level.ExplosionInteraction.BLOCK);
        }

        pebbles.clear();
        depletedPebbles.clear();
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks != null) {
            tanks.voidTank(0);
            tanks.voidTank(1);
        }
        if (items != null) items.setStackInSlot(0, ItemStack.EMPTY);
        reactivity = 0;
        heatPerTick = 0;
        temperature = MsrControllerBE.T_AMBIENT;
        critical = false;
    }

    private void spawnMeltdownRadiation(ServerLevel level, BlockPos pos, ItemStack fuelStack, int fuelCells) {
        if (fuelCells <= 0 || fuelStack.isEmpty()) return;
        RadiationProfile fuelProfile = RadiationBindings.of(fuelStack);
        if (fuelProfile.isEmpty()) return;
        long now = level.getGameTime();
        RadiationProfile leftover = RadiationProfile.empty();
        leftover.mergeAtoms(fuelProfile, fuelCells, now);
        WorldSourceRegistry registry = WorldSourceRegistry.get(level);
        registry.register(new LeftOverRadSource(level, pos, leftover, now, true));
        RadiationProfile chunk = leftover.copy(now);
        chunk.reduceAtoms(2);
        registry.setChunkRadiation(pos, RadiationProfile.empty(), RadiationProfile.empty(), chunk);
    }

    public void voidFuel() {
        pebbles.clear();
        depletedPebbles.clear();
    }

    private static double globalVolume(MsrCache cache) {
        return cache.fuelCellCount * (double) Multiblocks.msrVolumePerFuelCell;
    }

    private static int maxPebbles(MsrCache cache) {
        return cache.fuelCellCount * Math.max(1, Multiblocks.msrPebblesPerFuelCell);
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag pebblesTag = new ListTag();
        for (ReactorPebble pebble : pebbles) {
            pebblesTag.add(pebble.serializeNBT(registries));
        }
        tag.put("pebbles", pebblesTag);
        ListTag depletedTag = new ListTag();
        for (ItemStack stack : depletedPebbles) {
            depletedTag.add(ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow());
        }
        tag.put("depletedPebbles", depletedTag);
        tag.putDouble("temperature", temperature);
        tag.putDouble("reactivity", reactivity);
        tag.putDouble("impurity", impurity);
        tag.putDouble("depletion", depletion);
        tag.putDouble("heatPerTick", heatPerTick);
        tag.putInt("overheatTimer", overheatTimer);
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        pebbles.clear();
        ListTag pebblesTag = LegacyTagReaders.list(tag, "pebbles", Tag.TAG_COMPOUND);
        for (int i = 0; i < pebblesTag.size(); i++) {
            ReactorPebble pebble = new ReactorPebble();
            pebble.deserializeNBT(registries, pebblesTag.getCompoundOrEmpty(i));
            pebbles.add(pebble);
        }
        depletedPebbles.clear();
        ListTag depletedTag = LegacyTagReaders.list(tag, "depletedPebbles", Tag.TAG_COMPOUND);
        for (int i = 0; i < depletedTag.size(); i++) {
            ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),
                    depletedTag.getCompoundOrEmpty(i)).result().orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) depletedPebbles.add(stack);
        }
        temperature = tag.contains("temperature") ? tag.getDoubleOr("temperature", 0D) : MsrControllerBE.T_AMBIENT;
        reactivity = tag.getDoubleOr("reactivity", 0D);
        impurity = tag.getDoubleOr("impurity", 0D);
        depletion = tag.getDoubleOr("depletion", 0D);
        heatPerTick = tag.getDoubleOr("heatPerTick", 0D);
        overheatTimer = tag.getIntOr("overheatTimer", 0);
    }
}
