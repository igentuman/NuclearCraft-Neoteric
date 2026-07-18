package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NCShaders {

    public static final ShaderTracker BLACKHOLE_COLOR = new ShaderTracker();
    public static final ShaderTracker NUKE_CORE = new ShaderTracker();
    public static final ShaderTracker NUKE_SMOKE = new ShaderTracker();

    public static PostChain blackholePostEffect;
    public static PostChain nukePostEffect;
    public static PostChain q36FlashPostEffect;
    public static PostChain anomalyPostEffect;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        registerShader(event, rl("rendertype_blackhole"), DefaultVertexFormat.POSITION_COLOR_TEX, BLACKHOLE_COLOR);
        registerShader(event, rl("rendertype_nuke"), DefaultVertexFormat.POSITION_COLOR_TEX, NUKE_CORE);
        registerShader(event, rl("rendertype_nuke_smoke"), DefaultVertexFormat.POSITION_COLOR_TEX, NUKE_SMOKE);
        Minecraft mc = Minecraft.getInstance();

        nukePostEffect = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(), rl("shaders/post/nuke.json"));
        nukePostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        blackholePostEffect = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(), rl("shaders/post/black_hole.json"));
        blackholePostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        q36FlashPostEffect = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(), rl("shaders/post/q36_flash.json"));
        q36FlashPostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        anomalyPostEffect = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(), rl("shaders/post/anomaly.json"));
        anomalyPostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), shaderLocation, vertexFormat), tracker::setInstance);
    }

    static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final RenderStateShard.ShaderStateShard shard = new RenderStateShard.ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}
