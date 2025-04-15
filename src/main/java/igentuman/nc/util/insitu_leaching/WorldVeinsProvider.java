package igentuman.nc.util.insitu_leaching;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import static igentuman.nc.NuclearCraft.rl;

public class WorldVeinsProvider implements ICapabilitySerializable<CompoundTag> {

    public static Capability<WorldVeinOres> VEINS_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    private WorldVeinOres veinsData = createVeinData();
    private final LazyOptional<WorldVeinOres> opt = LazyOptional.of(() -> createVeinData());
    public static boolean isTracking = false;
    private Level level;

    public WorldVeinsProvider() {
    }

    public WorldVeinsProvider(Level level) {
        this.level = level;
    }

    public static void  attachVeinCapability(final AttachCapabilitiesEvent<Level> event) {
        if (!event.getObject().isClientSide() && !event.getObject().getCapability(VEINS_CAP).isPresent()) {
            event.addCapability(rl("veins"), new WorldVeinsProvider(event.getObject()));
            isTracking = true;
        }
    }

    public static void stopTracking() {
        isTracking = false;
    }

    public static void startTracking() {
        isTracking = true;
    }

    @Nonnull
    private WorldVeinOres createVeinData() {
        if (veinsData == null) {
            veinsData = new WorldVeinOres();
        }
        return new WorldVeinOres(level);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == VEINS_CAP) {
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
        return veinsData.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        veinsData.deserializeNBT(nbt);
    }
}
