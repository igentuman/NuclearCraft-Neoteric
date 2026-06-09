package igentuman.nc.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.bomb.entity.PrimedFissionBombEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.rl;

public class PrimedFissionBombRenderer extends EntityRenderer<PrimedFissionBombEntity> {

    private static final ResourceLocation TEXTURE = rl("textures/block/bomb/bomb_base.png");

    public PrimedFissionBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PrimedFissionBombEntity entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedFissionBombEntity entity) {
        return TEXTURE;
    }
}
