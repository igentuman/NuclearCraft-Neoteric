package igentuman.nc.item;

import igentuman.nc.content.materials.Dusts;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCDustItem extends Item {
    public NCDustItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Dusts.get().registered().containsKey(this.toString().replace("_dust", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
