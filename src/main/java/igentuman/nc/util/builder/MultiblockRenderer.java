package igentuman.nc.util.builder;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.model.data.ModelData;
import com.mojang.math.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class MultiblockRenderer {

    public static Vec3i getSize(HashMap<BlockPos, Block> blockMap) {
        if (blockMap.isEmpty()) {
            return new Vec3i(1, 1, 1);
        }

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blockMap.keySet()) {
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new Vec3i(maxX, maxY, maxZ);
    }

    public static void render(HashMap<BlockPos, Block> structure, PoseStack stack, int x, int y, int w, int h) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        Vec3i size = getSize(structure);
        int width = size.getX();
        int height = size.getY();
        int depth = size.getZ();
        ShaderInstance initial = RenderSystem.getShader();
        // Calculate appropriate scale to fit within the provided dimensions
        float maxDimension = Math.max(Math.max(width, height), depth);
        float scaleFactor = 0.8f; // Allow some margin around the structure
        float scale = (scaleFactor * Math.min(w, h)) / maxDimension;

        stack.pushPose();
        // Center within the provided x, y, w, h bounds
        stack.translate(x + w/2.0f, y + h/2.0f, 100);
        RenderSystem.setShader(GameRenderer::getRendertypeTextShader);
        // Apply isometric-style rotation for better viewing angle
        stack.mulPose(Vector3f.XP.rotationDegrees(30));
        stack.mulPose(Vector3f.YP.rotationDegrees(-135));

        // Scale to fit within bounds

        stack.scale(scale, scale, scale);

        // Center the structure
        stack.translate(-width/2.0f, -height/2.0f, -depth/2.0f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        int i = 0;
        for (Map.Entry<BlockPos, Block> entry : structure.entrySet()) {
            BlockPos pos = entry.getKey();

            BlockState state = entry.getValue().defaultBlockState();

            stack.pushPose();
            RenderSystem.setShader(GameRenderer::getRendertypeTextShader);
            // Directly subtract 1 from each coordinate instead of using minPos
            stack.translate(
                pos.getX() - 1,
                pos.getY() - 1,
                pos.getZ() - 1
            );
            blockRenderer.renderSingleBlock(
                    state,
                    stack,
                    bufferSource,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    null
            );
            stack.popPose();
            if(i++ > 1) {
               // break;
            }
        }
        bufferSource.endBatch();
        stack.popPose();

    }

}

