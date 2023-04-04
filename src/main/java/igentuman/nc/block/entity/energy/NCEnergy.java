package igentuman.nc.block.entity.energy;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.setup.energy.SolarPanels;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class NCEnergy extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    protected final CustomEnergyStorage energyStorage = createEnergy();

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    protected int counter;

    protected void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), getEnergyTransferPerTick()), false);
                                    capacity.addAndGet(-received);
                                    energyStorage.consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return;
                    }
                }
            }
        }
    }

    protected int getEnergyMaxStorage() {
        return 100;
    }

    protected int getEnergyTransferPerTick() {
        return Math.min(100, energyStorage.getEnergyStored());
    }

    private CustomEnergyStorage createEnergy() {
        //todo read config
        return new CustomEnergyStorage(getEnergyMaxStorage(), 0, getEnergyMaxStorage()) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    public NCEnergy(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, String name) {
        super(pType, pPos, pBlockState);
    }

    public NCEnergy(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCEnergyBlocks.ENERGY_BE.get(name).get(), pPos, pBlockState);
        this.name = name;
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

    public String getName() {
        return name;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        energy.invalidate();
    }


    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("Energy", energyStorage.serializeNBT());
    }

}
