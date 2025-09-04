package igentuman.nc.handler.event.server;

import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.item.HEVItem;
import igentuman.nc.item.HazmatItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.HEV_BOOTS;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPickupItem(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedUpItem = event.getItem().getItem();
        
        // Skip if the picked up item is empty
        if (pickedUpItem.isEmpty()) {
            return;
        }
        
        // Iterate through player inventory to find ContainerBlockItem
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack inventoryStack = player.getInventory().getItem(i);
            
            if (inventoryStack.getItem() instanceof ContainerBlockItem containerItem) {
                // Check if magnet mode is enabled
                if (containerItem.isMagnetModeEnabled(inventoryStack)) {
                    // Get the container's inventory
                    IItemHandler inventory = containerItem.getInventory(inventoryStack);
                    
                    if (inventory != null) {
                        // Create a copy of the picked up item to simulate insertion
                        ItemStack remainingStack = pickedUpItem.copy();
                        
                        // Try to insert the item into each slot of the container
                        for (int slot = 0; slot < inventory.getSlots() && !remainingStack.isEmpty(); slot++) {
                            // Simulate insertion first to check if it's possible
                            ItemStack simulatedRemaining = inventory.insertItem(slot, remainingStack, true);
                            
                            // If we can insert at least part of the stack
                            if (simulatedRemaining.getCount() < remainingStack.getCount()) {
                                // Actually insert the item
                                remainingStack = inventory.insertItem(slot, remainingStack, false);
                                
                                // If we inserted all items, update the picked up item and break
                                if (remainingStack.isEmpty()) {
                                    pickedUpItem.setCount(0);
                                    //event.getOriginalEntity().setItem(pickedUpItem);
                                    event.setResult(Event.Result.DENY);
                                    break;
                                } else {
                                    // Update the picked up item with the remaining count
                                    pickedUpItem.setCount(remainingStack.getCount());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource() != null && event.getSource().is(DamageTypes.MAGIC)) {
                if(isFullyEquipped(player)) {
                    event.setAmount(event.getAmount()/10F);
                }
            }
            if(event.getSource() != null && (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.STALAGMITE) || event.getSource().is(DamageTypes.HOT_FLOOR))) {
                player.getArmorSlots().forEach(stack -> {
                    if(stack.getItem().equals(HEV_BOOTS.get()) && isCharged(stack)) {
                        consumeEnergy(stack, 1000 * (event.getSource().is(DamageTypes.STALAGMITE) || event.getSource().is(DamageTypes.HOT_FLOOR) ? 2 : 1));
                        event.setCanceled(true);
                        return;
                    }
                });
            }
            int protectionRate = getHEVProtectionRate(player);
            if(protectionRate > 0) {
                event.setAmount(event.getAmount() - (event.getAmount() * (protectionRate * 0.1F)));
                for(ItemStack stack : player.getArmorSlots()) {
                    consumeEnergy(stack, 1000);
                }
            }
        }
    }


    public static int getHEVProtectionRate(Player player) {
        int rate = 0;
        for(ItemStack stack : player.getArmorSlots()) {
            if((stack.getItem() instanceof HEVItem) && isCharged(stack)) {
                rate++;
            }
        }
        return rate;
    }

    public static boolean isFullyEquipped(Player player) {
        for(ItemStack stack : player.getArmorSlots()) {
            if(!(stack.getItem() instanceof HazmatItem) && !(stack.getItem() instanceof HEVItem)) {
                return false;
            }
        }
        return true;
    }



    public static boolean isCharged(ItemStack item)
    {
        return item.getCapability(ForgeCapabilities.ENERGY).map(handler -> handler.getEnergyStored() > 0).orElse(false);
    }


    private static void consumeEnergy(ItemStack stack, int i) {
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> handler.extractEnergy(i, false));
    }
}
