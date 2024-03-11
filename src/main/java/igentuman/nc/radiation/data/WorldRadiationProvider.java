package igentuman.nc.radiation.data;

import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

public class WorldRadiationProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    public static Capability<WorldRadiation> WORLD_RADIATION = null;
    //CapabilityManager.get(new CapabilityToken<>(){});
    private WorldRadiation worldRadiation = createWorldRadiation();
    private final LazyOptional<WorldRadiation> opt = LazyOptional.of(() -> createWorldRadiation());

    @Nonnull
    private WorldRadiation createWorldRadiation() {
        if (worldRadiation == null) {
            worldRadiation = new WorldRadiation();
        }
        return worldRadiation;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == WORLD_RADIATION) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return worldRadiation.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        worldRadiation.deserializeNBT(nbt);
    }
}
