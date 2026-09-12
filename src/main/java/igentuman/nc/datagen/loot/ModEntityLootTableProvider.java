package igentuman.nc.datagen.loot;

import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Ghouls;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

/** Generates entity loot tables. */
public class ModEntityLootTableProvider extends EntityLootSubProvider {

    public ModEntityLootTableProvider(HolderLookup.Provider lookupProvider) {
        super(FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    public void generate() {
        add(Ghouls.FERAL_GHOUL.get(), LootTable.lootTable()
                .withPool(itemPool(Items.ROTTEN_FLESH, 1.0F, 2.0F)));

        add(Ghouls.FERAL_GHOUL_BOSS.get(), LootTable.lootTable()
                .withPool(itemPool(ModEntries.ISOTOPES.get("xenorium/298").base().get(), 0.0F, 1.0F))
                .withPool(itemPool(ModEntries.ISOTOPES.get("californium/252").base().get(), 0.0F, 1.0F))
                .withPool(itemPool(Items.ROTTEN_FLESH, 1.0F, 2.0F))
                .withPool(itemPool(ModEntries.get("wasteland_earth").block().get(), 0.0F, 2.0F)));
    }

    private LootPool.Builder itemPool(net.minecraft.world.level.ItemLike item, float min, float max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                registries, UniformGenerator.between(0.0F, 1.0F))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(Ghouls.FERAL_GHOUL.get(), Ghouls.FERAL_GHOUL_BOSS.get());
    }
}
