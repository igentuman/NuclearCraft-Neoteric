package igentuman.nc.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class CameraUtil {
    public static Vec3 worldToScreenPos(BlockPos pos, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Level level = mc.level;

        if (level == null) return null;

        // Check if block is occluded
        Vec3 cameraPos = camera.getPosition();
        Vec3 blockCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 direction = blockCenter.subtract(cameraPos);

        // Ray trace from camera to block
        HitResult hitResult = level.clip(
                new ClipContext(
                        cameraPos,
                        blockCenter,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        mc.player
                )
        );

        // If hit block is not our target, then target is occluded
        if (hitResult instanceof BlockHitResult blockHit &&
                !blockHit.getBlockPos().equals(pos)) {
           // return null;
        }

        // Convert block position to Vec3 and adjust for camera position
        Vec3 viewPos = blockCenter.subtract(cameraPos);

        float fov = (float) mc.options.fov().get();
        Matrix4f projMatrix = mc.gameRenderer.getProjectionMatrix(fov);
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();

        Vector4f screenPos = new Vector4f((float) viewPos.x, (float) viewPos.y, (float) viewPos.z, 1.0F);
        screenPos.mul(modelViewMatrix);
        screenPos.mul(projMatrix);

        if (screenPos.w != 0.0F) {
            float invW = 1.0F / screenPos.w;
            screenPos.set(screenPos.x * invW, screenPos.y * invW, screenPos.z * invW, 1.0F);

            double screenX = (screenPos.x + 1.0) * 0.5 * mc.getWindow().getGuiScaledWidth();
            double screenY = (1.0 - screenPos.y) * 0.5 * mc.getWindow().getGuiScaledHeight();

            return new Vec3(screenX, screenY, screenPos.z);
        }

        return null;
    }
}