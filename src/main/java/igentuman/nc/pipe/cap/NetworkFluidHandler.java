package igentuman.nc.pipe.cap;

import igentuman.nc.block.pipe.entity.PipeConnectorBE;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class NetworkFluidHandler extends NetworkHandler implements IFluidHandler {

    public NetworkFluidHandler(PipeConnectorBE connector) {
        super(connector);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || blocked()) {
            return 0;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return 0;
        }
        FluidStack remaining = resource.copy();
        int filled = 0;
        for (long packed : net.getDestinations(PipeCapabilityType.FLUID, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE dest = manager.getConnectorBE(packed);
            if (dest == null) {
                continue;
            }
            BlockPos dPos = dest.getBlockPos();
            for (Direction face : Direction.values()) {
                BlockEntity nbe = dest.getExternalNeighbor(face);
                if (nbe == null) {
                    continue;
                }
                IFluidHandler h = nbe.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) {
                    continue;
                }
                int accepted = h.fill(remaining, action);
                if (accepted > 0) {
                    filled += accepted;
                    remaining.shrink(accepted);
                    if (remaining.isEmpty()) {
                        return filled;
                    }
                }
            }
        }
        return filled;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return gather(h -> h.drain(resource, action));
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        return gather(h -> h.drain(maxDrain, action));
    }

    private FluidStack gather(java.util.function.Function<IFluidHandler, FluidStack> op) {
        if (blocked()) {
            return FluidStack.EMPTY;
        }
        ServerLevel level = level();
        PipeNetwork net = network();
        PipeNetworkManager manager = manager();
        if (level == null || net == null || manager == null) {
            return FluidStack.EMPTY;
        }
        for (long packed : net.getSources(PipeCapabilityType.FLUID, manager)) {
            if (packed == self()) {
                continue;
            }
            PipeConnectorBE src = manager.getConnectorBE(packed);
            if (src == null) {
                continue;
            }
            BlockPos sPos = src.getBlockPos();
            for (Direction face : Direction.values()) {
                BlockEntity nbe = src.getExternalNeighbor(face);
                if (nbe == null) {
                    continue;
                }
                IFluidHandler h = nbe.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) {
                    continue;
                }
                FluidStack got = op.apply(h);
                if (!got.isEmpty()) {
                    return got;
                }
            }
        }
        return FluidStack.EMPTY;
    }
}
