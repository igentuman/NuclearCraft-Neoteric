package igentuman.nc.radiation.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

public class PlayerRadiationProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerRadiation> PLAYER_RADIATION = CapabilityManager.get(new CapabilityToken<>(){});
    private PlayerRadiation playerRadiation = createPlayerRadiation();
    private final LazyOptional<PlayerRadiation> opt = LazyOptional.of(() -> createPlayerRadiation());

    @Nonnull
    private PlayerRadiation createPlayerRadiation() {
        if (playerRadiation == null) {
            playerRadiation = new PlayerRadiation();
        }
        return playerRadiation;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == PLAYER_RADIATION) {
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
    public CompoundTag serializeNBT() {
        return playerRadiation.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        playerRadiation.deserializeNBT(nbt);
    }
}
