package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NCShaders {

    public static final ShaderTracker BLACKHOLE_COLOR = new ShaderTracker();

    public static PostChain blackholePostEffect;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        registerShader(event, rl("rendertype_blackhole"), DefaultVertexFormat.POSITION_TEX_COLOR, BLACKHOLE_COLOR);
        Minecraft mc = Minecraft.getInstance();

        blackholePostEffect = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(), rl("shaders/post/black_hole.json"));
        blackholePostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), shaderLocation, vertexFormat), tracker::setInstance);
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
