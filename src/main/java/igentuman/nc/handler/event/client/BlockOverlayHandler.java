package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import igentuman.nc.block.ISizeToggable;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.item.MultitoolItem;
import igentuman.nc.item.QNP;
import igentuman.nc.util.NCBlockPos;
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
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.item.QNP.getMode;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BlockOverlayHandler {

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(BlockOverlayHandler::blockOverlayEvent);
        MinecraftForge.EVENT_BUS.addListener(BlockOverlayHandler::onRenderPre);
    }

    public static Pair<BlockPos, BlockPos> getArea(BlockPos pos, Direction facing, int radius, boolean depth) {
        BlockPos bottomLeft = pos.relative(facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN, radius).relative(facing.getAxis() == Direction.Axis.Y ? Direction.WEST : facing.getCounterClockWise(), radius);
        BlockPos topRight = pos.relative(facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP, radius).relative(facing.getAxis() == Direction.Axis.Y ? Direction.EAST : facing.getClockWise(), radius);
        if (facing.getAxis() != Direction.Axis.Y && radius > 0) {
            bottomLeft = bottomLeft.relative(Direction.UP, radius - 1);
            topRight = topRight.relative(Direction.UP, radius - 1);
        }
        if (depth) {
            topRight = topRight.relative(facing.getOpposite(), radius+1);
        }
        return Pair.of(bottomLeft, topRight);
    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderWorldLastEvent e) {
        final GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Player player = Minecraft.getInstance().player;
            for(BlockPos pos: fusionReactors) {
                if(true) continue; //disable for now
                BlockEntity be = player.level.getBlockEntity(pos);
                if(! (be instanceof FusionCoreBE)) continue;
                FusionCoreBE fusionBe = (FusionCoreBE) be;
                int size = fusionBe.size+2;

                AABB box1 = new AABB(-size, 0.01f,-size, size,0.99f, -size+1);
                AABB box2 = new AABB(-size, 0.01f, size, size,0.99f, size-1);

                drawBoundingBoxAtBlockPos(e.getMatrixStack(), box1, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                drawBoundingBoxAtBlockPos(e.getMatrixStack(), box2, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                //drawBoundingBoxAtBlockPos(e.getPoseStack(), box3, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
                // drawBoundingBoxAtBlockPos(e.getPoseStack(), box4, 1, 0, 0.5f, 1, pos.above(), player.blockPosition());
            }
        gameRenderer.resetProjectionMatrix(e.getProjectionMatrix());
        if (player.level.isClientSide) {
            for (BlockPos pos: outlineBlocks) {
                AABB aabb = new AABB(0, 0,0,1,1,1);
                drawBoundingBoxAtBlockPos(e.getMatrixStack(), aabb, 1, 0, 0, 1, pos, player.blockPosition());
            }

        }
    }

    @SubscribeEvent
    public static void blockOverlayEvent(DrawSelectionEvent.HighlightBlock event) {
        HitResult hit = event.getTarget();
        ItemStack stackItem = Minecraft.getInstance().player.getMainHandItem();
        handleQNP(event, hit, stackItem);
        handleMultitool(event, hit, stackItem);
    }

    private static void handleMultitool(DrawSelectionEvent.HighlightBlock event, HitResult hit, ItemStack stackItem) {
        if (hit.getType() == HitResult.Type.BLOCK && stackItem.getItem() instanceof MultitoolItem multitool) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            event.setCanceled(true);
            BlockPos blockPos = blockRayTraceResult.getBlockPos();

            Level world = Minecraft.getInstance().player.level;
            BlockEntity be = world.getBlockEntity(blockPos);
            if(! (be instanceof NuclearCraftBE)) return;
            NuclearCraftBE ncBe = (NuclearCraftBE) be;
            if(ncBe.sideConfig.isEmpty()) return;
            Direction hitSide = blockRayTraceResult.getDirection();
            if(Minecraft.getInstance().player.isShiftKeyDown()) {
                hitSide = hitSide.getOpposite();
            }
            ISizeToggable.SideMode mode = ncBe.sideConfig.get(hitSide.ordinal());
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
            Camera info = event.getInfo();
            stack.mulPose(Vector3f.XP.rotationDegrees(info.getXRot()));
            stack.mulPose(Vector3f.YP.rotationDegrees(info.getYRot() + 180));
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

    private static void handleQNP(DrawSelectionEvent.HighlightBlock event, HitResult hit, ItemStack stackItem) {
        if (hit.getType() == HitResult.Type.BLOCK && stackItem.getItem() instanceof QNP qnp) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            event.setCanceled(true);
            QNP.Mode mode = getMode(stackItem);
            Level world = Minecraft.getInstance().player.level;
            Pair<BlockPos, BlockPos> area = getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(),  mode.radius, mode.depth);

            PoseStack stack = new PoseStack();
            stack.pushPose();
            Camera info = event.getInfo();
            stack.mulPose(Vector3f.XP.rotationDegrees(info.getXRot()));
            stack.mulPose(Vector3f.YP.rotationDegrees(info.getYRot() + 180));
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
        if (event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof QNP)
            event.getPlayer().startUsingItem(InteractionHand.MAIN_HAND);
        else if (event.getPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof QNP)
            event.getPlayer().startUsingItem(InteractionHand.OFF_HAND);
    }

    public static List<BlockPos> outlineBlocks = new ArrayList<>();
    public static List<BlockPos> fusionReactors = new ArrayList<>();

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

    public static void addToOutline(NCBlockPos ncBlockPos) {
        if(!outlineBlocks.contains(ncBlockPos)) {
            outlineBlocks.add(ncBlockPos);
        }
    }

    public static void removeFromOutline(NCBlockPos ncBlockPos) {
        if(outlineBlocks.contains(ncBlockPos)) {
            outlineBlocks.remove(ncBlockPos);
        }
    }
}
