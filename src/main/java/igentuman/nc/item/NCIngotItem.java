package igentuman.nc.item;

import igentuman.nc.content.materials.Ingots;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCIngotItem extends Item {
    public NCIngotItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Ingots.get().registered().containsKey(this.toString().replace("_ingot", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
