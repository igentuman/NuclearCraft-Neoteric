package igentuman.nc.phosphophyllite.multiblock2;

import igentuman.nc.phosphophyllite.util.Util;
import igentuman.nc.util.annotation.OnModLoad;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MultiblockRegistry {
    
    private static final Object2ObjectOpenHashMap<ServerLevel, ObjectArrayList<MultiblockController<?, ?, ?>>> controllersToTick = new Object2ObjectOpenHashMap<>();
    private static final ObjectArrayList<MultiblockController<?, ?, ?>> newControllers = new ObjectArrayList<>();
    private static final ObjectArrayList<MultiblockController<?, ?, ?>> oldControllers = new ObjectArrayList<>();
    
    public static void addController(MultiblockController<?, ?, ?> controller) {
        newControllers.add(controller);
    }
    
    public static void removeController(MultiblockController<?, ?, ?> controller) {
        oldControllers.add(controller);
    }
    
    @OnModLoad
    private static void onModLoad() {
        MinecraftForge.EVENT_BUS.register(MultiblockRegistry.class);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onWorldUnload(final LevelEvent.Unload worldUnloadEvent) {
        if (!worldUnloadEvent.getLevel().isClientSide()) {
            //noinspection SuspiciousMethodCalls
            ObjectArrayList<MultiblockController<?, ?, ?>> controllersToTick = MultiblockRegistry.controllersToTick.remove(worldUnloadEvent.getLevel());
            if (controllersToTick != null) {
                for (MultiblockController<?, ?, ?> multiblockController : controllersToTick) {
                    multiblockController.suicide();
                }
            }
            // stragglers will exist
            newControllers.removeIf(multiblockController -> multiblockController.level == worldUnloadEvent.getLevel());
            oldControllers.removeIf(multiblockController -> multiblockController.level == worldUnloadEvent.getLevel());
        }
    }
    
    @SubscribeEvent
    static void onServerStop(final ServerStoppedEvent serverStoppedEvent) {
        controllersToTick.clear();
        newControllers.clear();
        oldControllers.clear();
    }
    
    @SubscribeEvent
    static void tickServer(TickEvent.ServerTickEvent e) {
        for (MultiblockController<?, ?, ?> newController : newControllers) {
            controllersToTick.computeIfAbsent((ServerLevel) newController.level, k -> new ObjectArrayList<>()).add(newController);
        }
        newControllers.clear();
        for (MultiblockController<?, ?, ?> oldController : oldControllers) {
            //noinspection SuspiciousMethodCalls
            var controllers = controllersToTick.get(oldController.level);
            controllers.remove(oldController);
        }
        oldControllers.clear();
    }
    
    @SubscribeEvent
    static void tickWorld(TickEvent.LevelTickEvent e) {
        if (!(e.level instanceof ServerLevel)) {
            return;
        }
        Util.updateBlockStates(e.level);
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        
        var controllersToTick = MultiblockRegistry.controllersToTick.get(e.level);
        if (controllersToTick != null) {
            for (var controller : controllersToTick) {
                if (controller != null) {
                    controller.update();
                }
            }
        }
    }
}
