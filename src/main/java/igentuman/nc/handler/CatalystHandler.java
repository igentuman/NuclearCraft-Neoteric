package igentuman.nc.handler;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static igentuman.nc.radiation.ItemRadiation.getItemByName;

public class CatalystHandler extends ItemStackHandler {

    protected final NCProcessorBE be;
    public boolean wasUpdated = true;

    public CatalystHandler(NCProcessorBE be) {
        super(1);
        this.be = be;
    }

    @Override
    protected void onContentsChanged(int slot) {
        wasUpdated = true;
        be.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        for(Object item: be.getAllowedCatalysts()) {
            if(stack.getItem().equals(item)) {
                return stack.getOrCreateTag().contains("is_nc_analyzed") || stack.is(getItemByName("immersiveengineering:coresample"));
            }
        }
        return false;
    }
}
