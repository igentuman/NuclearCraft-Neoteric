package igentuman.nc.client.render.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the animated fusion core: a spinning scaled copy of the controller block model plus a
 * floating scaled core item at the cage centre. Speed scales with the run state. Proxies render
 * invisible, so this BER draws the whole 3x3x3 visual from the controller.
 */
public class FusionCoreRenderer implements BlockEntityRenderer<FusionReactorControllerBE> {

    public FusionCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FusionReactorControllerBE be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockState state = be.getBlockState();
        ItemStack core = new ItemStack(state.getBlock().asItem());

        float step = -0.08f;
        if (be.isRunning() && be.getEfficiency() > 0.5) {
            step = -0.15f;
        }
        float angle = Util.getMillis() * step;
        if (!be.isRunning() || be.getEfficiency() < 0.1) {
            angle = 45f;
        }
        angle %= 360;

        BakedModel center = blockRenderer.getBlockModel(state);
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));
        poseStack.scale(1.4f, 1.25f, 1.4f);
        poseStack.translate(-0.5, 0.135f, -0.5);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                state, center, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay);
        poseStack.popPose();

        BakedModel itemModel = itemRenderer.getModel(core, be.getLevel(), null, 0);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.35, 0.5);
        poseStack.scale(3.8f, 3.8f, 3.8f);
        itemRenderer.render(core, ItemDisplayContext.FIXED, false, poseStack, buffer,
                LightTexture.FULL_SKY, combinedOverlay, itemModel);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorControllerBE be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
