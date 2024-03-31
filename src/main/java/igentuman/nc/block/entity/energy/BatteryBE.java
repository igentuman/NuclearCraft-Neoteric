package igentuman.nc.block.entity.energy;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.ISizeToggable;
import igentuman.nc.content.energy.BatteryBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BE;
import static net.minecraftforge.common.util.Constants.BlockFlags.DEFAULT;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class BatteryBE extends NCEnergy {
    public static final ModelProperty<HashMap<Integer, ISizeToggable.SideMode>> SIDE_CONFIG = new ModelProperty<>();
    public boolean syncSideConfig = true;
    public BatteryBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), ISizeToggable.SideMode.DEFAULT);
        }
    }

    public BatteryBE(String name) {
        super(BlockPos.ZERO, null, name);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), ISizeToggable.SideMode.DEFAULT);
        }
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }

    @Nonnull
    @Override
    public @NotNull IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(SIDE_CONFIG, sideConfig)
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
            TileEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be != null) {
                IEnergyStorage sideEnergy = be.getCapability(ENERGY, direction.getOpposite()).orElse(null);
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
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), DEFAULT);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ENERGY && (side != null && sideConfig.get(side.ordinal()) != ISizeToggable.SideMode.DISABLED)) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }


    @Override
    protected int getEnergyTransferPerTick() {
        return Math.min(BatteryBlocks.all().get(getName()).getStorage(), energyStorage.getEnergyStored());
    }

    public int getMaxTransfer() {
        return getEnergyMaxStorage();
    }

    protected int getEnergyMaxStorage() {
        return BatteryBlocks.all().get(getName()).getStorage();
    }


    @Override
    protected void saveClientData(CompoundNBT tag) {
        super.saveClientData(tag);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    @Override
    public void loadClientData(CompoundNBT tag) {
        super.loadClientData(tag);
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
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
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), DEFAULT);
        }
    }

    @Override
    public void saveAdditional(CompoundNBT tag) {
        super.saveAdditional(tag);
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public ISizeToggable.SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, ISizeToggable.SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), DEFAULT);
        return sideConfig.get(direction);
    }
}
