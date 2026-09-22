package igentuman.nc.compat.jei.particle;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.TextUtils;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class ParticleIngredientRenderer implements IIngredientRenderer<ParticleStack> {

    @Override
    public void render(GuiGraphics graphics, ParticleStack ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(ingredient.particleId());
        if (definition == null) return;
        graphics.blit(definition.textureId(), 0, 0, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public List<Component> getTooltip(ParticleStack ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        if (ingredient == null || ingredient.isEmpty()) return tooltip;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(ingredient.particleId());
        tooltip.add(definition == null
                ? Component.literal(ingredient.particleId().getPath())
                : Component.translatable(definition.translationKey()));
        if (ingredient.amount() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.amount", TextUtils.scaledFormat(ingredient.amount()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (ingredient.meanEnergyKeV() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.energy", TextUtils.formatParticleEnergy(ingredient.meanEnergyKeV()))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (ingredient.focus() > 0) {
            tooltip.add(__("tooltip.nuclearcraft.particlestack.focus", TextUtils.numberFormat(ingredient.focus()))
                    .withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }
}
