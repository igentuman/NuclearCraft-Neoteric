package igentuman.nc.client.render.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.block_entity.turbine.TurbineRotorBE;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class TurbineRotorRenderer implements BlockEntityRenderer<TurbineRotorBE> {

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TurbineRotorBE be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(TurbineRotorBlock.FACING)) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        Direction facing = state.getValue(TurbineRotorBlock.FACING);
        Direction.Axis axis = facing.getAxis();

        float speed = be.getRotationSpeed();
        float angle = speed <= 0f ? 0f : (Util.getMillis() * speed * 0.25f) % 360f;
        Quaternionf spin = switch (axis) {
            case X -> Axis.XP.rotationDegrees(angle);
            case Z -> Axis.ZP.rotationDegrees(angle);
            default -> Axis.YP.rotationDegrees(angle);
        };

        BakedModel shaft = dispatcher.getBlockModel(state);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(spin);
        poseStack.translate(-0.5, -0.5, -0.5);
        dispatcher.getModelRenderer().renderModel(
                poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                state, shaft, 1f, 1f, 1f, LightTexture.FULL_SKY, combinedOverlay);
        poseStack.popPose();

        if (!be.isTurbineFormed()) return;

        Level level = be.getLevel();
        if (level == null) return;
        BlockPos origin = be.getBlockPos();

        float scaling = computeScaling(level, origin, facing);
        float sx = axis == Direction.Axis.X ? 1f : scaling;
        float sy = axis == Direction.Axis.Y ? 1f : scaling;
        float sz = axis == Direction.Axis.Z ? 1f : scaling;

        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            BlockPos.MutableBlockPos p = origin.mutable();
            while (true) {
                p.move(dir);
                BlockState bladeState = level.getBlockState(p);
                if (!(bladeState.getBlock() instanceof TurbineBladeBlock)) break;
                BlockState visible = bladeState.setValue(TurbineBladeBlock.HIDDEN, false);
                BakedModel bladeModel = dispatcher.getBlockModel(visible);
                int dx = p.getX() - origin.getX();
                int dy = p.getY() - origin.getY();
                int dz = p.getZ() - origin.getZ();
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(spin);
                poseStack.scale(sx, sy, sz);
                poseStack.translate(-0.5, -0.5, -0.5);
                poseStack.translate(dx, dy, dz);
                dispatcher.getModelRenderer().renderModel(
                        poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                        visible, bladeModel, 1f, 1f, 1f, LightTexture.FULL_SKY, combinedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static float computeScaling(Level level, BlockPos origin, Direction facing) {
        Direction.Axis axis = facing.getAxis();
        int self = axis.choose(origin.getX(), origin.getY(), origin.getZ());
        int min = self;
        int max = self;
        for (Direction.AxisDirection ad : Direction.AxisDirection.values()) {
            Direction dir = Direction.fromAxisAndDirection(axis, ad);
            BlockPos.MutableBlockPos p = origin.mutable();
            while (true) {
                p.move(dir);
                if (!(level.getBlockState(p).getBlock() instanceof TurbineRotorBlock)) break;
                int c = axis.choose(p.getX(), p.getY(), p.getZ());
                min = Math.min(min, c);
                max = Math.max(max, c);
            }
        }
        if (max == min) return 1f;
        float t = (self - min) / (float) (max - min);
        t = 1f - t;
        return 0.3f + 0.7f * t;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
