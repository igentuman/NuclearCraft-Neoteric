package igentuman.nc.client.block.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.DirectionalBlock.FACING;

@NothingNullByDefault
public class EXPLRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public EXPLRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockState state = blockEntity.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);
        Direction facing = state.getValue(FACING);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5 + facing.getStepX() * 0.5,
                    0.5 + facing.getStepY() * 0.5,
                    0.5 + facing.getStepZ() * 0.5);
            switch (facing.getAxis()) {
                case X -> poseStack.scale(2, 1, 1);
                case Y -> poseStack.scale(1, 2, 1);
                case Z -> poseStack.scale(1, 1, 2);
            }
            poseStack.translate(-0.5, -0.5, -0.5);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.translucent()),
                    state, model, 1, 1, 1, packedLight, combinedOverlay, blockEntity.getModelData(), null);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return true;
    }
}
