package igentuman.nc.handler;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.content.processors.ProcessorPrefab;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class UpgradesHandler extends ItemStackHandler {
    protected NCProcessorBE be;
    public boolean wasUpdated = true;

    public UpgradesHandler(NCProcessorBE be) {
        super(be.prefab().getUpgradesSlots());
        this.be = be;
    }

    @Override
    protected void onContentsChanged(int slot) {
        wasUpdated = true;
        be.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if(be.prefab().supportEnergyUpgrade && slot == 0) {
            return stack.getItem().equals(NCItems.NC_ITEMS.get("upgrade_energy").get());
        }

        if(be.prefab().supportSpeedUpgrade && slot == 1) {
            return stack.getItem().equals(NCItems.NC_ITEMS.get("upgrade_speed").get());
        }

        return be.prefab().getUpgradesSlots() == 1 && stack.getItem().equals(NCItems.NC_ITEMS.get("upgrade_speed").get());
    }
}
