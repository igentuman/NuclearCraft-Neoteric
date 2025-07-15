package igentuman.nc.setup;

import igentuman.nc.radiation.data.IPlayerRadiationCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerRadiationStorage implements Capability.IStorage<IPlayerRadiationCapability> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IPlayerRadiationCapability> capability, IPlayerRadiationCapability instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IPlayerRadiationCapability> capability, IPlayerRadiationCapability instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}