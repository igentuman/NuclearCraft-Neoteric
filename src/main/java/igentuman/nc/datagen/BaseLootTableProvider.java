package igentuman.nc.datagen;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.loot.LootParameterSets.ALL_PARAMS;

public abstract class BaseLootTableProvider extends LootTableProvider {
    protected final Map<ResourceLocation, LootTable> tables = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ILootCondition.IBuilder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
            .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));
    protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();
    private final DataGenerator generator;

    public BaseLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
    }

    protected abstract void addTables();

    protected LootTable.Builder createSimpleTable(String name, Block block) {
        LootPool.Builder builder = LootPool.lootPool()
                .name(name)
                .setRolls(ConstantRange.exactly(1))
                .add(ItemLootEntry.lootTableItem(block));
        return LootTable.lootTable().withPool(builder);
    }

    public void run(@Nonnull DirectoryCache outCache)
    {
        tables.clear();
        Path outFolder = generator.getOutputFolder();

        addTables();
        tables.forEach((name, table) -> {
            Path out = getPath(outFolder, name);

            try
            {
                IDataProvider.save(GSON, outCache, LootTableManager.serialize(table), out);
            } catch(IOException x)
            {
                LOGGER.error("Couldn't save loot table {}", out, x);
            }

        });
    }

    private static Path getPath(Path pathIn, ResourceLocation id)
    {
        return pathIn.resolve("data/"+id.getNamespace()+"/loot_tables/"+id.getPath()+".json");
    }


    @Nonnull
    protected static LootTable.Builder createSilkTouchDispatchTable(@Nonnull Block block, @Nonnull LootEntry.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }


    @Nonnull
    protected static LootTable.Builder createSelfDropDispatchTable(@Nonnull Block block, @Nonnull ILootCondition.IBuilder conditionBuilder,
                                                                   @Nonnull LootEntry.Builder<?> entry) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .name("main")
                .setRolls(ConstantRange.exactly(1))
                .add(ItemLootEntry.lootTableItem(block)
                        .when(conditionBuilder)
                        .otherwise(entry)
                )
        );
    }

    @Override
    public String getName() {
        return "NuclearCraft LootTables";
    }
}