package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.entity.EntityWastelandProjectile;
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

/**
 * Renderer for the Wasteland Projectile entity
 * Displays a rotating block model of wasteland_earth
 */
public class WastelandProjectileRenderer extends EntityRenderer<EntityWastelandProjectile> {
    private final BlockRenderDispatcher blockRenderer;

    public WastelandProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(EntityWastelandProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState blockstate = WASTELAND_EARTH.get().defaultBlockState();

        // Save current matrix state
        poseStack.pushPose();

        // Scale down the block to appropriate size for a projectile
        float scale = 0.5F;
        poseStack.scale(scale, scale, scale);

        float rotationSpeed = 6.0F;
        float rotationVertical = (entity.tickCount + partialTick) * rotationSpeed % 360;
        float rotationHorizontal = (entity.tickCount + partialTick) * (rotationSpeed*2) % 360;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationVertical));
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotationHorizontal));

        // Center the block model
        poseStack.translate(-0.5D, -0.25D, -0.5D);

        // Render the block
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockstate,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        // Restore matrix state
        poseStack.popPose();

        // Call the superclass render method
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWastelandProjectile entity) {
        // Return block atlas texture, though this is not directly used for block rendering
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
