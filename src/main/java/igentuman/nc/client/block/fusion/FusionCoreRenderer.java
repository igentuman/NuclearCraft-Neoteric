package igentuman.nc.client.block.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.mojang.math.Axis.YN;

@NothingNullByDefault
public class FusionCoreRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    public FusionCoreRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        FusionCoreBE coreBE = (FusionCoreBE) blockEntity;
        BlockState state = blockEntity.getBlockState().setValue(FusionCoreBlock.ACTIVE, true);
        ItemStack core = new ItemStack(state.getBlock().asItem());
        BakedModel center = blockRenderer.getBlockModel(state);
        float step = coreBE.isRunning() && coreBE.efficiency > 0.5 ? -0.15f : -0.08f;
        float angle = !coreBE.isRunning() || coreBE.efficiency < 0.1
                ? 45f : (float) ((Util.getMillis() * (double) step) % 360);

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(YN.rotationDegrees(angle));
            poseStack.scale(1.4f, 1.25f, 1.4f);
            poseStack.translate(-0.5, 0.135f, -0.5);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                    state, center, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay, blockEntity.getModelData(), null);
        } finally {
            poseStack.popPose();
        }

        BakedModel base = itemRenderer.getModel(core, blockEntity.getLevel(), null, 0);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 1.35, 0.5);
            poseStack.scale(3.80F, 3.80F, 3.80F);
            itemRenderer.render(core, ItemDisplayContext.FIXED, false, poseStack, buffer,
                    LightTexture.FULL_SKY, combinedOverlay, base);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return true;
    }
}
