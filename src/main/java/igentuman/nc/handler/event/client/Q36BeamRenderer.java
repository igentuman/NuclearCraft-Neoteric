package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.NuclearCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class Q36BeamRenderer {

    private static final int LIFETIME_TICKS = 4;
    private static final List<Beam> ACTIVE = new ArrayList<>();

    private static class Beam {
        final Vec3 start;
        final Vec3 end;
        int ticksLeft;

        Beam(Vec3 s, Vec3 e) {
            this.start = s;
            this.end = e;
            this.ticksLeft = LIFETIME_TICKS;
        }
    }

    public static void add(Vec3 s, Vec3 e) {
        ACTIVE.add(new Beam(s, e));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post ev) {
        Iterator<Beam> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Beam b = it.next();
            if (--b.ticksLeft <= 0) it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ACTIVE.isEmpty()) return;

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = cam.getPosition();
        PoseStack pose = event.getPoseStack();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        boolean hasVertices = false;

        for (Beam b : ACTIVE) {
            float alpha = (float) b.ticksLeft / (float) LIFETIME_TICKS;
            float sx = (float) (b.start.x - camPos.x);
            float sy = (float) (b.start.y - camPos.y);
            float sz = (float) (b.start.z - camPos.z);
            float ex = (float) (b.end.x - camPos.x);
            float ey = (float) (b.end.y - camPos.y);
            float ez = (float) (b.end.z - camPos.z);

            float dx = ex - sx, dy = ey - sy, dz = ez - sz;
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0E-4F) continue;

            float cx = (float) (camPos.x - (b.start.x + b.end.x) * 0.5D);
            float cy = (float) (camPos.y - (b.start.y + b.end.y) * 0.5D);
            float cz = (float) (camPos.z - (b.start.z + b.end.z) * 0.5D);

            float px = dy * cz - dz * cy;
            float py = dz * cx - dx * cz;
            float pz = dx * cy - dy * cx;
            float pl = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (pl < 1.0E-4F) continue;
            float w = 0.12F;
            px = px / pl * w; py = py / pl * w; pz = pz / pl * w;

            int r = 120, g = 220, bl = 255;
            int a = (int) (alpha * 220.0F);

            var m = pose.last().pose();
            bb.addVertex(m, sx + px, sy + py, sz + pz).setColor(r, g, bl, a);
            bb.addVertex(m, sx - px, sy - py, sz - pz).setColor(r, g, bl, a);
            bb.addVertex(m, ex - px, ey - py, ez - pz).setColor(r, g, bl, a);
            bb.addVertex(m, ex + px, ey + py, ez + pz).setColor(r, g, bl, a);
            hasVertices = true;
        }

        if (hasVertices) {
            BufferUploader.drawWithShader(bb.buildOrThrow());
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
