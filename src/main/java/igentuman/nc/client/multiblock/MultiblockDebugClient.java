package igentuman.nc.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.renderer.ModRenderType;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.network.PacketMultiblockDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.util.StackUtils.isMultiTool;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public final class MultiblockDebugClient {

    private static final Map<Long, PacketMultiblockDebug> SNAPSHOTS = new ConcurrentHashMap<>();
    private static Level snapshotLevel;

    private MultiblockDebugClient() {
    }

    public static void update(PacketMultiblockDebug packet) {
        Level currentLevel = Minecraft.getInstance().level;
        if (snapshotLevel != currentLevel) {
            SNAPSHOTS.clear();
            snapshotLevel = currentLevel;
        }
        SNAPSHOTS.put(packet.controllerPos().asLong(), packet);
    }

    public static void remove(BlockPos controllerPos) {
        SNAPSHOTS.remove(controllerPos.asLong());
    }

    public static void clear() {
        SNAPSHOTS.clear();
        snapshotLevel = null;
    }

    public static PacketMultiblockDebug get(BlockPos controllerPos) {
        return SNAPSHOTS.get(controllerPos.asLong());
    }

    public static boolean isActive() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !Multiblocks.SPEC.isLoaded() || !Multiblocks.DEBUG_LOGGING.get()) return false;
        return isMultiTool(player.getMainHandItem()) || isMultiTool(player.getOffhandItem());
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!isActive()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || SNAPSHOTS.isEmpty()) return;

        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        double cameraX = camera.x;
        double cameraY = camera.y;
        double cameraZ = camera.z;
        PoseStack poseStack = event.getPoseStack();
        float width = minecraft.gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        // See-through lines: no depth test so the boxes stay visible through walls.
        VertexConsumer lines = buffers.getBuffer(ModRenderType.LINES_NO_DEPTH);

        for (PacketMultiblockDebug snapshot : SNAPSHOTS.values()) {
            if (snapshot.controllerPos().distSqr(minecraft.player.blockPosition()) > 256D * 256D) continue;
            if (snapshot.minimum() != null && snapshot.maximum() != null) {
                BlockPos min = snapshot.minimum();
                BlockPos max = snapshot.maximum();
                AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(),
                        max.getX() + 1D, max.getY() + 1D, max.getZ() + 1D);
                box(poseStack, lines, bounds, cameraX, cameraY, cameraZ, ARGB.colorFromFloat(0.9F, 1F, 0.85F, 0F), width);
            }
            drawBlock(poseStack, lines, snapshot.start(), cameraX, cameraY, cameraZ, 0F, 1F, 0F, width);
            drawBlock(poseStack, lines, snapshot.end(), cameraX, cameraY, cameraZ, 0.15F, 0.45F, 1F, width);
            drawBlock(poseStack, lines, snapshot.failingPosition(), cameraX, cameraY, cameraZ, 1F, 0F, 0F, width);
        }
        buffers.endBatch(ModRenderType.LINES_NO_DEPTH);
    }

    private static void drawBlock(PoseStack poseStack, VertexConsumer lines, BlockPos pos,
                                  double cameraX, double cameraY, double cameraZ,
                                  float red, float green, float blue, float width) {
        if (pos == null) return;
        box(poseStack, lines, new AABB(pos).inflate(0.006D), cameraX, cameraY, cameraZ,
                ARGB.colorFromFloat(1F, red, green, blue), width);
    }

    private static void box(PoseStack poseStack, VertexConsumer lines, AABB box,
                            double cameraX, double cameraY, double cameraZ, int color, float width) {
        ShapeRenderer.renderShape(poseStack, lines, Shapes.create(box), -cameraX, -cameraY, -cameraZ, color, width);
    }
}
