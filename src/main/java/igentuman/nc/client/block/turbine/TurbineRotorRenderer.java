package igentuman.nc.client.block.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import igentuman.nc.block.turbine.entity.TurbineRotorBE;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.mojang.math.Axis.*;
import static igentuman.nc.block.turbine.TurbineBladeBlock.HIDDEN;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.dummyBlade;
import static net.minecraft.core.Direction.Axis.Y;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@NothingNullByDefault
public class TurbineRotorRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    private TurbineRotorBE rotor;
    public final BlockState bottomBlade;
    public final BlockState leftBlade;
    public final BlockState bladeNorth;
    public final BakedModel verticalBladeModel;
    public final BakedModel horizontalBladeModel;
    public final BakedModel northBladeModel;

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context manager) {
        context = manager;
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        bottomBlade = dummyBlade.get()
                .defaultBlockState().setValue(FACING, Direction.DOWN);
        leftBlade = dummyBlade.get()
                .defaultBlockState().setValue(FACING, Direction.UP);
        bladeNorth = dummyBlade.get()
                .defaultBlockState().setValue(FACING, Direction.SOUTH);
        verticalBladeModel = blockRenderer.getBlockModel(bottomBlade);
        horizontalBladeModel = blockRenderer.getBlockModel(leftBlade);
        northBladeModel = blockRenderer.getBlockModel(bladeNorth);
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
        float step = rotorBe.getRotationSpeed() * 5f;

        float angle = time * step;

        angle %= 360;
        pPoseStack.translate(0.5, 0.5, 0.5);
        Direction facing = blockstate.getValue(TurbineRotorBlock.FACING);
        Quaternionf rotation = null;
        Quaternionf rotation2 = null;
        switch (facing) {
            case NORTH:
            case SOUTH:
                rotation = ZN.rotationDegrees(angle);
                rotation2 = ZN.rotationDegrees(angle+90);
                break;
            case EAST:
            case WEST:
                rotation = XN.rotationDegrees(angle);
                rotation2 = XN.rotationDegrees(angle+90);
                break;
            default:
                rotation = YN.rotationDegrees(angle);
                rotation2 = YN.rotationDegrees(angle+90);
                break;
        }

        pPoseStack.mulPose(rotation);
        pPoseStack.translate(-0.5, -0.5, -0.5);

        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), blockstate, center, 0.5f, 0.5f, 0.5f,
                LightTexture.FULL_SKY, combinedOverlay);
        pPoseStack.popPose();

        if(!rotor.isFormed()) {
            return;
        }
        if(facing.getAxis() == Y) {
            renderBlade(facing, pPoseStack, buffer, combinedOverlay, blockRenderer, rotation, northBladeModel);
            pPoseStack.translate(-0.5, -0.5, -0.5);
            renderBlade(facing, pPoseStack, buffer, combinedOverlay, blockRenderer, rotation2, northBladeModel);
        } else {
            renderBlade(facing, pPoseStack, buffer, combinedOverlay, blockRenderer, rotation, verticalBladeModel);
            pPoseStack.translate(-0.5, -0.5, -0.5);
            renderBlade(facing, pPoseStack, buffer, combinedOverlay, blockRenderer, rotation2, horizontalBladeModel);
        }

    }

    private void renderBlade(Direction facing, PoseStack pPoseStack, MultiBufferSource buffer, int combinedOverlay, BlockRenderDispatcher blockRenderer, Quaternionf rotation, BakedModel blade) {

        pPoseStack.translate(0.5, 0.5, 0.5);
        Transformation tr = new Transformation(new Vector3f(0, 0, 0), rotation, new Vector3f(1f, (getAttachedBlades()+1)/2f, 1f), null);
        BlockState theBlade = bottomBlade;
        if(facing.getAxis() == Y) {
            tr = new Transformation(
                    new Vector3f(0, 0, 0),
                    rotation,
                    new Vector3f(1f, 1f, (getAttachedBlades()+1)/2f),
                    null
            );
            theBlade = bladeNorth;
        }
        pPoseStack.pushTransformation(tr);
        pPoseStack.translate(-0.5, -0.5, -0.5);
        blockRenderer.getModelRenderer().renderModel(
                pPoseStack.last(), buffer.getBuffer(RenderType.cutout()), theBlade, blade, 0.7f, 0.7f, 0.7f,
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
