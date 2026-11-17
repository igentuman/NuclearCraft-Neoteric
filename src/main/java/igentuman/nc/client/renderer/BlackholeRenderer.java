package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import igentuman.nc.util.CustomEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import static igentuman.nc.NuclearCraft.rl;

public class BlackholeRenderer implements BlockEntityRenderer<BlockEntity> {
    private static final ResourceLocation TEXTURE = rl("textures/particle/blackhole_glow.png");
    private static final float MIN_SCALE = 0.1F;

    public BlackholeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int overlay) {
        float energyScale = ((BlackHoleBE) tile).getBlackholeScale();
        float boundedScale = Float.isFinite(energyScale) ? Math.max(0, Math.min(1, energyScale)) : 0;
        CustomEffect effect = new CustomEffect(TEXTURE);
        effect.setPos(Vec3.atCenterOf(tile.getBlockPos()));
        effect.setScale(MIN_SCALE + boundedScale * (BlackHoleBE.MAX_RENDER_SCALE - MIN_SCALE));
        BillboardingEffectRenderer.render(effect, "nc.blackhole");
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return true;
    }
}
