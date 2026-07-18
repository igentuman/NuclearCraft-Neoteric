package igentuman.nc.item;

import igentuman.nc.content.materials.Plates;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCPlateItem extends Item {
    public NCPlateItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Plates.get().registered().containsKey(this.toString().replace("_plate", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
