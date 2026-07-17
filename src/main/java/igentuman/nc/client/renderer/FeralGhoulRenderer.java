package igentuman.nc.client.renderer;

import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.entity.EntityFeralGhoul;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class FeralGhoulRenderer extends HumanoidMobRenderer<EntityFeralGhoul, ModelFeralGhoul> {
    private static final List<ResourceLocation> TEXTURE = List.of(
            rl("textures/entity/feral_ghoul1.png"),
            rl("textures/entity/feral_ghoul2.png"),
            rl("textures/entity/feral_ghoul3.png")
    );

    public FeralGhoulRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelFeralGhoul(context.bakeLayer(ModelFeralGhoul.LAYER_LOCATION)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, 
                new ModelFeralGhoul(context.bakeLayer(ModelFeralGhoul.LAYER_LOCATION)), 
                new ModelFeralGhoul(context.bakeLayer(ModelFeralGhoul.LAYER_LOCATION)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFeralGhoul entity) {
        int textureIndex = Math.abs((int)(entity.getId() % TEXTURE.size()));
        return TEXTURE.get(textureIndex);
    }
}