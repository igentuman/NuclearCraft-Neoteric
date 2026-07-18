package igentuman.nc.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import igentuman.nc.client.bomb.ActiveBomb;
import igentuman.nc.client.bomb.BombFxManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererCloudsMixin {

    @Shadow @Final private ClientLevel level;

    @Shadow public int ticks;

    @Inject(
            method = "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FDDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lcom/mojang/math/Matrix4f;Lcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"
            )
    )
    private void nc$applyShockwaveUniforms(PoseStack pPoseStack, Matrix4f pMatrix4f, float pPartialTick, double pCamX, double pCamY, double pCamZ, CallbackInfo ci) {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null || level == null) return;

        float cloudHeight = level.effects().getCloudHeight();
        if (Float.isNaN(cloudHeight)) return;

        double d0 = ((float) this.ticks + pPartialTick) * 0.03F;
        double d1 = (pCamX + d0) / 12.0;
        double d2 = (cloudHeight - (float) pCamY + 0.33F);
        double d3 = pCamZ / 12.0 + 0.33;
        d1 -= Mth.floor(d1 / 2048.0) * 2048;
        d3 -= Mth.floor(d3 / 2048.0) * 2048;
        float f1 = (float) (d1 - Mth.floor(d1));
        float f2 = (float) (d2 / 4.0 - Mth.floor(d2 / 4.0)) * 4.0F;
        float f3 = (float) (d3 - Mth.floor(d3));

        Uniform origin = shader.getUniform("CloudOriginWorld");
        if (origin != null) {
            origin.set((float) (pCamX - f1), (float) (pCamY + f2), (float) (pCamZ - f3));
        }

        Uniform[] sw = {
                shader.getUniform("Shockwave0"),
                shader.getUniform("Shockwave1"),
                shader.getUniform("Shockwave2"),
                shader.getUniform("Shockwave3")
        };
        Uniform countU = shader.getUniform("ShockwaveCount");
        Uniform softU = shader.getUniform("ShockwaveSoft");
        if (countU == null || sw[0] == null) return;

        List<ActiveBomb> bombs = BombFxManager.active();
        int count = 0;
        for (ActiveBomb b : bombs) {
            if (count >= 4) break;
            float r = b.cloudWipeRadius(pPartialTick);
            if (r <= 0f) continue;
            sw[count].set(
                    b.epicenter.getX() + 0.5f,
                    cloudHeight,
                    b.epicenter.getZ() + 0.5f,
                    r
            );
            count++;
        }
        for (int i = count; i < 4; i++) {
            sw[i].set(0f, 0f, 0f, 0f);
        }
        countU.set((float) count);
        if (softU != null) softU.set(24.0f);
    }
}
