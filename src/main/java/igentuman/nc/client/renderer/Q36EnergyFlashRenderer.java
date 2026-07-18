package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.handler.event.client.TickHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.renderer.NCRenderType.BLACKHOLE;

public class Q36EnergyFlashRenderer extends EntityRenderer<Q36EnergyFlash> {

    private static final ResourceLocation TEXTURE = rl("textures/particle/blackhole_transparent.png");
    private static final int GRID = 6;
    private static final int FRAMES = GRID * GRID;
    private static final float MAX_SCALE = 2.5F;
    private static final int GROW_TICKS = 6;

    public Q36EnergyFlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(Q36EnergyFlash entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        final int age = entity.tickCount;
        final float ageF = age + partialTick;
        final int life = Q36EnergyFlash.LIFETIME_TICKS;
        final Vec3 pos = entity.position();
        TickHandler.addTransparentRenderer(BLACKHOLE.apply(TEXTURE), new TickHandler.LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer vc, PoseStack stack, int renderTick, float pt, ProfilerFiller profiler) {
                int frame = Math.min((int) (ageF * FRAMES / life), FRAMES - 1);
                int yIndex = frame % GRID;
                int xIndex = frame / GRID;
                float ss = 1.0F / GRID;
                float minU = xIndex * ss;
                float maxU = minU + ss;
                float minV = yIndex * ss;
                float maxV = minV + ss;

                float scale;
                if (ageF <= GROW_TICKS) {
                    float t = ageF / GROW_TICKS;
                    scale = MAX_SCALE * (1.0F - (1.0F - t) * (1.0F - t));
                } else {
                    float t = (ageF - GROW_TICKS) / (life - GROW_TICKS);
                    if (t > 1.0F) t = 1.0F;
                    scale = MAX_SCALE * (1.0F - t * t);
                }

                Quaternion q = camera.rotation();
                Vector3f[] verts = {
                        new Vector3f(-1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, 1.0F, 0.0F),
                        new Vector3f(1.0F, -1.0F, 0.0F),
                        new Vector3f(-1.0F, -1.0F, 0.0F)
                };
                for (Vector3f v : verts) {
                    v.transform(q);
                    v.mul(scale);
                    v.add((float) pos.x, (float) pos.y, (float) pos.z);
                }

                stack.pushPose();
                Matrix4f m = stack.last().pose();
                vc.vertex(m, verts[0].x(), verts[0].y(), verts[0].z()).color(255, 255, 255, 255).uv(minU, maxV).endVertex();
                vc.vertex(m, verts[1].x(), verts[1].y(), verts[1].z()).color(255, 255, 255, 255).uv(maxU, maxV).endVertex();
                vc.vertex(m, verts[2].x(), verts[2].y(), verts[2].z()).color(255, 255, 255, 255).uv(maxU, minV).endVertex();
                vc.vertex(m, verts[3].x(), verts[3].y(), verts[3].z()).color(255, 255, 255, 255).uv(minU, minV).endVertex();
                stack.popPose();
            }

            @Override
            public Vec3 getCenterPos(float pt) {
                return pos;
            }

            @Override
            public String getProfilerSection() {
                return "nc.q36_energy_flash";
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Q36EnergyFlash entity) {
        return TEXTURE;
    }
}
