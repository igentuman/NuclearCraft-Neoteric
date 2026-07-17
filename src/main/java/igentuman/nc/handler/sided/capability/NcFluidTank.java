package igentuman.nc.handler.sided.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class NcFluidTank extends FluidTank {

    public NcFluidTank(int capacity) {
        super(capacity);
    }

    @Override
    public NcFluidTank readFromNBT(CompoundTag nbt) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
        setFluid(fluid);
        capacity = nbt.getInt("Capacity");
        return this;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        fluid.writeToNBT(nbt);
        nbt.putInt("Capacity", capacity);
        return nbt;
    }

}
