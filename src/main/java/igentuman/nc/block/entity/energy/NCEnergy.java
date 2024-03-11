package igentuman.nc.block.entity.energy;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class NCEnergy extends NuclearCraftBE implements ITickableTileEntity {

    protected String name = "";
    public static String NAME;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    public NCEnergy(TileEntityType<? extends NCEnergy> tileEntityType, String name) {
        super(tileEntityType, name);
        this.name = name;
        energyStorage = createEnergy();
        energy = LazyOptional.of(() -> energyStorage);
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }


    protected int counter;

    protected void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                TileEntity be = level.getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ENERGY, direction.getOpposite()).map(handler -> {
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
        if (cap == ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    public NCEnergy(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState, String name) {
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
    public @NotNull CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundNBT tag) {
        CompoundNBT infoTag = new CompoundNBT();
        saveTagData(infoTag);

        tag.put("Info", infoTag);

        tag.put("energy_storage", energyStorage.serializeNBT());
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    public void loadClientData(CompoundNBT tag) {
        if (tag.contains("energy_storage")) {
            energyStorage.deserializeNBT(tag.getCompound("energy_storage"));
        }
        if(tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
    }

/*    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundNBT tag = pkt.getTag();
        handleUpdateTag(tag);
        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }*/

    @Override
    public void load(BlockState state,CompoundNBT tag) {
        if (tag.contains("energy_storage")) {
            energyStorage.deserializeNBT(tag.getCompound("energy_storage"));
        }
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }

        super.load(state, tag);
    }

    public void saveAdditional(CompoundNBT tag) {
        tag.put("energy_storage", energyStorage.serializeNBT());
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void tick() {
        if(level == null) return;
        if(level.isClientSide()) {
            tickClient();
        }
        if(!level.isClientSide()) {
            tickServer();
        }
    }
}
