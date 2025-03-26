package igentuman.nc.client.block.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.kugelblitz.BlackHoleBE;
import igentuman.nc.client.renderer.BillboardingEffectRenderer;
import igentuman.nc.util.Color;
import igentuman.nc.util.CustomEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static igentuman.nc.NuclearCraft.rl;

public class BlackholeRenderer implements BlockEntityRenderer<BlockEntity> {
    private static final CustomEffect CORE = new CustomEffect(rl( "textures/particle/blackhole_glow.png"));
    private static final float MIN_SCALE = 0.1F, MAX_SCALE = 4F;
    private final Minecraft minecraft = Minecraft.getInstance();
    private static final Random rand = new Random();
    private final BlockEntityRendererProvider.Context context;

    static {
        CORE.setColor(Color.rgbai(255, 255, 255, 255));
    }

    public BlackholeRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(BlockEntity tile, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        float energyScale = 0.2f;
        float scale = getBoundedScale(energyScale, MIN_SCALE, MAX_SCALE);
        BillboardingEffectRenderer.render(CORE.getTexture(), "nc.blackhole", () -> {
            Vec3 center = Vec3.atCenterOf(tile.getBlockPos());
            CORE.setPos(center);
            CORE.setScale(scale);
            return CORE;
        });
    }

    private static float getBoundedScale(float scale, float min, float max) {
        return min + scale * (max - min);
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
