package igentuman.nc.handler.event;

import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.block_entity.LeacherBE;
import igentuman.nc.block_entity.PumpBE;
import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.SideMode;
import igentuman.nc.item.QNPItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.AreaUtil.getArea;
import static igentuman.nc.util.StackUtils.isMultiTool;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BlockOverlayHandler {

    /** A world-space box outline with an ARGB colour, captured during extraction. */
    private record Box(AABB box, int color) {
    }

    /**
     * Computes the QNP area, multitool side and leacher pump outlines while the level is
     * accessible, then draws the captured boxes from a custom outline renderer.
     */
    @SubscribeEvent
    public static void blockOverlayEvent(ExtractBlockOutlineRenderStateEvent event) {
        if (Minecraft.getInstance().player == null) return;
        BlockHitResult hit = event.getHitResult();
        ItemStack stackItem = Minecraft.getInstance().player.getMainHandItem();
        List<Box> boxes = new ArrayList<>();
        boolean replaceVanilla = handleQNP(hit, stackItem, boxes);
        replaceVanilla |= handleMultitool(hit, stackItem, boxes);
        handlePumpOutlines(hit, boxes);
        if (boxes.isEmpty() && !replaceVanilla) return;
        final boolean suppress = replaceVanilla;
        event.addCustomRenderer((state, buffers, poseStack, translucentPass, levelState) -> {
            // The outline is rendered in both the opaque and translucent passes; draw once.
            if (state.isTranslucent() != translucentPass) return suppress;
            Vec3 cam = levelState.cameraRenderState.pos;
            float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
            VertexConsumer builder = buffers.getBuffer(RenderTypes.lines());
            for (Box box : boxes) {
                ShapeRenderer.renderShape(poseStack, builder, Shapes.create(box.box()), -cam.x, -cam.y, -cam.z, box.color(), width);
            }
            buffers.endLastBatch();
            return suppress;
        });
    }

    private static void handlePumpOutlines(HitResult hit, List<Box> boxes) {
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Level world = Minecraft.getInstance().player.level();
        BlockPos hitPos = ((BlockHitResult) hit).getBlockPos();
        if (!(world.getBlockEntity(hitPos) instanceof LeacherBE be)) return;

        ChunkPos chunkPos = ChunkPos.containing(hitPos);
        int y = hitPos.getY();
        PumpBE[] pumps = be.getPumpsForClient();
        BlockPos[] corners = {
            new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMinBlockZ()),
            new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMaxBlockZ()),
            new BlockPos(chunkPos.getMaxBlockX(), y, chunkPos.getMaxBlockZ()),
            new BlockPos(chunkPos.getMaxBlockX(), y, chunkPos.getMinBlockZ())
        };

        for (int i = 0; i < 4; i++) {
            boolean valid = pumps != null && i < pumps.length && pumps[i] != null;
            BlockPos pos = corners[i];
            float r = valid ? 0.0F : 1.0F;
            float g = valid ? 1.0F : 0.0F;
            boxes.add(new Box(new AABB(pos), ARGB.colorFromFloat(0.8F, r, g, 0.0F)));
        }
    }

    private static boolean handleQNP(HitResult hit, ItemStack stackItem, List<Box> boxes) {
        if (hit.getType() != HitResult.Type.BLOCK || !(stackItem.getItem() instanceof QNPItem)) return false;
        BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
        QNPItem.Mode mode = QNPItem.getMode(stackItem);
        Level world = Minecraft.getInstance().player.level();
        Tuple<BlockPos, BlockPos> area = getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(), mode.radius, mode.depth);

        int color = ARGB.colorFromFloat(0.35F, 0, 0, 0);
        BlockPos.betweenClosed(area.getA(), area.getB()).forEach(blockPos -> {
            VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
            if (!shape.isEmpty() && !world.isEmptyBlock(blockPos)
                    && world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0
                    && !(world.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
                boxes.add(new Box(shape.bounds().move(blockPos), color));
            }
        });
        return true;
    }

    /**
     * Applies the QNP's item-use pose to this frame without starting item use on the live entity.
     */
    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
                if (Minecraft.getInstance().player == null || renderState.isUsingItem) return;
                if (avatar.getUUID().equals(Minecraft.getInstance().player.getUUID())
                        && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                    return;
                }
                InteractionHand hand;
                if (avatar.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof QNPItem) {
                    hand = InteractionHand.MAIN_HAND;
                } else if (avatar.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof QNPItem) {
                    hand = InteractionHand.OFF_HAND;
                } else return;
                // QNP uses the ordinary held-item arm pose (its use animation is NONE).
                // The flag controls arm ordering and swimming in HumanoidModel.setupAnim.
                renderState.isUsingItem = true;
                renderState.useItemHand = hand;
                renderState.ticksUsingItem = 0;
            }
        });
    }

    private static boolean handleMultitool(HitResult hit, ItemStack stackItem, List<Box> boxes) {
        if (hit.getType() == HitResult.Type.BLOCK && isMultiTool(stackItem)) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            BlockPos blockPos = blockRayTraceResult.getBlockPos();

            Level world = Minecraft.getInstance().player.level();
            BlockEntity be = world.getBlockEntity(blockPos);
            if(! (be instanceof AbstractStorageBE ncBe)) return true;
            Direction hitSide = blockRayTraceResult.getDirection();
            if(Minecraft.getInstance().player.isShiftKeyDown()) {
                hitSide = hitSide.getOpposite();
            }
            SideMode mode = ncBe.sideConfig[hitSide.ordinal()];
            if(mode == null) return true;
            float[] color = new float[]{0, 1, 0};
            switch (mode) {
                case DEFAULT -> color = new float[]{0, 1, 0};
                case IN -> color = new float[]{0, 0, 1};
                case OUT -> color = new float[]{1, 0, 0};
                case DISABLED -> color = new float[]{0.5f, 0.5f, 0.5f};
            }
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
            boxes.add(new Box(bounds.move(blockPos), ARGB.colorFromFloat(0.35F, color[0], color[1], color[2])));
            return true;
        }
        return false;
    }
}
