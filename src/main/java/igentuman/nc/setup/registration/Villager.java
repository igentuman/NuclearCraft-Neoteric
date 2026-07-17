package igentuman.nc.setup.registration;

import com.google.common.collect.ImmutableSet;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.content.materials.Materials.neptunium236;
import static igentuman.nc.content.materials.Materials.plutonium238;
import static igentuman.nc.content.processors.Processors.ANALYZER;
import static igentuman.nc.setup.registration.FissionFuel.NC_FUEL;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.NC_FOOD;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
import static igentuman.nc.setup.registration.Tags.*;
import static igentuman.nc.setup.registration.Tags.forgeIngot;
import static igentuman.nc.util.NcUtils.getItemStackByModPriority;
import static net.minecraft.world.item.Items.BOOK;
import static net.minecraft.world.item.Items.EMERALD;

public class Villager {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MODID);

    public static final RegistryObject<PoiType> ANALYZER_POI = POI_TYPES.register("analyzer_poi",
            () -> new PoiType(ImmutableSet.copyOf(PROCESSORS.get(ANALYZER).get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final RegistryObject<VillagerProfession> NUCLEAR_SCIENTIST =
            VILLAGER_PROFESSIONS.register("nuclear_scientist", () -> new VillagerProfession("nuclear_scientist",
                    holder -> holder.get() == ANALYZER_POI.get(), holder -> holder.get() == ANALYZER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void init(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        POI_TYPES.register(bus);
        VILLAGER_PROFESSIONS.register(bus);
    }

    public static void addVillagerTrades(VillagerTradesEvent event) {
        if(event.getType() == Villager.NUCLEAR_SCIENTIST.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 1),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeDust("graphite"), 2)),
                    32, 1, 0.02f));

            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 1),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeDust("quartz"), 2)),
                    32, 1, 0.02f));

            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("lead"), 8)),
                    new ItemStack(EMERALD, 1),
                    24, 1, 0.02f));

            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 6),
                    new ItemStack(NC_PARTS.get("plate_basic").get(), 1),
                    16, 5, 0.02f));

            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 9),
                    new ItemStack(NC_FOOD.get("smore").get(), 1),
                    16, 5, 0.02f));

            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 8),
                    new ItemStack(NC_FOOD.get("radaway").get(), 1),
                    6, 5, 0.02f));

            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(NC_ISOTOPES.get(neptunium236).get(), 1),
                    new ItemStack(EMERALD, 4),
                    7, 15, 0.02f));

            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 32),
                    new ItemStack(NC_ISOTOPES.get(plutonium238).get(), 2),
                    7, 15, 0.02f));

            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 20),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeDust("calcium_sulfate"), 2)),
                    7, 15, 0.02f));

            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgePlate("platinum"), 16)),
                    new ItemStack(NC_PARTS.get("plate_elite").get(), 1),
                    7, 20, 0.02f));

            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 32),
                    new ItemStack(BOOK, 4),
                    new ItemStack(NC_FUEL.get(List.of("fuel", "californium", "hecf-251", "")).get(), 2),
                    7, 20, 0.02f));

            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("platinum"), 16)),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("cobalt"), 4)),
                    new ItemStack(NC_FUEL.get(List.of("fuel", "americium", "hea-242", "")).get(), 2),
                    7, 20, 0.02f));

            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 16),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("cobalt"), 4)),
                    new ItemStack(NC_PARTS.get("coil_magnesium_diboride").get(), 2),
                    7, 30, 0.02f));

            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(EMERALD, 16),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("zinc"), 8)),
                    getItemStackByModPriority(IngredientCreatorAccess.item().from(forgeIngot("neutronium"), 2)),
                    7, 30, 0.02f));
        }
    }
}