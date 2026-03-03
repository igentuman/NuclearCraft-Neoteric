package igentuman.nc.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class HazmatItem extends ArmorItem {

    public HazmatItem(Holder<ArmorMaterial> armorMaterial, Type type, Properties hazmatProps) {
        super(armorMaterial, type, hazmatProps);
    }
}
