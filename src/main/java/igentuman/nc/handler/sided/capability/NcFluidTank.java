package igentuman.nc.handler.sided.capability;

import igentuman.api.platform.NCSerialization;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class NcFluidTank extends FluidTank {

    public NcFluidTank(int capacity) {
        super(capacity);
    }

    @Override
    public NcFluidTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        FluidStack fluid = NCSerialization.loadFluidStack(provider, nbt.getCompound("Fluid"));
        setFluid(fluid);
        if (nbt.contains("Capacity")) {
            capacity = nbt.getInt("Capacity");
        }
        return this;
    }

    @Override
    public CompoundTag writeToNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (!fluid.isEmpty()) {
            nbt.put("Fluid", NCSerialization.saveFluidStack(fluid, provider));
        }
        nbt.putInt("Capacity", capacity);
        return nbt;
    }

}
