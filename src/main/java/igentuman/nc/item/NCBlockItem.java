package igentuman.nc.item;

import igentuman.nc.content.materials.Blocks;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCBlockItem extends Item {
    public NCBlockItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Blocks.get().registered().containsKey(this.toString().replace("_block", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
