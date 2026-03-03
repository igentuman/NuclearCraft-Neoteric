package igentuman.nc.handler;

import igentuman.api.platform.NCItemStacks;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.world.item.ItemStack;

public class ItemEnergyHandler {

    private final int storage;
    private final int output;
    private final int input;
    public ItemStack stack;
    private ItemEnergy energyInstance;

    public int sendRate() { return output; }
    public int chargeRate() { return input; }
    public int capacity() { return storage; }

    public int getEnergyStored() {
        return getEnergy().getEnergyStored();
    }

    public ItemEnergy getEnergy() {
        if (energyInstance == null) {
            energyInstance = new ItemEnergy(stack, capacity(), chargeRate(), sendRate());
        }
        return energyInstance;
    }

    public ItemEnergyHandler(ItemStack stack, int storage, int output, int input) {
        this.stack = stack;
        this.storage = storage;
        this.output = output;
        this.input = input;
    }

    public class ItemEnergy extends CustomEnergyStorage {
        private ItemStack stack;
        public ItemEnergy(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
            this.stack = stack;
            energy = NCItemStacks.getInt(stack, "energy");
        }

        @Override
        public int extractEnergy(int extract, boolean simulate) {
            int amount = super.extractEnergy(extract, simulate);
            if (!simulate)
                NCItemStacks.putInt(stack, "energy", this.energy);
            return amount;
        }

        @Override
        public int receiveEnergy(int receieve, boolean simulate) {
            int amount = super.receiveEnergy(receieve, simulate);
            if (!simulate)
                NCItemStacks.putInt(stack, "energy", this.energy);
            return amount;
        }
    }
}
