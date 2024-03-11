package igentuman.nc.client.block.turbine;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3f;

@NothingNullByDefault
public class TurbineRotorRenderer extends TileEntityRenderer<TileEntity> {
    public TurbineRotorRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    @Override
    public void render(TileEntity tileEntity, float v, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int i, int i1) {

    }
  /*  private final BlockEntityRendererProvider.Context context;

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context manager) {
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
    public void render(TileEntity pBlockEntity, float pPartialTick, MatrixStack pMatrixStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        FusionCoreBE<?> coreBe = (FusionCoreBE<?>) pBlockEntity;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack core = new ItemStack(blockstate.getBlock().asItem());

        BakedModel center = blockRenderer.getBlockModel(blockstate.setValue(FusionCoreBlock.ACTIVE, true));

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
        blockRenderer.getModelRenderer().renderModel(pMatrixStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay);
        pMatrixStack.popPose();

        BakedModel base = itemRenderer.getModel(core, pBlockEntity.getLevel(), null, 0);
        pMatrixStack.clear();
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.5, 1.35, 0.5);
        pMatrixStack.scale(3.80F, 3.80F, 3.80F);
        itemRenderer.render(
                core,
                ItemTransforms.TransformType.FIXED,
                false, pMatrixStack, buffer, LightTexture.FULL_SKY, combinedOverlay,
                base);


       // blockRenderer.renderSingleBlock(blockstate, pMatrixStack, buffer, packedLight, combinedOverlay, pBlockEntity.getModelData(), RenderType.cutout());
        pMatrixStack.popPose();

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
    public boolean shouldRender(BlockEntity pBlockEntity, Vector3d pCameraPos) {
        return BlockEntityRenderer.super.shouldRender(pBlockEntity, pCameraPos);
    }*/
}
