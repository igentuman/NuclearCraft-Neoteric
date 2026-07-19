package igentuman.nc.client.render.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class EXPLRenderer implements BlockEntityRenderer<EXPLBE> {

    private static final float STRETCH = 2f;

    public EXPLRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EXPLBE be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState state = be.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        switch (state.getValue(FACING)) {
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180));
            default -> { }
        }

        poseStack.scale(1.0F, STRETCH, 1.0F);
        poseStack.translate(-0.5, -0.25, -0.5);

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.cutout()),
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                combinedOverlay
        );

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(EXPLBE be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
