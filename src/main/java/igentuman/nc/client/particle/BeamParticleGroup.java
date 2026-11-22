package igentuman.nc.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.NuclearCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class BeamParticleGroup extends ParticleGroup<FusionBeamParticle> {

    public static final ParticleRenderType TYPE = new ParticleRenderType("NUCLEARCRAFT_BEAM");

    private static final RenderType RENDER_TYPE = RenderType.create("nuclearcraft_beam_particle",
            RenderSetup.builder(RenderPipelines.TRANSLUCENT_PARTICLE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .sortOnUpload()
                    .createRenderSetup());

    private final State state = new State();

    public BeamParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @SubscribeEvent
    static void registerGroups(RegisterParticleGroupsEvent event) {
        event.register(TYPE, BeamParticleGroup::new);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        for (FusionBeamParticle particle : this.particles) {
            if (particle.isVisible(frustum)) {
                state.beams.add(particle.extract(camera, partialTickTime));
            }
        }
        return state;
    }

    record Beam(Vector3f[] first, Vector3f[] second, float u0, float u1, float v0, float v1,
                float r, float g, float b, float a, int light) {

        void emit(PoseStack.Pose pose, VertexConsumer buffer) {
            plane(pose, buffer, first);
            plane(pose, buffer, second);
        }

        private void plane(PoseStack.Pose pose, VertexConsumer buffer, Vector3f[] v) {
            vertex(pose, buffer, v[0], u1, v1);
            vertex(pose, buffer, v[1], u1, v0);
            vertex(pose, buffer, v[2], u0, v0);
            vertex(pose, buffer, v[3], u0, v1);
            // Back faces
            vertex(pose, buffer, v[1], u1, v0);
            vertex(pose, buffer, v[0], u1, v1);
            vertex(pose, buffer, v[3], u0, v1);
            vertex(pose, buffer, v[2], u0, v0);
        }

        private void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vector3f p, float u, float v) {
            buffer.addVertex(pose, p.x(), p.y(), p.z()).setUv(u, v).setColor(r, g, b, a).setLight(light);
        }
    }

    private static final class State implements ParticleGroupRenderState {
        private final List<Beam> beams = new ArrayList<>();

        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
            if (beams.isEmpty()) {
                return;
            }
            List<Beam> snapshot = List.copyOf(beams);
            submitNodeCollector.submitCustomGeometry(new PoseStack(), RENDER_TYPE, (pose, buffer) -> {
                for (Beam beam : snapshot) {
                    beam.emit(pose, buffer);
                }
            });
        }

        @Override
        public void clear() {
            beams.clear();
        }
    }
}
