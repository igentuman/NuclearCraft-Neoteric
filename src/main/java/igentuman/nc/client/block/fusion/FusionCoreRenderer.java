package igentuman.nc.client.block.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Vector3f;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;

@NothingNullByDefault
public class FusionCoreRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public FusionCoreRenderer(BlockEntityRendererProvider.Context manager) {
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
        FusionCoreBE<?> coreBe = (FusionCoreBE<?>) pBlockEntity;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack core = new ItemStack(blockstate.getBlock().asItem());

        BakedModel center = blockRenderer.getBlockModel(blockstate.setValue(FusionCoreBlock.ACTIVE, true));

        pPoseStack.clear();
        pPoseStack.pushPose();

        long time = Util.getMillis();
        float step = -0.08f;
        if(coreBe.efficiency > 0.5) {
            step = -0.15f;
        }
        float angel = time * step;

        if(coreBe.efficiency < 0.1) {
            angel = 45f;
        }
        angel %= 360;
        pPoseStack.translate(dx, 0, dz);
        pPoseStack.mulPose(Vector3f.YN.rotationDegrees(angel));
        pPoseStack.scale(1.4f, sy, 1.4f);
        pPoseStack.translate(-dx, 0.135f, -dz);
        blockRenderer.getModelRenderer().renderModel(pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay);
        pPoseStack.popPose();

        BakedModel base = itemRenderer.getModel(core, pBlockEntity.getLevel(), null, 0);
        pPoseStack.clear();
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.35, 0.5);
        pPoseStack.scale(3.80F, 3.80F, 3.80F);
        itemRenderer.render(
                core,
                ItemTransforms.TransformType.FIXED,
                false, pPoseStack, buffer, LightTexture.FULL_SKY, combinedOverlay,
                base);


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
