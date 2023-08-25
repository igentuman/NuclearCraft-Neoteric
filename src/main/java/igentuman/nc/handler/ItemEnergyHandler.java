package igentuman.nc.handler;

import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class ItemEnergyHandler implements ICapabilityProvider {
    private final int storage;
    private final int output;
    private final int input;

    protected final LazyOptional<ItemEnergy>  energy = LazyOptional.of(this::createEnergy);

    private ItemEnergy createEnergy() {
        return new ItemEnergy(stack, capacity(), chargeRate(), sendRate());
    }

    public int sendRate() {
        return output;
    }

    public int chargeRate() {
        return input;
    }

    public int capacity() {
        return storage;
    }

    public int getEnergyStored() {
        return getCapability(ENERGY, null).orElse(null).getEnergyStored();
    }

    public ItemStack stack;
    public ItemEnergyHandler(ItemStack stack, int storage, int output, int input) {
        this.stack = stack;
        this.storage = storage;
        this.output = output;
        this.input = input;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap ==  ENERGY) {
            return energy.cast();
        }
        return LazyOptional.empty();
    }

    public class ItemEnergy extends CustomEnergyStorage {
        private ItemStack stack;
        public ItemEnergy(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
            this.stack = stack;
            energy = stack.getOrCreateTag().contains("energy") ? stack.getOrCreateTag().getInt("energy") : 0;
        }

        @Override
        public int extractEnergy(int extract, boolean simulate) {
            int amount = super.extractEnergy(extract, simulate);
            if (!simulate)
                stack.getOrCreateTag().putInt("energy", this.energy);

            return amount;
        }

        @Override
        public int receiveEnergy(int receieve, boolean simulate) {
            int amount = super.receiveEnergy(receieve, simulate);
            if (!simulate)
                stack.getOrCreateTag().putInt("energy", this.energy);

            return amount;
        }
    }
}
