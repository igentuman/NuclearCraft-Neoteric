package igentuman.nc.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/**
 * Renders a {@link MultiblockStructure} as an isometric 3D preview inside a GUI rectangle.
 * Auto-rotates around Y axis until user drags with LMB to manually rotate yaw/pitch.
 */
public final class StructurePreviewRenderer {

    private static final float ROTATION_SPEED_DEG_PER_SEC = 15f;
    private static final float TILT_DEG = 30f;
    private static final float DRAG_SENSITIVITY = 0.5f;
    private static final long START_NANOS = System.nanoTime();

    private static float userYaw = 0f;
    private static float userPitch = TILT_DEG;
    private static boolean userControlled = false;
    private static boolean dragging = false;
    private static double lastMouseX = 0;
    private static double lastMouseY = 0;
    private static boolean rmbPrev = false;
    private static int sliceLevel = -1;

    private StructurePreviewRenderer() {}

    public static void render(GuiGraphics graphics, MultiblockStructure structure,
                              int x, int y, int width, int height,
                              double mouseX, double mouseY) {
        if (structure == null) return;

        int sw = structure.getWidth();
        int sh = structure.getHeight();
        int sd = structure.getDepth();
        int maxDim = Math.max(sw, Math.max(sh, sd));
        if (maxDim <= 0) return;

        float scale = Math.min(width, height) / (float) (maxDim * 1.8f);

        updateDragRotation(x, y, width, height, mouseX, mouseY);
        updateSlice(x, y, width, height, mouseX, mouseY, sh);

        float yaw, pitch;
        if (userControlled) {
            yaw = userYaw;
            pitch = userPitch;
        } else {
            yaw = ((System.nanoTime() - START_NANOS) / 1_000_000_000f) * ROTATION_SPEED_DEG_PER_SEC;
            pitch = TILT_DEG;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + width / 2f, y + height / 2f, 200f);
        pose.scale(scale, -scale, scale);
        pose.mulPose(new Quaternionf().rotationX((float) Math.toRadians(pitch)));
        pose.mulPose(new Quaternionf().rotationY((float) Math.toRadians(yaw)));
        pose.translate(-sw / 2f, -sh / 2f, -sd / 2f);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        RenderSystem.applyModelViewMatrix();
        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();

        for (Map.Entry<BlockPos, BlockState> entry : structure.getBlocks().entrySet()) {
            BlockState state = entry.getValue();
            if (state == null || state.isAir()) continue;
            BlockPos pos = entry.getKey();
            int rx = pos.getX() - structure.getMinX();
            int ry = pos.getY() - structure.getMinY();
            int rz = pos.getZ() - structure.getMinZ();

            if (sliceLevel >= 0 && ry >= sliceLevel) continue;

            pose.pushPose();
            pose.translate(rx, ry, rz);
            dispatcher.renderSingleBlock(state, pose, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        buffers.endBatch();

        renderBoundingBox(pose, sw, sh, sd);

        pose.popPose();
        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
    }

    private static void updateDragRotation(int x, int y, int width, int height,
                                            double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        boolean lmbDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        boolean inside = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (lmbDown && inside && !dragging) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (!lmbDown) {
            dragging = false;
        }

        if (dragging) {
            double dx = mouseX - lastMouseX;
            double dy = mouseY - lastMouseY;
            if (dx != 0 || dy != 0) {
                userYaw += (float) (dx * DRAG_SENSITIVITY);
                userPitch += (float) (dy * DRAG_SENSITIVITY);
                if (userPitch > 89f) userPitch = 89f;
                if (userPitch < -89f) userPitch = -89f;
                userControlled = true;
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    private static void updateSlice(int x, int y, int width, int height,
                                     double mouseX, double mouseY, int sh) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        boolean rmbDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean inside = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (rmbDown && !rmbPrev && inside) {
            if (sliceLevel < 0) {
                sliceLevel = sh - 1;
            } else {
                sliceLevel--;
            }
            if (sliceLevel <= 0) sliceLevel = -1;
        }
        rmbPrev = rmbDown;
    }

    private static void renderBoundingBox(PoseStack pose, int sw, int sh, int sd) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(2.0f);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f m = pose.last().pose();

        float r = 0.2f, g = 1.0f, b = 0.2f, a = 0.8f;
        float x0 = 0, y0 = 0, z0 = 0;
        float x1 = sw, y1 = sh, z1 = sd;

        // bottom rectangle
        line(buf, m, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(buf, m, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(buf, m, x1, y0, z1, x0, y0, z1, r, g, b, a);
        line(buf, m, x0, y0, z1, x0, y0, z0, r, g, b, a);
        // top rectangle
        line(buf, m, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(buf, m, x1, y1, z0, x1, y1, z1, r, g, b, a);
        line(buf, m, x1, y1, z1, x0, y1, z1, r, g, b, a);
        line(buf, m, x0, y1, z1, x0, y1, z0, r, g, b, a);
        // vertical edges
        line(buf, m, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(buf, m, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(buf, m, x1, y0, z1, x1, y1, z1, r, g, b, a);
        line(buf, m, x0, y0, z1, x0, y1, z1, r, g, b, a);

        MeshData mesh = buf.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void line(BufferBuilder buf, Matrix4f m,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float r, float g, float b, float a) {
        buf.addVertex(m, ax, ay, az).setColor(r, g, b, a);
        buf.addVertex(m, bx, by, bz).setColor(r, g, b, a);
    }
}
