package igentuman.nc.radiation.data;

import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.radiation.FluidRadiation;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.RadiationCleaningItems;
import igentuman.nc.util.ModUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

public class RadiationEvents {

    public static boolean isTracking = false;
    private static List<ItemEntity> droppedRadioactiveItems = new LinkedList<>();

    public static void attachWorldRadiation(final AttachCapabilitiesEvent<World> event) {
        if (!event.getObject().getCapability(WorldRadiationProvider.WORLD_RADIATION).isPresent()) {
            event.addCapability(new ResourceLocation(MODID, "radiation"), new WorldRadiationProvider());
            isTracking = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemUse(LivingEntityUseItemEvent.Finish event)
    {
        LivingEntity entity = event.getEntityLiving();
        ItemStack stack = event.getItem();
        if(stack.isEmpty()) {
            return;
        }
        int radiation = RadiationCleaningItems.byItem(stack.getItem());
        if(radiation == 0) return;
        PlayerRadiation radCap = entity.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
       /* if(radCap != null) {
            if(stack.getItem().toString().contains("radaway")) {
                if(entity.hasEffect(RADIATION_RESISTANCE.get())) {
                    entity.removeEffect(RADIATION_RESISTANCE.get());
                }
                entity.addEffect(new EffectInstance(RADIATION_RESISTANCE.get(), 1200, 1, false, true));
            } else if(stack.getItem().toString().contains("rad_x")) {
                if(entity.hasEffect(RADIATION_RESISTANCE.get())) {
                    entity.removeEffect(RADIATION_RESISTANCE.get());
                }
                entity.addEffect(new EffectInstance(RADIATION_RESISTANCE.get(), 1200, 2, false, true));
            }
            radCap.setRadiation(radCap.getRadiation() - radiation/1000);
            if(ModUtil.isMekanismLoadeed() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
                MekanismRadiation.addEntityRadiation((PlayerEntity) entity, -radiation/10000000);
            }
        }*/
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            if(stack.isEmpty()) {
                return;
            }
            double radiation = ItemRadiation.byItem(stack.getItem());
            if(radiation > 0.001) {
                RadiationManager.get(event.getWorld()).addRadiation(event.getWorld(), stack.getCount()*radiation/5, entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ());
                droppedRadioactiveItems.add((ItemEntity) entity);
            }
        }
    }


    public static void attachPlayerRadiation(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof PlayerEntity) {
            if (!event.getObject().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "radiation"), new PlayerRadiationProvider());
            }
        }
    }

    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // We need to copyFrom the capabilities
            event.getOriginal().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public void onFluidPlaced(BlockEvent.FluidPlaceBlockEvent event) {
        BlockState state = event.getState();
        if (state != null && !state.isAir()) {
            double radiation = FluidRadiation.byFluid(state.getFluidState().getType());
            if(radiation > 0.001) {
                // RadiationManager.get((Level) event.getLevel()).addRadiation((Level) event.getLevel(), radiation/5, event.getLiquidPos().getX(), event.getLiquidPos().getY(), event.getLiquidPos().getZ());
            }
        }
    }

    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if(!isTracking) {
            return;
        }
        // Don't do anything client side
        if (event.world.isClientSide) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        World world = event.world;
        RadiationManager manager = RadiationManager.get(event.world);
        if (!world.isClientSide) {
            int size = droppedRadioactiveItems.size();
            for(int i = 0; i < size; i++) {
                ItemEntity entity = droppedRadioactiveItems.get(i);
                if(entity.isAlive()) {
                    double radiation = ItemRadiation.byItem(entity.getItem().getItem());
                    if(radiation > 0.001) {
                        RadiationManager.get(world).addRadiation(world, radiation/5, entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ());
                    }
                } else {
                    droppedRadioactiveItems.remove(i);
                    i--;
                    size--;
                }
            }
        }
        manager.tick(event.world);
    }

    public static void stopTracking() {
        isTracking = false;
    }

    public static void startTracking() {
        isTracking = true;
    }
}
