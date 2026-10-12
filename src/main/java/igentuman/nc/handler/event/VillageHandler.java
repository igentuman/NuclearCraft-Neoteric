package igentuman.nc.handler.event;

import com.mojang.datafixers.util.Pair;
import igentuman.nc.config.Common;
import igentuman.nc.mixin.StructureTemplatePoolAccessor;
import igentuman.nc.recipe.TagOutputResolver;
import igentuman.nc.setup.NCVillagers;
import igentuman.nr.registry.NRMedicineItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TagUtil.dustTag;
import static igentuman.nc.util.TagUtil.ingotTag;
import static igentuman.nc.util.TagUtil.plateTag;

@EventBusSubscriber(modid = MODID)
public class VillageHandler {

    private static final String[] VILLAGE_TYPES = {"plains", "snowy", "savanna", "desert", "taiga"};
    private static final int SCIENTIST_HOUSE_WEIGHT = 4;
    private static final Set<StructureTemplatePool> MODIFIED_POOLS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final float VILLAGER_PRICE_MULTIPLIER = 0.02f;
    private static final float WANDERER_PRICE_MULTIPLIER = 0.2f;

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD || !Common.SCIENTIST_HOUSE.get()) return;
        Registry<StructureTemplatePool> pools = event.getRegistryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        for (String type : VILLAGE_TYPES) {
            pools.getOptional(ResourceLocation.withDefaultNamespace("village/" + type + "/houses"))
                    .ifPresent(pool -> addToPool(pool, rl("scientist_house/" + type)));
        }
    }

    private static void addToPool(StructureTemplatePool pool, ResourceLocation template) {
        if (!MODIFIED_POOLS.add(pool)) return;
        StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;
        StructurePoolElement element = StructurePoolElement.single(template.toString())
                .apply(StructureTemplatePool.Projection.RIGID);
        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(accessor.nc$getRawTemplates());
        rawTemplates.add(Pair.of(element, SCIENTIST_HOUSE_WEIGHT));
        accessor.nc$setRawTemplates(rawTemplates);
        for (int i = 0; i < SCIENTIST_HOUSE_WEIGHT; i++) {
            accessor.nc$getTemplates().add(element);
        }
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != NCVillagers.NUCLEAR_SCIENTIST.get()) return;
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        trades.get(1).add(trade(emeralds(1), tag(dustTag("graphite"), 2), 32, 1));
        trades.get(1).add(trade(emeralds(1), tag(dustTag("quartz"), 2), 32, 1));
        trades.get(1).add(trade(tag(ingotTag("lead"), 8), emeralds(1), 24, 1));

        trades.get(2).add(trade(emeralds(6), item("plate_basic", 1), 16, 5));
        trades.get(2).add(trade(emeralds(8), item(NRMedicineItems.RADAWAY, 1), 6, 5));

        trades.get(3).add(trade(item("neptunium_236", 1), emeralds(4), 7, 15));
        trades.get(3).add(trade(emeralds(32), item("plutonium_238", 2), 7, 15));
        trades.get(3).add(trade(emeralds(20), tag(dustTag("calcium_sulfate"), 2), 7, 15));

        trades.get(4).add(trade(tag(plateTag("platinum"), 16), item("plate_elite", 1), 7, 20));
        trades.get(4).add(trade(emeralds(32), item(Items.BOOK, 4), item("fuel_californium_hecf_251", 2), 7, 20));
        trades.get(4).add(trade(tag(ingotTag("platinum"), 16), tag(ingotTag("cobalt"), 4), item("fuel_americium_hea_242", 2), 7, 20));

        trades.get(5).add(trade(emeralds(16), tag(ingotTag("cobalt"), 4), item("coil_magnesium_diboride", 2), 7, 30));
        trades.get(5).add(trade(emeralds(16), tag(ingotTag("zinc"), 8), tag(ingotTag("neutronium"), 2), 7, 30));
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        if (!Common.WANDERING_TRADER_TRADES.get()) return;
        event.getGenericTrades().add(trade(emeralds(8), null, item("plutonium_239", 1), 8, 2, WANDERER_PRICE_MULTIPLIER));
    }

    private static VillagerTrades.ItemListing trade(Supplier<ItemStack> cost, Supplier<ItemStack> result, int maxUses, int xp) {
        return trade(cost, null, result, maxUses, xp, VILLAGER_PRICE_MULTIPLIER);
    }

    private static VillagerTrades.ItemListing trade(Supplier<ItemStack> costA, Supplier<ItemStack> costB,
                                                    Supplier<ItemStack> result, int maxUses, int xp, float priceMultiplier) {
        return (trader, random) -> {
            ItemStack a = costA.get();
            ItemStack b = costB == null ? ItemStack.EMPTY : costB.get();
            ItemStack out = result.get();
            if (a.isEmpty() || out.isEmpty() || (costB != null && b.isEmpty())) return null;
            return new MerchantOffer(toCost(a), b.isEmpty() ? Optional.empty() : Optional.of(toCost(b)),
                    out, maxUses, xp, priceMultiplier);
        };
    }

    private static VillagerTrades.ItemListing trade(Supplier<ItemStack> costA, Supplier<ItemStack> costB,
                                                    Supplier<ItemStack> result, int maxUses, int xp) {
        return trade(costA, costB, result, maxUses, xp, VILLAGER_PRICE_MULTIPLIER);
    }

    private static ItemCost toCost(ItemStack stack) {
        return new ItemCost(stack.getItem(), stack.getCount());
    }

    private static Supplier<ItemStack> emeralds(int count) {
        return item(Items.EMERALD, count);
    }

    private static Supplier<ItemStack> item(ItemLike item, int count) {
        return () -> new ItemStack(item, count);
    }

    private static Supplier<ItemStack> item(String name, int count) {
        return () -> new ItemStack(BuiltInRegistries.ITEM.get(rl(name)), count);
    }

    private static Supplier<ItemStack> tag(TagKey<Item> tag, int count) {
        return () -> TagOutputResolver.resolveItem(tag, count);
    }
}
