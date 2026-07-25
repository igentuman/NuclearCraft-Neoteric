package igentuman.nc.handler.event.client;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.Q36Item;
import igentuman.nc.network.PacketQ36Fire;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class Q36ClientInput {

    private static boolean holdsQ36() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return false;
        return p.getMainHandItem().getItem() instanceof Q36Item;
    }

    private static boolean clientReady(Player p) {
        ItemStack stack = p.getMainHandItem();
        return Q36Item.isReady(stack, p.level());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post ev) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        if (!mc.isWindowActive()) return;
        if (!holdsQ36()) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (!clientReady(mc.player)) return;
        PacketDistributor.sendToServer(new PacketQ36Fire());
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
        if (!(event.getEntity().getMainHandItem().getItem() instanceof Q36Item)) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player p = event.getEntity();
        if (!p.level().isClientSide) return;
        if (!(p.getMainHandItem().getItem() instanceof Q36Item)) return;
        event.setCanceled(true);
    }
}
