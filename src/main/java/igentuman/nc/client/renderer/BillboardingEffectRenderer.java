package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * Renders a {@link CustomEffect} as a camera-facing (billboarded) animated quad at a world position.
 * Generic: works for any texture/position, not bound to a specific block or entity.
 *
 * <p>Call {@link #submit(CustomEffect)} every frame for each effect you want drawn (e.g. from a
 * {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} or a client tick). The
 * actual draw is deferred to {@link DelayedRenderHandler} so all billboards batch together and
 * depth-sort correctly against translucent geometry.
 */
public final class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    /** Submit an effect for this frame, using its own texture. */
    public static void submit(CustomEffect effect) {
        submit(effect.getTexture(), () -> effect);
    }

    /**
     * Submit an effect for this frame under an explicit texture. The effect is resolved lazily at
     * draw time so callers may share a render type across multiple effects of the same texture.
     */
    public static void submit(ResourceLocation texture, Supplier<CustomEffect> lazyEffect) {
        DelayedRenderHandler.addTransparentRenderer(ModRenderType.BILLBOARD.apply(texture), new DelayedRenderHandler.LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick) {
                BillboardingEffectRenderer.render(camera, buffer, poseStack, renderTick, partialTick, lazyEffect.get());
            }

            @Override
            public Vec3 getCenterPos(float partialTick) {
                return lazyEffect.get().getPos(partialTick);
            }
        });
    }

    private static void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, CustomEffect effect) {
        int gridSize = effect.getTextureGridSize();

        int tick = renderTick % (gridSize * gridSize);
        int yIndex = tick % gridSize, xIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;

        Quaternionf quaternion = camera.rotation();
        Vector3f[] vertexPos = {new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vec3 pos = effect.getPos(partialTick);
        for (Vector3f vector3f : vertexPos) {
            quaternion.transform(vector3f);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }

        int[] color = effect.getColor();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z()).setUv(minU, maxV).setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z()).setUv(maxU, maxV).setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z()).setUv(maxU, minV).setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z()).setUv(minU, minV).setColor(color[0], color[1], color[2], color[3]);
        poseStack.popPose();
    }
}
