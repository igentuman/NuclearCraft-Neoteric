package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class DistortionShader {

    private static ShaderInstance distortionShader;
    private static final ResourceLocation DISTORTION_SHADER = rl("distortion");

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(Minecraft.getInstance().getResourceManager(), DISTORTION_SHADER, DefaultVertexFormat.POSITION_COLOR_TEX), shader -> {
                distortionShader = shader;
            });
        } catch (IOException ignored) { }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (distortionShader != null) {
            Minecraft mc = Minecraft.getInstance();
            mc.getMainRenderTarget().bindWrite(false);
            distortionShader.setSampler("Sampler0", mc.getMainRenderTarget().getColorTextureId());
            distortionShader.safeGetUniform("Time").set(event.getPartialTick());
            distortionShader.safeGetUniform("BlockPos").set(10.0f, -50.0f, 10.0f); // Example BlockPos
            distortionShader.safeGetUniform("DistortionAmount").set(5.0f);
            distortionShader.safeGetUniform("CameraPos").set(mc.gameRenderer.getMainCamera().getPosition().toVector3f());
            distortionShader.apply();
        }
    }
}
