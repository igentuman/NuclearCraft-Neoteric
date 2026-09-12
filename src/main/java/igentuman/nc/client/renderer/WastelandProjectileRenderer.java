package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class WastelandProjectileRenderer extends EntityRenderer<EntityBlockProjectile> {

    private final BlockRenderDispatcher blockRenderer;

    public WastelandProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockRenderer = context.getBlockRenderDispatcher();
        shadowRadius = 0.25F;
    }

    @Override
    public void render(EntityBlockProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        BlockState state = ModEntries.get("wasteland_earth").block().get().defaultBlockState();
        float age = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 6.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(age * 12.0F));
        poseStack.translate(-0.5D, -0.25D, -0.5D);
        blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBlockProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
