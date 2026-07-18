package igentuman.nc.item;

import igentuman.nc.content.materials.Chunks;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NCChunkItem extends Item {
    public NCChunkItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        if (Chunks.get().registered().containsKey(this.toString().replace("_chunk", ""))) {
            super.fillItemCategory(pCategory, pItems);
        }
    }
}
