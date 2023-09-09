package igentuman.nc.client.block.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.EmptyModel;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;

@NothingNullByDefault
public class FusionCoreRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public FusionCoreRenderer(BlockEntityRendererProvider.Context manager) {
        context = manager;
    }

    @Override
    public void render(BlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        pPoseStack.clear();
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.35, 0.5);
        pPoseStack.scale(3.80F, 3.80F, 3.80F);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        ItemStack core = new ItemStack(blockstate.getBlock().asItem());
        renderer.render(
                core,
                ItemTransforms.TransformType.FIXED,
                false, pPoseStack, buffer, LightTexture.FULL_SKY, combinedOverlay,
                renderer.getModel(core, pBlockEntity.getLevel(), null, 0));
       // blockRenderer.renderSingleBlock(blockstate, pPoseStack, buffer, packedLight, combinedOverlay, pBlockEntity.getModelData(), RenderType.cutout());
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
