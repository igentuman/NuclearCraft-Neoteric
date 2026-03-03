package igentuman.api.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

/**
 * Platform wrapper for villager trading APIs.
 * In 1.21.1: MerchantOffer cost parameters changed from ItemStack to ItemCost,
 * and dual-cost offers use Optional&lt;ItemCost&gt;.
 */
public final class NCTrading {
    private NCTrading() {}

    public static ItemCost cost(ItemLike item, int count) {
        return new ItemCost(item, count);
    }

    public static ItemCost cost(ItemStack stack) {
        return new ItemCost(stack.getItem(), stack.getCount());
    }

    public static MerchantOffer offer(ItemCost cost, ItemStack result,
                                      int maxUses, int xp, float priceMultiplier) {
        return new MerchantOffer(cost, result, maxUses, xp, priceMultiplier);
    }

    public static MerchantOffer offer(ItemCost costA, ItemCost costB, ItemStack result,
                                      int maxUses, int xp, float priceMultiplier) {
        return new MerchantOffer(costA, Optional.of(costB), result, maxUses, xp, priceMultiplier);
    }
}
