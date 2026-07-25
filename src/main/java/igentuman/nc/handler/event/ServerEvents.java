package igentuman.nc.handler.event;

import igentuman.nc.config.Common;
import igentuman.nc.handler.command.DetonateCommand;
import igentuman.nc.handler.storage.ContainerSyncDispatcher;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.item.MultitoolItem;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.world.anomaly.AnomalySpawnManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;

/** Server event subscriber: advances the global tick counter, runs multitool BombTask ticks,
 *  registers /nc_detonate, and routes magnet-mode item pickups. */
public class ServerEvents {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        TICK_COUNTER++;
        MultitoolItem.tickTasks();
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            AnomalySpawnManager.tick(serverLevel);
            tickCrystalBuffs(serverLevel);
        }
    }

    private static void tickCrystalBuffs(ServerLevel level) {
        int refresh = Common.ANOMALY_CONFIG.BUFF_REFRESH_TICKS.get();
        if (refresh <= 0) return;
        int duration = Common.ANOMALY_CONFIG.BUFF_DURATION_TICKS.get();
        for (ServerPlayer player : level.players()) {
            if ((level.getGameTime() + player.getId()) % refresh != 0) continue;
            for (ItemStack stack : player.getInventory().items) {
                if (!(stack.getItem() instanceof ResoniteCrystalItem)) continue;
                if (!ResoniteCrystalItem.isAnalyzed(stack)) continue;
                String effectIdStr = ResoniteCrystalItem.getEffectId(stack);
                if (effectIdStr.isEmpty()) continue;
                ResourceLocation rl = ResourceLocation.tryParse(effectIdStr);
                if (rl == null) continue;
                var key = ResourceKey.create(Registries.MOB_EFFECT, rl);
                BuiltInRegistries.MOB_EFFECT.getHolder(key).ifPresent(holder -> {
                    int amp = ResoniteCrystalItem.getShardRarity(stack).amplifier;
                    player.addEffect(new MobEffectInstance(holder, duration, amp, false, false));
                });
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        DetonateCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ContainerSyncDispatcher.unsubscribeAll(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;
        ItemStack picked = event.getItemEntity().getItem();
        if (picked.isEmpty() || picked.getItem() instanceof ContainerBlockItem) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack containerStack = player.getInventory().getItem(i);
            if (!(containerStack.getItem() instanceof ContainerBlockItem containerItem)) continue;
            if (!containerItem.isMagnetEnabled(containerStack)) continue;

            containerItem.assignUuid(containerStack);
            IItemHandler inventory = containerStack.getCapability(Capabilities.ItemHandler.ITEM);
            if (inventory == null) continue;

            for (int slot = 0; slot < inventory.getSlots() && !picked.isEmpty(); slot++) {
                ItemStack remainder = inventory.insertItem(slot, picked.copy(), false);
                picked.shrink(picked.getCount() - remainder.getCount());
            }
            if (picked.isEmpty()) return;
        }
    }
}
