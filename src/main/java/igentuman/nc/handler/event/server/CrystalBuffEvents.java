package igentuman.nc.handler.event.server;

import igentuman.nc.compat.curios.CuriosHelper;
import igentuman.nc.item.ResoniteCrystalItem;
import igentuman.nc.util.ModUtil;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;
import static igentuman.nc.setup.Registration.QUICKDRAW_BOOST;

/**
 * Passive effects from carried analyzed resonite crystals. Each refresh interval the player's main
 * inventory and equipped Curios slots are scanned; for each distinct granted {@link MobEffect} the
 * strongest rarity present wins (no stacking) and the effect is (re)applied just ahead of expiry.
 * Curios scanning is soft compat, guarded by {@link ModList#isLoaded}. Also drives the {@code QuickdrawBoost}
 * marker effect, which has no vanilla attribute, by trimming bow/crossbow draw ticks.
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrystalBuffEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide || !ANOMALY_CONFIG.ENABLED.get()) {
            return;
        }
        int interval = ANOMALY_CONFIG.BUFF_REFRESH_TICKS.get();
        if (player.tickCount % interval != 0) {
            return;
        }

        Map<MobEffect, Integer> strongest = new HashMap<>();
        for (ItemStack stack : player.getInventory().items) {
            accumulate(strongest, stack);
        }
        if (ModUtil.isCuriosLoaded()) {
            CuriosHelper.accumulateCurios(strongest, player);
        }

        int duration = ANOMALY_CONFIG.BUFF_DURATION_TICKS.get();
        for (Map.Entry<MobEffect, Integer> entry : strongest.entrySet()) {
            MobEffect effect = entry.getKey();
            int amplifier = entry.getValue();
            MobEffectInstance current = player.getEffect(effect);
            if (current != null && current.getAmplifier() == amplifier) {
                // Refresh duration in place instead of re-adding. addEffect would force a reapply
                // (new duration > remaining), and attribute effects like Health Boost clamp current
                // health on reapply, dropping bonus hearts and triggering the client hurt flicker.
                current.setDetailsFrom(new MobEffectInstance(effect, duration, amplifier, true, false, true));
                if (player instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), current));
                }
            } else if (current == null || current.getAmplifier() < amplifier) {
                player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
            }
        }
    }

    public static void accumulate(Map<MobEffect, Integer> strongest, ItemStack stack) {
        if (!(stack.getItem() instanceof ResoniteCrystalItem) || !ResoniteCrystalItem.isAnalyzed(stack)) {
            return;
        }
        MobEffect effect = ResoniteCrystalItem.effect(stack);
        if (effect == null) {
            return;
        }
        strongest.merge(effect, ResoniteCrystalItem.rarity(stack).amplifier, Math::max);
    }



    @SubscribeEvent
    public static void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(QUICKDRAW_BOOST.get())) {
            return;
        }
        ItemStack using = event.getItem();
        if (!(using.getItem() instanceof BowItem) && !(using.getItem() instanceof CrossbowItem)) {
            return;
        }
        int amplifier = entity.getEffect(QUICKDRAW_BOOST.get()).getAmplifier();
        event.setDuration(event.getDuration() - (amplifier + 1));
    }
}
