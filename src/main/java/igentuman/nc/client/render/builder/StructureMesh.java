package igentuman.nc.client.render.builder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class StructureMesh {

    private static final RenderType[] CHUNK_LAYERS = {
        RenderType.solid(),
        RenderType.cutoutMipped(),
        RenderType.cutout(),
        RenderType.translucent()
    };

    private final Map<RenderType, VertexBuffer> vbos = new HashMap<>();

    public void rebuild(FakeStructureLevel level) {
        close();

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
        RandomSource random = RandomSource.create();
        PoseStack poseStack = new PoseStack();

        Map<RenderType, ByteBufferBuilder> pool = new HashMap<>();
        Map<RenderType, BufferBuilder> builders = new HashMap<>();

        for (RenderType rt : CHUNK_LAYERS) {
            ByteBufferBuilder bbb = new ByteBufferBuilder(131072);
            BufferBuilder bb = new BufferBuilder(bbb, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            pool.put(rt, bbb);
            builders.put(rt, bb);
        }

        for (Map.Entry<BlockPos, BlockState> entry : level.getBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (state.isAir()) continue;

            var model = dispatcher.getBlockModel(state);
            ModelData modelData = model.getModelData(level, pos, state, ModelData.EMPTY);
            long seed = state.getSeed(pos);

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            for (RenderType rt : model.getRenderTypes(state, random, modelData)) {
                BufferBuilder builder = builders.get(rt);
                if (builder == null) continue;
                modelRenderer.tesselateBlock(
                    level, model, state, pos, poseStack, builder,
                    true, random, seed, OverlayTexture.NO_OVERLAY, modelData, rt
                );
            }

            poseStack.popPose();
        }

        for (Map.Entry<RenderType, BufferBuilder> entry : builders.entrySet()) {
            RenderType rt = entry.getKey();
            MeshData rendered = entry.getValue().build();
            if (rendered == null) {
                continue;
            }

            VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vbo.bind();
            vbo.upload(rendered);
            VertexBuffer.unbind();
            vbos.put(rt, vbo);

            rendered.close();
        }

        for (ByteBufferBuilder bbb : pool.values()) {
            bbb.close();
        }
    }

    public boolean isEmpty() {
        return vbos.isEmpty();
    }

    public void draw(Matrix4f modelViewMatrix, Matrix4f projMatrix, float alpha) {
        draw(modelViewMatrix, projMatrix, alpha, false);
    }

    public void draw(Matrix4f modelViewMatrix, Matrix4f projMatrix, float alpha, boolean depthTest) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        for (RenderType rt : CHUNK_LAYERS) {
            VertexBuffer vbo = vbos.get(rt);
            if (vbo == null) continue;

            ShaderInstance shader = getShader(rt);
            if (shader == null) continue;

            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            rt.setupRenderState();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (depthTest) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

            vbo.bind();
            vbo.drawWithShader(modelViewMatrix, projMatrix, shader);
            VertexBuffer.unbind();

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableDepthTest();
            rt.clearRenderState();
        }

        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }

    private static ShaderInstance getShader(RenderType rt) {
        if (rt == RenderType.solid()) return GameRenderer.getRendertypeSolidShader();
        if (rt == RenderType.cutoutMipped()) return GameRenderer.getRendertypeCutoutMippedShader();
        if (rt == RenderType.cutout()) return GameRenderer.getRendertypeCutoutShader();
        if (rt == RenderType.translucent()) return GameRenderer.getRendertypeTranslucentShader();
        return GameRenderer.getRendertypeSolidShader();
    }

    public void close() {
        for (VertexBuffer vbo : vbos.values()) {
            vbo.close();
        }
        vbos.clear();
    }
}
