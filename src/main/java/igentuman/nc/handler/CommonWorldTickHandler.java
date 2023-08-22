package igentuman.nc.handler;


import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.radiation.FluidRadiation;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.RadiationCleaningItems;
import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.util.ModUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;

public class CommonWorldTickHandler {

    private Map<ResourceLocation, Object2IntMap<ChunkPos>> chunkVersions;
    private Map<ResourceLocation, Queue<ChunkPos>> chunkRegenMap;
    public static boolean flushTagAndRecipeCaches;
    public static boolean monitoringCardboardBox;

    public void addRegenChunk(ResourceKey<Level> dimension, ChunkPos chunkCoord) {
        if (chunkRegenMap == null) {
            chunkRegenMap = new Object2ObjectArrayMap<>();
        }
        ResourceLocation dimensionName = dimension.location();
        if (!chunkRegenMap.containsKey(dimensionName)) {
            LinkedList<ChunkPos> list = new LinkedList<>();
            list.add(chunkCoord);
            chunkRegenMap.put(dimensionName, list);
        } else {
            Queue<ChunkPos> regenPositions = chunkRegenMap.get(dimensionName);
            if (!regenPositions.contains(chunkCoord)) {
                regenPositions.add(chunkCoord);
            }
        }
    }

    public void resetChunkData() {
        chunkRegenMap = null;
        chunkVersions = null;
    }

    private List<ItemEntity> droppedRadioactiveItems = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            if(stack.isEmpty()) {
                return;
            }
            double radiation = ItemRadiation.byItem(stack.getItem());
           if(radiation > 0.001) {
               RadiationManager.get(event.getLevel()).addRadiation(event.getLevel(), stack.getCount()*radiation/5, entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ());
                droppedRadioactiveItems.add((ItemEntity) entity);
           }
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemUse(LivingEntityUseItemEvent.Finish event)
    {
        Entity entity = event.getEntity();
        ItemStack stack = event.getItem();
        if(stack.isEmpty()) {
            return;
        }
        int radiation = RadiationCleaningItems.byItem(stack.getItem());
        if(radiation == 0) return;
        PlayerRadiation radCap = entity.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
        if(radCap != null) {
            radCap.setRadiation(radCap.getRadiation() - radiation/1000);
            if(ModUtil.isMekanismLoadeed() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
                MekanismRadiation.addEntityRadiation((Player) entity, -radiation/10000000);
            }
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

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (state != null && !state.isAir() && state.hasBlockEntity()) {

        }
    }

    @SubscribeEvent
    public void chunkUnloadEvent(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && chunkVersions != null) {
            chunkVersions.getOrDefault(level.dimension().location(), Object2IntMaps.emptyMap())
                  .removeInt(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void worldUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof Level level && chunkVersions != null) {
            chunkVersions.remove(level.dimension().location());
        }
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
            tickEnd((ServerLevel) event.level);
        }
    }

    private void tickEnd(ServerLevel world) {
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
    }
}