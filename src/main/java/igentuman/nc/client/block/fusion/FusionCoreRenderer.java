package igentuman.nc.client.block.fusion;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;

@NothingNullByDefault
public class FusionCoreRenderer extends TileEntityRenderer<TileEntity> {
    private final TileEntityRendererDispatcher context;

    public FusionCoreRenderer(TileEntityRendererDispatcher manager) {
        super(manager);
        context = manager;
    }
    public float lastAngle = 0;
    public float x = -0.25f;
    public float y = -0.2f;
    public float z = -0.25f;
    public float sy = 1.25f;
    public float dx = 0.5f;
    public float dz = 0.5f;


    @Override
    public void render(TileEntity pBlockEntity, float pPartialTick, MatrixStack pMatrixStack, IRenderTypeBuffer buffer, int packedLight, int combinedOverlay) {
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        FusionCoreBE<?> coreBe = (FusionCoreBE<?>) pBlockEntity;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack core = new ItemStack(blockstate.getBlock().asItem());

        IBakedModel center = blockRenderer.getBlockModel(blockstate.setValue(FusionCoreBlock.ACTIVE, true));

        pMatrixStack.clear();
        pMatrixStack.pushPose();

        long time = Util.getMillis();
        float step = -0.08f;
        if(coreBe.isRunning() && coreBe.efficiency > 0.5) {
            step = -0.15f;
        }
        float angel = time * step;

        if(!coreBe.isRunning() || coreBe.efficiency < 0.1) {
            angel = 45f;
        }
        angel %= 360;
        pMatrixStack.translate(dx, 0, dz);
        pMatrixStack.mulPose(Vector3f.YN.rotationDegrees(angel));
        pMatrixStack.scale(1.4f, sy, 1.4f);
        pMatrixStack.translate(-dx, 0.135f, -dz);
        blockRenderer.getModelRenderer().renderModel(pMatrixStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 1, 1, 1, combinedOverlay, combinedOverlay);
        pMatrixStack.popPose();

        IBakedModel base = itemRenderer.getModel(core, pBlockEntity.getLevel(), Minecraft.getInstance().player);
        pMatrixStack.clear();
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.5, 1.35, 0.5);
        pMatrixStack.scale(3.80F, 3.80F, 3.80F);
        itemRenderer.render(
                core,
                ItemCameraTransforms.TransformType.FIXED,
                false, pMatrixStack, buffer, LightTexture.sky(1), combinedOverlay,
                base);


        blockRenderer.renderSingleBlock(blockstate, pMatrixStack, buffer, packedLight, combinedOverlay);
        pMatrixStack.popPose();

    }

    @Override
    public boolean shouldRenderOffScreen(TileEntity pBlockEntity) {
        return super.shouldRenderOffScreen(pBlockEntity);
    }
}
