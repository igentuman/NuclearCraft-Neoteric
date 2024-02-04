package igentuman.nc.handler.event.server;

import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.item.HEVItem;
import igentuman.nc.item.HazmatItem;
import igentuman.nc.radiation.data.RadiationEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.HEV_BOOTS;
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {

    public WorldEvents() {

    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (state != null && !state.isAir() && state.hasBlockEntity()) {

        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        boolean placed = true;
        BlockState state = event.getState();
        if(state == null) return;
        if(state.getBlock() instanceof TurbineBladeBlock) {
            placed = TurbineBladeBlock.processBlockPlace(event.getLevel(), event.getPos(), event.getPlacedBlock(), state, event.getPlacedAgainst());
        }
        if(!placed) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void chunkUnloadEvent(ChunkEvent.Unload event) {

    }

    @SubscribeEvent
    public void worldUnloadEvent(LevelEvent.Unload event) {

    }

    @SubscribeEvent
    public void worldLoadEvent(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {

        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {

        }
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            RadiationEvents.onWorldTick(event);
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

    public static boolean isFullyEquitedInHazmat(Player player) {
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



    @SubscribeEvent
    public static void onPlayerDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource() != null && event.getSource().isMagic()) {
                if(isFullyEquitedInHazmat(player)) {
                    event.setAmount(event.getAmount()/10F);
                }
            }
            if(event.getSource() != null && event.getSource().isFall()) {
                player.getArmorSlots().forEach(stack -> {
                    if(stack.getItem().equals(HEV_BOOTS.get()) && isCharged(stack)) {
                        consumeEnergy(stack, 1000);
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

    private static void consumeEnergy(ItemStack stack, int i) {
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> handler.extractEnergy(i, false));
    }
}