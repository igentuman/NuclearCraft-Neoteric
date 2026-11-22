package igentuman.nc.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

final class PostEffectSupport {

    private PostEffectSupport() {
    }

    /** Starts a post-processing pipeline: screen-quad vertex stage, no blending, no depth test. */
    static RenderPipeline.Builder pipeline(Identifier location, Identifier fragmentShader) {
        return RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                .withLocation(location)
                .withVertexShader("core/screenquad")
                .withFragmentShader(fragmentShader);
    }

    /** Creates (or resizes) a scratch target matching {@code main}; stencil follows the main target. */
    static TextureTarget ensureTarget(@Nullable TextureTarget target, String label, RenderTarget main, boolean depth) {
        if (target == null || target.useDepth != depth || (depth && target.useStencil != main.useStencil)) {
            if (target != null) target.destroyBuffers();
            return new TextureTarget(label, main.width, main.height, depth, depth && main.useStencil);
        }
        if (target.width != main.width || target.height != main.height) {
            target.resize(main.width, main.height);
        }
        return target;
    }

    /** Copies the whole colour attachment of {@code source} into {@code destination} (same size). */
    static void copyColor(RenderTarget source, RenderTarget destination) {
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                source.getColorTexture(), destination.getColorTexture(), 0, 0, 0, 0, 0,
                Math.min(source.width, destination.width), Math.min(source.height, destination.height));
    }

    /** A per-effect uniform buffer that is rewritten (through a buffer copy, so ordering is safe) before each draw. */
    static final class Uniforms implements AutoCloseable {
        private final String label;
        private final int size;
        private @Nullable GpuBuffer buffer;

        Uniforms(String label, int size) {
            this.label = label;
            this.size = size;
        }

        GpuBuffer write(Consumer<Std140Builder> writer) {
            if (buffer == null || buffer.isClosed()) {
                buffer = RenderSystem.getDevice().createBuffer(() -> label,
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, size);
            }
            try (MemoryStack stack = MemoryStack.stackPush()) {
                Std140Builder builder = Std140Builder.onStack(stack, size);
                writer.accept(builder);
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
            }
            return buffer;
        }

        @Override
        public void close() {
            if (buffer != null) buffer.close();
            buffer = null;
        }
    }

    /** Builder for one full-screen draw. */
    static final class Draw {
        private final String label;
        private final RenderPipeline pipeline;
        private final Map<String, GpuTextureView> samplers = new LinkedHashMap<>();
        private final Map<String, GpuSampler> samplerStates = new LinkedHashMap<>();
        private final Map<String, GpuBuffer> uniforms = new LinkedHashMap<>();
        private boolean linear;

        Draw(String label, RenderPipeline pipeline) {
            this.label = label;
            this.pipeline = pipeline;
        }

        Draw sampler(String name, GpuTextureView view) {
            samplers.put(name, view);
            return this;
        }

        /** Binds a texture with its own sampler state (e.g. a repeating noise texture). */
        Draw sampler(String name, GpuTextureView view, GpuSampler state) {
            samplers.put(name, view);
            samplerStates.put(name, state);
            return this;
        }

        Draw uniform(String name, GpuBuffer buffer) {
            uniforms.put(name, buffer);
            return this;
        }

        Draw linear() {
            this.linear = true;
            return this;
        }

        /** Draws into {@code color} and optionally {@code depth} (needed when the shader writes {@code gl_FragDepth}). */
        void into(GpuTextureView color, @Nullable GpuTextureView depth) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(() -> label, color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                uniforms.forEach(pass::setUniform);
                var sampler = RenderSystem.getSamplerCache().getClampToEdge(linear ? FilterMode.LINEAR : FilterMode.NEAREST);
                samplers.forEach((name, view) -> pass.bindTexture(name, view, samplerStates.getOrDefault(name, sampler)));
                pass.draw(0, 3);
            }
        }
    }

    /** Projected screen position of a camera-relative point; {@code null} when behind the camera or outside the depth range. */
    static @Nullable Vector4f project(float x, float y, float z, Matrix4fc view, Matrix4fc projection) {
        Vector4f clip = new Vector4f(x, y, z, 1F).mul(view).mul(projection);
        if (clip.w == 0F) return null;
        clip.x /= clip.w;
        clip.y /= clip.w;
        clip.z /= clip.w;
        boolean zeroToOne = RenderSystem.getDevice().isZZeroToOne();
        float minZ = zeroToOne ? 0F : -1F;
        if (!(clip.z > minZ && clip.z < 1F)) return null;
        // w component reused as the normalized depth-buffer value of the point.
        clip.w = zeroToOne ? clip.z : (clip.z + 1F) * 0.5F;
        return clip;
    }

    static Matrix4f copy(Matrix4fc m) {
        return new Matrix4f(m);
    }
}
