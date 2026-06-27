package igentuman.nc.handler.event.server;

import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.multiblock.MultiblockExecutorManager;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.radiation.data.RadiationEvents;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.world.anomaly.AnomalySpawnManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.content.materials.Materials.plutonium239;
import static igentuman.nc.handler.config.WorldConfig.VILLAGE_CONFIG;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.Villager.addVillagerTrades;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {

    public final static LinkedList<Block> trackingBlocks = new LinkedList<>();
    
    public WorldEvents() {

    }

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        addVillagerTrades(event);
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        if(!VILLAGE_CONFIG.addWandererTrades.get()) return;
        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        genericTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 8),
                new ItemStack(NC_ISOTOPES.get(plutonium239).get(), 1),
                8, 2, 0.2f));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getPlayer().level().isClientSide()) return;
        BlockState state = event.getState();
        if(state == null) return;
        if(trackingBlocks.contains(state.getBlock())) {
            MultiblockHandler.get(event.getPlayer().level().dimension()).trackBlockChange(event.getPos());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getLevel() == null || event.getLevel().isClientSide()) return;
        boolean placed = true;
        BlockState state = event.getState();
        if(state == null) return;
        if(trackingBlocks.contains(state.getBlock())) {
            MultiblockHandler.get(((ServerLevel) event.getLevel()).dimension()).trackBlockChange(event.getPos());
        }
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void worldLoadEvent(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            // Ensure the executor is initialized when a world is loaded
            MultiblockExecutorManager.getExecutor();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.START) {
            currentTick++;
            igentuman.nc.item.MultitoolItem.tickTasks();
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(LevelTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.START) {
            if(currentTick % 5 != 0 || event.level.getChunkSource().getLoadedChunksCount() < 1) return;
            final ServerLevel level = (ServerLevel) event.level;
            RadiationEvents.tickAsync(event);
            MultiblockHandler.trackChangesAsync(level);
            AnomalySpawnManager.tick(level);
        }
    }

    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent event) {
        MultiblockHandler.clearAll();
        RadiationManager.clearAll();
        // Shutdown the executor service gracefully when the server is stopping
        MultiblockExecutorManager.shutdown();
    }
}