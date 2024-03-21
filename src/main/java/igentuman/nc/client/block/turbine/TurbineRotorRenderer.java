package igentuman.nc.client.block.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import igentuman.nc.block.entity.turbine.TurbineRotorBE;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

import static com.mojang.math.Axis.*;

@NothingNullByDefault
public class TurbineRotorRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;

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
    public void render(BlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        TurbineRotorBE rotorBe = (TurbineRotorBE) pBlockEntity;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack core = new ItemStack(blockstate.getBlock().asItem());

        BakedModel center = blockRenderer.getBlockModel(blockstate.setValue(TurbineRotorBlock.ACTIVE, true));

        pPoseStack.clear();
        pPoseStack.pushPose();

        long time = Util.getMillis();
        float step = rotorBe.getRotationSpeed();

        float angel = time * step;

        angel %= 360;
        pPoseStack.translate(0.5, 0.5, 0.5);
        Direction facing = blockstate.getValue(TurbineRotorBlock.FACING);
        switch (facing) {
            case NORTH:
            case SOUTH:
                pPoseStack.mulPose(ZN.rotationDegrees(angel));
                break;
            case EAST:
            case WEST:
                pPoseStack.mulPose(XN.rotationDegrees(-angel));
                break;
            default:
                pPoseStack.mulPose(YN.rotationDegrees(-angel));
                break;
        }
       // pPoseStack.scale(1.4f, sy, 1.4f);
        pPoseStack.translate(-0.5, -0.5, -0.5);
        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 1, 1, 1,
                LightTexture.FULL_SKY, combinedOverlay);
        pPoseStack.popPose();

 /*       BakedModel base = itemRenderer.getModel(core, pBlockEntity.getLevel(), null, 0);
        pPoseStack.clear();
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.35, 0.5);
        pPoseStack.scale(3.80F, 3.80F, 3.80F);
        itemRenderer.render(
                core,
                ItemDisplayContext.FIXED,
                false, pPoseStack, buffer, LightTexture.FULL_SKY, combinedOverlay,
                base);*/


       // blockRenderer.renderSingleBlock(blockstate, pPoseStack, buffer, packedLight, combinedOverlay, pBlockEntity.getModelData(), RenderType.cutout());
     //   pPoseStack.popPose();

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
