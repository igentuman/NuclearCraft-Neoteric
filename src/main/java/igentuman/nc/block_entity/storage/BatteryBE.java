package igentuman.nc.block_entity.storage;

import igentuman.nc.config.Common;
import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class BatteryBE extends AbstractStorageBE {

    private static final int LIGHTNING_COOLDOWN = 600;

    public final LargeEnergyStorage energyStorage;
    private long lastLightningStrike = -LIGHTNING_COOLDOWN;

    public BatteryBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
        int capacity = StorageDefs.batteryStorage(name);
        this.energyStorage = LargeEnergyStorage.create(capacity, capacity, capacity, this::setChanged);
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        if (side != null && getSideMode(side) == SideMode.DISABLED) return null;
        return energyStorage;
    }

    @Override
    protected boolean transfer() {
        if (level == null) return false;
        boolean changed = false;
        int rate = energyStorage.getMaxEnergyStored();
        for (Direction direction : Direction.values()) {
            SideMode mode = getSideMode(direction);
            if (mode == SideMode.DEFAULT || mode == SideMode.DISABLED) continue;
            IEnergyStorage neighbour = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;

            if (mode == SideMode.OUT) {
                int canExtract = energyStorage.extractEnergy(rate, true);
                if (canExtract <= 0) continue;
                int accepted = neighbour.receiveEnergy(canExtract, false);
                if (accepted > 0) {
                    energyStorage.extractEnergy(accepted, false);
                    changed = true;
                }
            } else if (mode == SideMode.IN) {
                int canReceive = energyStorage.receiveEnergy(rate, true);
                if (canReceive <= 0) continue;
                int provided = neighbour.extractEnergy(canReceive, false);
                if (provided > 0) {
                    energyStorage.receiveEnergy(provided, false);
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    public int getComparatorSignal() {
        int max = energyStorage.getMaxEnergyStored();
        if (max <= 0) return 0;
        return (int) ((energyStorage.getEnergyStored() / (double) max) * 15);
    }

    public void onLightningStrike() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int charge = Common.LIGHTNING_ROD_CHARGE.get();
        if (charge <= 0) return;
        long time = level.getGameTime();
        if (time - lastLightningStrike < LIGHTNING_COOLDOWN) return;
        lastLightningStrike = time;
        energyStorage.addEnergy(charge);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D,
                12, 0.3D, 0.1D, 0.3D, 0.0D);
    }

    public boolean hasContent() {
        return energyStorage.getEnergyStored() > 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Energy", energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
    }
}
