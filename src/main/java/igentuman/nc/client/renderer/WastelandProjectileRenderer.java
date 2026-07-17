package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.entity.EntityBlockProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;

public class WastelandProjectileRenderer extends EntityRenderer<EntityBlockProjectile> {
    private final BlockRenderDispatcher blockRenderer;

    public WastelandProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(EntityBlockProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState blockstate = WASTELAND_EARTH.get().defaultBlockState();

        poseStack.pushPose();

        float scale = 0.5F;
        poseStack.scale(scale, scale, scale);

        float rotationSpeed = 6.0F;
        float rotationVertical = (entity.tickCount + partialTick) * rotationSpeed % 360;
        float rotationHorizontal = (entity.tickCount + partialTick) * (rotationSpeed*2) % 360;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationVertical));
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotationHorizontal));

        poseStack.translate(-0.5D, -0.25D, -0.5D);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockstate,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBlockProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
