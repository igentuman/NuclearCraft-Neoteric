package igentuman.nc.util.builder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public final class DesignPreviewRenderer {

    private static final float ROTATION_SPEED_DEG_PER_SEC = 15f;
    private static final float TILT_DEG = 30f;
    private static final long START_NANOS = System.nanoTime();

    private DesignPreviewRenderer() {}

    public static void render(GuiGraphics graphics, HashMap<BlockPos, Block> blockMap,
                              int x, int y, int width, int height) {
        if (blockMap == null || blockMap.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : blockMap.keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        int sw = maxX - minX + 1;
        int sh = maxY - minY + 1;
        int sd = maxZ - minZ + 1;
        int maxDim = Math.max(sw, Math.max(sh, sd));
        if (maxDim <= 0) return;

        float scale = Math.min(width, height) / (float) (maxDim * 1.8f);
        float yaw = ((System.nanoTime() - START_NANOS) / 1_000_000_000f) * ROTATION_SPEED_DEG_PER_SEC;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + width / 2f, y + height / 2f, 200f);
        pose.scale(scale, -scale, scale);
        pose.mulPose(new Quaternionf().rotationX((float) Math.toRadians(TILT_DEG)));
        pose.mulPose(new Quaternionf().rotationY((float) Math.toRadians(yaw)));
        pose.translate(-sw / 2f, -sh / 2f, -sd / 2f);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        RenderSystem.applyModelViewMatrix();
        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();

        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockState state = entry.getValue().defaultBlockState();
            if (state.isAir()) continue;
            BlockPos pos = entry.getKey();
            pose.pushPose();
            pose.translate(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ);
            dispatcher.renderSingleBlock(state, pose, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        buffers.endBatch();
        pose.popPose();
        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
    }
}
