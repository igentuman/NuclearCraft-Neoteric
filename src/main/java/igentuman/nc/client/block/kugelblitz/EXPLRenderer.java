package igentuman.nc.client.block.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.kugelblitz.entity.EXPLBE;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.math.Vector3f;
import static net.minecraft.world.level.block.DirectionalBlock.FACING;

@NothingNullByDefault
public class EXPLRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public EXPLRenderer(BlockEntityRendererProvider.Context manager) {
        context = manager;
    }
    public float lastAngle = 0;
    public float x = -0.25f;
    public float y = -0.2f;
    public float z = -0.25f;
    public float sy = 2f;
    public float dx = 1.5f;
    public float dz = 1.5f;
    @Override
    public void render(BlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        EXPLBE expl = (EXPLBE) pBlockEntity;
        BakedModel center = blockRenderer.getBlockModel(blockstate);
        pPoseStack.clear();
        pPoseStack.pushPose();

        pPoseStack.translate(0.5D, 0.5D, 0.5D);

        //rotating model according to direction
        switch (blockstate.getValue(FACING)) {
            case NORTH -> pPoseStack.mulPose(Vector3f.ZN.rotationDegrees(90));
            case SOUTH -> pPoseStack.mulPose(Vector3f.ZN.rotationDegrees(-90));
            case EAST -> pPoseStack.mulPose(Vector3f.XN.rotationDegrees(90));
            case WEST -> pPoseStack.mulPose(Vector3f.XN.rotationDegrees(-90));
            case DOWN -> pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180));
        }

        //scaling model height
        switch (blockstate.getValue(FACING).getAxis()) {
            case X -> pPoseStack.scale(sy, 1.0F, 1.0F);
            case Z -> pPoseStack.scale(1.0F, 1.0F, sy);
            case Y -> pPoseStack.scale(1.0F, sy, 1.0F);
        }

        //translate back depending on orientation
        switch (blockstate.getValue(FACING)) {
            case NORTH -> pPoseStack.translate(-0.5, -0.5, -0.75);
            case SOUTH -> pPoseStack.translate(-0.5, -0.5, -0.25);
            case EAST -> pPoseStack.translate(-0.25, -0.5, -0.5);
            case WEST -> pPoseStack.translate(-0.75, -0.5, -0.5);
            case DOWN -> pPoseStack.translate(-0.5, -0.75, -0.5);
            case UP -> pPoseStack.translate(-0.5, -0.25, -0.5);
        }

        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(),
                buffer.getBuffer(RenderType.translucent()),
                blockstate,
                center,
                0.8F, 0.8F, 0.8F,
                packedLight,
                combinedOverlay
        );

        // Restore the transformation state
        pPoseStack.popPose();

    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity pBlockEntity) {
        return BlockEntityRenderer.super.shouldRenderOffScreen(pBlockEntity);
    }

    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance();
    }

    @Override
    public boolean shouldRender(BlockEntity pBlockEntity, Vec3 pCameraPos) {
        return BlockEntityRenderer.super.shouldRender(pBlockEntity, pCameraPos);
    }
}
