package igentuman.nc.setup;

import com.mojang.serialization.MapCodec;
import igentuman.nc.config.Common;
import igentuman.nr.registry.NRMedicineItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registers.LOOT_CONDITION_TYPES;

/**
 * Data-driven Nuclear Scientist and wandering-trader trades: registry keys, datagen bootstraps and the
 * config-backed merchant predicate.
 */
public final class NCTrades {
    private NCTrades() {
    }

    private static final float VILLAGER_PRICE_MULTIPLIER = 0.02f;
    private static final float WANDERER_PRICE_MULTIPLIER = 0.2f;
    public static final int MAX_LEVEL = 5;

    /** One trade definition; item suppliers resolve only during datagen bootstrap. */
    public record TradeDef(ResourceKey<VillagerTrade> key, int level, Supplier<? extends ItemLike> wants, int wantsCount,
                           Supplier<? extends ItemLike> additionalWants, int additionalCount,
                           Supplier<? extends ItemLike> gives, int givesCount, int maxUses, int xp, float priceMultiplier) {
    }

    /** Scientist trades in level order; level 0 marks the wandering-trader trade. */
    public static final List<TradeDef> TRADES = new ArrayList<>();

    public static final ResourceKey<VillagerTrade> WANDERER_PLUTONIUM_239 =
            ResourceKey.create(Registries.VILLAGER_TRADE, rl("wandering_trader/emerald_plutonium_239"));

    static {
        scientist(1, "emerald_graphite_dust", () -> Items.EMERALD, 1, nc("graphite_dust"), 2, 32, 1);
        scientist(1, "emerald_quartz_dust", () -> Items.EMERALD, 1, nc("quartz_dust"), 2, 32, 1);
        scientist(1, "lead_ingot_emerald", nc("lead_ingot"), 8, () -> Items.EMERALD, 1, 24, 1);

        scientist(2, "emerald_plate_basic", () -> Items.EMERALD, 6, nc("plate_basic"), 1, 16, 5);
        scientist(2, "emerald_radaway", () -> Items.EMERALD, 8, NRMedicineItems.RADAWAY, 1, 6, 5);

        scientist(3, "neptunium_236_emerald", nc("neptunium_236"), 1, () -> Items.EMERALD, 4, 7, 15);
        scientist(3, "emerald_plutonium_238", () -> Items.EMERALD, 32, nc("plutonium_238"), 2, 7, 15);
        scientist(3, "emerald_calcium_sulfate_dust", () -> Items.EMERALD, 20, nc("calcium_sulfate_dust"), 2, 7, 15);

        scientist(4, "platinum_plate_plate_elite", nc("platinum_plate"), 16, nc("plate_elite"), 1, 7, 20);
        scientist(4, "emerald_and_book_fuel_californium", () -> Items.EMERALD, 32, () -> Items.BOOK, 4,
                nc("fuel_californium_hecf_251"), 2, 7, 20);
        scientist(4, "platinum_and_cobalt_fuel_americium", nc("platinum_ingot"), 16, nc("cobalt_ingot"), 4,
                nc("fuel_americium_hea_242"), 2, 7, 20);

        scientist(5, "emerald_and_cobalt_coil", () -> Items.EMERALD, 16, nc("cobalt_ingot"), 4,
                nc("coil_magnesium_diboride"), 2, 7, 30);
        scientist(5, "emerald_and_zinc_neutronium", () -> Items.EMERALD, 16, nc("zinc_ingot"), 8,
                nc("neutronium_ingot"), 2, 7, 30);

        TRADES.add(new TradeDef(WANDERER_PLUTONIUM_239, 0, () -> Items.EMERALD, 8, null, 0,
                nc("plutonium_239"), 1, 8, 2, WANDERER_PRICE_MULTIPLIER));
    }

    private static final Map<Integer, ResourceKey<TradeSet>> SCIENTIST_SETS = new LinkedHashMap<>();
    private static final Map<Integer, TagKey<VillagerTrade>> SCIENTIST_TAGS = new LinkedHashMap<>();

