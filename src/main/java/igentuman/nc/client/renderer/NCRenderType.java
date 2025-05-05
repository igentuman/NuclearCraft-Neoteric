package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class NCRenderType extends RenderType {
    private NCRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
                               Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static final Function<ResourceLocation, RenderType> BLACKHOLE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(NCShaders.BLACKHOLE_COLOR.shard)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setOutputState(RenderType.MAIN_TARGET)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .createCompositeState(true);
        return create("nc_blackhole", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, true, true, state);
    });

}
