package igentuman.nc.datagen.tag;

import igentuman.nc.setup.NCTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.MODID;

/** Groups NuclearCraft trades into the scientist level pools and the wandering-trader common pool. */
public class ModVillagerTradeTagProvider extends KeyTagProvider<VillagerTrade> {
    public ModVillagerTradeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.VILLAGER_TRADE, lookupProvider, MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (NCTrades.TradeDef def : NCTrades.TRADES) {
            tag(NCTrades.tagFor(def)).add(def.key());
        }
    }
}
