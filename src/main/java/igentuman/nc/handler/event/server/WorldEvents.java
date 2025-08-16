package igentuman.nc.handler.event.server;

import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.item.HEVItem;
import igentuman.nc.item.HazmatItem;
import igentuman.nc.multiblock.MultiblockExecutorManager;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.radiation.data.RadiationEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.content.materials.Materials.plutonium239;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.HEV_BOOTS;
import static igentuman.nc.setup.registration.Villager.addVillagerTrades;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {

    public final static List<Block> trackingBlocks = new ArrayList<>();
    
    public WorldEvents() {

    }

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        addVillagerTrades(event);
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {

        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        genericTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 8),
                new ItemStack(NC_ISOTOPES.get(plutonium239).get(), 1),
                8, 2, 0.2f));
    }

    @SubscribeEvent
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
        if(event.getEntity().level().isClientSide()) return;
        boolean placed = true;
        BlockState state = event.getState();
        if(state == null) return;
        if(trackingBlocks.contains(state.getBlock())) {
            MultiblockHandler.get(event.getEntity().level().dimension()).trackBlockChange(event.getPos());
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

    @SubscribeEvent
    public void worldLoadEvent(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            // Ensure the executor is initialized when a world is loaded
            MultiblockExecutorManager.getExecutor();
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {

        }
    }

    private CompletableFuture<Void> validationFuture;
    private CompletableFuture<Void> radiationFuture;

    @SubscribeEvent
    public void onTick(LevelTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.START && event.haveTime()) {
            if(event.isCanceled() || event.level.getGameTime() % 5 != 0) return;
            final ServerLevel level = (ServerLevel) event.level;

            if(event.level.getGameTime() % 10 == 0) {
                if (radiationFuture != null && !radiationFuture.isDone()) {
                    // Previous radiation processing is still running, skip this tick
                } else {
                    radiationFuture = CompletableFuture.runAsync(
                            () -> RadiationEvents.onWorldTick(event),
                            MultiblockExecutorManager.getExecutor()
                    );
                }
            }

            //todo remove, this for testing purposes only
/*
            List<BlockState> blocks = new ArrayList<>();
            for(int x = -2; x < 3; x++) {
                for(int y = -2; y < 3; y++) {
                    //blocks.add(level.getBlockState(new BlockPos(250+x, -58+y, -1022)));
                }
            }
            for(int i = 0; i < 9000; i++) {
                int id = 0;
                for(int x = -2; x < 3; x++) {
                    for(int y = -2; y < 3; y++) {
                       // level.setBlock(new BlockPos(250+x, -58+y, -1023-i), blocks.get(id), Block.UPDATE_ALL);
                        id++;
                    }
                }
            }
*/

            if(level.getChunkSource().getLoadedChunksCount() < 1) {
                return;
            }
            if (validationFuture != null && !validationFuture.isDone()) {
                return;
            }
            validationFuture = CompletableFuture.runAsync(
                    () -> MultiblockHandler.get(level.dimension()).tick(level),
                    MultiblockExecutorManager.getExecutor()
            );
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

    @SubscribeEvent
    public static void onPlayerDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource() != null && event.getSource().is(DamageTypes.MAGIC)) {
                if(isFullyEquipped(player)) {
                    event.setAmount(event.getAmount()/10F);
                }
            }
            if(event.getSource() != null && event.getSource().is(DamageTypes.FALL)) {
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
    
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Shutdown the executor service gracefully when the server is stopping
        MultiblockExecutorManager.shutdown();
    }
}