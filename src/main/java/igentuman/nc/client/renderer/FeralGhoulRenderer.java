package igentuman.nc.client.renderer;

import igentuman.nc.client.model.ModelFeralGhoul;
import igentuman.nc.entity.EntityFeralGhoul;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class FeralGhoulRenderer extends HumanoidMobRenderer<EntityFeralGhoul, FeralGhoulRenderState, ModelFeralGhoul> {
    private static final List<Identifier> TEXTURES = List.of(
            rl("textures/entity/feral_ghoul1.png"),
            rl("textures/entity/feral_ghoul2.png"),
            rl("textures/entity/feral_ghoul3.png")
    );

    public FeralGhoulRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelFeralGhoul(context.bakeLayer(ModelFeralGhoul.LAYER_LOCATION)), 0.5F);
        // Armour uses the vanilla zombie armour meshes; ModelFeralGhoul#setupAnim poses them like the body.
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), ModelFeralGhoul::new),
                context.getEquipmentRenderer()));
    }

    @Override
    public FeralGhoulRenderState createRenderState() {
        return new FeralGhoulRenderState();
    }

    @Override
    public void extractRenderState(EntityFeralGhoul entity, FeralGhoulRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.textureIndex = Math.abs(entity.getId() % TEXTURES.size());
    }

    @Override
    public Identifier getTextureLocation(FeralGhoulRenderState state) {
        return TEXTURES.get(state.textureIndex);
    }
}
