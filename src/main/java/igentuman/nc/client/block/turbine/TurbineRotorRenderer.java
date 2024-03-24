package igentuman.nc.client.block.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import igentuman.nc.block.entity.turbine.TurbineRotorBE;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.mojang.math.Axis.*;
import static igentuman.nc.block.turbine.TurbineBladeBlock.HIDDEN;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@NothingNullByDefault
public class TurbineRotorRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    private TurbineRotorBE rotor;
    public BlockState bladeVertical = null;
    public BlockState bladeHorizontal = null;
    public TurbineRotorRenderer(BlockEntityRendererProvider.Context manager) {
        context = manager;
    }
    public float lastAngle = 0;
    public float x = -0.25f;
    public float y = -0.2f;
    public float z = -0.25f;

    public int getAttachedBlades() {
        return rotor.getAttachedBlades();
    }
    @Override
    public void render(BlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        rotor = (TurbineRotorBE) pBlockEntity;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState blockstate = pBlockEntity.getBlockState();
        TurbineRotorBE rotorBe = (TurbineRotorBE) pBlockEntity;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        BakedModel center = blockRenderer.getBlockModel(blockstate.setValue(TurbineRotorBlock.ACTIVE, true));

        pPoseStack.clear();
        pPoseStack.pushPose();

        long time = Util.getMillis();
        float step = rotorBe.getRotationSpeed() * 2;

        float angel = time * step;
        bladeVertical = TURBINE_BLOCKS.get("turbine_basic_rotor_blade").get()
                .defaultBlockState().setValue(FACING, Direction.DOWN).setValue(HIDDEN, false);
        bladeHorizontal = TURBINE_BLOCKS.get("turbine_basic_rotor_blade").get()
                .defaultBlockState().setValue(FACING, Direction.DOWN).setValue(HIDDEN, false);
        angel %= 360;
        pPoseStack.translate(0.5, 0.5, 0.5);
        Direction facing = blockstate.getValue(TurbineRotorBlock.FACING);
        Quaternionf rotation = new Quaternionf();
        Quaternionf rotation2 = new Quaternionf();
        switch (facing) {
            case NORTH:
            case SOUTH:
                rotation = ZN.rotationDegrees(angel);
                rotation2 = ZN.rotationDegrees(angel+90);
                break;
            case EAST:
            case WEST:
                rotation = XN.rotationDegrees(-angel);
                rotation2 = XN.rotationDegrees(angel+90);
                break;
            default:
                rotation = YN.rotationDegrees(-angel);
                rotation2 = YN.rotationDegrees(angel+90);
                break;
        }

        pPoseStack.mulPose(rotation);
        pPoseStack.translate(-0.5, -0.5, -0.5);

        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 1, 1, 1,
                LightTexture.FULL_SKY, combinedOverlay);
        pPoseStack.popPose();

        BakedModel verticalBlade = blockRenderer.getBlockModel(bladeVertical);
        BakedModel horizontalBlade = blockRenderer.getBlockModel(bladeHorizontal);
        renderBlade(pPoseStack, buffer, combinedOverlay, blockRenderer, rotation, verticalBlade);
        pPoseStack.translate(-0.5, -0.5, -0.5);
        renderBlade(pPoseStack, buffer, combinedOverlay, blockRenderer, rotation2, horizontalBlade);

    }

    private void renderBlade(PoseStack pPoseStack, MultiBufferSource buffer, int combinedOverlay, BlockRenderDispatcher blockRenderer, Quaternionf rotation, BakedModel blade) {

        pPoseStack.translate(0.5, 0.5, 0.5);
        Transformation tr = new Transformation(new Vector3f(0, 0, 0), rotation, new Vector3f(1f, getAttachedBlades()+1, 1f), null);
        pPoseStack.pushTransformation(tr);
        pPoseStack.translate(-0.5, -0.5, -0.5);
        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), bladeVertical, blade, 0.7f, 0.7f, 0.7f,
                LightTexture.FULL_SKY, combinedOverlay);

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
