package igentuman.nc.block.entity;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.content.storage.BarrelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BE;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class BarrelBE extends NuclearCraftBE implements ISizeToggable {

    public final FluidTank fluidTank;

    private FluidTank createTank() {
        return new FluidTank(BarrelBlocks.all().get(getName()).config().getCapacity()) {
            @Override
            public void setFluid(FluidStack fluid) {
                super.setFluid(fluid);
                setChanged();
            }
        };
    }

    public LazyOptional<IFluidHandler> getFluidHandler() {
        return fluidHandler;
    }

    protected final LazyOptional<IFluidHandler> fluidHandler;

    public static final ModelProperty<HashMap<Integer, SideMode>> SIDE_CONFIG = new ModelProperty<>();
    public boolean syncSideConfig = true;
    public BarrelBE(BlockPos pPos, BlockState pBlockState) {
        super(STORAGE_BE.get(getName(pBlockState)).get(), pPos, pBlockState);
        for (Direction direction : Direction.values()) {
            sideConfig.put(direction.ordinal(), SideMode.DEFAULT);
        }
        fluidTank = createTank();
        fluidHandler = LazyOptional.of(() -> fluidTank);
    }

    public void tickClient() {

    }
    public void tickServer() {
        transferFluid();
    }

    /**
     * Push pull fluids to adjacent blocks
     */
    protected void transferFluid() {
        AtomicInteger currentAmount = new AtomicInteger(fluidTank.getFluidAmount());
        boolean wasUpdated = false;
        for (Direction direction : Direction.values()) {
            if(
                    sideConfig.get(direction.ordinal()) == SideMode.DISABLED ||
                    sideConfig.get(direction.ordinal()) == SideMode.DEFAULT
            ) continue;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be != null) {
                IFluidHandler sideHandler = be.getCapability(FLUID_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
                if(sideHandler == null) continue;
                if (currentAmount.get() > 0 && sideConfig.get(direction.ordinal()) == SideMode.OUT) {
                    int accepted = sideHandler.fill(fluidTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                    if(accepted > 0) {
                        fluidTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                        wasUpdated = true;
                    }
                    currentAmount.addAndGet(-accepted);
                } else if (currentAmount.get() < getTankCapacity() && sideConfig.get(direction.ordinal()) == SideMode.IN) {
                    int extracted = fluidTank.fill(sideHandler.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                    if(extracted > 0) {
                        sideHandler.drain(extracted, IFluidHandler.FluidAction.EXECUTE);
                        wasUpdated = true;
                    }
                    currentAmount.addAndGet(extracted);
                }
            }
        }
        if(wasUpdated) {
            level.setBlockAndUpdate(worldPosition, getBlockState());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private int getTankCapacity() {
        return BarrelBlocks.all().get(getName()).config().getCapacity();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == FLUID_HANDLER_CAPABILITY && (side != null && sideConfig.get(side.ordinal()) != SideMode.DISABLED)) {
            return getFluidHandler().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    protected void saveClientData(CompoundTag tag) {
        CompoundTag tank = new CompoundTag();
        tag.put("Fluid", fluidTank.getFluid().writeToNBT(tank));
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public void loadClientData(CompoundTag tag) {
        if(tag.contains("Fluid")) {
            fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid")));
        }
        if (!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("Fluid")) {
            fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid")));
        }
        if(!tag.contains("sideConfig")) return;
        loadSideConfig(tag.getIntArray("sideConfig"));
    }

    private void loadSideConfig(int[] tagData) {
        boolean changed = false;
        for (int i = 0; i < sideConfig.size(); i++) {
            SideMode newMode = SideMode.values()[tagData[i]];
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

    public void saveAdditional(CompoundTag tag) {
        CompoundTag tank = new CompoundTag();
        tag.put("Fluid", fluidTank.getFluid().writeToNBT(tank));
        tag.putIntArray("sideConfig", sideConfig.values().stream().mapToInt(Enum::ordinal).toArray());
    }

    public SideMode toggleSideConfig(int direction) {
        sideConfig.put(direction, SideMode.values()[(sideConfig.get(direction).ordinal() + 1) % 4]);
        setChanged();
        level.setBlockAndUpdate(worldPosition, getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        return sideConfig.get(direction);
    }
}
