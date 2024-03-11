package igentuman.nc.util.insitu_leaching;

import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

public class WorldVeinsProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    //public static Capability<WorldVeinOres> VEINS_CAP = CapabilityManager.INSTANCE.(new CapabilityToken<>(){});
    private WorldVeinOres veinsData = createVeinData();
    private final LazyOptional<WorldVeinOres> opt = LazyOptional.of(() -> createVeinData());

    @Nonnull
    private WorldVeinOres createVeinData() {
        if (veinsData == null) {
            veinsData = new WorldVeinOres();
        }
        return veinsData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
      /*  if (cap == VEINS_CAP) {
            return opt.cast();
        }*/
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return veinsData.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        veinsData.deserializeNBT(nbt);
    }
}
