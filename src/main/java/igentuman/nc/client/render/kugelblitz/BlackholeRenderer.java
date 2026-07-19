package igentuman.nc.client.render.kugelblitz;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.client.renderer.BillboardingEffectRenderer;
import igentuman.nc.client.renderer.CustomEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.NuclearCraft.rl;

public class BlackholeRenderer implements BlockEntityRenderer<BlackHoleBE> {

    private static final CustomEffect CORE = new CustomEffect(rl("textures/particle/blackhole_glow.png"));
    private static final float MIN_SCALE = 1.0F, MAX_SCALE = 5F;

    public BlackholeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlackHoleBE be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int combinedOverlay) {
        float scale = MIN_SCALE + be.getBlackholeScale() * (MAX_SCALE - MIN_SCALE);
        BillboardingEffectRenderer.submit(CORE.getTexture(), () -> {
            CORE.setPos(Vec3.atCenterOf(be.getBlockPos()));
            CORE.setScale(scale);
            return CORE;
        });
    }

    @Override
    public boolean shouldRenderOffScreen(BlackHoleBE be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
