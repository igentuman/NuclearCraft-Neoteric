package igentuman.nc.client.renderer;

import igentuman.api.platform.NCLevels;
import igentuman.api.platform.NCRendering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.renderer.NCShaders.blackholePostEffect;
import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class DistortShader {

    private static int currentSize = 0;
    public static void register() {
        NeoForge.EVENT_BUS.register(DistortShader.class);
    }

    public static final BlackholeRegistry blackhole = new BlackholeRegistry();

    public static class BlackholeRegistry {
        private final Set<BlockPos> positions = new HashSet<>();

        public boolean contains(BlockPos pos) { return positions.contains(pos); }
        public void add(BlockPos pos) { positions.add(pos); }
        public void remove(BlockPos pos) { positions.remove(pos); }
        public Set<BlockPos> getPositions() { return Collections.unmodifiableSet(positions); }
    }

    private static boolean isBlackHoleBlock(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return level.getBlockState(pos).is(KUGELBLITZ_BLOCKS.get("black_hole").get());
    }

    private static boolean processBlackHole(Minecraft mc, RenderLevelStageEvent event, EffectInstance effect, BlockPos pos) {
        if (mc.level == null && mc.player == null) {
            return false;
        }

        // Calculate distance to blackhole
        double distanceSq = mc.player.position().distanceToSqr(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );
        double distance = Math.sqrt(distanceSq);
        if(distance > 64) {
            return false;
        }
        BlockEntity be = NCLevels.getExistingBlockEntity(mc.level, pos);
        float scaleMult = 1;
        if(be instanceof BlackHoleBE blackHoleBE) {
            scaleMult = 0.3f / blackHoleBE.scale;
        } else {
            return false;
        }
        if(scaleMult != 1) {
            scaleMult = (float) Math.pow(scaleMult+0.375f, 5);
        }
        // Default values
        float blurX = 0.5f;
        float blurY = 0.5f;
        boolean blackholeVisible = false;
        float distanceFactor = 0.0f;


        // Get matrices
        Matrix4f viewMatrix = event.getPoseStack().last().pose();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

        // Get camera position
        net.minecraft.client.Camera camera = mc.gameRenderer.getMainCamera();
        net.minecraft.world.phys.Vec3 cameraPos = camera.getPosition();

        // World to screen projection
        float posX = (float)(pos.getX() + 0.5 - cameraPos.x());
        float posY = (float)(pos.getY() + 0.5 - cameraPos.y());
        float posZ = (float)(pos.getZ() + 0.5 - cameraPos.z());

        // Create position vector
        org.joml.Vector4f pos1 = new org.joml.Vector4f(posX, posY, posZ, 1.0f);
        pos1.mul(viewMatrix);
        pos1.mul(projectionMatrix);

        // Perspective division
        if (pos1.w != 0.0f) {
            pos1.x /= pos1.w;
            pos1.y /= pos1.w;
            pos1.z /= pos1.w;
        }

        // Calculate the normalized depth value (0.0 to 1.0)
        // In OpenGL/Minecraft, depth is in range [-1, 1] after projection, normalize to [0, 1]
        float normalizedDepth = (pos1.z + 1.0f) * 0.5f;
        
        // Check if in front of camera
        if (pos1.z > -1.0f && pos1.z < 1.0f) {
            // Calculate screen coordinates
            blurX = (pos1.x * 0.5f + 0.5f);
            blurY = (pos1.y * 0.5f + 0.5f);

            // Check if on screen (with margin)
            float margin = 0.1f;
            if (blurX >= -margin && blurX <= 1.0f + margin &&
                    blurY >= -margin && blurY <= 1.0f + margin) {
                blackholeVisible = true;
                distanceFactor = (float) (7f/distance);
            }
        }

        // Scale radius and magnification based on distance
        float baseRadius = blackholeVisible ? 150.0f : 0.0f;
        float radius = baseRadius * distanceFactor * scaleMult;
        float baseMagnification = blackholeVisible ? 5.8f : 0.1f;

        effect.getUniform("BlurPos").set(blurX, blurY);
        effect.getUniform("Radius").set(radius, baseMagnification/scaleMult);
        effect.getUniform("BlackHoleDepth").set(normalizedDepth);
        return true;
    }

    @SubscribeEvent
    public static void onRenderTick(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();

        if(!KUGELBLITZ_CONFIG.BLACKHOLE_SHADER.get()) return;
        if (blackholePostEffect != null) {
            EffectInstance effect = blackholePostEffect.passes.get(0).getEffect();

            if(currentSize != mc.getWindow().getWidth() + mc.getWindow().getHeight()) {
                currentSize = mc.getWindow().getWidth() + mc.getWindow().getHeight();
                blackholePostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                effect.getUniform("BlurDir").set(0.2f, 0.0f);
            }

            // Enable depth testing but disable depth writing
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            
            for (BlockPos pos : blackhole.getPositions()) {
                if(processBlackHole(mc, event, effect, pos)) {
                    blackholePostEffect.process(mc.getTimer().getGameTimeDeltaPartialTick(true));
                }
            }
            
            mc.getMainRenderTarget().bindWrite(false);
            blackholePostEffect.passes.get(blackholePostEffect.passes.size()-1).outTarget.bindRead();
            
            // Use standard depth function (GL_LEQUAL = 515)
            RenderSystem.depthFunc(515);
            
            // Set up blending to preserve what's already in the framebuffer
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );

            // Draw a fullscreen quad with the processed shader result
            int rtWidth = mc.getMainRenderTarget().width;
            int rtHeight = mc.getMainRenderTarget().height;
            NCRendering.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, buffer -> {
                buffer.addVertex(0, rtHeight, 0).setUv(0, 0);
                buffer.addVertex(rtWidth, rtHeight, 0).setUv(1, 0);
                buffer.addVertex(rtWidth, 0, 0).setUv(1, 1);
                buffer.addVertex(0, 0, 0).setUv(0, 1);
            });

            // Restore render state
            RenderSystem.depthFunc(515);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }
}
