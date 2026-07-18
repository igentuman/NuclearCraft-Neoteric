package igentuman.nc.item;

import igentuman.nc.content.materials.Gems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCBGemItem extends Item {
    public NCBGemItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Gems.get().registered().containsKey(this.toString().replace("_gem", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