    static {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            SCIENTIST_SETS.put(level, ResourceKey.create(Registries.TRADE_SET, rl("nuclear_scientist/level_" + level)));
            SCIENTIST_TAGS.put(level, TagKey.create(Registries.VILLAGER_TRADE, rl("nuclear_scientist/level_" + level)));
        }
    }

    public static Int2ObjectMap<ResourceKey<TradeSet>> scientistTradeSets() {
        Int2ObjectMap<ResourceKey<TradeSet>> map = new Int2ObjectOpenHashMap<>();
        SCIENTIST_SETS.forEach((level, set) -> map.put(level.intValue(), set));
        return map;
    }

    public static TagKey<VillagerTrade> scientistTag(int level) {
        return SCIENTIST_TAGS.get(level);
    }

    /** Tag receiving the trade with the given level; level 0 is the vanilla wandering-trader common pool. */
    public static TagKey<VillagerTrade> tagFor(TradeDef def) {
        return def.level() == 0 ? VillagerTradeTags.WANDERING_TRADER_COMMON : scientistTag(def.level());
    }

    public static void bootstrapTrades(BootstrapContext<VillagerTrade> context) {
        for (TradeDef def : TRADES) {
            Optional<TradeCost> additional = def.additionalWants() == null ? Optional.empty()
                    : Optional.of(new TradeCost(def.additionalWants().get(), def.additionalCount()));
            Optional<LootItemCondition> predicate = def.level() == 0
                    ? Optional.of(WanderingTraderTradesEnabled.INSTANCE) : Optional.empty();
            context.register(def.key(), new VillagerTrade(
                    new TradeCost(def.wants().get(), def.wantsCount()), additional,
                    new ItemStackTemplate(def.gives().get().asItem(), def.givesCount()),
                    def.maxUses(), def.xp(), def.priceMultiplier(), predicate, List.of()));
        }
    }

    public static void bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        HolderGetter<VillagerTrade> trades = context.lookup(Registries.VILLAGER_TRADE);
        SCIENTIST_SETS.forEach((level, key) -> context.register(key, new TradeSet(
                trades.getOrThrow(scientistTag(level)), ConstantValue.exactly(2.0F), false,
                Optional.of(rl("trade_set/nuclear_scientist/level_" + level)))));
    }

    private static void scientist(int level, String name, Supplier<? extends ItemLike> wants, int wantsCount,
                                  Supplier<? extends ItemLike> gives, int givesCount, int maxUses, int xp) {
        scientist(level, name, wants, wantsCount, null, 0, gives, givesCount, maxUses, xp);
    }

    private static void scientist(int level, String name, Supplier<? extends ItemLike> wants, int wantsCount,
                                  Supplier<? extends ItemLike> additionalWants, int additionalCount,
                                  Supplier<? extends ItemLike> gives, int givesCount, int maxUses, int xp) {
        ResourceKey<VillagerTrade> key = ResourceKey.create(Registries.VILLAGER_TRADE,
                rl("nuclear_scientist/" + level + "/" + name));
        TRADES.add(new TradeDef(key, level, wants, wantsCount, additionalWants, additionalCount, gives, givesCount,
                maxUses, xp, VILLAGER_PRICE_MULTIPLIER));
    }

    private static Supplier<Item> nc(String name) {
        return () -> BuiltInRegistries.ITEM.getValueOrThrow(ResourceKey.create(Registries.ITEM, rl(name)));
    }

    /** Merchant predicate that offers the trade only while {@code villages.wandering_trader_trades} is enabled. */
    public record WanderingTraderTradesEnabled() implements LootItemCondition {
        public static final WanderingTraderTradesEnabled INSTANCE = new WanderingTraderTradesEnabled();
        public static final MapCodec<WanderingTraderTradesEnabled> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<? extends LootItemCondition> codec() {
            return CODEC;
        }

        @Override
        public boolean test(LootContext context) {
            return Common.WANDERING_TRADER_TRADES.get();
        }
    }

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<WanderingTraderTradesEnabled>>
            WANDERING_TRADER_TRADES_ENABLED = LOOT_CONDITION_TYPES.register("wandering_trader_trades_enabled",
            () -> WanderingTraderTradesEnabled.CODEC);

    public static void init() {
    }
}
