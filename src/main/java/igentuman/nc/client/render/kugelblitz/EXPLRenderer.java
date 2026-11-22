package igentuman.nc.client.render.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block.kugelblitz.EXPLBlock;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class EXPLRenderer implements BlockEntityRenderer<EXPLBE, EXPLRenderer.State> {

    private static final float STRETCH = 2f;
    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public EXPLRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EXPLBE be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        BlockState blockState = be.getBlockState();
        state.facing = blockState.getValue(FACING);
        blockModelResolver.update(state.body, blockState, DISPLAY);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        switch (state.facing) {
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180));
            default -> { }
        }

        poseStack.scale(1.0F, STRETCH, 1.0F);
        poseStack.translate(-0.5, -0.25, -0.5);

        state.body.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(EXPLBE be) {
        return EXPLBlock.bodyBounds(be.getBlockPos(), be.getBlockState().getValue(FACING));
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class State extends BlockEntityRenderState {
        public final BlockModelRenderState body = new BlockModelRenderState();
        public Direction facing = Direction.UP;
    }
}
