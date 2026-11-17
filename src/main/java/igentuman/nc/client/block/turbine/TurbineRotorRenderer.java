package igentuman.nc.client.block.turbine;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.turbine.entity.TurbineRotorBE;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import static com.mojang.math.Axis.*;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.dummyBlade;
import static net.minecraft.core.Direction.Axis.Y;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@NothingNullByDefault
public class TurbineRotorRenderer implements BlockEntityRenderer<BlockEntity> {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockState bottomBlade;
    private final BlockState leftBlade;
    private final BlockState bladeNorth;

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getBlockRenderDispatcher(), dummyBlade.get().defaultBlockState());
    }

    TurbineRotorRenderer(BlockRenderDispatcher dispatcher, BlockState blade) {
        blockRenderer = dispatcher;
        bottomBlade = blade.setValue(FACING, Direction.DOWN);
        leftBlade = blade.setValue(FACING, Direction.UP);
        bladeNorth = blade.setValue(FACING, Direction.SOUTH);
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int combinedOverlay) {
        TurbineRotorBE rotor = (TurbineRotorBE) blockEntity;
        BlockState state = blockEntity.getBlockState().setValue(TurbineRotorBlock.ACTIVE, true);
        BakedModel center = blockRenderer.getBlockModel(state);
        float step = rotor.getRotationSpeed() * 5f;
        float angle = (float) ((Util.getMillis() * (double) step) % 360);
        Direction facing = state.getValue(FACING);
        Quaternionf rotation = rotation(facing, angle);
        Quaternionf secondRotation = rotation(facing, angle + 90);

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(rotation);
            poseStack.translate(-0.5, -0.5, -0.5);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                    state, center, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay, blockEntity.getModelData(), null);
        } finally {
            poseStack.popPose();
        }

        if (!rotor.isFormed()) {
            return;
        }
        float scale = (rotor.getAttachedBlades() + 1) / 2f * rotor.getScaling();
        if (facing.getAxis() == Y) {
            renderBlade(facing, poseStack, buffer, combinedOverlay, rotation, bladeNorth, scale);
            renderBlade(facing, poseStack, buffer, combinedOverlay, secondRotation, bladeNorth, scale);
        } else {
            renderBlade(facing, poseStack, buffer, combinedOverlay, rotation, bottomBlade, scale);
            renderBlade(facing, poseStack, buffer, combinedOverlay, secondRotation, leftBlade, scale);
        }
    }

    private static Quaternionf rotation(Direction facing, float angle) {
        return switch (facing.getAxis()) {
            case X -> XN.rotationDegrees(angle);
            case Y -> YN.rotationDegrees(angle);
            case Z -> ZN.rotationDegrees(angle);
        };
    }

    private void renderBlade(Direction facing, PoseStack poseStack, MultiBufferSource buffer, int combinedOverlay,
                             Quaternionf rotation, BlockState state, float scale) {
        BakedModel model = blockRenderer.getBlockModel(state);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(rotation);
            if (facing.getAxis() == Y) {
                poseStack.scale(1, 1, scale);
            } else {
                poseStack.scale(1, scale, 1);
            }
            poseStack.translate(-0.5, -0.5, -0.5);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()),
                    state, model, 1, 1, 1, LightTexture.FULL_SKY, combinedOverlay);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return true;
    }
}
