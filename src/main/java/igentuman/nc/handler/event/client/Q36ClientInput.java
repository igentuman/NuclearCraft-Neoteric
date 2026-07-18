package igentuman.nc.handler.event.client;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.Q36Item;
import igentuman.nc.network.toServer.PacketQ36Fire;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Q36ClientInput {

    private static boolean holdsQ36() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return false;
        ItemStack stack = p.getMainHandItem();
        return stack.getItem() instanceof Q36Item;
    }

    private static boolean clientReady(Player p) {
        ItemStack stack = p.getMainHandItem();
        long end = stack.getOrCreateTag().getLong(Q36Item.TAG_COOLDOWN_END);
        return p.level.getGameTime() >= end;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        if (!mc.isWindowActive()) return;
        if (!holdsQ36()) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (!clientReady(mc.player)) return;
        NuclearCraft.packetHandler().sendToServer(new PacketQ36Fire());
        Q36HandRenderer.triggerRecoil(Q36Item.getMode(mc.player.getMainHandItem()));
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        if (!holdsQ36()) return;
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player p = event.getEntity();
        if (!(p.getMainHandItem().getItem() instanceof Q36Item)) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player p = event.getEntity();
        if (!p.level.isClientSide) return;
        if (!(p.getMainHandItem().getItem() instanceof Q36Item)) return;
        event.setCanceled(true);
    }
}
