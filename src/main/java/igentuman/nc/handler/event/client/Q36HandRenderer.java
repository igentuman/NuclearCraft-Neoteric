package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.item.Q36Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Q36HandRenderer {

    private static long lastFireMs = 0L;
    private static Q36Item.FireMode lastMode = Q36Item.FireMode.PULSE;

    public static void triggerRecoil(Q36Item.FireMode mode) {
        lastFireMs = System.currentTimeMillis();
        lastMode = mode;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent ev) {
        ItemStack stack = ev.getItemStack();
        if (!(stack.getItem() instanceof Q36Item)) return;

        long elapsed = System.currentTimeMillis() - lastFireMs;
        int duration = lastMode == Q36Item.FireMode.BEAM ? 220 : 140;
        if (elapsed < 0 || elapsed >= duration) return;

        float t = elapsed / (float) duration;
        // fast rise (0..0.2), slow decay (0.2..1)
        float kick = t < 0.2F ? t / 0.2F : 1.0F - (t - 0.2F) / 0.8F;

        float maxTiltDeg = lastMode == Q36Item.FireMode.BEAM ? 12.0F : 6.0F;
        float maxPushBack = lastMode == Q36Item.FireMode.BEAM ? 0.18F : 0.08F;

        PoseStack pose = ev.getPoseStack();
        pose.translate(0.0F, 0.0F, maxPushBack * kick);
        pose.mulPose(Axis.XP.rotationDegrees(-maxTiltDeg * kick));
    }
}
