package igentuman.nc.util;

import igentuman.nc.client.render.builder.StructurePreviewState;
import igentuman.nc.client.render.builder.PreviewBlockEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders a rotatable, sliceable 3D preview of a multiblock structure inside a GUI rectangle, through
 * a picture-in-picture state drawn by {@link igentuman.nc.client.render.builder.StructurePreviewPipRenderer}.
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

    public static void render(GuiGraphicsExtractor graphics, MultiblockStructure structure,
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

        Map<BlockPos, BlockState> relative = relativeBlocks(structure);
        submit(graphics, relative, relativePayloads(structure), sw, sh, sd, yaw, pitch, sliceLevel, true, x, y, width, height, scale);
    }

    private static final java.util.WeakHashMap<MultiblockStructure, Map<BlockPos, BlockState>> RELATIVE_BLOCKS = new java.util.WeakHashMap<>();

    /** Block states keyed relative to the structure minimum, cached per structure so the mesh is reused. */
    private static Map<BlockPos, BlockState> relativeBlocks(MultiblockStructure structure) {
        return RELATIVE_BLOCKS.computeIfAbsent(structure, s -> {
            Map<BlockPos, BlockState> out = new HashMap<>();
            for (Map.Entry<BlockPos, BlockState> entry : s.getBlocks().entrySet()) {
                BlockState state = entry.getValue();
                if (state == null || state.isAir()) continue;
                BlockPos pos = entry.getKey();
                out.put(new BlockPos(pos.getX() - s.getMinX(), pos.getY() - s.getMinY(), pos.getZ() - s.getMinZ()), state);
            }
            return Map.copyOf(out);
        });
    }

    /** Copies native template payloads into the same coordinate space as the block snapshot. */
    private static Map<BlockPos, CompoundTag> relativePayloads(MultiblockStructure structure) {
        Map<BlockPos, CompoundTag> payloads = new HashMap<>();
        var template = structure.getStructureNbt();
        if (template == null) return payloads;
        var entries = template.getListOrEmpty("blocks");
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompoundOrEmpty(i);
            var pos = entry.getListOrEmpty("pos");
            if (pos.size() != 3) continue;
            entry.getCompound("nbt").ifPresent(tag -> payloads.put(new BlockPos(
                    pos.getIntOr(0, 0) - structure.getMinX(), pos.getIntOr(1, 0) - structure.getMinY(),
                    pos.getIntOr(2, 0) - structure.getMinZ()), tag.copy()));
        }
        return payloads;
    }

    /**
     * Submits a picture-in-picture preview at a GUI rectangle given in the current pose's space.
     * Shared with {@link igentuman.nc.util.builder.DesignPreviewRenderer}.
     * The block map must be a stable snapshot; submit a new map when the layout changes.
     */
    public static void submit(GuiGraphicsExtractor graphics, Map<BlockPos, BlockState> blocks, int sw, int sh, int sd,
                              float yaw, float pitch, int slice, boolean boundingBox,
                              int x, int y, int width, int height, float scale) {
        submit(graphics, blocks, Map.of(), sw, sh, sd, yaw, pitch, slice, boundingBox,
                x, y, width, height, scale);
    }

    public static void submit(GuiGraphicsExtractor graphics, Map<BlockPos, BlockState> blocks,
                              Map<BlockPos, CompoundTag> payloads, int sw, int sh, int sd,
                              float yaw, float pitch, int slice, boolean boundingBox,
                              int x, int y, int width, int height, float scale) {
        Vector2f a = graphics.pose().transformPosition(new Vector2f(x, y));
        Vector2f b = graphics.pose().transformPosition(new Vector2f(x + width, y + height));
        int x0 = Math.round(Math.min(a.x, b.x));
        int y0 = Math.round(Math.min(a.y, b.y));
        int x1 = Math.round(Math.max(a.x, b.x));
        int y1 = Math.round(Math.max(a.y, b.y));
        float poseScale = width == 0 ? 1f : (x1 - x0) / (float) width;
        ScreenRectangle scissor = graphics.peekScissorStack();
        graphics.submitPictureInPictureRenderState(new StructurePreviewState(blocks, PreviewBlockEntities.extract(blocks, payloads, slice), sw, sh, sd, yaw, pitch, slice,
                boundingBox, x0, y0, x1, y1, scale * poseScale, scissor,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissor)));
    }

    private static void updateDragRotation(int x, int y, int width, int height,
                                            double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().handle();
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
        long window = mc.getWindow().handle();
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

}
