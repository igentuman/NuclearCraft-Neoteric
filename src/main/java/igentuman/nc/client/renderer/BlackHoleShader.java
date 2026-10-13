package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.config.Multiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Black-hole-only post effect; sources are submitted anew by the BE renderer each frame. */
public final class BlackHoleShader {
    private static final List<BlackHoleBE> SOURCES = new ArrayList<>();
    private static TextureTarget output;
    private static PostPass pass;

    private BlackHoleShader() {}

    public static void clear() {
        SOURCES.clear();
    }

    public static boolean hasWork() {
        return pass != null && Multiblocks.kugelblitzBlackholeShader && !SOURCES.isEmpty();
    }

    public static boolean submit(BlackHoleBE be) {
        if (!Multiblocks.kugelblitzBlackholeShader || pass == null
                || be.getLevel() != Minecraft.getInstance().level) return false;
        if (!SOURCES.contains(be)) SOURCES.add(be);
        return true;
    }

    public static void reload(ResourceProvider resources) {
        clear();
        if (pass != null) pass.close();
        if (output != null) output.destroyBuffers();
        pass = null;
        output = null;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        try {
            output = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            pass = new PostPass(resources, "nuclearcraft:black_hole", main, output, false);
            pass.addAuxAsset("DepthSampler", main::getDepthTextureId, main.width, main.height);
            resize(main);
        } catch (IOException | RuntimeException e) {
            if (pass != null) pass.close();
            if (output != null) output.destroyBuffers();
            pass = null;
            output = null;
            NuclearCraft.LOGGER.error("Unable to load black hole shader; using sprite fallback", e);
        } finally {
            main.bindWrite(false);
        }
    }

    private static void resize(RenderTarget main) {
        if (main.isStencilEnabled() && !output.isStencilEnabled()) output.enableStencil();
        if (output.width != main.width || output.height != main.height) {
            output.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        ScenePostEffects.matchDepthFormat(output, main);
        pass.setOrthoMatrix(new Matrix4f().setOrtho(0, main.width, 0, main.height, 0.1F, 1000F));
    }

    static void render(RenderLevelStageEvent event) {
        // Preserve opaque occlusion, then let chamber glass blend over the completed effect.
        Minecraft mc = Minecraft.getInstance();
        if (pass == null || mc.level == null || !Multiblocks.kugelblitzBlackholeShader) {
            clear();
            return;
        }
        if (SOURCES.isEmpty()) return;

        RenderTarget main = mc.getMainRenderTarget();
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Matrix4f projection = event.getProjectionMatrix();
        Matrix4f view = event.getModelViewMatrix();
        resize(main);
        pass.getEffect().safeGetUniform("InverseProjection").set(new Matrix4f(projection).invert());
        pass.getEffect().safeGetUniform("Projection").set(projection);
        SOURCES.sort(Comparator.comparingDouble(be -> -camera.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))));

        try {
            for (BlackHoleBE be : SOURCES) {
                if (be.isRemoved() || be.getLevel() != mc.level) continue;
                Vec3 relative = Vec3.atCenterOf(be.getBlockPos()).subtract(camera);
                if (relative.lengthSqr() > 128D * 128D) continue;
                Vector3f center = view.transformPosition(relative.toVector3f());
                Vector4f clip = projection.transform(new Vector4f(center, 1F));
                if (clip.w <= 0.05F) continue;

                float age = be.getFormationAge(partialTick);
                float formation = Math.min(age / 2.5F, 1F);
                float growth = Math.min(age / 0.45F, 1F);
                float radius = 0.7F * (0.85F + be.getBlackholeScale() * 0.5F) * (0.25F + 0.75F * growth);
                float projectedRadius = Math.abs(projection.m11() * radius / clip.w) * 0.5F;
                float x = clip.x / clip.w * 0.5F + 0.5F;
                float y = clip.y / clip.w * 0.5F + 0.5F;
                float marginY = projectedRadius * 8F;
                float marginX = marginY * main.height / main.width;
                if (x < -marginX || x > 1F + marginX || y < -marginY || y > 1F + marginY) continue;

                float wobble = (1F - formation) * (1F - formation);
                Vector3f normal = new Vector3f(0.18F + wobble * 0.25F * (float) Math.sin(age * 11F),
                        1F, 0.25F + wobble * 0.2F * (float) Math.cos(age * 9F)).normalize();
                Vector3f axis = new Vector3f(1F, -normal.x / normal.y, 0F).normalize();
                view.transformDirection(normal);
                view.transformDirection(axis);

                var effect = pass.getEffect();
                effect.safeGetUniform("CenterView").set(center.x, center.y, center.z);
                effect.safeGetUniform("CenterUV").set(x, y);
                effect.safeGetUniform("CoreRadius").set(radius);
                effect.safeGetUniform("ProjectedRadius").set(projectedRadius);
                effect.safeGetUniform("DiskNormal").set(normal.x, normal.y, normal.z);
                effect.safeGetUniform("DiskAxis").set(axis.x, axis.y, axis.z);
                // PostPass overwrites Time; keep animation on a separate uniform.
                effect.safeGetUniform("AnimationTime").set(((mc.level.getGameTime() % 240000L) + partialTick) / 20F);
                effect.safeGetUniform("FormationAge").set(Math.min(age, 2.5F));
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                pass.process(partialTick);
                // The shader preserves scene depth and adds the opaque core/disk, so rear glass
                // cannot draw through the hole. Never let PostPass clear the main target.
                main.copyDepthFrom(output);
                main.bindWrite(false);
                output.blitToScreen(main.width, main.height);
            }
        } finally {
            clear();
            main.bindWrite(true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.defaultBlendFunc();
        }
    }
}
