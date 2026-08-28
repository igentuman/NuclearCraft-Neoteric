package igentuman.nc.handler.event;

import igentuman.nc.compat.curios.CuriosHelper;
import igentuman.nc.config.Common;
import igentuman.nc.handler.command.DetonateCommand;
import igentuman.nc.handler.storage.ContainerSyncDispatcher;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.item.MultitoolItem;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.util.ModUtil;
import igentuman.nc.world.anomaly.AnomalySpawnManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
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

import java.util.HashMap;
import java.util.Map;

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
            Map<Holder<MobEffect>, Integer> strongest = new HashMap<>();
            for (ItemStack stack : player.getInventory().items) {
                accumulateCrystal(strongest, stack);
            }
            if (ModUtil.isCuriosLoaded()) {
                CuriosHelper.accumulateCurios(strongest, player);
            }
            for (Map.Entry<Holder<MobEffect>, Integer> entry : strongest.entrySet()) {
                Holder<MobEffect> effect = entry.getKey();
                int amplifier = entry.getValue();
                MobEffectInstance current = player.getEffect(effect);
                if (current == null || current.getAmplifier() < amplifier || current.getDuration() < duration / 2) {
                    player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
                }
            }
        }
    }

    public static void accumulateCrystal(Map<Holder<MobEffect>, Integer> strongest, ItemStack stack) {
        if (!(stack.getItem() instanceof ResoniteCrystalItem) || !ResoniteCrystalItem.isAnalyzed(stack)) return;
        Holder<MobEffect> effect = ResoniteCrystalItem.getEffect(stack);
        if (effect == null) return;
        strongest.merge(effect, ResoniteCrystalItem.getShardRarity(stack).amplifier, Math::max);
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
