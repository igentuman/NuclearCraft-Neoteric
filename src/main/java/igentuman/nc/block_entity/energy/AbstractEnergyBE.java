package igentuman.nc.block_entity.energy;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.setup.ModEntries;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.radiation.source.BlockRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractEnergyBE extends GlobalBlockEntity {

    public final LargeEnergyStorage energyStorage;

    @Nullable
    private UUID radSourceId;

    protected AbstractEnergyBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name, int capacity) {
        super(type, pos, state, name);
        this.energyStorage = LargeEnergyStorage.create(capacity, 0, capacity, this::setChanged);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drops();
    }

    @Nullable
    @Override
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    protected abstract int generate();

    protected void emitRadiation() {
    }

    @Override
    public void serverTick() {
        if (name != null && !ModEntries.isEnabled(name)) return;
        int generated = generate();
        if (generated > 0) {
            energyStorage.addEnergy(generated);
            setChanged();
        }
        pushEnergy();
        emitRadiation();
        if (wasChanged && level != null) {
            wasChanged = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void clientTick() {
    }

    private void pushEnergy() {
        if (level == null || energyStorage.getEnergyStored() <= 0) return;
        int rate = energyStorage.getMaxEnergyStored();
        for (Direction direction : Direction.values()) {
            EnergyHandler neighbour = level.getCapability(
                    Capabilities.Energy.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;
            if (energyStorage.getAmountAsLong() <= 0) return;
            EnergyHandlerUtil.move(energyStorage, neighbour, rate, null);
        }
    }

    protected void assertRadiationSource(RadiationProfile profile) {
        if (!(level instanceof ServerLevel server)) return;
        WorldSourceRegistry registry = WorldSourceRegistry.get(server);
        long now = server.getGameTime();
        if (registry.atBlock(worldPosition) instanceof BlockRadSource existing) {
            existing.refresh(profile);
            radSourceId = existing.getId();
            return;
        }
        UUID id = UUID.randomUUID();
        registry.register(new BlockRadSource(id, server.dimension(), worldPosition, profile, now, false));
        radSourceId = id;
    }

    protected void clearRadiationSource() {
        if (radSourceId != null && level instanceof ServerLevel server) {
            WorldSourceRegistry.get(server).remove(radSourceId);
        }
        radSourceId = null;
    }

    protected static RadiationProfile isotopeProfile(String isotopeId, double targetBq, long now) {
        RadiationProfile profile = new RadiationProfile();
        Isotope iso = IsotopeRegistry.get(isotopeId);
        if (iso == null) return profile;
        double perAtom = new IsotopeStack(iso, 1.0, now).currentActivityBq();
        double atoms = perAtom > 0 ? targetBq / perAtom : targetBq;
        profile.put(new IsotopeStack(iso, atoms, now));
        return profile;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        clearRadiationSource();
    }

    @Override
    protected void saveTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveTagData(tag, registries);
        tag.put("Energy", energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTagData(tag, registries);
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
    }
}
