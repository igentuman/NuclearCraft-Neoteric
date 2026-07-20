package igentuman.nc.client.render.bomb;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.entity.PrimedFissionBombEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PrimedFissionBombRenderer extends EntityRenderer<PrimedFissionBombEntity> {

    public PrimedFissionBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull PrimedFissionBombEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PrimedFissionBombEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    }
}
