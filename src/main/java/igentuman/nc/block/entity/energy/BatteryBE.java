package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.ISizeToggable;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.handler.sided.SlotModePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BatteryBE extends NCEnergy {
    public static final ModelProperty<HashMap<Integer, ISizeToggable.SideMode>> SIDE_CONFIG = new ModelProperty<>();
    public boolean syncSideConfig = true;
    public BatteryBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), ISizeToggable.SideMode.DEFAULT);
        }
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }

    @Nonnull
    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(SIDE_CONFIG, sideConfig)
                .build();
    }
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        transferEnergy();
    }

    /**
     * Push pull energy to adjacent blocks
     */
    protected void transferEnergy() {
        AtomicInteger capacity = new AtomicInteger(energyStorage.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if(
                    sideConfig.get(direction.ordinal()) == ISizeToggable.SideMode.DISABLED ||
                    sideConfig.get(direction.ordinal()) == ISizeToggable.SideMode.DEFAULT
            ) continue;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be != null) {
                IEnergyStorage sideEnergy = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                if(sideEnergy == null) continue;
                if (capacity.get() > 0 && sideConfig.get(direction.ordinal()) == ISizeToggable.SideMode.OUT) {
                    int accepted = sideEnergy.receiveEnergy(Math.min(capacity.get(), getEnergyTransferPerTick()), false);
                    capacity.addAndGet(-accepted);
                } else if (capacity.get() < getEnergyMaxStorage() && sideConfig.get(direction.ordinal()) == ISizeToggable.SideMode.IN) {
                    int extracted = sideEnergy.extractEnergy(Math.min(getEnergyTransferPerTick(), getEnergyMaxStorage() - capacity.get()), false);
                    capacity.addAndGet(extracted);
                }
            }
        }
        if(capacity.get() != energyStorage.getEnergyStored()) {
            energyStorage.setEnergy(capacity.get());
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && (side != null && sideConfig.get(side.ordinal()) != ISizeToggable.SideMode.DISABLED)) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }


    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(BatteryBlocks.all().get(getName()).getStorage(), energyStorage.getEnergyStored());
    }

    public int getMaxTransfer() {
        return getEnergyMaxStorage();
    }

    protected int getEnergyMaxStorage() {
        return BatteryBlocks.all().get(getBlockState().getBlock().asItem().toString()).getStorage();
    }


    @Override
    public void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    private void loadSideConfig(int[] tagData) {
        boolean changed = false;
        for (int i = 0; i < sideConfig.size(); i++) {
            ISizeToggable.SideMode newMode = ISizeToggable.SideMode.values()[tagData[i]];
            if(sideConfig.get(i) != newMode) {
                changed = true;
                sideConfig.remove(i);
                sideConfig.put(i, newMode);
            }

        }
        if(changed) {
            requestModelDataUpdate();
            if(level == null) return;
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public ISizeToggable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, ISizeToggable.SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        return sideConfig.get(direction);
    }
}
