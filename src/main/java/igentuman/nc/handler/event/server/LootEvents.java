package igentuman.nc.handler.event.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.RESONITE_SHARD;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootEvents {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (!name.getNamespace().equals("minecraft") || !name.getPath().startsWith("chests/")) {
            return;
        }

        event.getTable().addPool(LootPool.lootPool()
                .name(MODID + ":resonite_shards")
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(RESONITE_SHARD.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1))))
                .build());
    }
}
