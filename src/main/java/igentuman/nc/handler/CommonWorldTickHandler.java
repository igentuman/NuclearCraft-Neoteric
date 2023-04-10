package igentuman.nc.handler;

import igentuman.nc.handler.radiation.RadiationManager;
import igentuman.nc.util.NBTConstants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CommonWorldTickHandler {

    private static final long maximumDeltaTimeNanoSecs = 16_000_000; // 16 milliseconds

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        //If we are in the middle of breaking a block using a cardboard box, cancel any items
        // that are dropped, we do this at highest priority to ensure we cancel it the same tick
        // before forge replaces items with custom item entities with a tick delay
        // We also cancel any experience orbs from spawning as things like the furnace will store
        // how much xp they have but also try to drop it on replace
        if (monitoringCardboardBox) {
            Entity entity = event.getEntity();
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                entity.discard();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (state != null && !state.isAir() && state.hasBlockEntity()) {
            //If the block might have a block entity, look it up from the world and see if the player has access to destroy it
           // BlockEntity blockEntity = WorldUtils.getTileEntity(event.getLevel(), event.getPos());
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
            RadiationManager.INSTANCE.createOrLoad();
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            serverTick();
        }
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            tickEnd((ServerLevel) event.level);
        }
    }

    private void serverTick() {
        RadiationManager.INSTANCE.tickServer();
    }

    private void tickEnd(ServerLevel world) {
        if (!world.isClientSide) {
            RadiationManager.INSTANCE.tickServerWorld(world);
        }
    }
}