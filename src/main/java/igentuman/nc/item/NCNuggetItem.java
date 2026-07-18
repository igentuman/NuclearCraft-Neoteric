package igentuman.nc.item;

import igentuman.nc.content.materials.Nuggets;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCNuggetItem extends Item {
    public NCNuggetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Nuggets.get().registered().containsKey(this.toString().replace("_nugget", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
