package igentuman.nc.client.render.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.block_entity.turbine.TurbineRotorBE;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurbineRotorRenderer implements BlockEntityRenderer<TurbineRotorBE, TurbineRotorRenderer.State> {

    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TurbineRotorBE be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.blades.clear();
        BlockState blockState = be.getBlockState();
        state.valid = blockState.hasProperty(TurbineRotorBlock.FACING);
        if (!state.valid) return;

        Direction facing = blockState.getValue(TurbineRotorBlock.FACING);
        Direction.Axis axis = facing.getAxis();
        Level level = be.getLevel();
        state.axis = axis;
        state.speed = level == null ? 0f : be.getRotationSpeed();
        blockModelResolver.update(state.shaft, blockState, DISPLAY);

        if (level == null || !be.isTurbineFormed()) return;
        BlockPos origin = be.getBlockPos();

        float scaling = computeScaling(level, origin, facing);
        state.sx = axis == Direction.Axis.X ? 1f : scaling;
        state.sy = axis == Direction.Axis.Y ? 1f : scaling;
        state.sz = axis == Direction.Axis.Z ? 1f : scaling;

        Map<BlockState, BlockModelRenderState> resolved = new HashMap<>();
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            BlockPos.MutableBlockPos p = origin.mutable();
            while (true) {
                p.move(dir);
                BlockState bladeState = level.getBlockState(p);
                if (!(bladeState.getBlock() instanceof TurbineBladeBlock)) break;
                BlockState visible = bladeState.setValue(TurbineBladeBlock.HIDDEN, false);
                BlockModelRenderState model = resolved.computeIfAbsent(visible, s -> {
                    BlockModelRenderState m = state.models.computeIfAbsent(s, k -> new BlockModelRenderState());
                    blockModelResolver.update(m, s, DISPLAY);
                    return m;
                });
                state.blades.add(new Blade(p.getX() - origin.getX(), p.getY() - origin.getY(),
                        p.getZ() - origin.getZ(), model));
            }
        }
        state.models.keySet().retainAll(resolved.keySet());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.valid) return;

        float speed = state.speed;
        float angle = speed <= 0f ? 0f : (Util.getMillis() * speed * 0.25f) % 360f;
        Quaternionf spin = switch (state.axis) {
            case X -> Axis.XP.rotationDegrees(angle);
            case Z -> Axis.ZP.rotationDegrees(angle);
            default -> Axis.YP.rotationDegrees(angle);
        };

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(spin);
        poseStack.translate(-0.5, -0.5, -0.5);
        state.shaft.submit(poseStack, collector, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        for (Blade blade : state.blades) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(spin);
            poseStack.scale(state.sx, state.sy, state.sz);
            poseStack.translate(-0.5, -0.5, -0.5);
            poseStack.translate(blade.dx(), blade.dy(), blade.dz());
            blade.model().submit(poseStack, collector, LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
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

    public record Blade(int dx, int dy, int dz, BlockModelRenderState model) {
    }

    public static class State extends BlockEntityRenderState {
        public boolean valid;
        public Direction.Axis axis = Direction.Axis.Y;
        public float speed;
        public float sx = 1f;
        public float sy = 1f;
        public float sz = 1f;
        public final BlockModelRenderState shaft = new BlockModelRenderState();
        public final List<Blade> blades = new ArrayList<>();
        final Map<BlockState, BlockModelRenderState> models = new HashMap<>();
    }
}
