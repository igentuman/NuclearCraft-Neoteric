package igentuman.nc.handler.event.server;

import igentuman.nc.item.HEVItem;
import igentuman.nc.item.HazmatItem;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.HEV_BOOTS;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {


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
