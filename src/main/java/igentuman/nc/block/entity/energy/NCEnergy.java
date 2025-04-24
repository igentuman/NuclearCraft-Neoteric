package igentuman.nc.block.entity.energy;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class NCEnergy extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    public NCEnergy(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, String name) {
        super(pType, pPos, pBlockState);
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    public NCEnergy(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCEnergyBlocks.ENERGY_BE.get(name).get(), pPos, pBlockState);
        this.name = name;
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    protected void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(energyStorage().getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), getEnergyTransferPerTick()), false);
                                    capacity.addAndGet(-received);
                                    energyStorage().consumeEnergy(received);
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

    @Override
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected int getEnergyMaxStorage() {
        return 100;
    }

    protected int getEnergyTransferPerTick() {
        return Math.min(getEnergyMaxStorage(), energyStorage().getEnergyStored());
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(getEnergyMaxStorage(), getMaxTransfer(), getEnergyMaxStorage()) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public int getMaxTransfer() {
        return 0;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return getEnergy().cast();
        }
        return super.getCapability(cap, side);
    }

    public String getName() {
        return name;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        getEnergy().invalidate();
    }
}
