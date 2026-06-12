package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import igentuman.api.nc.SideModeToggleable;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.item.QNP;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.collection.HashList;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mojang.math.Axis.XP;
import static com.mojang.math.Axis.YP;
import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.item.QNP.getMode;
import static igentuman.nc.util.AreaUtil.getArea;
import static igentuman.nc.util.StackUtils.isMultiTool;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BlockOverlayHandler {

    private static int outlineCooldown = 5;
    private final static HashList<BlockPos> highlightsToRemove = new HashList<>();
    private final static HashMap<Long, RenderBox> boxes = new HashMap<>();
    public final static HashList<FissionControllerBE> reactors = new HashList<>();
    private final static Set<MultiblockControllerBE> debugControllers = ConcurrentHashMap.newKeySet();

    public static void registerDebugController(MultiblockControllerBE be) {
        debugControllers.add(be);
    }

    public static void unregisterDebugController(MultiblockControllerBE be) {
        debugControllers.remove(be);
    }

    public static Set<MultiblockControllerBE> getDebugControllers() {
        return debugControllers;
    }

    public static boolean isDebugOverlayActive() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return false;
        if (!CommonConfig.MISC_CONFIG.DEBUG_LOG.get()) return false;
        return isMultiTool(p.getMainHandItem()) || isMultiTool(p.getOffhandItem());
    }

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(BlockOverlayHandler::blockOverlayEvent);
        MinecraftForge.EVENT_BUS.addListener(BlockOverlayHandler::onRenderPre);
    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderLevelStageEvent e) {
        final GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Player player = Minecraft.getInstance().player;
        if(e.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS)) {
            for(BlockPos pos: fusionReactors) {
                if(true) continue; //disable for now
                BlockEntity be = player.level().getBlockEntity(pos);
                if(! (be instanceof FusionCoreBE)) continue;
                FusionCoreBE fusionBe = (FusionCoreBE) be;
                int size = fusionBe.size+2;

                AABB box1 = new AABB(-size, 0.01f,-size, size,0.99f, -size+1);
                AABB box2 = new AABB(-size, 0.01f, size, size,0.99f, size-1);

                drawBoundingBoxAtBlockPos(e.getPoseStack(), box1, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                drawBoundingBoxAtBlockPos(e.getPoseStack(), box2, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                //drawBoundingBoxAtBlockPos(e.getPoseStack(), box3, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                // drawBoundingBoxAtBlockPos(e.getPoseStack(), box4, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
            }
        }
        if(!highlightsToRemove.isEmpty()) {
            outlineCooldown--;
            if (outlineCooldown < 1) {
                outlineCooldown = 400;
                for (BlockPos pos : highlightsToRemove) {
                    outlineBlocks.remove(pos);
                }
                highlightsToRemove.clear();
            }
        }
        if(e.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) {
            gameRenderer.resetProjectionMatrix(e.getProjectionMatrix());
            if (player.level().isClientSide) {
                for (FissionControllerBE reactor : reactors) {
                    //todo enable this later
                    //renderFilledBox(e.getPoseStack(), reactor.getGlowAABB(), 0.1f, 0.6f, 0.7f, 0.2f, reactor.getBlockPos(), player.blockPosition());
                }
                for(RenderBox box: boxes.values()) {
                    drawBoundingBoxAtBlockPos(e.getPoseStack(), box.boundingBox, box.red, box.green, box.blue, box.alpha, box.relative, player.blockPosition());
                }
                renderMultiblockDebugOverlays(e.getPoseStack(), player);
                if (outlineBlocks.isEmpty()) return;
                for (BlockPos pos: outlineBlocks) {
                    if(pos.equals(BlockPos.ZERO)) continue;
                    AABB aabb = new AABB(0, 0,0,1,1,1);
                    drawBoundingBoxAtBlockPos(e.getPoseStack(), aabb, 1, 0, 0, 1, pos, player.blockPosition());
                    highlightsToRemove.add(pos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockOverlayEvent(RenderHighlightEvent.Block event) {
        HitResult hit = event.getTarget();
        ItemStack stackItem = Minecraft.getInstance().player.getMainHandItem();
        handleQNP(event, hit, stackItem);
        handleMultitool(event, hit, stackItem);
    }

    private static void handleMultitool(RenderHighlightEvent.Block event, HitResult hit, ItemStack stackItem) {
        if (hit.getType() == HitResult.Type.BLOCK && isMultiTool(stackItem)) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            event.setCanceled(true);
            BlockPos blockPos = blockRayTraceResult.getBlockPos();

            Level world = Minecraft.getInstance().player.level();
            BlockEntity be = world.getBlockEntity(blockPos);
            if(! (be instanceof NuclearCraftBE)) return;
            NuclearCraftBE ncBe = (NuclearCraftBE) be;
            if(ncBe.sideConfig.isEmpty()) return;
            Direction hitSide = blockRayTraceResult.getDirection();
            if(Minecraft.getInstance().player.isShiftKeyDown()) {
                hitSide = hitSide.getOpposite();
            }
            SideModeToggleable.SideMode mode = ncBe.sideConfig.get(hitSide.ordinal());
            if(mode == null) return;
            float[] color = new float[]{0, 1, 0};
            switch (mode) {
                case DEFAULT -> color = new float[]{0, 1, 0};
                case IN -> color = new float[]{0, 0, 1};
                case OUT -> color = new float[]{1, 0, 0};
                case DISABLED -> color = new float[]{0.5f, 0.5f, 0.5f};
            }
            PoseStack stack = new PoseStack();
            stack.pushPose();
            Camera info = event.getCamera();
            stack.mulPose(XP.rotationDegrees(info.getXRot()));
            stack.mulPose(YP.rotationDegrees(info.getYRot() + 180));
            double d0 = info.getPosition().x();
            double d1 = info.getPosition().y();
            double d2 = info.getPosition().z();
            VertexConsumer builder = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines());
            VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
            AABB bounds = shape.bounds();
            switch (hitSide) {
                case DOWN -> bounds = bounds.setMaxY(0.01);
                case UP ->  bounds = bounds.setMinY(0.99);
                case NORTH -> bounds = bounds.setMaxZ(0.01);
                case SOUTH -> bounds = bounds.setMinZ(0.99);
                case WEST -> bounds = bounds.setMaxX(0.01);
                case EAST -> bounds = bounds.setMinX(0.99);
            }
            LevelRenderer.renderLineBox(stack, builder, bounds.move(blockPos.getX() - d0, blockPos.getY() - d1, blockPos.getZ() - d2), color[0], color[1], color[2], 0.35F);

            stack.popPose();
        }
    }

    private static void handleQNP(RenderHighlightEvent.Block event, HitResult hit, ItemStack stackItem) {
        if (hit.getType() == HitResult.Type.BLOCK && stackItem.getItem() instanceof QNP qnp) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            event.setCanceled(true);
            QNP.Mode mode = getMode(stackItem);
            Level world = Minecraft.getInstance().player.level();
            Pair<BlockPos, BlockPos> area = getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(),  mode.radius, mode.depth);

            PoseStack stack = new PoseStack();
            stack.pushPose();
            Camera info = event.getCamera();
            stack.mulPose(XP.rotationDegrees(info.getXRot()));
            stack.mulPose(YP.rotationDegrees(info.getYRot() + 180));
            double d0 = info.getPosition().x();
            double d1 = info.getPosition().y();
            double d2 = info.getPosition().z();
            VertexConsumer builder = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines());
            BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
                VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
                if (shape != null && !shape.isEmpty() && !world.isEmptyBlock(blockPos) && world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0 && !(world.getBlockState(blockPos).getBlock() instanceof IFluidBlock) && !(world.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
                    LevelRenderer.renderLineBox(stack, builder, shape.bounds().move(blockPos.getX() - d0, blockPos.getY() - d1, blockPos.getZ() - d2), 0, 0, 0, 0.35F);
                }
            });
            stack.popPose();
        }
    }

    @SubscribeEvent
    public static void onRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getUUID().equals(Minecraft.getInstance().player.getUUID()) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
            return;
        if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof QNP)
            event.getEntity().startUsingItem(InteractionHand.MAIN_HAND);
        else if (event.getEntity().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof QNP)
            event.getEntity().startUsingItem(InteractionHand.OFF_HAND);
    }

    public static List<BlockPos> outlineBlocks = new CopyOnWriteArrayList<>();
    public static List<BlockPos> fusionReactors = new CopyOnWriteArrayList<>();

    private static void renderMultiblockDebugOverlays(PoseStack poseStack, Player player) {
        if (!isDebugOverlayActive()) return;
        BlockPos playerPos = player.blockPosition();
        AABB unit = new AABB(0, 0, 0, 1, 1, 1);
        for (MultiblockControllerBE be : debugControllers) {
            if (be.isRemoved()) continue;
            BlockPos bl = be.bottomLeft;
            BlockPos tr = be.topRight;
            if (bl == null || tr == null) continue;
            if (bl.equals(BlockPos.ZERO) && tr.equals(BlockPos.ZERO)) continue;
            drawBoundingBoxAtBlockPos(poseStack, unit, 0f, 1f, 0f, 1f, bl, playerPos);
            drawBoundingBoxAtBlockPos(poseStack, unit, 0f, 0f, 1f, 1f, tr, playerPos);
            AABB full = new AABB(bl.getX(), bl.getY(), bl.getZ(), tr.getX() + 1, tr.getY() + 1, tr.getZ() + 1);
            drawBoundingBoxAtBlockPos(poseStack, full, 1f, 1f, 0f, 1f, BlockPos.ZERO, playerPos);
        }
    }

    public static void drawBoundingBoxAtBlockPos(PoseStack matrixStackIn, AABB aabbIn, float red, float green, float blue, float alpha, BlockPos pos, BlockPos aimed) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        double camX = cam.x, camY = cam.y, camZ = cam.z;

        matrixStackIn.pushPose();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawShapeOutline(matrixStackIn, Shapes.create(aabbIn), pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ, red, green, blue, alpha, pos, aimed);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        matrixStackIn.popPose();
    }


    private static void drawShapeOutline(PoseStack matrixStack, VoxelShape voxelShape, double originX, double originY, double originZ, float red, float green, float blue, float alpha, BlockPos pos, BlockPos aimed) {
        PoseStack.Pose pose = matrixStack.last();
        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer bufferIn = renderTypeBuffer.getBuffer(RenderType.lines());
        voxelShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            if (!pos.equals(aimed)){
                bufferIn.vertex(pose.pose(), (float) (x0 + originX), (float) (y0 + originY), (float) (z0 + originZ))
                        .color(red, green, blue, alpha)
                        .normal(pose.normal(), (float) (x1-x0), (float) (y1-y0), (float) (z1-z0))
                        .endVertex();
                bufferIn.vertex(pose.pose(), (float) (x1 + originX), (float) (y1 + originY), (float) (z1 + originZ))
                        .color(red, green, blue, alpha)
                        .normal(pose.normal(), (float) (x1-x0), (float) (y1-y0), (float) (z1-z0))
                        .endVertex();
            }

        });

        renderTypeBuffer.endBatch(RenderType.lines());
    }

    public static void renderFilledBox(PoseStack poseStack, AABB box, float r, float g, float b, float alpha, BlockPos pos, BlockPos player) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Get camera position for proper translation
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double camX = cam.x, camY = cam.y, camZ = cam.z;

        // Push matrix to apply transformations
        poseStack.pushPose();

        // Important: Don't translate to the block position since the AABB already contains world coordinates
        // Just translate relative to camera
        poseStack.translate(-camX, -camY, -camZ);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // Use the box coordinates directly - they're already in world space
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Bottom face
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();

        // Top face
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();

        // North
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();

        // South
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();

        // West
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).endVertex();

        // East
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).endVertex();

        tesselator.end();

        // Pop the matrix stack to restore previous state
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    public static void removeFromOutline(BlockPosInstance pos, boolean instant) {
        if (instant) {
            outlineBlocks.remove(pos);
        } else {
            removeFromOutline(pos);
        }
    }

    public static void removeBoxFromOutline(BlockPos blockPos) {
        if (boxes.containsKey(blockPos.asLong())) {
            boxes.remove(blockPos.asLong());
        }
    }

    private static class RenderBox {
        public final AABB boundingBox;
        public final float red;
        public final float green;
        public final float blue;
        public final float alpha;
        public final BlockPos relative;

        public RenderBox(AABB boundingBox, float red, float green, float blue, float alpha, BlockPos relative) {
            this.boundingBox = boundingBox;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.relative = relative;
        }
    }

    public static void addBoxToOutline(AABB boundingBox, float v, float v1, float v2, float v3, BlockPos pos) {
        if(boxes.containsKey(pos.asLong())) {
            return; // already added
        }
        boxes.put(pos.asLong(), new RenderBox(boundingBox, v, v1, v2, v3, pos));
    }

    public void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, VertexConsumer renderBuffer,
                        Vector3f blpos, Vector3f brpos, Vector3f trpos, Vector3f tlpos,
                        Vec2 blUVpos, Vec2 brUVpos, Vec2 trUVpos, Vec2 tlUVpos,
                        Vector3f normalVector, Color color, int lightmapValue) {
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, blpos, blUVpos, normalVector, color, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, brpos, brUVpos, normalVector, color, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, trpos, trUVpos, normalVector, color, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, tlpos, tlUVpos, normalVector, color, lightmapValue);
    }

    static void addQuadVertex(Matrix4f matrixPos, Matrix3f matrixNormal, VertexConsumer renderBuffer,
                              Vector3f pos, Vec2 texUV,
                              Vector3f normalVector, Color color, int lightmapValue) {
        renderBuffer.vertex(matrixPos, pos.x(), pos.y(), pos.z()) // position coordinate
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())        // color
                .uv(texUV.x, texUV.y)                     // texel coordinate
                .overlayCoords(OverlayTexture.NO_OVERLAY)  // only relevant for rendering Entities (Living)
                .uv2(lightmapValue)         			    // lightmap with full brightness
                .normal(matrixNormal, normalVector.x(), normalVector.y(), normalVector.z())
                .endVertex();
    }

    public static void addFusionReactor(BlockPos pos) {
        if(!fusionReactors.contains(pos)) {
            fusionReactors.add(pos);
        }
    }

    public static void removeFusionReactor(BlockPos pos) {
        if(fusionReactors.contains(pos)) {
            fusionReactors.remove(pos);
        }
    }

    public static void addToOutline(BlockPosInstance blockPosInstance) {
        if(!outlineBlocks.contains(blockPosInstance)) {
            outlineBlocks.add(blockPosInstance);
            highlightsToRemove.remove(blockPosInstance);
        }
    }

    public static void removeFromOutline(BlockPosInstance blockPosInstance) {
        if(!highlightsToRemove.contains(blockPosInstance)) {
            highlightsToRemove.add(blockPosInstance);
        }
    }
}
