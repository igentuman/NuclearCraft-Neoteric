package igentuman.nc.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class NCBucketItem extends BucketItem {
    public NCBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
        super(supplier.get(), builder);
    }
}
