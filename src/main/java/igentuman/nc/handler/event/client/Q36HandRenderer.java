package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.NuclearCraft;
import igentuman.nc.item.Q36Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class Q36HandRenderer {

    private static long lastFireMs = 0L;
    private static Q36Item.FireMode lastMode = Q36Item.FireMode.PULSE;

    public static void triggerRecoil(Q36Item.FireMode mode) {
        lastFireMs = System.currentTimeMillis();
        lastMode = mode;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent ev) {
        if (ev.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack stack = ev.getItemStack();
        if (!(stack.getItem() instanceof Q36Item)) return;

        long elapsed = System.currentTimeMillis() - lastFireMs;
        int duration = lastMode == Q36Item.FireMode.BEAM ? 220 : 140;
        if (elapsed < 0 || elapsed >= duration) return;

        float t = elapsed / (float) duration;
        float kick = t < 0.2F ? t / 0.2F : 1.0F - (t - 0.2F) / 0.8F;

        float maxTiltDeg = lastMode == Q36Item.FireMode.BEAM ? 12.0F : 6.0F;
        float maxPushBack = lastMode == Q36Item.FireMode.BEAM ? 0.18F : 0.08F;

        PoseStack pose = ev.getPoseStack();
        pose.translate(0.0F, 0.0F, maxPushBack * kick);
        pose.mulPose(Axis.XP.rotationDegrees(-maxTiltDeg * kick));
    }
}
