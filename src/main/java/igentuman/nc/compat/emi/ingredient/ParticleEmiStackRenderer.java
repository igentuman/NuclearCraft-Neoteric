package igentuman.nc.compat.emi.ingredient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;

/**
 * EMI renderer for ParticleStack
 * Based on the JEI ParticleStackRenderer but adapted for EMI's rendering system
 */
public class ParticleEmiStackRenderer {
    
    public static void render(EmiStack stack, PoseStack graphics, int x, int y, float delta, int flags) {
        if (!(stack instanceof ParticleEmiStack particleEmiStack)) {
            return;
        }
        
        ParticleStack particleStack = particleEmiStack.getParticleStack();
        if (particleStack == null || particleStack.getParticle() == null) {
            return;
        }
        
        renderParticle(graphics, x, y, particleStack);
    }
    
    private static void renderParticle(PoseStack graphics, int x, int y, ParticleStack particleStack) {
        Particle particle = particleStack.getParticle();
        if (particle == null) {
            return;
        }
        
        // Set up proper rendering state
        RenderSystem.setShaderTexture(0, particle.getTexture());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw the texture
        net.minecraft.client.gui.GuiComponent.blit(graphics, x, y, 0, 0, 16, 16, 16, 16);

        RenderSystem.disableBlend();
    }
}