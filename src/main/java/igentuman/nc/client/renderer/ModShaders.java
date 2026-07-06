package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import igentuman.nc.NuclearCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Registers the mod's custom render shaders and post-processing chains.
 *
 * <ul>
 *   <li>{@link #BILLBOARD} - core shader backing {@link ModRenderType#BILLBOARD}.</li>
 *   <li>{@link #distortPostEffect} - screen-space lens distortion used by {@link DistortShader}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class ModShaders {

    public static final ShaderTracker BILLBOARD = new ShaderTracker();

    public static PostChain distortPostEffect;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), NuclearCraft.rl("rendertype_billboard"), DefaultVertexFormat.POSITION_TEX_COLOR),
                BILLBOARD::setInstance
        );

        Minecraft mc = Minecraft.getInstance();
        distortPostEffect = new PostChain(
                mc.getTextureManager(),
                mc.getResourceManager(),
                mc.getMainRenderTarget(),
                NuclearCraft.rl("shaders/post/distort.json")
        );
        distortPostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    /**
     * Holds a loaded {@link ShaderInstance} and exposes a {@link RenderStateShard.ShaderStateShard}
     * so it can be wired into a {@link net.minecraft.client.renderer.RenderType}.
     */
    public static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        private final RenderStateShard.ShaderStateShard shard = new RenderStateShard.ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        public RenderStateShard.ShaderStateShard shard() {
            return shard;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}
