package igentuman.api.platform;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Consumer;

/**
 * Platform translation layer for NeoForge 1.21.1 rendering APIs.
 *
 * <p>Wraps the Tesselator/BufferBuilder/BufferUploader pipeline that changed
 * between 1.20→1.21. NC code calls {@link #draw} instead of touching
 * Tesselator directly.
 *
 * <h3>Tesselator pipeline</h3>
 * <ul>
 *   <li>1.20: {@code tesselator.getBuilder()} + {@code builder.begin(mode, fmt)} + ... + {@code tesselator.end()}</li>
 *   <li>1.21: {@code tesselator.begin(mode, fmt)} returns BufferBuilder + ... + {@code BufferUploader.drawWithShader(builder.buildOrThrow())}</li>
 * </ul>
 */
public final class NCRendering {

    private NCRendering() {}

    /**
     * Draw primitives using Tesselator. Wraps the begin/build/upload pipeline.
     * Caller provides vertex data via the consumer.
     *
     * @param mode         the vertex format mode (e.g. QUADS, TRIANGLES)
     * @param format       the vertex format (e.g. POSITION_COLOR, POSITION_TEX)
     * @param vertexBuilder consumer that receives a BufferBuilder and adds vertices
     */
    public static void draw(VertexFormat.Mode mode, VertexFormat format,
                            Consumer<BufferBuilder> vertexBuilder) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(mode, format);
        vertexBuilder.accept(buffer);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
