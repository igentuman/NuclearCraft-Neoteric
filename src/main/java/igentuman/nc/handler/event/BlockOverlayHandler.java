package igentuman.nc.handler.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.SideMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.StackUtils.isMultiTool;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BlockOverlayHandler {

    @SubscribeEvent
    public static void blockOverlayEvent(RenderHighlightEvent.Block event) {
        if (Minecraft.getInstance().player == null) return;
        HitResult hit = event.getTarget();
        ItemStack stackItem = Minecraft.getInstance().player.getMainHandItem();
        handleMultitool(event, hit, stackItem);
    }

    private static void handleMultitool(RenderHighlightEvent.Block event, HitResult hit, ItemStack stackItem) {
        if (hit.getType() == HitResult.Type.BLOCK && isMultiTool(stackItem)) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            event.setCanceled(true);
            BlockPos blockPos = blockRayTraceResult.getBlockPos();

            Level world = Minecraft.getInstance().player.level();
            BlockEntity be = world.getBlockEntity(blockPos);
            if(! (be instanceof AbstractStorageBE ncBe)) return;
            Direction hitSide = blockRayTraceResult.getDirection();
            if(Minecraft.getInstance().player.isShiftKeyDown()) {
                hitSide = hitSide.getOpposite();
            }
            SideMode mode = ncBe.sideConfig[hitSide.ordinal()];
            if(mode == null) return;
            float[] color = new float[]{0, 1, 0};
            switch (mode) {
                case DEFAULT -> color = new float[]{0, 1, 0};
                case IN -> color = new float[]{0, 0, 1};
                case OUT -> color = new float[]{1, 0, 0};
                case DISABLED -> color = new float[]{0.5f, 0.5f, 0.5f};
            }
            PoseStack stack = event.getPoseStack();
            stack.pushPose();
            Camera info = event.getCamera();
            double d0 = info.getPosition().x();
            double d1 = info.getPosition().y();
            double d2 = info.getPosition().z();
            VertexConsumer builder = event.getMultiBufferSource().getBuffer(RenderType.lines());
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
}
