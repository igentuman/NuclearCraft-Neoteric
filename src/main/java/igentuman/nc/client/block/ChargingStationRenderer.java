package igentuman.nc.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.ChargingStationBE;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class ChargingStationRenderer implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<ChargingStationBE> {

    public ChargingStationRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ChargingStationBE be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = be.items.getStackInSlot(0);
        if (stack.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, be.getLevel(), null, 0);

        long time = Util.getMillis();
        float bob = (float) Math.sin(time / 600.0D) * 0.06F;
        float angle = (time % 3600L) / 10F;

        pose.pushPose();
        pose.translate(0.5D, 0.5D + bob, 0.5D);
        pose.mulPose(new Quaternionf().rotateY((float) Math.toRadians(angle)));
        pose.scale(0.7F, 0.7F, 0.7F);

        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, pose, buffer,
                light, OverlayTexture.NO_OVERLAY, model);

        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ChargingStationBE be) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
