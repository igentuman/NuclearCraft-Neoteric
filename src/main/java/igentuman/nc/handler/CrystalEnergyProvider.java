package igentuman.nc.handler;

import igentuman.nc.item.ResoniteCrystalItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

/**
 * Infinite, stateless Forge-energy capability attached to an analyzed {@link ResoniteCrystalItem}.
 * Always reads full; never stores or decrements; supplies a fixed FE/t derived from the crystal's
 * rolled rarity. A raw (un-analyzed) crystal exposes the cap but outputs nothing.
 */
public class CrystalEnergyProvider implements ICapabilityProvider {

    public static final int MAX = Integer.MAX_VALUE;

    private final LazyOptional<IEnergyStorage> energy;

    public CrystalEnergyProvider(ItemStack stack) {
        this.energy = LazyOptional.of(() -> new CrystalEnergy(stack));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ENERGY) {
            return energy.cast();
        }
        return LazyOptional.empty();
    }

    public static class CrystalEnergy implements IEnergyStorage {

        private final ItemStack stack;

        public CrystalEnergy(ItemStack stack) {
            this.stack = stack;
        }

        /** Re-read each call so a raw crystal becomes a live source the moment it is analyzed. */
        private int fePerTick() {
            return ResoniteCrystalItem.feOutput(stack);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return Math.min(Math.max(0, maxExtract), fePerTick());
        }

        @Override
        public int getEnergyStored() {
            return fePerTick() > 0 ? MAX : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX;
        }

        @Override
        public boolean canExtract() {
            return fePerTick() > 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
