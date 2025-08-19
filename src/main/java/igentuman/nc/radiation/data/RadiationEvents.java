package igentuman.nc.radiation.data;

import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.radiation.FluidRadiation;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.RadiationCleaningItems;
import igentuman.nc.util.ModUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.setup.Registration.RADIATION_DECAY;
import static igentuman.nc.setup.Registration.RADIATION_RESISTANCE;

public class RadiationEvents {

    public static boolean isTracking = false;
    private static HashMap<Level, List<ItemEntity>> droppedRadioactiveItems = new HashMap<>();

    public static void attachWorldRadiation(final AttachCapabilitiesEvent<Level> event) {
        if (!event.getObject().getCapability(WorldRadiationProvider.WORLD_RADIATION).isPresent()) {
            event.addCapability(rl("radiation"), new WorldRadiationProvider());
            isTracking = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemUse(LivingEntityUseItemEvent.Finish event)
    {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();
        if(stack.isEmpty()) {
            return;
        }
        long radiation = RadiationCleaningItems.byItem(stack.getItem());

        PlayerRadiation radCap = entity.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
        if(radCap != null) {
            if(stack.getItem().toString().equals("radaway")) {
                if(entity.hasEffect(RADIATION_RESISTANCE.get())) {
                    entity.removeEffect(RADIATION_RESISTANCE.get());
                }
                entity.addEffect(new MobEffectInstance(RADIATION_RESISTANCE.get(), 1200, 1, false, true));
            } else if(stack.getItem().toString().contains("rad_x")) {
                if(entity.hasEffect(RADIATION_RESISTANCE.get())) {
                    entity.removeEffect(RADIATION_RESISTANCE.get());
                }
                entity.addEffect(new MobEffectInstance(RADIATION_RESISTANCE.get(), 1200, 2, false, true));
            } else if(stack.getItem().toString().contains("radaway_slow")) {
                if(entity.hasEffect(RADIATION_DECAY.get())) {
                    entity.removeEffect(RADIATION_DECAY.get());
                }
                entity.addEffect(new MobEffectInstance(RADIATION_DECAY.get(), 2400, 1, false, true));
            }
            if(radiation == 0) return;
            radCap.setRadiation(Math.max(0, radCap.getRadiation() - radiation/1000));
            if(ModUtil.isMekanismLoaded() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
                MekanismRadiation.addEntityRadiation((Player) entity, -radiation/10000000);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            if(stack.isEmpty()) {
                return;
            }
            double radiation = ItemRadiation.byItem(stack.getItem());
            if(radiation > 0.001) {
                RadiationManager.get(event.getLevel()).addRadiation(event.getLevel(), stack.getCount()*radiation/5, entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ());
                List<ItemEntity> items = droppedRadioactiveItems.computeIfAbsent(event.getLevel(), k -> new LinkedList<>());
                items.add((ItemEntity) entity);
                droppedRadioactiveItems.put(event.getLevel(), items);
            }
        }
    }


    public static void attachPlayerRadiation(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).isPresent()) {
                event.addCapability(rl("radiation"), new PlayerRadiationProvider());
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
                 RadiationManager.get((Level) event.getLevel()).addRadiation((Level) event.getLevel(), radiation/5, event.getLiquidPos());
            }
        }
    }

    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if(!isTracking || event.level.isClientSide) {
            return;
        }
        Level world = event.level;
        if(!droppedRadioactiveItems.containsKey(world)) {
            droppedRadioactiveItems.put(world, new LinkedList<>());
        }
        RadiationManager manager = RadiationManager.get(event.level);
        int size = droppedRadioactiveItems.get(world).size();
        for(int i = 0; i < size; i++) {
            ItemEntity entity = droppedRadioactiveItems.get(world).get(i);
            if(entity.isAlive()) {
                double radiation = ItemRadiation.byItem(entity.getItem().getItem());
                if(radiation > 0.001) {
                    RadiationManager.get(world).addRadiation(world, radiation/5, entity.blockPosition());
                }
            } else {
                droppedRadioactiveItems.get(world).remove(i);
                i--;
                size--;
            }
        }
        manager.tick(event.level);
    }

    public static void stopTracking() {
        isTracking = false;
    }

    public static void startTracking() {
        isTracking = true;
    }
}
