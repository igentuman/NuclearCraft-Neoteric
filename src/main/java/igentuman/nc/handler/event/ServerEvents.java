package igentuman.nc.handler.event;

import igentuman.nc.handler.command.DetonateCommand;
import igentuman.nc.handler.storage.ContainerSyncDispatcher;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.item.MultitoolItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
